package com.appfitness.app.ui.exercises

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.model.Equipment
import com.appfitness.app.data.model.ExerciseCategory

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (Exercise) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExerciseCategory.STRENGTH) }
    var equipment by remember { mutableStateOf(Equipment.BODYWEIGHT) }
    var timeBased by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuovo esercizio") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = muscle,
                    onValueChange = { muscle = it },
                    label = { Text("Gruppo muscolare") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione") },
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text("Categoria", modifier = Modifier.padding(top = 12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExerciseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) },
                        )
                    }
                }
                Text("Attrezzo", modifier = Modifier.padding(top = 12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Equipment.entries.forEach { eq ->
                        FilterChip(
                            selected = equipment == eq,
                            onClick = { equipment = eq },
                            label = { Text("${eq.emoji} ${eq.label}") },
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(checked = timeBased, onCheckedChange = { timeBased = it })
                    Text(
                        text = if (timeBased) "A tempo (secondi)" else "A ripetizioni",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(
                        Exercise(
                            name = name.trim(),
                            category = category,
                            muscleGroup = muscle.ifBlank { "Generico" },
                            description = description.trim(),
                            isTimeBased = timeBased,
                            isCustom = true,
                            equipment = equipment,
                        )
                    )
                },
            ) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
