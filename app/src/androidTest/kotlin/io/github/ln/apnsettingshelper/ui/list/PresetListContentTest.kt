package io.github.ln.apnsettingshelper.ui.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ln.apnsettingshelper.ui.testListState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PresetListContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsFavoritesSectionGroupsAndLastApplied() {
        composeRule.setContent {
            PresetListContent(state = testListState(), onPresetClick = {}, onToggleFavorite = {})
        }

        composeRule.onNodeWithText("Fav Preset").assertIsDisplayed()
        composeRule.onNodeWithText("HIS SoftBank").assertIsDisplayed()
        composeRule.onNodeWithText("Last applied 2026-06-27 14:30").assertIsDisplayed()
    }

    @Test
    fun clickingRowInvokesCallbackWithId() {
        var clicked: String? = null
        composeRule.setContent {
            PresetListContent(state = testListState(), onPresetClick = { clicked = it }, onToggleFavorite = {})
        }

        composeRule.onNodeWithText("HIS SoftBank").performClick()

        assertEquals("his-sb", clicked)
    }

    @Test
    fun tappingHeartInvokesToggleCallback() {
        var toggled: String? = null
        composeRule.setContent {
            PresetListContent(state = testListState(), onPresetClick = {}, onToggleFavorite = { toggled = it })
        }

        // The non-favorited row's heart shows the "add" description; uniquely identifies his-sb.
        composeRule.onNodeWithContentDescription("Add to favorites").performClick()

        assertEquals("his-sb", toggled)
    }
}
