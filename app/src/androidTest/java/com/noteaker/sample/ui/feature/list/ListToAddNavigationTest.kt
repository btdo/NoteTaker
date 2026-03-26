package com.noteaker.sample.ui.feature.list

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.ListRoute
import com.noteaker.sample.ui.theme.NoteTakerTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI test: List → Add without [MainActivity] / Hilt.
 *
 * Pattern: host a small [NavHost] in [setContent] that reuses the same route paths as production
 * ([ListRoute.path], [AddRoute.path]) and wires [ListScreen]’s FAB to [navController.navigate].
 * The Add destination is a minimal stub so we don’t need [AddViewModel] or NoteDetailsScreen.
 */
class ListToAddNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun listScreen_tapAddFab_navigatesToAddDestination() {
        composeTestRule.setContent {
            NoteTakerTheme(darkTheme = true) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = ListRoute.path,
                ) {
                    composable(ListRoute.path) {
                        ListScreen(
                            notes = emptyList(),
                            onAddClick = { navController.navigate(AddRoute.path) },
                        )
                    }
                    composable(AddRoute.path) {
                        Text("Add Note")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("No Notes Yet", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Save Note").performClick()
        composeTestRule.onNodeWithText("Add Note").assertIsDisplayed()
    }
}
