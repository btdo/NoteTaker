package com.noteaker.sample.ui.feature.edit

import app.cash.turbine.test
import com.noteaker.sample.TestCoroutineRule
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.model.UIState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditViewModelTest {
    @MockK(relaxed = true)
    lateinit var navigationManager: NavigationManager

    @MockK
    lateinit var repository: NoteRepository

    lateinit var editViewModel: EditViewModel

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        editViewModel = EditViewModel(repository, navigationManager)
    }

    @Test
    fun testGetEditNote() = runTest(testCoroutineRule.testDispatcher) {
        val fakeNote = Note(id = 123, title = "Test", note = "Test Note", attachments = emptyList())
        coEvery {
            repository.get(fakeNote.id.toInt())
        } returns fakeNote

        val job =  testCoroutineRule.testScope.launch (start = CoroutineStart.LAZY) {
            editViewModel.getEditNote(fakeNote.id.toInt())
        }

        editViewModel.uiState.test {
            job.start()
            val state = awaitItem()
            assertEquals(UIState.Loading, state)
            val state2 = awaitItem()
            assertEquals(UIState.Success(Unit), state2)
        }

        editViewModel.note.test {
            val note =  awaitItem()
            assertEquals(fakeNote, note)
        }
    }

}