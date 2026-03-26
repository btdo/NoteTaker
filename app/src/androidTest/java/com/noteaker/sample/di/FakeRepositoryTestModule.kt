package com.noteaker.sample.di

import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import com.noteaker.sample.fakes.FakeNoteRepository
import com.noteaker.sample.fakes.FakeQuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces production [RepositoryModule] in instrumented tests with fakes (no Room / Retrofit).
 *
 * Applied automatically to all `@HiltAndroidTest` classes when using [com.noteaker.sample.HiltTestRunner].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakeRepositoryTestModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: FakeNoteRepository): NoteRepository

    @Binds
    @Singleton
    abstract fun bindQuoteRepository(impl: FakeQuoteRepository): QuoteRepository
}
