package com.noteaker.sample.di

import com.noteaker.sample.data.repository.MyNoteRepository
import com.noteaker.sample.data.repository.MyQuoteRepository
import com.noteaker.sample.data.repository.NoteRepository
import com.noteaker.sample.data.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindNoteRepository(impl: MyNoteRepository): NoteRepository

    @Binds
    abstract fun bindQuoteRepository(impl: MyQuoteRepository): QuoteRepository

}
