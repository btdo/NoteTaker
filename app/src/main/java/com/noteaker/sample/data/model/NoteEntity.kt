package com.noteaker.sample.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import java.util.Date

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String,
    val lastUpdated: Long
) {
    fun toNote(attachments: List<Attachment>): Note = Note(
        id = id.toInt(),
        title = title,
        note = note,
        lastUpdated = Date(lastUpdated),
        attachments = attachments
    )
}