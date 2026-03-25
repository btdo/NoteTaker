package com.noteaker.sample.ui.model

import androidx.compose.runtime.Stable
import com.noteaker.sample.domain.model.Note

@Stable
data class NoteUI(
    val id: Long = 0,
    val title: String,
    val note: String,
    val lastUpdated: Long,
    val attachments: List<AttachmentUI> = emptyList()
) {
    companion object {
        fun fromNote(note: Note): NoteUI = NoteUI(
            id = note.id,
            title = note.title,
            note = note.note,
            lastUpdated = note.lastUpdated,
            attachments = note.attachments.map { AttachmentUI.fromAttachment(it) }
        )
    }
}

