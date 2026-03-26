package com.noteaker.sample.ai

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.noteaker.sample.di.IoDispatcher
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Local (on-device) implementation of [NavigationIntentProvider] using ML Kit GenAI Prompt API
 * (Gemini Nano). No network; output is parsed from plain text (NAVIGATE:Route).
 * If the model is DOWNLOADABLE, this provider triggers the download and waits for completion
 * before running inference.
 */
@ActivityRetainedScoped
class LocalNavigationIntentProvider @Inject constructor(
    private val navigationManager: NavigationManager,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : NavigationIntentProvider {

    private val model by lazy { Generation.getClient() }

    override suspend fun processUserIntent(userMessage: String): Result<String?> =
        withContext(dispatcher) {
            runCatching {
                val route = when (model.checkStatus()) {
                    FeatureStatus.AVAILABLE -> runLocalInference(userMessage)
                    FeatureStatus.DOWNLOADABLE -> {
                        Timber.d("LocalNavigationIntentProvider: downloading model...")
                        downloadModelAndRun(userMessage)
                    }

                    FeatureStatus.DOWNLOADING -> {
                        Timber.d("LocalNavigationIntentProvider: model already downloading, waiting...")
                        waitForAvailableThenRun(userMessage)
                    }

                    FeatureStatus.UNAVAILABLE -> {
                        Timber.w("LocalNavigationIntentProvider: Gemini Nano unavailable on device")
                        null
                    }

                    else -> null
                }

                route?.let {
                    navigationManager.navigate(
                        NavState.NavigateToRoute(NavigationCommand(path = route))
                    )
                    return@runCatching route
                }
                throw IllegalStateException("No navigate function call found in response")
            }.onFailure {
                Timber.e(it, "LocalNavigationIntentProvider: processUserIntent failed")
            }
        }

    /**
     * Starts the model download, waits for completion (or failure), then runs inference if successful.
     */
    private suspend fun downloadModelAndRun(userMessage: String): String? {
        var downloadSucceeded = false
        var downloadError: Throwable? = null
        model.download().collect { status ->
            when (status) {
                is DownloadStatus.DownloadStarted ->
                    Timber.d("LocalNavigationIntentProvider: download started")

                is DownloadStatus.DownloadProgress ->
                    Timber.d("LocalNavigationIntentProvider: download progress ${status.totalBytesDownloaded} bytes")

                is DownloadStatus.DownloadCompleted -> {
                    Timber.d("LocalNavigationIntentProvider: download complete")
                    downloadSucceeded = true
                }

                is DownloadStatus.DownloadFailed -> {
                    Timber.e(status.e, "LocalNavigationIntentProvider: download failed")
                    downloadError = status.e
                }
            }
        }
        return when {
            downloadError != null -> throw (downloadError ?: Exception("Download failed"))
            downloadSucceeded -> runLocalInference(userMessage)
            else -> null
        }
    }

    /**
     * If another process started the download, poll until AVAILABLE or timeout, then run inference.
     */
    private suspend fun waitForAvailableThenRun(userMessage: String): String? {
        val maxWaitMs = 10 * 60 * 1000L // 10 minutes
        val pollIntervalMs = 2000L
        var waited = 0L
        while (waited < maxWaitMs) {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> return runLocalInference(userMessage)
                FeatureStatus.DOWNLOADABLE -> return downloadModelAndRun(userMessage)
                FeatureStatus.UNAVAILABLE -> return null
                else -> { /* DOWNLOADING, keep waiting */
                }
            }
            delay(pollIntervalMs)
            waited += pollIntervalMs
        }
        Timber.w("LocalNavigationIntentProvider: timed out waiting for model")
        return null
    }

    private suspend fun runLocalInference(userMessage: String): String? {
        val prompt = buildPrompt(userMessage)
        val response = model.generateContent(prompt)
        val text = response.candidates.firstOrNull()?.text?.trim() ?: return null
        return parseRouteFromResponse(text)
    }

    private fun buildPrompt(userMessage: String): String {
        return """
$LOCAL_PROMPT_INSTRUCTION

User: $userMessage
Reply (only this line): NAVIGATE:<route>
""".trimIndent()
    }

    private fun parseRouteFromResponse(responseText: String): String? {
        val match = NAVIGATE_PATTERN.find(responseText) ?: return null
        return match.groupValues.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
    }

    companion object {
        private val NAVIGATE_PATTERN = Regex("""NAVIGATE\s*:\s*(\S+)""", RegexOption.IGNORE_CASE)

        private val LOCAL_PROMPT_INSTRUCTION = """
You are a navigation assistant for a note-taking app. Reply with exactly one line: NAVIGATE:<route>.

Valid routes: ListRoute, AddRoute, EditRoute/<id> (e.g. EditRoute/42).
- Add / new note -> NAVIGATE:AddRoute
- List / home -> NAVIGATE:ListRoute
- Edit note with id N -> NAVIGATE:EditRoute/N
""".trimIndent()
    }
}
