package com.appfitness.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.audio.SpeechCoach
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.domain.RepProfiles
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.components.rememberUserPreferences
import com.appfitness.app.ui.exercises.ExerciseViewModel
import com.appfitness.app.ui.guided.GuidedSetDialog
import com.appfitness.app.ui.guided.GuidedTimedDialog
import com.appfitness.app.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onFinished: () -> Unit,
    viewModel: WorkoutViewModel = viewModel(factory = AppViewModelProvider.Factory),
    exerciseViewModel: ExerciseViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val elapsed by viewModel.elapsedSec.collectAsStateWithLifecycle()
    val rest by viewModel.rest.collectAsStateWithLifecycle()
    val nextUp by viewModel.nextUp.collectAsStateWithLifecycle()
    val exercises by exerciseViewModel.exercises.collectAsStateWithLifecycle()
    val userAge by viewModel.userAge.collectAsStateWithLifecycle()
    val prefs = rememberUserPreferences()

    var showPicker by remember { mutableStateOf(false) }
    var showFinish by remember { mutableStateOf(false) }
    var activeSet by remember { mutableStateOf<SetLog?>(null) }

    val sets = session?.sets.orEmpty()

    // Screen-level voice for announcing the next exercise during rest.
    val context = LocalContext.current
    val coach = remember { SpeechCoach(context) }
    DisposableEffect(Unit) { onDispose { coach.shutdown() } }
    LaunchedEffect(Unit) {
        viewModel.announce.collect { phrase -> if (prefs.voiceCoach) coach.speak(phrase) }
    }
    // When the rest ends (or is skipped), auto-start the next set.
    LaunchedEffect(Unit) {
        viewModel.autoStart.collect { set -> activeSet = set }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session?.session?.title ?: "Allenamento")
                        Text(
                            text = formatDuration(elapsed),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showFinish = true }) { Text("Termina") }
                },
            )
        },
        floatingActionButton = {
            if (rest.isRunning.not()) {
                Button(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Esercizio", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (rest.isRunning) {
                RestBanner(
                    remaining = rest.remainingSec,
                    progress = rest.progress,
                    nextName = nextUp?.exerciseName,
                    onAdd = { viewModel.adjustRest(15) },
                    onSub = { viewModel.adjustRest(-15) },
                    onSkip = { viewModel.stopRest() },
                )
            }

            if (sets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aggiungi il primo esercizio per iniziare 💪",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val grouped = sets.groupBy { it.exerciseName }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    grouped.forEach { (name, exerciseSets) ->
                        item(key = name) {
                            ExerciseBlock(
                                name = name,
                                sets = exerciseSets.sortedBy { it.setNumber },
                                onUpdate = viewModel::updateSet,
                                onToggle = viewModel::toggleCompleted,
                                onDelete = viewModel::deleteSet,
                                onGuided = { activeSet = it },
                            )
                        }
                    }
                    item { androidx.compose.foundation.layout.Spacer(Modifier.padding(40.dp)) }
                }
            }
        }
    }

    if (showPicker) {
        ExercisePickerSheet(
            exercises = exercises,
            onPick = {
                viewModel.addExercise(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }

    if (showFinish) {
        FinishWorkoutDialog(
            onDismiss = { showFinish = false },
            onDiscard = {
                showFinish = false
                viewModel.discard(onFinished)
            },
            onConfirm = { mood, energy, note ->
                showFinish = false
                viewModel.finishWorkout(mood, energy, note, onFinished)
            },
        )
    }

    activeSet?.let { set ->
        val exercise = exercises.firstOrNull { it.id == set.exerciseId }
        if (set.isTimeBased) {
            GuidedTimedDialog(
                exerciseName = set.exerciseName,
                durationSec = set.durationSec,
                onDone = {
                    viewModel.completeTimedSet(set)
                    activeSet = null
                },
                onCancel = { activeSet = null },
                imageAsset = exercise?.imageAsset,
                voiceEnabled = prefs.voiceCoach,
            )
        } else {
            GuidedSetDialog(
                exerciseName = set.exerciseName,
                targetReps = set.reps,
                onDone = { actualReps ->
                    viewModel.completeGuidedSet(set, actualReps)
                    activeSet = null
                },
                onCancel = { activeSet = null },
                age = userAge,
                repProfile = RepProfiles.forCategory(exercise?.category),
                imageAsset = exercise?.imageAsset,
            )
        }
    }
}

@Composable
private fun RestBanner(
    remaining: Int,
    progress: Float,
    nextName: String?,
    onAdd: () -> Unit,
    onSub: () -> Unit,
    onSkip: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recupero", style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatDuration(remaining),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (nextName != null) "Preparati: $nextName" else "Ultima serie 💪",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSub) { Text("-15s") }
                OutlinedButton(onClick = onAdd) { Text("+15s") }
                Button(onClick = onSkip) { Text("Salta") }
            }
        }
    }
}

@Composable
private fun ExerciseBlock(
    name: String,
    sets: List<SetLog>,
    onUpdate: (SetLog) -> Unit,
    onToggle: (SetLog) -> Unit,
    onDelete: (Long) -> Unit,
    onGuided: (SetLog) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            sets.forEach { set ->
                SetRow(
                    set = set,
                    onUpdate = onUpdate,
                    onToggle = { onToggle(set) },
                    onDelete = { onDelete(set.id) },
                    onGuided = { onGuided(set) },
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    set: SetLog,
    onUpdate: (SetLog) -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onGuided: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#${set.setNumber}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            if (set.isTimeBased) {
                Stepper(
                    label = "Durata",
                    valueText = formatDuration(set.durationSec),
                    onMinus = { onUpdate(set.copy(durationSec = (set.durationSec - 5).coerceAtLeast(0))) },
                    onPlus = { onUpdate(set.copy(durationSec = set.durationSec + 5)) },
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Stepper(
                        label = "Ripetizioni",
                        valueText = set.reps.toString(),
                        onMinus = { onUpdate(set.copy(reps = (set.reps - 1).coerceAtLeast(0))) },
                        onPlus = { onUpdate(set.copy(reps = set.reps + 1)) },
                    )
                    Stepper(
                        label = "Peso (kg)",
                        valueText = set.weightKg.toString(),
                        onMinus = { onUpdate(set.copy(weightKg = (set.weightKg - 2.5f).coerceAtLeast(0f))) },
                        onPlus = { onUpdate(set.copy(weightKg = set.weightKg + 2.5f)) },
                    )
                }
            }
        }

        IconButton(onClick = onGuided) {
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = if (set.isTimeBased) "Avvia (a tempo)" else "Modalità guidata",
            )
        }
        FilledIconButton(onClick = onToggle) {
            Icon(
                imageVector = if (set.completed) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = if (set.completed) "Completata" else "Completa",
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Close, contentDescription = "Elimina serie")
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus) {
                Icon(Icons.Filled.Remove, contentDescription = "Diminuisci")
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onPlus) {
                Icon(Icons.Filled.Add, contentDescription = "Aumenta")
            }
        }
    }
}
