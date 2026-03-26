package com.noteaker.sample.domain.model

import androidx.compose.runtime.Immutable
import com.noteaker.sample.data.model.NoteEntity

@Immutable
data class Note(
    val id: Long = 0,
    val title: String,
    val note: String,
    val status: NoteStatus = NoteStatus.ACTIVE,
    val lastUpdated: Long = System.currentTimeMillis(),
    val attachments: List<Attachment> = emptyList()
)

enum class NoteStatus {
    ARCHIVED,
    ACTIVE,
}

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = if (id.toInt() == 0) 0L else id,
    title = title,
    note = note,
    status = status.name,
    lastUpdated = lastUpdated
)

