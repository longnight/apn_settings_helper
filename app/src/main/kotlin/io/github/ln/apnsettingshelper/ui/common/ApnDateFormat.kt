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

    fun format(
        epochMillis: Long,
        locale: Locale = Locale.getDefault(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val pattern = if (locale.language == "ja") JA_PATTERN else EN_PATTERN
        val formatter = DateTimeFormatter.ofPattern(pattern, locale).withZone(zone)
        return formatter.format(Instant.ofEpochMilli(epochMillis))
    }
}
