package com.noteaker.sample.data.repository

import com.noteaker.sample.TestCoroutineRule
import com.noteaker.sample.data.dao.AttachmentDao
import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.database.AppDatabase
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.toEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random


class NoteRepositoryTest {

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    @MockK(relaxed = true)
    lateinit var database: AppDatabase

    @MockK(relaxed = true)
    lateinit var noteDao: NoteDao

    @MockK(relaxed = true)
    lateinit var attachmentDao: AttachmentDao

    lateinit var noteRepository: MyNoteRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        noteRepository =
            MyNoteRepository(database, noteDao, attachmentDao, testCoroutineRule.testDispatcher)
    }

    @Test
    fun testAddNoteWithAttachments() = runTest {
        val newNoteId = Random.nextLong()
        val newAttachmentId = Random.nextLong()
        coEvery {
            noteDao.insert(any())
        } returns newNoteId

        coEvery {
            attachmentDao.insert(any())
        } returns newAttachmentId

        val attachment  = Attachment(0, "file://test.file", "My File", "text")
        val attachments = listOf(attachment)
        val note = Note(title = "Test", note = "Test Note", attachments = attachments)
        val newNote = noteRepository.addImpl(note)
        val expectedNote = note.copy(
            id = newNoteId,
            attachments = attachments.map { it.copy(id = newAttachmentId) })
        val expectedAttachment = attachments[0].copy(id = newAttachmentId)
        coVerify {
            noteDao.insert(note.toEntity())
        }

        coVerify(exactly = 1) {
            attachmentDao.insert(any())
        }

        assertEquals(expectedNote, newNote)
        assertEquals(expectedAttachment, newNote.attachments[0])
    }
}