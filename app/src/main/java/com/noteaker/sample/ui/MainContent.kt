package com.noteaker.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import com.noteaker.sample.ui.navigation.ListRoute
import com.noteaker.sample.ui.navigation.MainNavigation
import com.noteaker.sample.ui.navigation.RouteView
import timber.log.Timber

@Composable
fun MainContent(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    navigationManager: NavigationManager
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val routePath = navBackStackEntry?.destination?.route
    val route = RouteView.findRouteFromPath(routePath)
    val shouldShowTopBar by route.view.isShowTopBar.collectAsStateLifeCycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Timber.d("MainContent: Starting snackbar collection")
        navigationManager.snackBar.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    } , topBar = {
        if (shouldShowTopBar) {
            TopBar(
                topBarItems = route.view.topBarItems,
                isShowBackButton = false,
                onBackClicked = navigationManager::popBackStack,
                onItemClicked = viewModel::onTopBarItemClick
            )
        }
    }) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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