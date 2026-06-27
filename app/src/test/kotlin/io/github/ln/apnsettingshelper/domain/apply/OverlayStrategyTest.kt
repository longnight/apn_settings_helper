package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.testutil.samplePreset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayStrategyTest {
    @Test
    fun `reports the overlay tier`() {
        assertEquals(ApplyTier.OVERLAY, OverlayStrategy().tier)
    }

    @Test
    fun `apply is a v1 stub that fails`() =
        runTest {
            // Documents the deferred-stub contract: overlay is off in v1 (AGENTS.md).
            val outcome = OverlayStrategy().apply(samplePreset(id = "x"))
            assertTrue(outcome is ApplyOutcome.Failed)
        }
}
