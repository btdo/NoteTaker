package com.noteaker.sample.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.NoteStatus
import com.noteaker.sample.domain.model.SyncStatus

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["serverId"], unique = true)  // Make serverId UNIQUE for conflict detection
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val status: String,
    val note: String,
    val lastUpdated: Long,
    val imageUri: String? = null,
    // Sync fields
    val version: Long = 1,                // Optimistic locking version
    val syncStatus: String = SyncStatus.SYNCED.name,    // SYNCED, PENDING, CONFLICT
    val serverId: Long? = null
) {
    fun toNote(attachments: List<Attachment>): Note = Note(
        id = id,
        title = title,
        note = note,
        status = NoteStatus.valueOf(status),
        lastUpdated = lastUpdated,
        imageUri = imageUri,
        attachments = attachments,
        version = version,
        syncStatus = SyncStatus.valueOf(syncStatus) ,
        serverId = serverId
    )
}