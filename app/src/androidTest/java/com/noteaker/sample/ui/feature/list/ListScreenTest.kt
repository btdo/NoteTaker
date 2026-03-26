package com.noteaker.sample.ui.feature.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.theme.NoteTakerTheme
import org.junit.Rule
import org.junit.Test

class ListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    fun setup(list: List<NoteUI> = emptyList()) {
        composeTestRule.setContent {
            NoteTakerTheme(darkTheme = true) {
                ListScreen(notes = list)
            }
        }
    }

    @Test
    fun testEmptyList() {
        setup(emptyList())
        composeTestRule.onNodeWithText("No Notes Yet", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testOneItemList() {
        setup(listOf(NoteUI(1, "Test", "Test Note", System.currentTimeMillis(), emptyList())))
        composeTestRule.onNodeWithText("Test Note", useUnmergedTree = true).assertIsDisplayed()
    }
}