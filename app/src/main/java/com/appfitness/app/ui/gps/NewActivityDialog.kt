package com.appfitness.app.ui.gps

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.model.GpsActivityType
import com.appfitness.app.domain.GeoUtils
import com.appfitness.app.tracking.GhostConfig
import com.appfitness.app.tracking.GhostMode

@Composable
fun NewActivityDialog(
    pastActivities: List<GpsActivity>,
    onDismiss: () -> Unit,
    onConfirm: (GpsActivityType, GhostConfig) -> Unit,
) {
    var type by remember { mutableStateOf(GpsActivityType.RUN) }
    var mode by remember { mutableStateOf(GhostMode.NONE) }

    // PACE params
    var paceMin by remember { mutableIntStateOf(6) }
    var paceSec by remember { mutableIntStateOf(0) }
    // TARGET_TIME params
    var targetKm by remember { mutableIntStateOf(5) }
    var targetMin by remember { mutableIntStateOf(30) }
    // REPLAY param
    var replayId by remember { mutableStateOf(pastActivities.firstOrNull()?.id ?: -1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova attività") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Label("Tipo")
                ChipRow {
                    GpsActivityType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text("${t.emoji} ${t.label}") },
                        )
                    }
                }

                Label("Modalità fantasma")
                ChipRow {
                    GhostOption(mode == GhostMode.NONE, "Nessuna") { mode = GhostMode.NONE }
                    GhostOption(mode == GhostMode.PACE, "Ritmo") { mode = GhostMode.PACE }
                    GhostOption(mode == GhostMode.TARGET_TIME, "Tempo") { mode = GhostMode.TARGET_TIME }
                    if (pastActivities.isNotEmpty()) {
                        GhostOption(mode == GhostMode.REPLAY, "Sfida sessione") { mode = GhostMode.REPLAY }
                    }
                }

                when (mode) {
                    GhostMode.PACE -> {
                        Label("Ritmo obiettivo (min/km)")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Stepper("min", paceMin, 1) { paceMin = it.coerceIn(2, 12) }
                            Text(":", modifier = Modifier.padding(horizontal = 4.dp))
                            Stepper("sec", paceSec, 5) { paceSec = ((it % 60) + 60) % 60 }
                        }
                    }
                    GhostMode.TARGET_TIME -> {
                        Label("Obiettivo")
                        Row {
                            Stepper("km", targetKm, 1) { targetKm = it.coerceIn(1, 100) }
                            Stepper("min", targetMin, 1) { targetMin = it.coerceIn(1, 600) }
                        }
                        Text(
                            "Ritmo richiesto: ${GeoUtils.formatPace(targetMin * 60.0 / targetKm)}/km",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    GhostMode.REPLAY -> {
                        Label("Sessione da sfidare")
                        Column {
                            pastActivities.take(8).forEach { a ->
                                FilterChip(
                                    selected = replayId == a.id,
                                    onClick = { replayId = a.id },
                                    label = {
                                        Text("${a.type.emoji} ${GeoUtils.formatKm(a.distanceM.toDouble())} km in ${a.durationSec / 60}'")
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                        }
                    }
                    GhostMode.NONE -> {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(type, buildConfig(mode, paceMin, paceSec, targetKm, targetMin, replayId))
            }) { Text("Avvia") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

private fun buildConfig(
    mode: GhostMode,
    paceMin: Int,
    paceSec: Int,
    targetKm: Int,
    targetMin: Int,
    replayId: Long,
): GhostConfig = when (mode) {
    GhostMode.NONE -> GhostConfig(GhostMode.NONE)
    GhostMode.PACE -> GhostConfig(GhostMode.PACE, paceSecPerKm = (paceMin * 60 + paceSec).toDouble())
    GhostMode.TARGET_TIME -> GhostConfig(
        GhostMode.TARGET_TIME,
        targetDistanceM = targetKm * 1000.0,
        targetTimeSec = targetMin * 60,
    )
    GhostMode.REPLAY -> GhostConfig(GhostMode.REPLAY, replayActivityId = replayId)
}

@Composable
private fun GhostOption(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun Stepper(label: String, value: Int, step: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(value - step) }) {
                Icon(Icons.Filled.Remove, contentDescription = "Diminuisci")
            }
            Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onChange(value + step) }) {
                Icon(Icons.Filled.Add, contentDescription = "Aumenta")
            }
        }
    }
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
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}
