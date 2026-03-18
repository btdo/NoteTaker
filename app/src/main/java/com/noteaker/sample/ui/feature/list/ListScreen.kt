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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noteaker.sample.data.model.ZenQuotes
import com.noteaker.sample.domain.model.Attachment
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.ui.theme.NoteTakerTheme

@Composable
fun ListSearchScreen(viewModel: ListViewModel) {
    val notes by viewModel.searchResults.collectAsStateWithLifecycle(listOf())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
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

        ListScreen(
            notes = notes,
            onAddClick = viewModel::addClick,
            onEditClick = viewModel::onEditClick,
            isSearching = isSearching
        )
    }
}

@Composable
fun ZenQuoteBox(quote: ZenQuotes.ZenQuotesItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceTint
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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = quote.q,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "— ${quote.a}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview
@Composable
fun ZenQuoteBoxPreview() {
    NoteTakerTheme {
        ZenQuoteBox(ZenQuotes.ZenQuotesItem(q = "Successful people tend to become more successful because they are always thinking about their successes.", a = "Brian Tracy", h= "<blockquote>&ldquo;Successful people tend to become more successful because they are always thinking about their successes.&rdquo; &mdash; <footer>Brian Tracy</footer></blockquote>"))
    }
}

@Composable
fun ListScreen(
    notes: List<Note>,
    isSearching: Boolean = false,
    onAddClick: () -> Unit = {},
    onEditClick: (note: Note) -> Unit = {},

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
                        NoteCard(note = note, onClick = { onEditClick(note) })
                    }
                }
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
                lastUpdated = System.currentTimeMillis() - 3600000,
                attachments = listOf(
                    Attachment(uri = "file://test.pdf", displayName = "document.pdf")
                )
            ),
            Note(
                id = 2,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits",
                lastUpdated = System.currentTimeMillis() - 7200000
            ),
            Note(
                id = 3,
                title = "Ideas for App",
                note = "Add dark mode support, implement search functionality, create backup feature",
                lastUpdated = System.currentTimeMillis() - 86400000,
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
                lastUpdated = System.currentTimeMillis() - 3600000
            ),
            Note(
                id = 2,
                title = "Shopping List",
                note = "Milk, Eggs, Bread, Coffee, Fruits",
                lastUpdated = System.currentTimeMillis() - 7200000
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
