package io.github.ln.apnsettingshelper.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ln.apnsettingshelper.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end: the real app (MainActivity → AppNavHost → real bundled presets + DataStore).
 * Clicking a preset row navigates to its detail screen.
 */
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun clickingPresetRowOpensDetail() {
        // A preset label from the bundled JP data (HIS Mobile is the first carrier).
        composeRule.onNodeWithText("HIS Mobile (Docomo line)").performClick()

        composeRule.onNodeWithText("Open system APN editor").assertIsDisplayed()
    }
}
