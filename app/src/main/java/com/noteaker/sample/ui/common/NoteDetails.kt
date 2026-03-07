package com.noteaker.sample.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noteaker.sample.domain.model.Note
import com.noteaker.sample.ui.theme.NoteTakerTheme

@Composable
fun DetailsScreen(
    note: Note? = null,
    onMicrophoneClick: () -> Unit,
    onCameraClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: (note: Note) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.note ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with accent
        DetailsHeader()

        // Title Card
        CardTitle(
            title = title,
            onTitleChange = { newTitle -> title = newTitle }
        )

        // Content Card
        DetailsContent(content) {
            content = it
        }

        // Action Buttons Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onMicrophoneClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    IconButton(
                        onClick = onCameraClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Add Photo",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Cancel/Save Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Cancel Button
                    FilledIconButton(
                        onClick = onCancelClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "Save Note",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Save Button
                    FilledIconButton(
                        onClick = {
                            onSaveClick(Note(id = note?.id ?: 0, title = title, note = content))
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Save Note",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DetailsContent(content: String, onContentChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            placeholder = {
                Text(
                    "Start writing your note...",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            maxLines = Int.MAX_VALUE,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun DetailsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Create Note",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.weight(1f))
        // Accent dot
        Card(
            modifier = Modifier.size(12.dp),
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {}
    }
}

@Composable
private fun CardTitle(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            placeholder = {
                Text(
                    "Note Title",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun DetailsScreenPreviewLight() {
    NoteTakerTheme(darkTheme = false) {
        DetailsScreen(
            onSaveClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onCancelClick = {})
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun DetailsScreenPreviewDark() {
    NoteTakerTheme(darkTheme = true) {
        DetailsScreen(
            onSaveClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onCancelClick = {})
    }
}
