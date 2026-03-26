package com.noteaker.sample.ui.feature.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noteaker.sample.ui.common.ZenQuoteBox
import com.noteaker.sample.ui.model.AttachmentUI
import com.noteaker.sample.ui.model.NoteUI
import com.noteaker.sample.ui.model.UIState
import com.noteaker.sample.ui.theme.NoteTakerTheme

@Composable
fun ListSearchScreen(viewModel: ListViewModel) {
    val uiState by viewModel.searchRetry.collectAsStateWithLifecycle(UIState.Loading)
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsStateWithLifecycle()
    val isSearching by remember {
        derivedStateOf { searchQuery.isNotEmpty() }
    }

    val quote by viewModel.quotes.collectAsStateWithLifecycle(null)
    Column(modifier = Modifier.fillMaxSize()) {
        quote?.let {
            ZenQuoteBox(it, modifier = Modifier.padding(16.dp))
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search by title or content...") },
            singleLine = true
        )

        if (uiState is UIState.Success<*>) {
            ListScreen(
                notes = (uiState as UIState.Success<List<NoteUI>>).data,
                selectedNoteIds = selectedNoteIds,
                onAddClick = viewModel::addClick,
                onEditClick = viewModel::onEditClick,
                onSelectionChange = viewModel::onSelectionChange,
                isSearching = isSearching,
                onDeleteClick = viewModel::onDeleteClick
            )
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Sorry, something went wrong.  Click retry to try again."
            )
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = viewModel::retry
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ListScreen(
    notes: List<NoteUI>,
    selectedNoteIds: Set<Long> = emptySet(),
    isSearching: Boolean = false,
    onAddClick: () -> Unit = {},
    onEditClick: (note: NoteUI) -> Unit = {},
    onSelectionChange: (noteId: Long, isSelected: Boolean) -> Unit = { _, _ -> },
    onDeleteClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (notes.isEmpty()) {
                EmptyNotesView(isSearching = isSearching)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isSelected = note.id in selectedNoteIds,
                            onClick = { onEditClick(note) },
                            onSelectionChange = { isSelected ->
                                onSelectionChange(note.id, isSelected)
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-16).dp)
        ) {
            if (selectedNoteIds.isNotEmpty()) {
                FilledIconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    modifier = Modifier
                        .size(56.dp)
                        .offset(x = (-16).dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Note",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

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
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Save Note",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Preview(name = "List with Notes - Light", showBackground = true)
@Composable
fun ListScreenPreview() {
    NoteTakerTheme(darkTheme = false) {
        val notes = listOf(
            NoteUI(
                id = 1,
                title = "Meeting Notes",
                note = "Discussed project timeline and deliverables. Need to follow up with the team about the new requirements.",
                lastUpdated = System.currentTimeMillis() - 3600000,
                attachments = listOf(
                    AttachmentUI(uri = "file://test.pdf", displayName = "document.pdf")
                )
            ),
            NoteUI(
                id = 2,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits",
                lastUpdated = System.currentTimeMillis() - 7200000
            ),
            NoteUI(
                id = 3,
                title = "Ideas for App",
                note = "Add dark mode support, implement search functionality, create backup feature",
                lastUpdated = System.currentTimeMillis() - 86400000,
                attachments = listOf(
                    AttachmentUI(uri = "file://sketch1.png"),
                    AttachmentUI(uri = "file://sketch2.png")
                )
            )
        )

        ListScreen(notes, setOf(1, 2))
    }
}

@Preview(name = "List with Notes - Dark", showBackground = true)
@Composable
fun ListScreenPreviewDark() {
    NoteTakerTheme(darkTheme = true) {
        val notes = listOf(
            NoteUI(
                id = 1,
                title = "Meeting Notes",
                note = "Discussed project timeline and deliverables. Need to follow up with the team about the new requirements.",
                lastUpdated = System.currentTimeMillis() - 3600000
            ),
            NoteUI(
                id = 2,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits",
                lastUpdated = System.currentTimeMillis() - 7200000
            )
        )

        ListScreen(
            notes = notes,
        )
    }
}

@Preview(name = "Empty List - Light", showBackground = true)
@Composable
fun EmptyListScreenPreview() {
    NoteTakerTheme(darkTheme = false) {
        ListScreen(
            notes = emptyList(),
        )
    }
}

@Preview(name = "Empty List - Dark", showBackground = true)
@Composable
fun EmptyListScreenPreviewDark() {
    NoteTakerTheme(darkTheme = true) {
        ListScreen(emptyList())
    }
}

@Composable
fun EmptyNotesView(isSearching: Boolean = false) {
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
                text = if (isSearching) "No matches" else "No Notes Yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = if (isSearching) "Try a different search" else "Tap the + button to create your first note",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}
