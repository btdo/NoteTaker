package com.noteaker.sample.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.model.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}