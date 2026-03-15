package com.noteaker.sample.ai

import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.noteaker.sample.di.IoDispatcher
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Cloud implementation of [NavigationIntentProvider] using Firebase AI (Gemini) with function calling.
 */
@ActivityRetainedScoped
class CloudNavigationIntentProvider @Inject constructor(
    private val geminiModelProvider: GeminiModelProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : NavigationIntentProvider {

    override suspend fun processUserIntent(userMessage: String): Result<String?> = withContext(dispatcher) {
        runCatching {
            val model = geminiModelProvider.model
            val chat = model.startChat()
            val response = chat.sendMessage(userMessage)

            val functionCalls = response.functionCalls
            val navigateCall = functionCalls.find { it.name == GeminiModelProvider.TOOL_NAVIGATE }

            if (navigateCall != null) {
                extractRouteFromFunctionCall(navigateCall)
            } else {
                Timber.d("CloudNavigationIntentProvider: no navigate function call in response")
                null
            }
        }.onFailure {
            Timber.e(it, "CloudNavigationIntentProvider: processUserIntent failed")
        }
    }

    private fun extractRouteFromFunctionCall(functionCall: Any): String? {
        return try {
            val getArgs = functionCall.javaClass.getMethod("getArgs")
            val args = getArgs.invoke(functionCall) as? Map<*, *> ?: return null
            val routeArg = args["route"] ?: return null
            try {
                val getContent = routeArg.javaClass.getMethod("getContent")
                getContent.invoke(routeArg) as? String
            } catch (_: NoSuchMethodException) {
                val getAsString = routeArg.javaClass.getMethod("getAsString")
                getAsString.invoke(routeArg) as? String
            }
        } catch (e: Exception) {
            Timber.e(e, "CloudNavigationIntentProvider: extractRouteFromFunctionCall failed")
            null
        }
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
                .text(NAVIGATION_SYSTEM_INSTRUCTION)
                .build()
        )
    }

    companion object {
        const val TOOL_NAVIGATE = "navigate"

        /** RAG-style system instruction for cloud model. */
        private val NAVIGATION_SYSTEM_INSTRUCTION = """
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
""".trimIndent()

        private val navigateTool = FunctionDeclaration(
            name = TOOL_NAVIGATE,
            description = "Navigate the app to a screen. Use ListRoute for the note list, AddRoute to add a new note, or EditRoute/<noteId> to edit a note (e.g. EditRoute/42).",
            parameters = mapOf(
                "route" to Schema.string(
                    description = "The route to navigate to: ListRoute, AddRoute, or EditRoute/<id>."
                )
            )
        )
    }
}
