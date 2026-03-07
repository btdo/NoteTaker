package com.noteaker.sample.di

import android.content.Context
import androidx.room.Room
import com.noteaker.sample.data.dao.AttachmentDao
import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notetaker.db"
        )
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideAttachmentDao(database: AppDatabase): AttachmentDao = database.attachmentDao()
}
