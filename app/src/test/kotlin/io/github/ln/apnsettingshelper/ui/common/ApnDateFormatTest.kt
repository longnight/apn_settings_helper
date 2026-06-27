package io.github.ln.apnsettingshelper.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class ApnDateFormatTest {
    private val tokyo = ZoneId.of("Asia/Tokyo")

    // 2026-06-27 14:30 in Tokyo (UTC+9) == 05:30Z.
    private val summerMillis = Instant.parse("2026-06-27T05:30:00Z").toEpochMilli()

    // 2026-01-05 09:30 in Tokyo == 00:30Z (single-digit month/day, to check ja has no leading zeros).
    private val januaryMillis = Instant.parse("2026-01-05T00:30:00Z").toEpochMilli()

    @Test
    fun `english uses ISO-style pattern`() {
        assertEquals("2026-06-27 14:30", ApnDateFormat.format(summerMillis, Locale.ENGLISH, tokyo))
        assertEquals("2026-01-05 09:30", ApnDateFormat.format(januaryMillis, Locale.ENGLISH, tokyo))
    }

    @Test
    fun `japanese uses year-month-day kanji without leading zeros`() {
        assertEquals("2026年6月27日 14:30", ApnDateFormat.format(summerMillis, Locale.JAPANESE, tokyo))
        assertEquals("2026年1月5日 09:30", ApnDateFormat.format(januaryMillis, Locale.JAPANESE, tokyo))
    }

    @Test
    fun `other locales fall back to english`() {
        assertEquals("2026-06-27 14:30", ApnDateFormat.format(summerMillis, Locale.FRENCH, tokyo))
    }
}
