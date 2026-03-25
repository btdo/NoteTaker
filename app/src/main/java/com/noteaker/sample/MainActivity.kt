package com.noteaker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.noteaker.sample.navigation.NavigationManager
import com.noteaker.sample.ui.MainContent
import com.noteaker.sample.ui.theme.NoteTakerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navManager: NavigationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalNavigationManager provides navManager) {
                NoteTakerTheme {
                    BackHandler {
                        navManager.popBackStack()
                    }

                    MainContent(navigationManager = navManager)
                }
            }
        }
    }
}

val LocalNavigationManager =
    compositionLocalOf<NavigationManager> { error("NavigationManager not set") }
