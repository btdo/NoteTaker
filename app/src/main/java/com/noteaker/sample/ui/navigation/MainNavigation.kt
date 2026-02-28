package com.noteaker.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@OptIn(FlowPreview::class)
@Composable
fun MainNavigation(navigationManager: NavigationManager, navController: NavController) {
    LaunchedEffect(key1 = Unit) {
        navigationManager.navigationState
            .debounce(100)
            .distinctUntilChanged()
            .onEach { navState ->
                when (navState) {
                    is NavState.NavigateToRoute -> {
                        navigateToRoute(navState, navController)
                    }

                    is NavState.PopToRoute -> {
                        navController.popBackStack(navState.destination, navState.isInclusive)
                    }

                    is NavState.PopBackStack -> {
                        navController.popBackStack()
                    }

                    is NavState.Idle -> {}
                }

                navigationManager.onNavigated(navState)
            }.launchIn(scope = this)
    }
}

fun navigateToRoute(
    navigationState: NavState.NavigateToRoute,
    navController: NavController
) {
    try {
        navController.navigate(navigationState.command.path) {
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
            if (navigationState.command.clearBackStack) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Error while navigating to ${navigationState.command.path}")
    }
}