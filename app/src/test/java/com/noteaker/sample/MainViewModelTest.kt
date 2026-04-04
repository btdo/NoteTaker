package com.noteaker.sample

import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.sync.SyncScheduler
import com.noteaker.sample.ui.navigation.TopBarItem
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

    @MockK(relaxed = true)
    lateinit var navigationManager: NavigationManager

    @MockK
    lateinit var syncScheduler: SyncScheduler

    lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = MainViewModel(navigationManager, syncScheduler)
    }

    @Test
    fun testOnTopBarItemClick() {
        viewModel.onTopBarItemClick(TopBarItem.Home)
        verify { navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(TopBarItem.Home.route.path))) }
    }
}