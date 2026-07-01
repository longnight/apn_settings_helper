package io.github.ln.apnsettingshelper

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.github.ln.apnsettingshelper.data.store.DataStoreSettingsStore
import io.github.ln.apnsettingshelper.ui.common.LocaleSupport
import io.github.ln.apnsettingshelper.ui.nav.AppNavHost
import io.github.ln.apnsettingshelper.ui.theme.ApnSettingsHelperTheme
import kotlinx.coroutines.launch
import java.util.Locale

/** Single-activity Compose entry point. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as ApnApplication).graph
        // Seed the first frame from the synchronously-read tag so there's no system-locale flash;
        // the DataStore flow then drives live changes.
        val initialLanguage = DataStoreSettingsStore.peekLanguage(this)
        setContent {
            val scope = rememberCoroutineScope()
            val languageTag by graph.settingsStore.language.collectAsState(initial = initialLanguage)
            LocalizedApp(
                languageTag = languageTag,
                onLanguageChange = { tag -> scope.launch { graph.settingsStore.setLanguage(tag) } },
            )
        }
    }
}

/**
 * Applies [languageTag] to the whole UI without recreating the Activity: overriding `LocalContext` +
 * `LocalConfiguration` makes every `stringResource` re-read, so a language pick re-localizes in place
 * (the drawer simply closes — no flash). [Locale.setDefault] keeps non-Compose consumers in sync
 * (`PresetDetailViewModel`/`ApnDateFormat` read the default locale).
 */
@Composable
private fun LocalizedApp(
    languageTag: String?,
    onLanguageChange: (String?) -> Unit,
) {
    val baseContext = LocalContext.current
    val locale = LocaleSupport.effectiveLocale(languageTag)
    val localizedContext = remember(languageTag) { LocaleSupport.localizedContext(baseContext, languageTag) }
    LaunchedEffect(locale) { Locale.setDefault(locale) }
    val layoutDirection =
        if (TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalLayoutDirection provides layoutDirection,
    ) {
        ApnSettingsHelperTheme {
            AppNavHost(
                currentLanguageTag = languageTag,
                onLanguageChange = onLanguageChange,
            )
        }
    }
}
