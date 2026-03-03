package com.noteaker.sample.di

import com.noteaker.sample.data.repository.MyNoteRepository
import com.noteaker.sample.data.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindNoteRepository(impl: MyNoteRepository): NoteRepository
}
