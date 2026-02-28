package com.noteaker.sample.ui.feature.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
fun AddScreen(
    onMicrophoneClick: () -> Unit,
    onCameraClick: () -> Unit,
    onAddClick: (note: Note) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with accent
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

        // Title Card
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
                onValueChange = { newText -> title = newText },
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

        // Content Card
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
                onValueChange = { newText -> content = newText },
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

                // Save Button
                FilledIconButton(
                    onClick = {
                        onAddClick(Note(title = title, note = content))
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

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun AddScreenPreviewLight() {
    NoteTakerTheme(darkTheme = false) {
        AddScreen(onAddClick = {}, onMicrophoneClick = {}, onCameraClick = {})
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun AddScreenPreviewDark() {
    NoteTakerTheme(darkTheme = true) {
        AddScreen(onAddClick = {}, onMicrophoneClick = {}, onCameraClick = {})
    }
}
