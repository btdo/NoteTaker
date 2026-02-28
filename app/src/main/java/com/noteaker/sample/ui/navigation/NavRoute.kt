package com.noteaker.sample.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.noteaker.sample.MainViewModel
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.feature.add.AddScreen
import com.noteaker.sample.ui.feature.add.AddViewModel
import com.noteaker.sample.ui.feature.list.ListScreen
import com.noteaker.sample.ui.feature.list.ListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


val AppRoutes = listOf<NavRoute<*>>(ListRoute, AddRoute)

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
        val notes by viewModel.notes.collectAsState(listOf())
        ListScreen(notes) {
            viewModel.addClick()
        }
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
        AddScreen(onMicrophoneClick = {}, onCameraClick = {}) { note ->
            viewModel.add(note)
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

open class ScaffoldView(
    val defaultIsShowTopBar: Boolean = true
) : RouteView {
    private val _isShowTopBar = MutableStateFlow(defaultIsShowTopBar)
    override val isShowTopBar: StateFlow<Boolean> = _isShowTopBar
    override val topBarItems: List<TopBarItem> = listOf<TopBarItem>(TopBarItem.Question)
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
}

sealed class TopBarItem(
    open val shouldDisplay: MutableStateFlow<Boolean> = MutableStateFlow(true),
    open val icon: ImageVector,
    @StringRes open val text: Int? = null,
    open val contentDescription: String,
    open val route: NavRoute<*>
) {
    object Question : TopBarItem(
        icon = Icons.Default.QuestionMark,
        text = null,
        contentDescription = "Help",
        route = ListRoute
    )
}