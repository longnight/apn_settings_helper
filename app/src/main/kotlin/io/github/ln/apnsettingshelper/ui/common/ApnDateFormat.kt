package io.github.ln.apnsettingshelper.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Locale-aware formatting of the last-applied timestamp (AGENTS.md spec):
 * - en → `2026-06-26 14:30`
 * - ja → `2026年6月26日 14:30`
 *
 * Japanese is chosen for any `ja` language tag; everything else uses the ISO-ish English
 * form. The zone defaults to the device zone; tests pass a fixed [ZoneId] for determinism.
 */
object ApnDateFormat {
    private const val EN_PATTERN = "yyyy-MM-dd HH:mm"
    private const val JA_PATTERN = "yyyy年M月d日 HH:mm"

    // Parsing a pattern is the expensive part, so build each formatter once and only re-bind the
    // (cheap, immutable) zone per call instead of re-parsing on every emission.
    private val EN_FORMATTER = DateTimeFormatter.ofPattern(EN_PATTERN, Locale.ENGLISH)
    private val JA_FORMATTER = DateTimeFormatter.ofPattern(JA_PATTERN, Locale.JAPANESE)

    fun format(
        epochMillis: Long,
        locale: Locale = Locale.getDefault(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val formatter = if (locale.language == "ja") JA_FORMATTER else EN_FORMATTER
        return formatter.withZone(zone).format(Instant.ofEpochMilli(epochMillis))
    }
}
