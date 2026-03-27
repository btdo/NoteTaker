package com.noteaker.sample.ui.feature.list

import app.cash.turbine.test
import com.noteaker.sample.TestCoroutineRule
import com.noteaker.sample.ai.IntentOrchestrator
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.NoteStatus
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.navigation.SnackBar
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.model.UIState
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.EditRoute
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
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
                attachments = emptyList()
            )
        )

        coVerify(exactly = 1) {
            intentOrchestrator.processUserIntent(any())
        }
    }

    @Test
    fun testSearchResults() = runTest scope@{
        val notes = listOf(
            Note(
                1,
                "Test",
                "Test Note",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "Test2",
                "Test Note2",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                3,
                "Not",
                "abcd",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
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
            this@scope.advanceTimeBy(500)
            assertEquals(3, awaitItem().size)
            searchViewModel.onSearchQuery("Test")
            this@scope.advanceTimeBy(500)
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Test", result[0].title)
            assertEquals("Test2", result[1].title)
        }
    }

    @Test
    fun testSearchResultsWithAttachment() = runTest scope@{
        val notes = listOf(
            Note(
                1,
                "Test",
                "Test Note",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "Test2",
                "Test Note2",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                3, "Not", "abcd", lastUpdated = System.currentTimeMillis(), attachments = listOf(
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
            this@scope.advanceTimeBy(500)
            assertEquals(3, awaitItem().size)
            searchViewModel.onSearchQuery("Test")
            this@scope.advanceTimeBy(500)
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

        val note =
            NoteUI(42, "Title", "Body", System.currentTimeMillis(), attachments = emptyList())
        viewModel.onEditClick(note)

        coVerify(exactly = 1) {
            navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(42)))
        }
    }

    @Test
    fun testSearchResultsByContent() = runTest scope@{
        val notes = listOf(
            Note(
                1,
                "Alpha",
                "Hello World",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "Beta",
                "Another body",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            )
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            this@scope.advanceTimeBy(500)
            assertEquals(2, awaitItem().size)
            searchViewModel.onSearchQuery("world")
            this@scope.advanceTimeBy(500)
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alpha", result[0].title)
        }
    }

    @Test
    fun testSearchIsCaseInsensitive() = runTest scope@{
        val notes = listOf(
            Note(
                1,
                "Test",
                "body",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "TeSt2",
                "body",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            )
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            this@scope.advanceTimeBy(500)
            assertEquals(2, awaitItem().size)
            searchViewModel.onSearchQuery("tEsT")
            this@scope.advanceTimeBy(500)
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Test", result[0].title)
            assertEquals("TeSt2", result[1].title)
        }
    }

    @Test
    fun testWhitespaceQueryReturnsAll() = runTest scope@{
        val notes = listOf(
            Note(
                1,
                "One",
                "aaa",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "Two",
                "bbb",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                3,
                "Three",
                "ccc",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            )
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        searchViewModel.searchResult.test {
            this@scope.advanceTimeBy(500)
            assertEquals(3, awaitItem().size)
            searchViewModel.onSearchQuery("   ")
            this@scope.advanceTimeBy(500)
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
            Note(
                1,
                "Test",
                "body",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "TeSt2",
                "body",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            )
        )
        every { repository.noteList } returns flow {
            emit(notes)
            awaitCancellation()
        }
        val searchViewModel =
            ListViewModel(repository, navigationManager, intentOrchestrator, quoteRepository)

        coEvery {
            repository.updateNoteStatus(deletedSet, NoteStatus.ARCHIVED)
        } returns Result.success(Unit)

        searchViewModel.onSelectionChange(1, true)
        searchViewModel.onSelectionChange(2, true)
        searchViewModel.onDeleteClick()
        advanceUntilIdle()
        coVerify(exactly = 1) {
            repository.updateNoteStatus(deletedSet, NoteStatus.ARCHIVED)
        }

        val snackBarSlot = slot<SnackBar>()
        coVerify {
            navigationManager.showSnackBar(capture(snackBarSlot))
        }
        assertEquals("Notes deleted", snackBarSlot.captured.message)
        assertEquals("Undo", snackBarSlot.captured.snackBarAction?.label)
        assertEquals(emptySet<Long>(), searchViewModel.selectedNoteIds.value)
    }

    @Test
    fun testSearchResultsWithoutTurbine() = runTest scope@{
        val notes = listOf(
            Note(
                1,
                "Test",
                "Test Note",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                2,
                "Test2",
                "Test Note2",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            ),
            Note(
                3, "Not", "abcd", lastUpdated = System.currentTimeMillis(), attachments = listOf(
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
        var list: List<NoteUI>? = null
        val job = launch {
            searchViewModel.searchResult.collect { list = it }
        }
        this@scope.advanceTimeBy(500)
        searchViewModel.onSearchQuery("Test")
        this@scope.advanceTimeBy(500)
        advanceUntilIdle()
        assertEquals(3, list?.size)
        job.cancelAndJoin()
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

        errorViewModel.searchResultWithRetry.test {
            // StateFlow keeps only the latest value; with UnconfinedTestDispatcher, Loading → Error
            // can happen before the first awaitItem(), so the first emission may already be Error.
            val first = awaitItem()
            val err = if (first is UIState.Loading) {
                advanceUntilIdle()
                awaitItem()
            } else {
                first
            }
            assertTrue(err is UIState.Error)
            assertTrue((err as UIState.Error).message.contains("Database"))
            cancelAndIgnoreRemainingEvents()
        }
    }
}