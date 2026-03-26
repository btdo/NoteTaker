package com.noteaker.sample.ai

import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import com.noteaker.sample.di.IoDispatcher
import com.noteaker.sample.navigation.NavigationManager
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Cloud implementation of [NavigationIntentProvider] using Firebase AI (Gemini) with function calling.
 */
@ActivityRetainedScoped
class CloudNavigationNavigationIntentProvider @Inject constructor(
    private val navigationManager: NavigationManager,
    private val geminiModelProvider: NavigationGeminiModelProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : NavigationIntentProvider {

    override suspend fun processUserIntent(userMessage: String): Result<String?> =
        withContext(dispatcher) {
            runCatching {
                val model = geminiModelProvider.model
                val chat = model.startChat()
                val response = chat.sendMessage(userMessage)
                val functionCalls = response.functionCalls
                val navigateCall = functionCalls.find { it.name == NavigationManager.TOOL_NAVIGATE }
                if (navigateCall != null) {
                    val getArgs = navigateCall.javaClass.getMethod("getArgs")
                    val args = getArgs.invoke(navigateCall) as? Map<*, *>
                        ?: throw IllegalStateException("No args returned")
                    navigationManager.processRequest(args)
                    return@runCatching "Success"
                }
                throw IllegalStateException("No navigate function call found in response")
            }.onFailure {
                Timber.e(it, "CloudNavigationIntentProvider: processUserIntent failed")
            }
        }
}

/**
 * Provides a [GenerativeModel] configured with the navigate tool and system instruction.
 * Kept separate so the model (and its tool list) can be created once and reused.
 */
@ActivityRetainedScoped
class NavigationGeminiModelProvider @Inject constructor(val navigationManager: NavigationManager) {
    val model by lazy {
        FirebaseAI.getInstance(FirebaseApp.getInstance(), GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.5-flash",
                tools = listOf(Tool.functionDeclarations(listOf(navigationManager.tool))),
                systemInstruction = com.google.firebase.ai.type.Content.Builder()
                    .text(NAVIGATION_SYSTEM_INSTRUCTION)
                    .build()
            )
    }

    companion object {
        /** High-level role; route specifics live in the navigate tool description. */
        private val NAVIGATION_SYSTEM_INSTRUCTION = """
You are a navigation assistant for a note-taking app. When the user expresses a navigation intent, call the navigate tool with the appropriate route. Respond only with the function call; do not reply with plain text for navigation.
""".trimIndent()

    }
}
