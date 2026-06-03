package com.appfitness.app.ui.mood

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfitness.app.ui.components.MoodSelector

@Composable
fun AddMoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (mood: Int, energy: Int, tags: String, note: String) -> Unit,
) {
    var mood by remember { mutableStateOf<Int?>(null) }
    var energy by remember { mutableStateOf<Int?>(null) }
    var tags by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check-in emozionale") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                MoodSelector(title = "Come ti senti?", selected = mood, onSelect = { mood = it })
                MoodSelector(
                    title = "Livello di energia",
                    selected = energy,
                    onSelect = { energy = it },
                    modifier = Modifier.padding(top = 16.dp),
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tag (es. motivato, stanco)") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 16.dp),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = mood != null && energy != null,
                onClick = { onConfirm(mood!!, energy!!, tags.trim(), note.trim()) },
            ) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
