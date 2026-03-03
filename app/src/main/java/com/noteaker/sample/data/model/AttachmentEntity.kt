package com.noteaker.sample.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.noteaker.sample.domain.model.Attachment

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val uri: String,
    val displayName: String? = null,
    val mimeType: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toAttachment(): Attachment = Attachment(
        id = id,
        uri = uri,
        displayName = displayName,
        mimeType = mimeType
    )
}
