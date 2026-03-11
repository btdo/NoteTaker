package com.noteaker.sample.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.EditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<Note>> =
        combine(_searchQuery.debounce(500), repository.noteList) { query, notes ->
            val q = query.trim()
            if (q.isEmpty()) notes
            else notes.filter {
                it.note.contains(q, ignoreCase = true) || it.title.contains(q, ignoreCase = true)
            }
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addClick() {
        navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(AddRoute.path)))
    }

    fun onEditClick(note: Note) {
        navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(note.id)))
    }

    fun onSearchQuery(searchQuery: String) {
        _searchQuery.value = searchQuery
    }
}