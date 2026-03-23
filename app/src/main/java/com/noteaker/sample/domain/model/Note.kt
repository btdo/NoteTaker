package com.noteaker.sample.domain.model

import androidx.compose.runtime.Immutable
import com.noteaker.sample.data.model.NoteEntity

@Immutable
data class Note(
    val id: Long = 0,
    val title: String,
    val note: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val attachments: List<Attachment> = emptyList()
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = if (id.toInt() == 0) 0L else id,
    title = title,
    note = note,
    lastUpdated = lastUpdated
)

