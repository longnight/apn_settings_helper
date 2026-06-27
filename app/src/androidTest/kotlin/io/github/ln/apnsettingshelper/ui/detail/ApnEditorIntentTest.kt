package io.github.ln.apnsettingshelper.ui.detail

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.provider.Settings
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ln.apnsettingshelper.ui.testDetailState
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApnEditorIntentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        Intents.init()
        // Stub the editor launch so the test doesn't actually leave the app.
        intending(hasAction(Settings.ACTION_APN_SETTINGS))
            .respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun openApnEditorButtonFiresApnSettingsIntent() {
        composeRule.setContent {
            PresetDetailContent(
                state = testDetailState(),
                onBack = {},
                onToggleFavorite = {},
                onRecordApplied = {},
                onApplyNow = {},
            )
        }

        composeRule.onNodeWithText("Open system APN editor").performClick()

        intended(hasAction(Settings.ACTION_APN_SETTINGS))
    }
}
