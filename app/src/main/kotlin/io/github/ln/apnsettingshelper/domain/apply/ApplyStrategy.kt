package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.domain.model.Preset

/**
 * Privilege tiers for applying a preset (AGENTS.md). MANUAL is the universal default;
 * ROOT is an opt-in bonus for already-rooted devices; OVERLAY is a v1 stub.
 */
enum class ApplyTier { MANUAL, OVERLAY, ROOT }

/** Result of an [ApplyStrategy.apply] attempt. */
sealed interface ApplyOutcome {
    /** No programmatic write happened; the UI should drive the copy/checklist/open-editor flow. */
    data object ManualGuidance : ApplyOutcome

    /** The preset was written programmatically **and** selected as the active APN (root). */
    data class Applied(
        val presetId: String,
    ) : ApplyOutcome

    /**
     * The preset row was written, but it could not be set as the active/preferred APN (e.g. the
     * inserted row's id could not be read back). The user must select it manually in the editor.
     */
    data class WrittenNotSelected(
        val presetId: String,
    ) : ApplyOutcome

    /** An apply was attempted but failed; [message] is a (technical, English) reason. */
    data class Failed(
        val message: String,
    ) : ApplyOutcome
}

/**
 * The single seam for "apply this preset". Keeping every tier behind this interface means a
 * future opt-in self-healing watcher (deferred post-v1) is an additive change, not a rewrite.
 */
interface ApplyStrategy {
    val tier: ApplyTier

    suspend fun apply(preset: Preset): ApplyOutcome
}
