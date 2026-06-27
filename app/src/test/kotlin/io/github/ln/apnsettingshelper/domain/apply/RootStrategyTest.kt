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
            val insert = shell.commands.first()
            assertTrue(insert.startsWith("content insert --uri content://telephony/carriers"))
            assertTrue(insert.contains("apn:s:dm.jplat.net"))
            assertTrue(insert.contains("numeric:s:44010"))
            assertTrue(insert.contains("user:s:his@his"))
            assertTrue(insert.contains("authtype:i:3"))
            assertTrue(insert.contains("protocol:s:IPV4V6"))
            assertTrue(insert.contains("current:i:1"))
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

            assertFalse(shell.commands.first().contains("mvno_type"))
        }

    @Test
    fun `includes mvno binds when type is set`() =
        runTest {
            val shell = FakeShellRunner(rootAvailable = true)

            RootStrategy(shell).apply(preset.copy(mvnoType = MvnoType.SPN, mvnoValue = "HIS"))

            val insert = shell.commands.first()
            assertTrue(insert.contains("mvno_type:s:spn"))
            assertTrue(insert.contains("mvno_match_data:s:HIS"))
        }
}
