package com.noteaker.sample.navigation

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@ActivityRetainedScoped
class NavigationManager @Inject constructor() {
    private val _navigationState = MutableStateFlow<NavState>(NavState.Idle)
    val navigationState: StateFlow<NavState> = _navigationState.asStateFlow()

    private val _snackBar = MutableSharedFlow<String>()
    val snackBar: SharedFlow<String> = _snackBar.asSharedFlow()

    suspend fun showSnackBar(message: String) {
        _snackBar.emit(message)
    }

    fun onNavigated(state: NavState) {
        _navigationState.compareAndSet(state, NavState.Idle)
    }

    fun popBackStack() {
        navigate(NavState.PopBackStack)
    }

    fun navigate(state: NavState) {
        _navigationState.value = state
    }
}

/**
 * Navigate to route.  When isRoot is true, clear the back stack
 */
data class NavigationCommand (val path: String, val clearBackStack: Boolean = false)

sealed class NavState {
    object Idle : NavState()

    data class NavigateToRoute(val command: NavigationCommand) : NavState()

    /**
     * @param destination is the static destination to pop to, without parameter replacements.
     */
    data class PopToRoute(val destination: String, val isInclusive: Boolean = false) : NavState()

    object PopBackStack : NavState()
}