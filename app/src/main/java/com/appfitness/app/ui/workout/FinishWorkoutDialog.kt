package com.appfitness.app.ui.workout

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
 * Post-workout check-in. Capturing mood/energy *after* training is what lets
 * AppFitness show users how exercise shifts how they feel.
 */
@Composable
fun FinishWorkoutDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onConfirm: (moodAfter: Int?, energyAfter: Int?, note: String) -> Unit,
) {
    var mood by remember { mutableStateOf<Int?>(null) }
    var energy by remember { mutableStateOf<Int?>(null) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Come ti senti adesso?") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                MoodSelector(
                    title = "Umore dopo",
                    selected = mood,
                    onSelect = { mood = it },
                )
                MoodSelector(
                    title = "Energia dopo",
                    selected = energy,
                    onSelect = { energy = it },
                    modifier = Modifier.padding(top = 16.dp),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (opzionale)") },
                    modifier = Modifier.padding(top = 16.dp),
                )
                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("Elimina allenamento")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(mood, energy, note) }) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Continua") }
        },
    )
}
