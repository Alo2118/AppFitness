package com.appfitness.app.ui.programs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.Sport

private val weekOptions = listOf(4, 6, 8)
private val sessionOptions = listOf(2, 3, 4)

@Composable
fun CreateProgramDialog(
    suggestedLevel: FitnessLevel,
    onDismiss: () -> Unit,
    onConfirm: (sport: Sport, level: FitnessLevel, weeks: Int, sessionsPerWeek: Int) -> Unit,
) {
    var sport by remember { mutableStateOf(Sport.FITNESS) }
    var level by remember { mutableStateOf(suggestedLevel) }
    var weeks by remember { mutableIntStateOf(6) }
    var sessions by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuovo programma") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Label("Sport")
                ChipRow {
                    Sport.entries.forEach { s ->
                        FilterChip(
                            selected = sport == s,
                            onClick = { sport = s },
                            label = { Text("${s.emoji} ${s.label}") },
                        )
                    }
                }
                Text(
                    sport.focus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Label("Livello")
                ChipRow {
                    FitnessLevel.entries.forEach { l ->
                        FilterChip(
                            selected = level == l,
                            onClick = { level = l },
                            label = { Text(l.label) },
                        )
                    }
                }

                Label("Durata (settimane)")
                ChipRow {
                    weekOptions.forEach { w ->
                        FilterChip(selected = weeks == w, onClick = { weeks = w }, label = { Text("$w") })
                    }
                }

                Label("Sessioni a settimana")
                ChipRow {
                    sessionOptions.forEach { s ->
                        FilterChip(selected = sessions == s, onClick = { sessions = s }, label = { Text("$s") })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sport, level, weeks, sessions) }) { Text("Crea") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}
