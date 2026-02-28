package com.noteaker.sample.domain.model

import com.noteaker.sample.data.model.NoteEntity
import java.util.Date

data class Note(
    val id: Int = 0,
    val title: String,
    val note: String,
    val lastUpdated: Date = Date(System.currentTimeMillis()),
    val attachments: List<Attachment> = emptyList()
)


fun Note.toEntity(): NoteEntity = NoteEntity(
    id = if (id == 0) 0L else id.toLong(),
    title = title,
    note = note,
    lastUpdated = lastUpdated.time
)