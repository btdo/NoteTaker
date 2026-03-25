package com.noteaker.sample.ai

import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.GenerativeBackend
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
class CloudIntentProvider @Inject constructor(
    private val geminiModelProvider: GeminiModelProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun processUserIntent(userMessage: String): Result<String?> = withContext(dispatcher) {
        runCatching {
            val model = geminiModelProvider.model
            val chat = model.startChat()
            val response = chat.sendMessage(userMessage)
            val functionCalls = response.functionCalls
            var result: String? = null
            functionCalls.forEach { functionCall ->
                geminiModelProvider.toolList[functionCall.name]?.let { functionDeclaration ->
                    result = extractRouteFromFunctionCall(functionCall, functionDeclaration)
                }

                if (geminiModelProvider.toolList[functionCall.name] == null) {
                    Timber.d("CloudNavigationIntentProvider: no navigate function call in response")
                }
            }
            result
        }.onFailure {
            Timber.e(it, "CloudNavigationIntentProvider: processUserIntent failed")
        }
    }

    private fun extractRouteFromFunctionCall(functionCall: Any, functionDeclaration: AiToolDeclaration): String? {
        return try {
            val getArgs = functionCall.javaClass.getMethod("getArgs")
            val args = getArgs.invoke(functionCall) as? Map<*, *> ?: return null
            functionDeclaration.processRequest(args)
            "Success"
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
class GeminiModelProvider @Inject constructor(val toolList: Map<String, @JvmSuppressWildcards AiToolDeclaration>) {
    val model by lazy {
        FirebaseAI.getInstance(FirebaseApp.getInstance(), GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.5-flash",
                tools = listOf(Tool.functionDeclarations(toolList.values.toList().map { it.tool })),
                systemInstruction = Content.Builder()
                    .text(LLM_ORCHESTRATOR_SYSTEM_INSTRUCTION)
                    .build()
            )
    }

    companion object {
        /** High-level role; route specifics live in the navigate tool description. */
        private val LLM_ORCHESTRATOR_SYSTEM_INSTRUCTION = """
You are a navigation assistant for a note-taking app. When the user expresses an intent, you will use a list of tools provided to address the user's intent. Respond only with the function call; do not reply with plain text for navigation.
""".trimIndent()
    }
}
