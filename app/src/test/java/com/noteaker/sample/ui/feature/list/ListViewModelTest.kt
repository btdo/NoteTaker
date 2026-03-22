package com.noteaker.sample.ui.feature.list

import app.cash.turbine.test
import com.noteaker.sample.TestCoroutineRule
import com.noteaker.sample.ai.NavigationOrchestrator
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.navigation.AddRoute
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ListViewModelTest {

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    lateinit var viewModel: ListViewModel

    @MockK(relaxed = true)
    lateinit var repository: NoteRepository

    @MockK(relaxed = true)
    lateinit var navigationManager: NavigationManager

    @MockK(relaxed = true)
    lateinit var navigationOrchestrator: NavigationOrchestrator

    @MockK(relaxed = true)
    lateinit var quoteRepository: QuoteRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel =
            ListViewModel(repository, navigationManager, navigationOrchestrator, quoteRepository)
    }

    @Test
    fun testAddClickSuccess() {
        coEvery {
            navigationOrchestrator.processUserIntent(any())
        } returns Result.success(Unit)

        viewModel.addClick()

        coVerify(exactly = 1) {
            navigationOrchestrator.processUserIntent(any())
        }
    }

    @Test
    fun testAddClickFailure() {
        coEvery {
            navigationOrchestrator.processUserIntent(any())
        } returns Result.failure(Exception("Failed to process intent"))

        viewModel.addClick()

        coVerify(exactly = 1) {
            navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(AddRoute.path)))
        }
    }

    @Test
    fun testOnEditClickSuccess() {
        coEvery {
            navigationOrchestrator.processUserIntent(any())
        } returns Result.success(Unit)

        viewModel.onEditClick(Note(1, "Test", "Test Note", System.currentTimeMillis(), emptyList()))

        coVerify(exactly = 1) {
            navigationOrchestrator.processUserIntent(any())
        }
    }

    @Test
    fun testSearchResults() = runTest {
        val notes = listOf(
            Note(1, "Test", "Test Note", System.currentTimeMillis(), emptyList()),
            Note(2, "Test2", "Test Note2", System.currentTimeMillis(), emptyList()),
            Note(3, "Not", "abcd", System.currentTimeMillis(), emptyList())
        )
        // combine() cancels when any upstream completes; flowOf emits once then completes,
        // so debounce never gets to emit with the notes still available.
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        // noteList is read when ListViewModel initializes combine(...); stub before construction.
        val searchViewModel =
            ListViewModel(repository, navigationManager, navigationOrchestrator, quoteRepository)

        searchViewModel.searchResults.test {
            assertEquals(0, awaitItem().size)
            searchViewModel.onSearchQuery("Test")
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Test", result[0].title)
            assertEquals("Test2", result[1].title)
        }
    }

    @Test
    fun testSearchResultsWithAttachment() = runTest {
        val notes = listOf(
            Note(1, "Test", "Test Note", System.currentTimeMillis(), emptyList()),
            Note(2, "Test2", "Test Note2", System.currentTimeMillis(), emptyList()),
            Note(
                3, "Not", "abcd", System.currentTimeMillis(), listOf(
                    Attachment(
                        1,
                        "uri",
                        "Test",
                        "mimeType"
                    )
                )
            )
        )
        // combine() cancels when any upstream completes; flowOf emits once then completes,
        // so debounce never gets to emit with the notes still available.
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        // noteList is read when ListViewModel initializes combine(...); stub before construction.
        val searchViewModel =
            ListViewModel(repository, navigationManager, navigationOrchestrator, quoteRepository)

        searchViewModel.searchResults.test {
            assertEquals(0, awaitItem().size)
            searchViewModel.onSearchQuery("Test")
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Test", result[0].title)
            assertEquals("Test2", result[1].title)
            assertEquals("Not", result[2].title)
        }
    }
}