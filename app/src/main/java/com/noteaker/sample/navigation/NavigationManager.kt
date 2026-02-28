package com.noteaker.sample.navigation

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class NavigationManager @Inject constructor() {
    val navigationState: MutableStateFlow<NavState> =
        MutableStateFlow(NavState.Idle)

    fun onNavigated(state: NavState) {
        navigationState.compareAndSet(state, NavState.Idle)
    }

    fun popBackStack() {
        navigate(NavState.PopBackStack)
    }

    fun navigate(state: NavState) {
        navigationState.value = state
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