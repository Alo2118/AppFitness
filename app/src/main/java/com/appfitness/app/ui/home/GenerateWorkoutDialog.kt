package com.appfitness.app.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.WorkoutGoal
import com.appfitness.app.domain.GeneratedExercise
import com.appfitness.app.domain.WorkoutGenerator
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.components.MoodSelector
import com.appfitness.app.ui.exercises.ExerciseViewModel
import com.appfitness.app.ui.util.formatDuration
import kotlin.random.Random

private val durations = listOf(20, 30, 45, 60)

/**
 * Lets the user generate a complete workout path: pick goal, level, duration and
 * current energy, preview the plan, reshuffle, then start it in one tap.
 */
@Composable
fun GenerateWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, moodBefore: Int?, energyBefore: Int?, plan: List<GeneratedExercise>) -> Unit,
    exerciseViewModel: ExerciseViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val library by exerciseViewModel.exercises.collectAsStateWithLifecycle()

    var goal by remember { mutableStateOf(WorkoutGoal.TOTAL_BODY) }
    var level by remember { mutableStateOf(FitnessLevel.INTERMEDIO) }
    var duration by remember { mutableIntStateOf(30) }
    var energy by remember { mutableStateOf<Int?>(null) }
    var seed by remember { mutableIntStateOf(0) }

    val plan = remember(goal, level, duration, energy, seed, library) {
        WorkoutGenerator.generate(goal, level, duration, energy, library, Random(seed.toLong()))
    }
    val totalMin = plan.sumOf { it.estimatedSeconds } / 60

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Genera un percorso") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Label("Obiettivo")
                ChipRow {
                    WorkoutGoal.entries.forEach { g ->
                        FilterChip(
                            selected = goal == g,
                            onClick = { goal = g },
                            label = { Text("${g.emoji} ${g.label}") },
                        )
                    }
                }

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

                Label("Durata")
                ChipRow {
                    durations.forEach { d ->
                        FilterChip(
                            selected = duration == d,
                            onClick = { duration = d },
                            label = { Text("$d min") },
                        )
                    }
                }

                MoodSelector(
                    title = "Energia di oggi (adatta l'intensità)",
                    selected = energy,
                    onSelect = { energy = it },
                    modifier = Modifier.padding(top = 16.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Anteprima · ~${totalMin}'",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AssistChip(
                        onClick = { seed++ },
                        label = { Text("Rigenera") },
                        leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                    )
                }

                Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                    if (plan.isEmpty()) {
                        Text(
                            "Nessun esercizio disponibile.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    plan.forEachIndexed { index, item ->
                        val target = if (item.exercise.isTimeBased) {
                            "${item.sets} × ${item.durationSec}s"
                        } else {
                            "${item.sets} × ${item.reps} rip"
                        }
                        Text(
                            text = "${index + 1}. ${item.exercise.name} — $target · recupero ${item.restSec}s",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = plan.isNotEmpty(),
                onClick = {
                    onConfirm("${goal.emoji} ${goal.label} · ${duration}'", null, energy, plan)
                },
            ) { Text("Inizia") }
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
    ) {
        content()
    }
}
