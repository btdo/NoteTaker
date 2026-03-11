package com.noteaker.sample

import androidx.lifecycle.ViewModel
import com.noteaker.sample.ui.navigation.TopBarItem
import com.noteaker.sample.data.repository.MyNoteRepository
import com.noteaker.sample.navigation.NavState
import com.noteaker.sample.navigation.NavigationCommand
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.navigation.AddRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val navigationManager: NavigationManager) : ViewModel() {

    fun onTopBarItemClick(item: TopBarItem) {
        navigationManager.navigate(NavState.NavigateToRoute(NavigationCommand(item.route.path)))
    }
}