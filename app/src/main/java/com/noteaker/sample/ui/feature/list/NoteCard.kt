package com.noteaker.sample.ui.feature.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noteaker.sample.ui.model.AttachmentUI
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.theme.NoteTakerTheme
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun NoteCard(
    note: NoteUI,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSelectionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(note.lastUpdated),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (note.attachments.isNotEmpty()) {
                    Text(
                        text = "${note.attachments.size} attachment${if (note.attachments.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(timestamp)
}

@Preview(name = "Note Card - Light", showBackground = true)
@Composable
fun NoteCardPreview() {
    NoteTakerTheme(darkTheme = false) {
        NoteCard(
            note = NoteUI(
                id = 1,
                title = "Meeting Notes",
                note = "Discussed project timeline and deliverables. Need to follow up with the team about the new requirements.",
                lastUpdated = System.currentTimeMillis() - 3600000,
                attachments = emptyList()
            ),
            isSelected = false,
            onClick = {},
            onSelectionChange = {}
        )
    }
}

@Preview(name = "Note Card with Attachments - Light", showBackground = true)
@Composable
fun NoteCardWithAttachmentsPreview() {
    NoteTakerTheme(darkTheme = false) {
        NoteCard(
            note = NoteUI(
                id = 2,
                title = "Project Documentation",
                note = "Important files and resources for the upcoming presentation. Review all materials before the meeting.",
                lastUpdated = System.currentTimeMillis() - 7200000,
                attachments = listOf(
                    AttachmentUI(uri = "file://document.pdf", displayName = "document.pdf"),
                    AttachmentUI(uri = "file://image.png", displayName = "image.png")
                )
            ),
            isSelected = true,
            onClick = {},
            onSelectionChange = {}
        )
    }
}

@Preview(name = "Note Card - Dark", showBackground = true)
@Composable
fun NoteCardDarkPreview() {
    NoteTakerTheme(darkTheme = true) {
        NoteCard(
            note = NoteUI(
                id = 3,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits, Vegetables, Chicken",
                lastUpdated = System.currentTimeMillis() - 86400000,
                attachments = emptyList()
            ),
            isSelected = false,
            onClick = {},
            onSelectionChange = {}
        )
    }
}

@Preview(name = "Note Card Long Title - Light", showBackground = true)
@Composable
fun NoteCardLongTitlePreview() {
    NoteTakerTheme(darkTheme = false) {
        NoteCard(
            note = NoteUI(
                id = 4,
                title = "This is a very long title that should be truncated with ellipsis when it exceeds the maximum width",
                note = "This is a note with a very long title to test the text overflow behavior. The title should be truncated properly.",
                lastUpdated = System.currentTimeMillis() - 172800000,
                attachments = listOf(
                    AttachmentUI(uri = "file://test.pdf", displayName = "test.pdf")
                )
            ),
            isSelected = false,
            onClick = {},
            onSelectionChange = {}
        )
    }
}