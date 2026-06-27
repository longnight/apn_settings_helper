package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.testutil.samplePreset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ManualCopyStrategyTest {
    @Test
    fun `tier is manual`() {
        assertEquals(ApplyTier.MANUAL, ManualCopyStrategy().tier)
    }

    @Test
    fun `apply returns manual guidance`() =
        runTest {
            val outcome = ManualCopyStrategy().apply(samplePreset("p1"))
            assertEquals(ApplyOutcome.ManualGuidance, outcome)
        }
}
