package com.noteaker.sample.ui.feature.list

import androidx.lifecycle.ViewModel
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.EditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {
    val notes: Flow<List<Note>> = repository.noteList

    fun addClick() {
        navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(AddRoute.path)))
    }

    fun onEditClick(note: Note) {
        navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(note.id)))
    }
}