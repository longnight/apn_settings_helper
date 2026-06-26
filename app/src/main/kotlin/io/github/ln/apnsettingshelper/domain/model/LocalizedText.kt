package io.github.ln.apnsettingshelper.domain.model

import kotlinx.serialization.Serializable

/** A user-facing string available in English and Japanese. */
@Serializable
data class LocalizedText(
    val en: String,
    val ja: String,
) {
    /** Resolve for a language tag (e.g. "ja", "ja-JP", "en"); falls back to English. */
    fun resolve(languageTag: String): String = if (languageTag.startsWith("ja")) ja else en
}
