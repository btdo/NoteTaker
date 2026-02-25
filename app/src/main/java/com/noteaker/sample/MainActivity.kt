package com.noteaker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
            CompositionLocalProvider(NavigationManager provides navManager) {
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

val NavigationManager =
    compositionLocalOf<NavigationManager> { error("NavigationManager not set") }
