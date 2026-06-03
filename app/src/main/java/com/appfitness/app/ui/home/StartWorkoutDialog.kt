package com.appfitness.app.ui.home

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

/**
 * Pre-workout check-in: captures an optional title and the "before" emotional
 * and energy state, the first half of AppFitness's mood-vs-training insight.
 */
@Composable
fun StartWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, moodBefore: Int?, energyBefore: Int?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf<Int?>(null) }
    var energy by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Come ti senti ora?") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome allenamento (opzionale)") },
                    singleLine = true,
                )
                MoodSelector(
                    title = "Umore prima",
                    selected = mood,
                    onSelect = { mood = it },
                    modifier = Modifier.padding(top = 16.dp),
                )
                MoodSelector(
                    title = "Energia prima",
                    selected = energy,
                    onSelect = { energy = it },
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, mood, energy) }) { Text("Inizia") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
