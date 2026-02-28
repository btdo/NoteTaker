package com.noteaker.sample.ui.feature.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Add Note",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = title,
            onValueChange = { newText -> title = newText },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Title") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = content,
            onValueChange = { newText -> content = newText },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top = 8.dp),
            placeholder = { Text("Note content") },
            maxLines = Int.MAX_VALUE
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onMicrophoneClick,
            ) {
                Icon(imageVector = Icons.Filled.Mic, contentDescription = "Microphone")
            }

            IconButton(onClick = onCameraClick) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Camera")
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    onAddClick(Note(title = title, note = content))
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done",
                    tint = if (title.isNotBlank() && content.isNotBlank())
                        Color(0xFF4CAF50) // Bright green for success/done action
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
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
