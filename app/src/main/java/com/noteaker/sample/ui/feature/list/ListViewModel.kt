package com.noteaker.sample.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.ai.NavigationOrchestrator
import com.noteaker.sample.data.model.ZenQuotes
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.model.UIState
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.EditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager,
    private val navigationOrchestrator: NavigationOrchestrator,
    private val quoteRepository: QuoteRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNoteIds: StateFlow<Set<Long>> = _selectedNoteIds.asStateFlow()

    val quotes = flow<ZenQuotes.ZenQuotesItem?> {
        for (i in 0 until 5) {
            val quotes = quoteRepository.getQuote()
            emit(quotes[0])
            delay(120000)
        }
    }.catch { _ ->
        emit(null)
    }

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<NoteUI>> =
        combine(
            _searchQuery.debounce(500),
            repository.noteList
                .map { notes ->
                    notes.map { note -> NoteUI.fromNote(note) }
                }
                .retry(3) { cause ->
                    Timber.w(cause, "Error loading notes, retrying...")
                    delay(1000) // Wait before retry
                    true // Retry on any exception
                }
                .catch { e ->
                    // After 3 retries, emit empty list and continue
                    Timber.e(e, "Error loading notes after retries, showing empty list")
                    emit(emptyList())
                }
        ) { query, notes ->
            try {
                val q = query.trim()
                if (q.isEmpty()) notes
                else notes.filter {
                    it.note.contains(q, ignoreCase = true) || it.title.contains(
                        q,
                        ignoreCase = true
                    ) || it.attachments.filter { attachment ->
                        (attachment.displayName ?: "").contains(q, ignoreCase = true)
                    }.isNotEmpty()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error filtering notes")
                emptyList()
            }
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addClick() {
        viewModelScope.launch {
            navigationOrchestrator.processUserIntent(
                "User tapped the Add button to add a new note."
            ).onFailure {
                // Fallback: navigate directly if AI call fails (e.g. no network)
                Timber.e(it, "Failed to process add note intent, use fallback navigation")
                navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(AddRoute.path)))
            }
        }
    }

    fun onEditClick(note: NoteUI) {
        viewModelScope.launch {
            navigationOrchestrator.processUserIntent(
                "User tapped on note with id ${note.id} to edit it."
            ).onFailure {
                Timber.e(it, "Failed to process edit note intent, use fallback navigation")
                navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(note.id.toInt())))
            }
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            repository.deleteSelectedNotes(_selectedNoteIds.value).onSuccess {
                navigationManager.showSnackBar("Note${if (_selectedNoteIds.value.size > 1) "s" else ""} deleted")
                _selectedNoteIds.value = setOf()
            }.onFailure {
                navigationManager.showSnackBar("Failed to delete notes.  Try again later")
            }
        }
    }

    fun onSearchQuery(searchQuery: String) {
        _searchQuery.value = searchQuery
    }

    fun onSelectionChange(noteId: Long, isSelected: Boolean) {
        _selectedNoteIds.update { currentSet ->
            if (isSelected) {
                currentSet + noteId
            } else {
                currentSet - noteId
            }
        }
    }
}