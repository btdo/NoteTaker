package com.noteaker.sample.ui.feature.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.ui.theme.NoteTakerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListScreen(notes: List<Note>, onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (notes.isEmpty()) {
            EmptyNotesView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = notes, key = { it.id }) { note ->
                    NoteCard(note = note, onClick = { /* TODO: Navigate to edit */ })
                }
            }
        }

        // Save Button
        FilledIconButton(
            onClick = {
                onAddClick()
            },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(56.dp)
                .offset(x = (-16).dp, y = (-16).dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Save Note",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(name = "List with Notes - Light", showBackground = true)
@Composable
fun ListScreenPreview() {
    NoteTakerTheme(darkTheme = false) {
        val notes = listOf(
            Note(
                id = 1,
                title = "Meeting Notes",
                note = "Discussed project timeline and deliverables. Need to follow up with the team about the new requirements.",
                lastUpdated = Date(System.currentTimeMillis() - 3600000),
                attachments = listOf(
                    Attachment(uri = "file://test.pdf", displayName = "document.pdf")
                )
            ),
            Note(
                id = 2,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits",
                lastUpdated = Date(System.currentTimeMillis() - 7200000)
            ),
            Note(
                id = 3,
                title = "Ideas for App",
                note = "Add dark mode support, implement search functionality, create backup feature",
                lastUpdated = Date(System.currentTimeMillis() - 86400000),
                attachments = listOf(
                    Attachment(uri = "file://sketch1.png"),
                    Attachment(uri = "file://sketch2.png")
                )
            )
        )

        ListScreen(notes) { }
    }
}

@Preview(name = "List with Notes - Dark", showBackground = true)
@Composable
fun ListScreenPreviewDark() {
    NoteTakerTheme(darkTheme = true) {
        val notes = listOf(
            Note(
                id = 1,
                title = "Meeting Notes",
                note = "Discussed project timeline and deliverables. Need to follow up with the team about the new requirements.",
                lastUpdated = Date(System.currentTimeMillis() - 3600000)
            ),
            Note(
                id = 2,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits",
                lastUpdated = Date(System.currentTimeMillis() - 7200000)
            )
        )

        ListScreen(notes) { }
    }
}

@Preview(name = "Empty List - Light", showBackground = true)
@Composable
fun EmptyListScreenPreview() {
    NoteTakerTheme(darkTheme = false) {
        ListScreen(emptyList()) { }
    }
}

@Preview(name = "Empty List - Dark", showBackground = true)
@Composable
fun EmptyListScreenPreviewDark() {
    NoteTakerTheme(darkTheme = true) {
        ListScreen(emptyList()) { }
    }
}

@Composable
fun EmptyNotesView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.EditNote,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Text(
                text = "No Notes Yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "Tap the + button to create your first note",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}
