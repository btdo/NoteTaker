package com.noteaker.sample.ui.feature.list

import app.cash.turbine.test
import com.noteaker.sample.TestCoroutineRule
import com.noteaker.sample.ai.IntentOrchestrator
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.model.UIState
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.EditRoute
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ListViewModelTest {

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    lateinit var viewModel: ListViewModel

    @MockK(relaxed = true)
    lateinit var repository: NoteRepository

    @MockK(relaxed = true)
    lateinit var navigationManager: NavigationManager

    @MockK(relaxed = true)
    lateinit var intentOrchestrator: IntentOrchestrator

    @MockK(relaxed = true)
    lateinit var quoteRepository: QuoteRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)
    }

    @Test
    fun testAddClickSuccess() {
        coEvery {
            intentOrchestrator.processUserIntent(any())
        } returns Result.success(Unit)

        viewModel.addClick()

        coVerify(exactly = 1) {
            intentOrchestrator.processUserIntent(any())
        }
    }

    @Test
    fun testAddClickFailure() {
        coEvery {
            intentOrchestrator.processUserIntent(any())
        } returns Result.failure(Exception("Failed to process intent"))

        viewModel.addClick()

        coVerify(exactly = 1) {
            navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(AddRoute.path)))
        }
    }

    @Test
    fun testOnEditClickSuccess() {
        coEvery {
            intentOrchestrator.processUserIntent(any())
        } returns Result.success(Unit)

        viewModel.onEditClick(
            NoteUI(
                1,
                "Test",
                "Test Note",
                System.currentTimeMillis(),
                emptyList()
            )
        )

        coVerify(exactly = 1) {
            intentOrchestrator.processUserIntent(any())
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
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            assertEquals(UIState.Loading, (awaitItem() as UIState))
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
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            assertEquals(0, awaitItem().size)
            searchViewModel.onSearchQuery("Test")
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Test", result[0].title)
            assertEquals("Test2", result[1].title)
            assertEquals("Not", result[2].title)
        }
    }

    @Test
    fun testOnEditClickFailure() {
        coEvery {
            intentOrchestrator.processUserIntent(any())
        } returns Result.failure(Exception("Failed to process intent"))

        val note = NoteUI(42, "Title", "Body", System.currentTimeMillis(), emptyList())
        viewModel.onEditClick(note)

        coVerify(exactly = 1) {
            navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(42)))
        }
    }

    @Test
    fun testSearchResultsByContent() = runTest {
        val notes = listOf(
            Note(1, "Alpha", "Hello World", System.currentTimeMillis(), emptyList()),
            Note(2, "Beta", "Another body", System.currentTimeMillis(), emptyList())
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            assertEquals(0, awaitItem().size)
            searchViewModel.onSearchQuery("world")
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alpha", result[0].title)
        }
    }

    @Test
    fun testSearchIsCaseInsensitive() = runTest {
        val notes = listOf(
            Note(1, "Test", "body", System.currentTimeMillis(), emptyList()),
            Note(2, "TeSt2", "body", System.currentTimeMillis(), emptyList())
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            assertEquals(0, awaitItem().size)
            searchViewModel.onSearchQuery("tEsT")
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Test", result[0].title)
            assertEquals("TeSt2", result[1].title)
        }
    }

    @Test
    fun testWhitespaceQueryReturnsAll() = runTest {
        val notes = listOf(
            Note(1, "One", "aaa", System.currentTimeMillis(), emptyList()),
            Note(2, "Two", "bbb", System.currentTimeMillis(), emptyList()),
            Note(3, "Three", "ccc", System.currentTimeMillis(), emptyList())
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            assertEquals(0, awaitItem().size)
            searchViewModel.onSearchQuery("   ")
            val result = awaitItem()
            assertEquals(3, result.size)
        }
    }

    @Test
    fun testQuotesFlowEmitsNullOnError() = runTest {
        coEvery { quoteRepository.getQuote() } throws Exception("network")

        viewModel.quotes.test {
            val first = awaitItem()
            assertEquals(null, first)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testDeleteSelectedNotes() = runTest {
        val deletedSet = setOf<Long>(1, 2)
        val notes = listOf(
            Note(1, "Test", "body", System.currentTimeMillis(), emptyList()),
            Note(2, "TeSt2", "body", System.currentTimeMillis(), emptyList())
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        coEvery {
            repository.deleteSelectedNotes(deletedSet)
        } returns Result.success(Unit)

        searchViewModel.onSelectionChange(1, true)
        searchViewModel.onSelectionChange(2, true)
        searchViewModel.onDeleteClick()
        advanceUntilIdle()
        coVerify(exactly = 1) {
            repository.deleteSelectedNotes(deletedSet)
        }
        coVerify {
            navigationManager.showSnackBar("Notes deleted")
        }
        assertEquals(emptySet<Long>(), searchViewModel.selectedNoteIds.value)
    }

    @Test
    fun testSearchResultsWithoutTurbine() = runTest(testCoroutineRule.testDispatcher) {
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
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)
        searchViewModel.onSearchQuery("Test")

        var list : List<NoteUI>? = null
        val job = launch {
            searchViewModel.searchResult.collect {
                if (it.isEmpty()) {
                    list = it
                }

            }
        }

        try {
            withTimeout(1000) {
                while(true) {
                    advanceTimeBy(500)
                    list?.let {
                        assertEquals(3, it.size)
                        job.cancelAndJoin()
                        break
                    }
                }
            }

        } catch (e: Exception) {
            fail("Did not assert on time")
        }
    }

    @Test
    fun testSearchResultsHandlesRepositoryException() = runTest {
        // Repository throws an exception
        every { repository.noteList } returns flow {
            throw RuntimeException("Database corrupted!")
        }

        val errorViewModel = ListViewModel(
            repository, navigationManager, intentOrchestrator, quoteRepository
        )

        // With retry + catch, should emit empty list instead of crashing
        errorViewModel.searchResultWithRetry.test {
            val result = awaitItem()
            assertEquals(emptyList<NoteUI>(), result)
            cancelAndIgnoreRemainingEvents()
        }
    }
}