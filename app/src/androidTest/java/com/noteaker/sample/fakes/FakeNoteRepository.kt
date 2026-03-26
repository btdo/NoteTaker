package com.noteaker.sample.fakes

import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory [NoteRepository] for instrumented tests — no Room.
 * Seed [initialNotes] to control what appears on the list screen.
 */
@Singleton
class FakeNoteRepository @Inject constructor() : NoteRepository {

    private val _notes = MutableStateFlow(
        listOf(
            Note(
                id = 1L,
                title = "Instrumented title",
                note = "Hello from fake repository",
                lastUpdated = System.currentTimeMillis(),
                attachments = emptyList()
            )
        )
    )

    override val noteList: Flow<List<Note>> = _notes.asStateFlow()

    override suspend fun add(note: Note): Note {
        val nextId = (_notes.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val withId = if (note.id == 0L) note.copy(id = nextId) else note
        _notes.value = _notes.value + withId
        return withId
    }

    override suspend fun edit(note: Note) {
        _notes.value = _notes.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun delete(note: Long): Result<Unit> {
        _notes.value = _notes.value.filter { it.id != note }
        return Result.success(Unit)
    }

    override suspend fun get(id: Int): Note =
        _notes.value.firstOrNull { it.id == id.toLong() }
            ?: throw IllegalArgumentException("Note not found for $id")

    override suspend fun deleteSelectedNotes(noteIds: Set<Long>): Result<Unit> {
        _notes.value = _notes.value.filter { it.id !in noteIds }
        return Result.success(Unit)
    }
}
