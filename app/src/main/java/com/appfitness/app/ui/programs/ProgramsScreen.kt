package com.appfitness.app.ui.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.data.entity.AssessmentResult
import com.appfitness.app.data.entity.CardioAssessment
import com.appfitness.app.data.entity.TrainingProgram
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.util.formatTimestamp

@Composable
fun ProgramsScreen(
    onStartSession: (Long) -> Unit,
    onOpenCardioTest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgramsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val programs by viewModel.programs.collectAsStateWithLifecycle()
    val assessment by viewModel.latestAssessment.collectAsStateWithLifecycle()
    val cardio by viewModel.latestCardio.collectAsStateWithLifecycle()

    var showAssessment by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nuovo programma")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                com.appfitness.app.ui.components.ScreenHeader(
                    "Programmi",
                    "Test, percorsi per sport e progressione adattiva.",
                )
            }

            item { AssessmentCard(assessment) { showAssessment = true } }

            item { CardioCard(cardio, onTest = onOpenCardioTest) }

            item {
                com.appfitness.app.ui.components.SectionTitle(
                    "I tuoi programmi",
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (programs.isEmpty()) {
                item {
                    com.appfitness.app.ui.components.EmptyHint(
                        "Nessun programma. Tocca + per crearne uno specifico per il tuo sport.",
                    )
                }
            } else {
                items(programs, key = { it.id }) { program ->
                    ProgramCard(
                        program = program,
                        onStart = { viewModel.startNextSession(program.id, null, onStartSession) },
                        onDelete = { viewModel.deleteProgram(program.id) },
                    )
                }
            }
        }
    }

    if (showAssessment) {
        AssessmentDialog(
            onDismiss = { showAssessment = false },
            onConfirm = { p, s, plank ->
                viewModel.saveAssessment(p, s, plank)
                showAssessment = false
            },
        )
    }

    if (showCreate) {
        CreateProgramDialog(
            suggestedLevel = cardio?.level ?: assessment?.level ?: FitnessLevel.INTERMEDIO,
            onDismiss = { showCreate = false },
            onConfirm = { sport, level, weeks, sessions ->
                viewModel.createProgram(sport, level, weeks, sessions)
                showCreate = false
            },
        )
    }
}

@Composable
private fun AssessmentCard(assessment: AssessmentResult?, onTest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTest,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📋 Test di inizio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (assessment == null) {
                Text(
                    "Misura il tuo punto di partenza per calibrare i programmi. Tocca per iniziare.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    "Livello: ${assessment.level.label} · punteggio ${assessment.score}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "${assessment.pushUps} push-up · ${assessment.squats} squat · ${assessment.plankSec}s plank — ${formatTimestamp(assessment.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Tocca per rifare il test",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CardioCard(cardio: CardioAssessment?, onTest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTest,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "❤️ Test cardio con fascia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (cardio == null) {
                Text(
                    "Collega un cardiofrequenzimetro Bluetooth per stimare VO₂max e recupero cardiaco. Tocca per iniziare.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    "VO₂max ${cardio.vo2max} (${cardio.category}) · HRR ${cardio.hrr} bpm · livello ${cardio.level.label}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Tocca per rifare il test",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: TrainingProgram,
    onStart: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${program.sport.emoji} ${program.sport.label}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${program.level.label} · ${program.weeks} sett · ${program.sessionsPerWeek}/sett",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text("Carico ${(program.loadMultiplier * 100).toInt()}%") },
                )
            }

            LinearProgressIndicator(
                progress = { program.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            )
            Text(
                "Settimana ${program.currentWeek}/${program.weeks} · ${program.completedSessions}/${program.totalSessions} sessioni",
                style = MaterialTheme.typography.bodySmall,
            )

            if (program.lastSuggestion.isNotBlank()) {
                Text(
                    "🤖 ${program.lastSuggestion}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (program.isFinished) {
                    Text("✅ Programma completato!", fontWeight = FontWeight.SemiBold)
                } else {
                    Button(onClick = onStart) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("Prossima sessione", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina programma")
                }
            }
        }
    }
}
