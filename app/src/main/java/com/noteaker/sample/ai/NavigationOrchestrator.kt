package com.noteaker.sample.ai

import com.noteaker.sample.di.IoDispatcher
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Coordinates navigation from user intents by delegating to a [NavigationIntentProvider] (cloud or local).
 * When the provider returns a route, this orchestrator calls [NavigationManager] to navigate.
 */
@ActivityRetainedScoped
class NavigationOrchestrator @Inject constructor(
    private val navigationManager: NavigationManager,
    private val intentProvider: NavigationIntentProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    /**
     * Sends the user intent to the configured provider; if a route is returned, navigates.
     * Caller can use [Result.onFailure] to fall back to direct navigation.
     */
    suspend fun processUserIntent(userMessage: String): Result<Unit> = withContext(dispatcher) {
        intentProvider.processUserIntent(userMessage)
            .mapCatching { route ->
                if (route.isNullOrBlank()) throw Exception("No route returned")
                navigationManager.navigate(
                    NavState.NavigateToRoute(NavigationCommand(path = route))
                )
            }
            .onFailure {
                Timber.e(it, "NavigationOrchestrator: processUserIntent failed")
            }
    }
}
