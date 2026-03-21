package com.noteaker.sample.data.repository

import androidx.room.withTransaction
import com.noteaker.sample.data.dao.AttachmentDao
import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.database.AppDatabase
import com.noteaker.sample.data.model.NoteEntity
import com.noteaker.sample.di.IoDispatcher
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface NoteRepository {

    val noteList: Flow<List<Note>>

    suspend fun add(note: Note): Note
    suspend fun edit(note: Note)
    suspend fun delete(note: Note)
    suspend fun get(id: Int): Note
}

class MyNoteRepository @Inject constructor(
    private val database: AppDatabase,
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : NoteRepository {

    override val noteList: Flow<List<Note>> = noteDao.getAllFlow()
        .map { entities -> loadNotesWithAttachments(entities) }
        .flowOn(dispatcher)

    private suspend fun loadNotesWithAttachments(entities: List<NoteEntity>): List<Note> =
        withContext(dispatcher) {
            entities.map { entity ->
                val attachments = attachmentDao.getByNoteIdOnce(entity.id).map { it.toAttachment() }
                entity.toNote(attachments)
            }
        }

    override suspend fun add(note: Note): Note = withContext(dispatcher) {
        delay(500)
        return@withContext database.withTransaction {
            addImpl(note)
        }
    }

    suspend fun addImpl(note: Note): Note = withContext(dispatcher) {
        var newNote: Note? = null
        var attachments: MutableList<Attachment>? = null
        val noteId = noteDao.insert(note.toEntity())
        note.attachments.forEach {
            if (attachments == null) attachments = mutableListOf()
            val attachmentId = attachmentDao.insert(it.toEntity(noteId))
            attachments?.add(it.copy(id = attachmentId))
        }
        newNote = note.copy(id = noteId, attachments = attachments ?: emptyList())
        if (newNote == null) throw Exception("Failed to add note")
        return@withContext newNote
    }

    override suspend fun edit(note: Note) = withContext(dispatcher) {
        delay(500)
        database.withTransaction {
            val entity = note.toEntity()
            noteDao.update(entity)
            attachmentDao.deleteByNoteId(note.id.toLong())
            note.attachments.forEach { attachmentDao.insert(it.toEntity(entity.id)) }
        }
    }

    override suspend fun delete(note: Note) = withContext(dispatcher) {
        database.withTransaction {
            noteDao.delete(note.toEntity())
            attachmentDao.deleteByNoteId(note.id.toLong())
        }
    }

    override suspend fun get(id: Int): Note {
        delay(500)
        noteDao.getById(id.toLong())?.let { note ->
            val attachments = attachmentDao.getByNoteIdOnce(note.id).map { it.toAttachment() }
            return note.toNote(attachments)
        }

        throw IllegalArgumentException("Note not found for $id")
    }
}
