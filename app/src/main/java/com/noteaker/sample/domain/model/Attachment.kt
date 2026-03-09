package com.noteaker.sample.domain.model

import androidx.compose.runtime.Immutable
import com.noteaker.sample.data.model.AttachmentEntity

@Immutable
data class Attachment(
    val id: Long = 0,
    val uri: String,
    val displayName: String? = null,
    val mimeType: String? = null,
)

fun Attachment.toEntity(noteId: Long): AttachmentEntity = AttachmentEntity(
    noteId = noteId,
    uri = uri,
    displayName = displayName,
    mimeType = mimeType
)
