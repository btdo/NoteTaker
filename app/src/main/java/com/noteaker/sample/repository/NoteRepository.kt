package com.noteaker.sample.repository

import com.noteaker.sample.model.Note
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface NoteRepository {

    val noteList: StateFlow<List<Note>>

    fun add(note: Note)
    fun edit(note: Note)
    fun delete(note: Note)
    fun get(id: String)
}

@ActivityRetainedScoped
class MyNoteRepository @Inject constructor() : NoteRepository {
    private val _noteList = MutableStateFlow<List<Note>>(listOf())
    override val noteList: StateFlow<List<Note>> = _noteList

    override fun add(note: Note) {
        TODO("Not yet implemented")
    }

    override fun edit(note: Note) {
        TODO("Not yet implemented")
    }

    override fun delete(note: Note) {
        TODO("Not yet implemented")
    }

    override fun get(id: String) {
        TODO("Not yet implemented")
    }
}