package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.domain.model.Preset

/**
 * v1 stub (AGENTS.md: overlay is off by default and deferred). When implemented it will use
 * `SYSTEM_ALERT_WINDOW` to float the preset values over the system APN editor. Kept as a
 * concrete tier so the [ApplyStrategyResolver] seam already accounts for it.
 */
class OverlayStrategy : ApplyStrategy {
    override val tier: ApplyTier = ApplyTier.OVERLAY

    override suspend fun apply(preset: Preset): ApplyOutcome = ApplyOutcome.Failed("Overlay apply is not implemented in v1")
}
