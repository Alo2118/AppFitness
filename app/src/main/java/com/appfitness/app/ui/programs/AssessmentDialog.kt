package com.appfitness.app.ui.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The "test di inizio": a short standardized assessment whose results set the
 * recommended starting level and baseline load for programs.
 */
@Composable
fun AssessmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (pushUps: Int, squats: Int, plankSec: Int) -> Unit,
) {
    var pushUps by remember { mutableIntStateOf(15) }
    var squats by remember { mutableIntStateOf(25) }
    var plankSec by remember { mutableIntStateOf(45) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Test di inizio") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Esegui ogni esercizio al massimo e inserisci il risultato. Useremo questi dati per calibrare i tuoi programmi.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Counter("Push-up (max)", pushUps, step = 1, onChange = { pushUps = it })
                Counter("Squat in 60s", squats, step = 1, onChange = { squats = it })
                Counter("Plank (secondi)", plankSec, step = 5, onChange = { plankSec = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pushUps, squats, plankSec) }) { Text("Calcola livello") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}

@Composable
private fun Counter(label: String, value: Int, step: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange((value - step).coerceAtLeast(0)) }) {
                Icon(Icons.Filled.Remove, contentDescription = "Diminuisci")
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = { onChange(value + step) }) {
                Icon(Icons.Filled.Add, contentDescription = "Aumenta")
            }
        }
    }
}
