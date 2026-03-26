package com.noteaker.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.noteaker.sample.MainViewModel
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.common.TopBar
import com.noteaker.sample.ui.common.collectAsStateLifeCycle
import com.noteaker.sample.ui.navigation.AppRoutes
import com.noteaker.sample.ui.navigation.DEFAULT_TOP_BAR_ITEMS
import com.noteaker.sample.ui.navigation.ListRoute
import com.noteaker.sample.ui.navigation.MainNavigation
import com.noteaker.sample.ui.navigation.RouteView
import com.noteaker.sample.ui.navigation.TopBarItem
import timber.log.Timber

@Composable
fun MainContent(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    navigationManager: NavigationManager
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        navigationManager.snackBar
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { message ->
                if (message.snackBarAction != null) {
                    val result = snackbarHostState.showSnackbar(
                        message.message,
                        actionLabel = message.snackBarAction.label,
                        duration = SnackbarDuration.Long
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> message.snackBarAction.action()
                        else -> {}
                    }
                } else {
                    snackbarHostState.showSnackbar(message.message)
                }
            }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }, topBar = {
        RouteAwareTopBar(
            navController = navController,
            onBackClicked = navigationManager::popBackStack,
            onItemClicked = viewModel::onTopBarItemClick
        )
    }) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            MainNavigation(navigationManager, navController)
            NavHost(
                navController = navController,
                startDestination = ListRoute.path
            ) {
                AppRoutes.forEach {
                    it.composable(this, navController, navigationManager, viewModel)
                }
            }
        }
    }
}

/**
 * Isolates route/nav state reading so only this composable (and TopBar when needed) recomposes on
 * navigation. MainContent no longer reads [currentBackStackEntryAsState], so it stays stable.
 * Uses [DEFAULT_TOP_BAR_ITEMS] so TopBar receives the same list reference and can skip recomposition.
 */
@Composable
private fun RouteAwareTopBar(
    navController: NavHostController,
    onBackClicked: () -> Unit,
    onItemClicked: (TopBarItem) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = RouteView.findRouteFromPath(navBackStackEntry?.destination?.route)
    val shouldShowTopBar by route.view.isShowTopBar.collectAsStateLifeCycle()
    if (shouldShowTopBar) {
        TopBar(
            topBarItems = route.view.topBarItems,
            isShowBackButton = false,
            onBackClicked = onBackClicked,
            onItemClicked = onItemClicked
        )
    }
}