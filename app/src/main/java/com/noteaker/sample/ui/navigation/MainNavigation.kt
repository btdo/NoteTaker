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
        val path = navigationState.command.path
        val isAlreadyInStack = isRouteInBackStack(navController, path)
        navController.navigate(path) {
            launchSingleTop = true
            restoreState = true
            if (isAlreadyInStack) {
                popUpTo(path) { inclusive = false }
            }
            if (navigationState.command.clearBackStack) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Error while navigating to ${navigationState.command.path}")
    }
}

/**
 * Checks if a route is already in the back stack using public NavController API.
 * Uses [NavController.getBackStackEntry] with destination IDs from the graph.
 */
private fun isRouteInBackStack(navController: NavController, path: String): Boolean {
    val pathBase = path.substringBefore("/")
    for (node in navController.graph) {
        val nodeRouteBase = node.route?.substringBefore("/") ?: continue
        if (nodeRouteBase != pathBase) continue
        return try {
            navController.getBackStackEntry(node.id)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }
    return false
}