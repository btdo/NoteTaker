package com.noteaker.sample.ui.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.NoteStatus
import com.noteaker.sample.domain.model.SyncStatus
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.navigation.SnackBar
import com.noteaker.sample.navigation.SnackBarAction
import com.noteaker.sample.ui.model.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    fun onSave(note: Note) {
        viewModelScope.launch {
            try {
                val success = _uiState.compareAndSet(UIState.Success(Unit), UIState.Loading)
                if (!success) return@launch
                val newNote = note.copy(syncStatus = SyncStatus.PENDING, version = note.version + 1)
                _note.value = newNote
                repository.edit(newNote)
                navigationManager.showSnackBar("Note Saved")
                navigationManager.popBackStack()
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Failed to save note")
            }
        }
    }

    fun onDelete(note: Note) {
        viewModelScope.launch {
            val deletedSet = mutableSetOf(note.id)
            repository.updateNoteStatus(deletedSet, NoteStatus.ARCHIVED, syncStatus = SyncStatus.PENDING).onSuccess {
                navigationManager.showSnackBar(SnackBar("Note deleted", SnackBarAction("Undo", {
                    repository.updateNoteStatus(deletedSet, note.status, syncStatus = note.syncStatus)
                })))
                navigationManager.popBackStack()
            }.onFailure {
                navigationManager.showSnackBar("Failed to delete note")
            }
        }
    }

    suspend fun getEditNote(noteId: Int): Note? {
        try {
            _uiState.value = UIState.Loading
            val note = repository.get(noteId)
            _note.value = note
            _uiState.value = UIState.Success(Unit)
            return note
        } catch (e: Exception) {
            _uiState.value = UIState.Error(e.message ?: "Failed to load note")
            return null
        }
    }

    fun onCancel() {
        navigationManager.popBackStack()
    }
}