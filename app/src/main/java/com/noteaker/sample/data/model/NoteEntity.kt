package com.noteaker.sample.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.domain.model.NoteStatus

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val status: String,
    val note: String,
    val lastUpdated: Long
) {
    fun toNote(attachments: List<Attachment>): Note = Note(
        id = id,
        title = title,
        note = note,
        status = NoteStatus.valueOf(status),
        lastUpdated = lastUpdated,
        attachments = attachments
    )
}