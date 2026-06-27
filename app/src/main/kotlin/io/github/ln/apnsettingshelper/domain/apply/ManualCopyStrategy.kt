package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.domain.model.Preset

/**
 * The zero-permission default: there is no silent write on modern Android, so "applying"
 * is really "guide the user". Returns [ApplyOutcome.ManualGuidance]; the detail screen
 * provides the per-field copy buttons, the "set to X" checklist, and the open-editor button.
 */
class ManualCopyStrategy : ApplyStrategy {
    override val tier: ApplyTier = ApplyTier.MANUAL

    override suspend fun apply(preset: Preset): ApplyOutcome = ApplyOutcome.ManualGuidance
}
