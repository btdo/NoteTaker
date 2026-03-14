package com.noteaker.sample.ui.feature.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.model.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Success(Unit))
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    fun onSave(note: Note) {
        viewModelScope.launch {
            try {
                _uiState.value = UIState.Loading
                repository.add(note)
                _uiState.value = UIState.Success(Unit)
                navigationManager.showSnackBar("Note Saved")
                navigationManager.popBackStack()
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Failed to save note")
            }
        }
    }

    fun onCancel() {
        navigationManager.popBackStack()
    }
}