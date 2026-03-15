package com.noteaker.sample.ai

import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import timber.log.Timber
import javax.inject.Inject

/**
 * Uses Firebase AI (Gemini) with function calling to decide navigation from user intents.
 * When the user performs an action (e.g. taps Add), the app sends that intent to the model;
 * the model calls the registered [navigate] tool, and we execute it via [NavigationManager].
 *
 * RAG-style context is provided via [NAVIGATION_SYSTEM_INSTRUCTION] so the model knows
 * which route string to use for each intent.
 */
@ActivityRetainedScoped
class NavigationOrchestrator @Inject constructor(
    private val navigationManager: NavigationManager,
    private val modelProvider: GeminiModelProvider
) {

    /**
     * Sends the user intent to the model. The model should respond with a navigate(route) call;
     * we execute that via [NavigationManager].
     */
    suspend fun processUserIntent(userMessage: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val model = modelProvider.model
            val chat = model.startChat()
            val response = chat.sendMessage(userMessage)

            val functionCalls = response.functionCalls
            val navigateCall = functionCalls.find { it.name == TOOL_NAVIGATE }

            if (navigateCall != null) {
                val route = extractRouteFromFunctionCall(navigateCall)
                if (!route.isNullOrBlank()) {
                    navigationManager.navigate(
                        NavState.NavigateToRoute(NavigationCommand(path = route))
                    )
                } else {
                    Timber.w("NavigationOrchestrator: navigate call had no valid route")
                }
            } else {
                Timber.d("NavigationOrchestrator: no navigate function call in response")
            }
        }.onFailure {
            Timber.e(it, "NavigationOrchestrator: processUserIntent failed")
        }
    }

    /**
     * Extracts the "route" string from the function call. Firebase AI may use either
     * kotlinx.serialization (JsonLiteral.content) or Gson (getAsString()); we try both via reflection.
     */
    private fun extractRouteFromFunctionCall(functionCall: Any): String? {
        return try {
            val getArgs = functionCall.javaClass.getMethod("getArgs")
            val args = getArgs.invoke(functionCall) as? Map<*, *> ?: return null
            val routeArg = args["route"] ?: return null
            // kotlinx.serialization JsonLiteral: use getContent() (Kotlin property "content")
            try {
                val getContent = routeArg.javaClass.getMethod("getContent")
                getContent.invoke(routeArg) as? String
            } catch (_: NoSuchMethodException) {
                // Gson JsonPrimitive: use getAsString()
                val getAsString = routeArg.javaClass.getMethod("getAsString")
                getAsString.invoke(routeArg) as? String
            }
        } catch (e: Exception) {
            Timber.e(e, "NavigationOrchestrator: extractRouteFromFunctionCall failed")
            null
        }
    }

    companion object {
        const val TOOL_NAVIGATE = "navigate"

        /** RAG-style system instruction: maps intents to route strings the model should use. */
        const val NAVIGATION_SYSTEM_INSTRUCTION = """
You are a navigation assistant for a note-taking app. When the user wants to do something, you must call the navigate function with the correct route.

Routes:
- "ListRoute" — go to the list of notes (home).
- "AddRoute" — go to the screen to add a new note.
- "EditRoute/<id>" — go to edit the note with the given integer id (e.g. EditRoute/42).

Rules:
- If the user taps Add or wants to add a note, call navigate with route "AddRoute".
- If the user taps the list/home or wants to see notes, call navigate with route "ListRoute".
- If the user taps a note or wants to edit a note, call navigate with route "EditRoute/<id>" using the note id from context (e.g. "User tapped note with id 5" -> "EditRoute/5").
- Always respond with a function call; do not reply with plain text for navigation intents.
"""
    }
}

/**
 * Provides a [GenerativeModel] configured with the navigate tool and system instruction.
 * Kept separate so the model (and its tool list) can be created once and reused.
 */
@ActivityRetainedScoped
class GeminiModelProvider @Inject constructor() {

    val model by lazy {
        FirebaseAI.getInstance(FirebaseApp.getInstance(), GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            tools = listOf(Tool.functionDeclarations(listOf(navigateTool))),
            systemInstruction = com.google.firebase.ai.type.Content.Builder()
                .text(NavigationOrchestrator.NAVIGATION_SYSTEM_INSTRUCTION)
                .build()
        )
    }

    companion object {
        private val navigateTool = FunctionDeclaration(
            name = NavigationOrchestrator.TOOL_NAVIGATE,
            description = "Navigate the app to a screen. Use ListRoute for the note list, AddRoute to add a new note, or EditRoute/<noteId> to edit a note (e.g. EditRoute/42).",
            parameters = mapOf(
                "route" to Schema.string(
                    description = "The route to navigate to: ListRoute, AddRoute, or EditRoute/<id>."
                )
            )
        )
    }
}
