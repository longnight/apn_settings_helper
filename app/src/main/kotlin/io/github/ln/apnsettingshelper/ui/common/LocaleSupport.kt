package io.github.ln.apnsettingshelper.ui.common

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.ContextThemeWrapper
import java.util.Locale

/**
 * Resolves the UI locale for a persisted language tag and builds a [Context] whose resources use it.
 *
 * The app applies the locale **in-place** (no Activity recreate): a CompositionLocal override feeds
 * [localizedContext] to `LocalContext`/`LocalConfiguration`, so `stringResource`s re-read and the UI
 * re-localizes the instant the language changes. A `null`/blank tag means "follow the system",
 * resolved to the real device locale via [Resources.getSystem] (not [Locale.getDefault], which an
 * earlier override may have moved). `Configuration.setLocale` also sets the layout direction, so RTL
 * languages (Arabic) get a mirrored UI for free (the manifest declares `android:supportsRtl="true"`).
 */
object LocaleSupport {
    /** The locale a [tag] selects, or the live system locale when [tag] is `null`/blank. */
    fun effectiveLocale(tag: String?): Locale =
        if (tag.isNullOrBlank()) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Locale.forLanguageTag(tag)
        }

    /**
     * [base] wrapped so its resources resolve against [effectiveLocale] of [tag]. Uses a
     * [ContextThemeWrapper] + [applyOverrideConfiguration][ContextThemeWrapper.applyOverrideConfiguration]
     * (not `createConfigurationContext`) so the wrapper's `baseContext` chain still reaches the
     * Activity — owner lookups that walk it (e.g. `rememberLauncherForActivityResult` finding the
     * `ActivityResultRegistryOwner`) keep working when this is provided as `LocalContext`.
     */
    fun localizedContext(
        base: Context,
        tag: String?,
    ): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(effectiveLocale(tag))
        return ContextThemeWrapper(base, 0).apply { applyOverrideConfiguration(config) }
    }
}
