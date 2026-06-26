package io.github.ln.apnsettingshelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.ln.apnsettingshelper.ui.nav.AppNavHost
import io.github.ln.apnsettingshelper.ui.theme.ApnSettingsHelperTheme

/** Single-activity Compose entry point. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApnSettingsHelperTheme {
                AppNavHost()
            }
        }
    }
}
