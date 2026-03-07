package com.noteaker.sample.data.repository

import androidx.room.withTransaction
import com.noteaker.sample.data.dao.AttachmentDao
import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.database.AppDatabase
import com.noteaker.sample.data.model.NoteEntity
import com.noteaker.sample.di.IoDispatcher
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.toEntity
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface NoteRepository {

    val noteList: Flow<List<Note>>

    suspend fun add(note: Note)
    suspend fun edit(note: Note)
    suspend fun delete(note: Note)
    suspend fun get(id: Int) : Note
}

@ActivityRetainedScoped
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

    override suspend fun add(note: Note) = withContext(dispatcher) {
        database.withTransaction {
            val noteId = noteDao.insert(note.toEntity())
            note.attachments.forEach { attachmentDao.insert(it.toEntity(noteId)) }
        }
    }

    override suspend fun edit(note: Note) = withContext(dispatcher) {
        database.withTransaction {
            val entity = note.toEntity()
            noteDao.update(entity)
            attachmentDao.deleteByNoteId(entity.id)
            note.attachments.forEach { attachmentDao.insert(it.toEntity(entity.id)) }
        }
    }

    override suspend fun delete(note: Note) = withContext(dispatcher) {
        noteDao.delete(note.toEntity())
    }

    override suspend fun get(id: Int): Note {
        noteDao.getById(id.toLong())?.let {
            val attachments = attachmentDao.getByNoteIdOnce(it.id).map { it.toAttachment() }
            return it.toNote(attachments)
        }

        throw IllegalArgumentException("Note not found for $id")
    }
}
