package com.noteaker.sample.ai

/**
 * Abstraction for resolving a user intent (e.g. "User tapped Add") to a navigation route string.
 * Implementations may use cloud (Firebase/Gemini) or local (e.g. ML Kit Gemini Nano) models.
 *
 * Returns [Result.success] with the route (e.g. "AddRoute", "EditRoute/42") when the model
 * indicates navigation, or [Result.failure] / success(null) when no route or on error.
 */
interface NavigationIntentProvider {

    /**
     * Sends the user intent to the model and returns the route to navigate to, if any.
     * @return [Result.success] with route string, or [Result.failure] / success(null)
     */
    suspend fun processUserIntent(userMessage: String): Result<String?>
}
