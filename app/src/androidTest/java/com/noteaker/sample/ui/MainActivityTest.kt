package com.noteaker.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.noteaker.sample.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI test that launches the real [MainActivity] (same as a user opening the app).
 *
 * [HiltAndroidRule] + [HiltTestRunner] provide Hilt so `hiltViewModel()` and `@AndroidEntryPoint`
 * behave like production.
 *
 * List content comes from [com.noteaker.sample.di.FakeRepositoryTestModule] → [com.noteaker.sample.fakes.FakeNoteRepository],
 * not Room (option 3: fake [com.noteaker.sample.data.repository.NoteRepository]).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_showsListSearchUi() {
        composeRule.onNodeWithText("Search by title or content...", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun mainActivity_showsSeededNotesFromFakeRepository() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Instrumented title"), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Instrumented title", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Hello from fake repository", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun mainActivity_clickEditButton_navigatesToEditScreen() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Instrumented title"), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Instrumented title", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Instrumented title").performClick()
        composeRule.waitUntil(3000) {
            composeRule.onNodeWithText("Edit Note", useUnmergedTree = true).isDisplayed()
        }
    }
}
