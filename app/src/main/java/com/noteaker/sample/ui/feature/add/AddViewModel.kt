package com.noteaker.sample.ui.feature.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(private val repository: NoteRepository) : ViewModel() {

    fun add(note: Note) {
        viewModelScope.launch {
            repository.add(note)
        }
    }
}