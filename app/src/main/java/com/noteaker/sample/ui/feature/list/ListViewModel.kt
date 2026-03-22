package com.noteaker.sample.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteaker.sample.ai.NavigationOrchestrator
import com.noteaker.sample.data.model.ZenQuotes
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
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
import kotlinx.coroutines.flow.stateIn
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

    val quotes = flow<ZenQuotes.ZenQuotesItem?> {
        for (i in 0 until 5) {
            val quotes = quoteRepository.getQuote()
            emit(quotes[0])
            delay(120000)
        }
    }.catch { e ->
        emit(null)
    }

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<Note>> =
        combine(_searchQuery.debounce(500), repository.noteList) { query, notes ->
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

    fun onEditClick(note: Note) {
        viewModelScope.launch {
            navigationOrchestrator.processUserIntent(
                "User tapped on note with id ${note.id} to edit it."
            ).onFailure {
                Timber.e(it, "Failed to process edit note intent, use fallback navigation")
                navigationManager.navigate(NavState.NavigateToRoute(EditRoute.getRoute(note.id.toInt())))
            }
        }
    }

    fun onSearchQuery(searchQuery: String) {
        _searchQuery.value = searchQuery
    }
}