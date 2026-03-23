package com.noteaker.sample.ui.model

import com.noteaker.sample.domain.model.Attachment

data class AttachmentUI(
    val id: Long = 0,
    val uri: String,
    val displayName: String? = null,
    val mimeType: String? = null
) {
    companion object {
        fun fromAttachment(attachment: Attachment): AttachmentUI {
            return AttachmentUI(
                id = attachment.id,
                uri = attachment.uri,
                displayName = attachment.displayName,
                mimeType = attachment.mimeType
            )
        }
    }
}



