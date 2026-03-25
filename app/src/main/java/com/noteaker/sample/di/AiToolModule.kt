package com.noteaker.sample.di

import com.noteaker.sample.ai.AiToolDeclaration
import com.noteaker.sample.navigation.NavigationManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class AiToolModule {

    @IntoMap
    @StringKey(NavigationManager.TOOL_NAVIGATE)
    @Binds
    abstract fun bindNavigateListener(navigationManager: NavigationManager) : AiToolDeclaration

}
