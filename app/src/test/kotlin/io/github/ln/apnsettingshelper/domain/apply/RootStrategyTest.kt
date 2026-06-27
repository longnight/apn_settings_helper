package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.MvnoType
import io.github.ln.apnsettingshelper.testutil.FakeShellRunner
import io.github.ln.apnsettingshelper.testutil.samplePreset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootStrategyTest {
    private val preset =
        samplePreset(
            id = "his-d",
            apn = "dm.jplat.net",
            username = "his@his",
            mnc = "10",
            authType = AuthType.PAP_OR_CHAP,
        )

    /** The `content insert` into the carriers table (not the delete, query, or preferapn insert). */
    private fun List<String>.carrierInsert(): String = first { it.startsWith("content insert --uri content://telephony/carriers --bind") }

    @Test
    fun `fails when root is unavailable`() =
        runTest {
            val outcome = RootStrategy(FakeShellRunner(rootAvailable = false)).apply(preset)
            assertTrue(outcome is ApplyOutcome.Failed)
        }

    @Test
    fun `insert command carries the mapped provider fields and returns applied`() =
        runTest {
            val shell = FakeShellRunner(rootAvailable = true)

            val outcome = RootStrategy(shell).apply(preset)

            assertEquals(ApplyOutcome.Applied("his-d"), outcome)
            val insert = shell.commands.carrierInsert()
            assertTrue(insert.contains("apn:s:dm.jplat.net"))
            assertTrue(insert.contains("numeric:s:44010"))
            assertTrue(insert.contains("user:s:his@his"))
            assertTrue(insert.contains("authtype:i:3"))
            assertTrue(insert.contains("protocol:s:IPV4V6"))
            assertTrue(insert.contains("current:i:1"))
        }

    @Test
    fun `deletes any prior copy of the preset before inserting`() =
        runTest {
            val shell = FakeShellRunner(rootAvailable = true)

            RootStrategy(shell).apply(preset)

            val deleteIdx = shell.commands.indexOfFirst { it.startsWith("content delete --uri content://telephony/carriers") }
            val insertIdx = shell.commands.indexOfFirst { it.startsWith("content insert --uri content://telephony/carriers --bind") }
            assertTrue("a delete is issued", deleteIdx >= 0)
            assertTrue("delete runs before insert", deleteIdx < insertIdx)
            assertTrue("delete matches this preset", shell.commands[deleteIdx].contains("dm.jplat.net"))
        }

    @Test
    fun `returns failed with stderr when the insert fails`() =
        runTest {
            val shell = FakeShellRunner(rootAvailable = true) { ShellResult(success = false, err = listOf("boom")) }

            val outcome = RootStrategy(shell).apply(preset)

            assertTrue(outcome is ApplyOutcome.Failed)
            assertTrue((outcome as ApplyOutcome.Failed).message.contains("boom"))
        }

    @Test
    fun `returns failed when the inserted row cannot be verified`() =
        runTest {
            // Insert "succeeds" (exit 0) but the read-back finds no row → must not report Applied.
            val shell =
                FakeShellRunner(rootAvailable = true) { command ->
                    if (command.startsWith("content query")) {
                        ShellResult(success = true, out = emptyList())
                    } else {
                        ShellResult(success = true)
                    }
                }

            val outcome = RootStrategy(shell).apply(preset)

            assertTrue(outcome is ApplyOutcome.Failed)
        }

    @Test
    fun `returns written-not-selected when activation fails`() =
        runTest {
            // Row written and read back, but marking it the preferred APN fails.
            val shell =
                FakeShellRunner(rootAvailable = true) { command ->
                    when {
                        command.startsWith("content query") -> ShellResult(success = true, out = listOf("_id=7"))
                        command.contains("/preferapn") -> ShellResult(success = false, err = listOf("denied"))
                        else -> ShellResult(success = true)
                    }
                }

            val outcome = RootStrategy(shell).apply(preset)

            assertEquals(ApplyOutcome.WrittenNotSelected("his-d"), outcome)
        }

    @Test
    fun `selects the inserted row as preferred apn using the parsed id`() =
        runTest {
            val shell =
                FakeShellRunner(rootAvailable = true) { command ->
                    if (command.startsWith("content query")) {
                        ShellResult(success = true, out = listOf("Row: 0 _id=42, name=HIS"))
                    } else {
                        ShellResult(success = true)
                    }
                }

            RootStrategy(shell).apply(preset)

            val prefer = shell.commands.last()
            assertTrue(prefer.contains("content://telephony/carriers/preferapn"))
            assertTrue(prefer.contains("apn_id:i:42"))
        }

    @Test
    fun `omits mvno binds when type is none`() =
        runTest {
            val shell = FakeShellRunner(rootAvailable = true)

            RootStrategy(shell).apply(preset)

            assertFalse(shell.commands.carrierInsert().contains("mvno_type"))
        }

    @Test
    fun `includes mvno binds when type is set`() =
        runTest {
            val shell = FakeShellRunner(rootAvailable = true)

            RootStrategy(shell).apply(preset.copy(mvnoType = MvnoType.SPN, mvnoValue = "HIS"))

            val insert = shell.commands.carrierInsert()
            assertTrue(insert.contains("mvno_type:s:spn"))
            assertTrue(insert.contains("mvno_match_data:s:HIS"))
        }
}
