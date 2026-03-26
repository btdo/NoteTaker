package com.noteaker.sample.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.ai.IntentOrchestrator
import com.noteaker.sample.data.model.ZenQuotes
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.domain.model.NoteStatus
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.navigation.SnackBar
import com.noteaker.sample.navigation.SnackBarAction
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.model.UIState
import com.noteaker.sample.ui.navigation.AddRoute
import com.noteaker.sample.ui.navigation.EditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val navigationManager: NavigationManager,
    private val intentOrchestrator: IntentOrchestrator,
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

    private val retryEmitter = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val searchResult = combine(
        _searchQuery.debounce(500),
        repository.noteList
            .map { notes ->
                notes.map { note -> NoteUI.fromNote(note) }
            }
    ) { query, notes ->
        val q = query.trim()
        val filteredNotes = if (q.isEmpty()) notes else notes.filter {
            it.note.contains(q, ignoreCase = true) || it.title.contains(
                q,
                ignoreCase = true
            ) || it.attachments.filter { attachment ->
                (attachment.displayName ?: "").contains(q, ignoreCase = true)
            }.isNotEmpty()
        }

        filteredNotes
    }

    val searchResultWithRetry: StateFlow<UIState> = retryEmitter
        .onStart { emit(Unit) }
        .flatMapLatest {
            searchResult.map {
                UIState.Success(it) as UIState
            }.catch {
                emit(UIState.Error(it.message ?: "Failed to load notes"))
            }
        }.catch {
            emit(UIState.Error(it.message ?: "Failed to load notes"))
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UIState.Loading
        )

    fun retry() {
        viewModelScope.launch {
            retryEmitter.emit(Unit)
        }
    }

    fun addClick() {
        viewModelScope.launch {
            intentOrchestrator.processUserIntent(
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
            intentOrchestrator.processUserIntent(
                "User tapped on note with id ${note.id} to edit it."
            ).onFailure {
                Timber.e(it, "Failed to process edit note intent, use fallback navigation")
                navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(note.id.toInt())))
            }
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            val selectedNoteIds = _selectedNoteIds.value
            repository.updateNoteStatus(selectedNoteIds, NoteStatus.ARCHIVED).onSuccess {
                navigationManager.showSnackBar(SnackBar("Notes deleted", SnackBarAction("Undo", {
                    repository.updateNoteStatus(selectedNoteIds, NoteStatus.ACTIVE)
                })))
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