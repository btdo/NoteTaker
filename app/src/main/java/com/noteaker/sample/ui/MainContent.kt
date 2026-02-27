package com.noteaker.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.noteaker.sample.MainViewModel
import com.noteaker.sample.ui.navigation.AppRoutes
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.navigation.ListRoute
import com.noteaker.sample.ui.navigation.RouteView
import com.noteaker.sample.ui.navigation.MainNavigation
import com.noteaker.sample.ui.common.TopBar
import com.noteaker.sample.ui.common.collectAsStateLifeCycle

@Composable
fun MainContent(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    navigationManager: NavigationManager
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val route = RouteView.findRouteFromPath(currentRoute)
    val shouldShowTopBar by route.view.isShowTopBar.collectAsStateLifeCycle()
    Scaffold(modifier = Modifier.fillMaxSize(), floatingActionButton = {
        IconButton(onClick = {
            viewModel.addClick()
        }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }
    }, topBar = {
        if (shouldShowTopBar) {
            TopBar(
                topBarItems = route.view.topBarItems,
                isShowBackButton = false,
                onBackClicked = { navigationManager.popBackStack() }) { item ->
                viewModel.onTopBarItemClick(item)
            }
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