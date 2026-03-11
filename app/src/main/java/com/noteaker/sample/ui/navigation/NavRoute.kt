package com.noteaker.sample.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.noteaker.sample.MainViewModel
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.common.ShimmerOverlay
import com.noteaker.sample.ui.common.collectAsStateLifeCycle
import com.noteaker.sample.ui.feature.DetailsHeader
import com.noteaker.sample.ui.feature.NoteDetailsScreen
import com.noteaker.sample.ui.feature.add.AddViewModel
import com.noteaker.sample.ui.feature.edit.EditViewModel
import com.noteaker.sample.ui.feature.list.ListScreen
import com.noteaker.sample.ui.feature.list.ListViewModel
import com.noteaker.sample.ui.model.UIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


val AppRoutes = listOf(ListRoute, AddRoute, EditRoute)

@OptIn(ExperimentalCoroutinesApi::class)
interface NavRoute<T : ViewModel> {
    val path: String
        get() = this.javaClass.simpleName
    val view: RouteView

    /**
     * Override when this page uses arguments.
     *
     * We do it here and not in the [NavigationComponent to keep it centralized]
     */
    fun getArguments(): List<NamedNavArgument> = listOf()

    /**
     * Returns the screen's ViewModel. Needs to be overridden so that Hilt can generate code for the factory for the ViewModel class.
     */
    @Composable
    fun viewModel(): T

    /**
     * Returns the screen's content.
     */
    @Composable
    fun Content(backStackEntry: NavBackStackEntry, viewModel: T)

    fun composable(
        builder: NavGraphBuilder,
        navController: NavHostController,
        navManager: NavigationManager,
        viewModel: MainViewModel
    ) {
        builder.composable(
            path,
            getArguments(),
            enterTransition = { null },
            exitTransition = { null },
            popEnterTransition = { null },
            popExitTransition = { null }
        ) { backStackEntry ->
            Content(backStackEntry, viewModel())
        }
    }
}

object ListRoute : NavRoute<ListViewModel> {
    override val view: RouteView
        get() = NoteTakerView.ListView

    @Composable
    override fun viewModel(): ListViewModel = hiltViewModel()

    @Composable
    override fun Content(
        backStackEntry: NavBackStackEntry,
        viewModel: ListViewModel
    ) {
        val notes by viewModel.searchResults.collectAsStateWithLifecycle(listOf())
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        ListScreen(
            notes = notes,
            onAddClick = viewModel::addClick,
            onEditClick = viewModel::onEditClick,
            searchQuery = searchQuery,
            onQueryChange = viewModel::onSearchQuery
        )
    }
}

object AddRoute : NavRoute<AddViewModel> {
    override val view: RouteView
        get() = NoteTakerView.AddView

    @Composable
    override fun viewModel(): AddViewModel = hiltViewModel()

    @Composable
    override fun Content(
        backStackEntry: NavBackStackEntry,
        viewModel: AddViewModel
    ) {
        val uiState by viewModel.uiState.collectAsStateLifeCycle()

        Box(modifier = Modifier.fillMaxSize()) {
            NoteDetailsScreen(
                header = { DetailsHeader("Add Note") },
                additionalNote = {
                    if (uiState is UIState.Error) {
                        Text(
                            text = (uiState as UIState.Error).message,
                            color = Color.Red
                        )
                    }
                },
                note = null,
                onMicrophoneClick = {},
                onCameraClick = {},
                onCancelClick = viewModel::onCancel,
                onSaveClick = viewModel::onSave
            )
            if (uiState == UIState.Loading) {
                ShimmerOverlay()
            }
        }
    }
}

object EditRoute : NavRoute<EditViewModel> {
    const val NOTE_ID_KEY = "noteId"
    override val path: String
        get() = "${super.path}/{$NOTE_ID_KEY}"
    override val view: RouteView
        get() = NoteTakerView.EditView

    override fun getArguments(): List<NamedNavArgument> = listOf(navArgument("$NOTE_ID_KEY") {
        type = NavType.IntType
    })

    fun getRoute(noteId: Int): NavigationCommand {
        return NavigationCommand(
            path.replace("{$NOTE_ID_KEY}", "$noteId")
        )
    }

    @Composable
    override fun viewModel(): EditViewModel = hiltViewModel()

    @Composable
    override fun Content(
        backStackEntry: NavBackStackEntry,
        viewModel: EditViewModel
    ) {
        val noteId = backStackEntry.arguments?.getInt("$NOTE_ID_KEY") ?: 0
        val uiState by viewModel.uiState.collectAsStateLifeCycle()
        val note by viewModel.note.collectAsStateLifeCycle(null)

        LaunchedEffect(noteId) {
            viewModel.getEditNote(noteId)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            NoteDetailsScreen(
                header = { DetailsHeader("Edit Note") },
                additionalNote = {
                    if (uiState is UIState.Error) {
                        Text(
                            text = (uiState as UIState.Error).message, color = Color.Red
                        )
                    }
                },
                note = note,
                onMicrophoneClick = {},
                onCameraClick = {},
                onCancelClick = viewModel::onCancel,
                onSaveClick = viewModel::onSave
            )
            if (uiState == UIState.Loading) {
                ShimmerOverlay()
            }
        }


    }
}

interface RouteView {
    val isShowTopBar: StateFlow<Boolean>
    val topBarItems: List<TopBarItem>
    fun setIsShowTopBar(isShow: Boolean)
    fun reset()

    companion object {
        fun findRouteFromPath(route: String?): NavRoute<*> {
            if (route == null) return ListRoute
            val path = route.substringBefore("/")
            AppRoutes.forEach { route ->
                if (route.path.substringBefore("/") == path) {
                    return route
                }
            }

            throw IllegalArgumentException("Route $path is not recognized.")
        }
    }
}

/** Shared top bar items so the same list reference is passed to TopBar across routes (avoids recomposition). */
val DEFAULT_TOP_BAR_ITEMS: List<TopBarItem> = listOf()

open class ScaffoldView(
    val defaultIsShowTopBar: Boolean = true
) : RouteView {
    private val _isShowTopBar = MutableStateFlow(defaultIsShowTopBar)
    override val isShowTopBar: StateFlow<Boolean> = _isShowTopBar
    override val topBarItems: List<TopBarItem> = DEFAULT_TOP_BAR_ITEMS
    override fun setIsShowTopBar(isShow: Boolean) {
        _isShowTopBar.value = isShow
    }

    override fun reset() {
        _isShowTopBar.value = defaultIsShowTopBar
    }
}

sealed class NoteTakerView {
    object ListView : ScaffoldView()
    object AddView : ScaffoldView()
    object EditView : ScaffoldView()
}

sealed class TopBarItem(
    open val shouldDisplay: MutableStateFlow<Boolean> = MutableStateFlow(true),
    open val icon: ImageVector,
    @StringRes open val text: Int? = null,
    open val contentDescription: String,
    open val route: NavRoute<*>
) {
    object Something : TopBarItem(
        icon = Icons.Filled.Home,
        text = null,
        contentDescription = "Home",
        route = ListRoute
    )
}