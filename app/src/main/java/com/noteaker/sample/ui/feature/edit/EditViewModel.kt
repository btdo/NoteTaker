package com.noteaker.sample.ui.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    fun onSave(note: Note) {
        viewModelScope.launch {
            repository.edit(note)
            navigationManager.popBackStack()
        }
    }

    suspend fun getEditNote(noteId: Int): Note {
        return repository.get(noteId)
    }

    fun onCancel() {
        navigationManager.popBackStack()
    }
}