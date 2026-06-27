package io.github.ln.apnsettingshelper.domain.model

/**
 * The single "most recently applied" slot. Overwritten on each apply; `null` until the
 * first apply. A passive history note (not a claim about the device's current APN — the
 * app cannot verify that). [epochMillis] is wall-clock time of the apply (UTC millis);
 * the UI formats it per locale.
 */
data class LastApplied(
    val presetId: String,
    val epochMillis: Long,
)
