package com.noteaker.sample.navigation

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.noteaker.sample.ai.AiToolDeclaration
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.collections.get

data class SnackBarAction(val label: String, val action: suspend () -> Unit)
data class SnackBar (val message: String, val snackBarAction: SnackBarAction? = null  )


@ActivityRetainedScoped
class NavigationManager @Inject constructor() : AiToolDeclaration {
    companion object {
        const val TOOL_NAVIGATE = "navigate"
    }

    private val _navigationState = MutableStateFlow<NavState>(NavState.Idle)
    val navigationState: StateFlow<NavState> = _navigationState.asStateFlow()

    private val _snackBar = MutableSharedFlow<SnackBar>()
    val snackBar: SharedFlow<SnackBar> = _snackBar.asSharedFlow()

    override val tool: FunctionDeclaration = FunctionDeclaration(
        name = TOOL_NAVIGATE,
        description = """
Navigate the app to a screen. Routes:
- "ListRoute" — list of notes (home).
- "AddRoute" — add a new note.
- "EditRoute/<id>" — edit the note with the given integer id (e.g. EditRoute/42).
Rules: Add / new note -> "AddRoute". List / home -> "ListRoute". Edit note with id N -> "EditRoute/N". Use the navigate tool with the correct route; do not reply with plain text.
            """.trimIndent(),
        parameters = mapOf(
            "route" to Schema.string(
                description = "Route: ListRoute, AddRoute, or EditRoute/<id> (e.g. EditRoute/5)."
            )
        )
    )

    override fun processRequest(input: Map<*, *>?) {
        val routeArg = input?.get("route") ?: throw Exception("No route returned")
        val route = try {
            val getContent = routeArg.javaClass.getMethod("getContent")
            getContent.invoke(routeArg) as? String
        } catch (_: NoSuchMethodException) {
            val getAsString = routeArg.javaClass.getMethod("getAsString")
            getAsString.invoke(routeArg) as? String
        }
        navigate(
            NavState.NavigateToRoute(
                NavigationCommand(
                    route ?: throw Exception("No route returned")
                )
            )
        )
    }

    suspend fun showSnackBar(message: String) {
        _snackBar.emit(SnackBar(message))
    }

    suspend fun showSnackBar(snackBar: SnackBar) {
        _snackBar.emit(snackBar)
    }

    fun onNavigated(state: NavState) {
        _navigationState.compareAndSet(state, NavState.Idle)
    }

    fun popBackStack() {
        navigate(NavState.PopBackStack)
    }

    fun navigate(state: NavState) {
        _navigationState.value = state
    }
}

/**
 * Navigate to route.  When isRoot is true, clear the back stack
 */
data class NavigationCommand(val path: String, val clearBackStack: Boolean = false)

sealed class NavState {
    object Idle : NavState()

    data class NavigateToRoute(val command: NavigationCommand) : NavState()

    /**
     * @param destination is the static destination to pop to, without parameter replacements.
     */
    data class PopToRoute(val destination: String, val isInclusive: Boolean = false) : NavState()

    object PopBackStack : NavState()
}