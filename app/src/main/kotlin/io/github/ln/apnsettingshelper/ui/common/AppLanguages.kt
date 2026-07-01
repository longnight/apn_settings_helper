package io.github.ln.apnsettingshelper.ui.common

/**
 * One selectable UI language. [tag] is a BCP-47 language tag (the value persisted via
 * `SettingsStore.setLanguage` and fed to `Locale.forLanguageTag`); [endonym] is the language's
 * own native name, shown in the picker so it stays recognisable whatever the current UI language.
 */
data class AppLanguage(
    val tag: String,
    val endonym: String,
)

/**
 * The languages offered in the in-app language drawer — the same set the README is translated into
 * (see `docs/readme/`), in the same order. Japanese is the project's primary locale (the root
 * `README.md`); English is the default `res/values`. Every tag here has a matching `res/values-*`
 * folder so switching actually changes the UI. A "system default" choice (tag-less) is added by the
 * picker itself, not listed here.
 */
object AppLanguages {
    val supported: List<AppLanguage> =
        listOf(
            AppLanguage("ja", "日本語"),
            AppLanguage("en", "English"),
            AppLanguage("vi", "Tiếng Việt"),
            AppLanguage("zh-CN", "简体中文"),
            AppLanguage("zh-TW", "繁體中文"),
            AppLanguage("ko", "한국어"),
            AppLanguage("de", "Deutsch"),
            AppLanguage("es", "Español"),
            AppLanguage("fr", "Français"),
            AppLanguage("it", "Italiano"),
            AppLanguage("da", "Dansk"),
            AppLanguage("pl", "Polski"),
            AppLanguage("bs", "Bosanski"),
            AppLanguage("ar", "العربية"),
            AppLanguage("nb", "Norsk"),
            AppLanguage("pt-BR", "Português (Brasil)"),
            AppLanguage("th", "ไทย"),
            AppLanguage("tr", "Türkçe"),
            AppLanguage("km", "ភាសាខ្មែរ"),
            AppLanguage("uk", "Українська"),
        )
}
