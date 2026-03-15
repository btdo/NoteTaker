package com.noteaker.sample.di

import com.noteaker.sample.ai.AiModelConfigSource
import com.noteaker.sample.ai.AiModelType
import com.noteaker.sample.ai.DefaultAiModelConfigSource
import com.noteaker.sample.ai.CloudNavigationIntentProvider
import com.noteaker.sample.ai.LocalNavigationIntentProvider
import com.noteaker.sample.ai.NavigationIntentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object AiModule {

    @Provides
    fun provideAiModelConfigSource(impl: DefaultAiModelConfigSource): AiModelConfigSource = impl

    @Provides
    fun provideNavigationIntentProvider(
        cloud: CloudNavigationIntentProvider,
        local: LocalNavigationIntentProvider,
        config: AiModelConfigSource
    ): NavigationIntentProvider = when (config.current()) {
        AiModelType.CLOUD -> cloud
        AiModelType.LOCAL -> local
    }
}
