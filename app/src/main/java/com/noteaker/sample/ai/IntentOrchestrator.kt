package com.noteaker.sample.ai

import com.noteaker.sample.di.IoDispatcher
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
class IntentOrchestrator @Inject constructor(
    private val navigationIntentProvider: NavigationIntentProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    /**
     * Sends the user intent to the configured provider; if a route is returned, navigates.
     * Caller can use [Result.onFailure] to fall back to direct navigation.
     */
    suspend fun processUserIntent(userMessage: String): Result<Unit> = withContext(dispatcher) {
        navigationIntentProvider.processUserIntent(userMessage)
            .mapCatching { _ ->
            }
            .onFailure {
                Timber.e(it, "NavigationOrchestrator: processUserIntent failed")
            }
    }
}
