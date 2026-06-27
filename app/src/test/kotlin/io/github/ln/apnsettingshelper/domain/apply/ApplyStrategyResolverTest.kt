package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.testutil.FakeShellRunner
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyStrategyResolverTest {
    @Test
    fun `resolves root when a root shell is available`() =
        runTest {
            val strategy = ApplyStrategyResolver(FakeShellRunner(rootAvailable = true)).resolve()

            assertEquals(ApplyTier.ROOT, strategy.tier)
            assertTrue(strategy is RootStrategy)
        }

    @Test
    fun `resolves manual when there is no root and no overlay`() =
        runTest {
            val strategy = ApplyStrategyResolver(FakeShellRunner(rootAvailable = false)).resolve()

            assertEquals(ApplyTier.MANUAL, strategy.tier)
        }

    @Test
    fun `resolves overlay when no root but overlay is available`() =
        runTest {
            val strategy =
                ApplyStrategyResolver(
                    FakeShellRunner(rootAvailable = false),
                    overlayAvailable = { true },
                ).resolve()

            assertEquals(ApplyTier.OVERLAY, strategy.tier)
        }
}
