package com.noteaker.sample.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.noteaker.sample.data.dao.AttachmentDao
import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.model.AttachmentEntity
import com.noteaker.sample.data.model.NoteEntity

@Database(
    entities = [NoteEntity::class, AttachmentEntity::class],
    version = 6,  // Incremented: Added UNIQUE constraint on serverId
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao
}