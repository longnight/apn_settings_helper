package io.github.ln.apnsettingshelper.ui.detail

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.ln.apnsettingshelper.ui.TEST_APN
import io.github.ln.apnsettingshelper.ui.testDetailState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PresetDetailContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersCopyFieldsAndChecklist() {
        composeRule.setContent {
            PresetDetailContent(
                state = testDetailState(),
                onBack = {},
                onToggleFavorite = {},
                onRecordApplied = {},
                onApplyNow = {},
                onSetRootEnabled = {},
            )
        }

        composeRule.onNodeWithText("APN").assertIsDisplayed()
        composeRule.onNodeWithText(TEST_APN).assertIsDisplayed()
        // Dropdown fields are checklist instructions, not copy buttons (scroll to reach them).
        val checklistLine = "Set Authentication type to PAP or CHAP"
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(checklistLine))
        composeRule.onNodeWithText(checklistLine).assertIsDisplayed()
    }

    @Test
    fun copyButtonPutsValueOnClipboard() {
        composeRule.setContent {
            PresetDetailContent(
                state = testDetailState(),
                onBack = {},
                onToggleFavorite = {},
                onRecordApplied = {},
                onApplyNow = {},
                onSetRootEnabled = {},
            )
        }

        composeRule.onNodeWithContentDescription("Copy APN").performClick()

        assertEquals(TEST_APN, readClipboardText())
    }

    private fun readClipboardText(): String {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var text = ""
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            text =
                clipboard.primaryClip
                    ?.getItemAt(0)
                    ?.text
                    ?.toString()
                    .orEmpty()
        }
        return text
    }
}
