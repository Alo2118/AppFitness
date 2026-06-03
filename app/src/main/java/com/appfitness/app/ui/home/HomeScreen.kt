package com.appfitness.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import com.appfitness.app.data.model.MoodLevel
import com.appfitness.app.data.relation.SessionWithSets
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.util.formatDuration
import com.appfitness.app.ui.util.formatTimestamp

@Composable
fun HomeScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenSession: (Long) -> Unit,
    onOpenGps: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showStartDialog by remember { mutableStateOf(false) }
    var showGenerateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showStartDialog = true },
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                text = { Text("Inizia allenamento") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Ciao! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = "Pronto a muoverti e sentirti meglio?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        value = state.weekWorkouts.toString(),
                        label = "Allenamenti\nquesta settimana",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = "${state.weekMinutes}'",
                        label = "Minuti\nattivi",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = moodAverageEmoji(state),
                        label = "Umore\nrecente",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                GenerateCard(onClick = { showGenerateDialog = true })
            }

            item {
                GpsCard(onClick = onOpenGps)
            }

            item {
                Text(
                    text = "Allenamenti recenti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (state.recentSessions.isEmpty()) {
                item {
                    Text(
                        text = "Nessun allenamento ancora. Tocca \"Inizia allenamento\" per cominciare!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.recentSessions, key = { it.session.id }) { session ->
                    RecentSessionCard(session) { onOpenSession(session.session.id) }
                }
            }

            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(40.dp)) }
        }
    }

    if (showStartDialog) {
        StartWorkoutDialog(
            onDismiss = { showStartDialog = false },
            onConfirm = { title, mood, energy ->
                showStartDialog = false
                viewModel.startWorkout(title, mood, energy, onStartWorkout)
            },
        )
    }

    if (showGenerateDialog) {
        GenerateWorkoutDialog(
            onDismiss = { showGenerateDialog = false },
            onConfirm = { title, mood, energy, plan ->
                showGenerateDialog = false
                viewModel.startGenerated(title, mood, energy, plan, onStartWorkout)
            },
        )
    }
}

@Composable
private fun GenerateCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✨ Genera un percorso completo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Scegli obiettivo, livello e durata: costruiamo noi l'allenamento, adattato alla tua energia di oggi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GpsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏃 Corsa & Bici (GPS)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Traccia il percorso e sfida un fantasma: una tua sessione, un ritmo costante o un tempo da battere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentSessionCard(session: SessionWithSets, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val before = session.session.moodBefore
                val after = session.session.moodAfter
                if (before != null && after != null) {
                    Text("${MoodLevel.fromValue(before).emoji} → ${MoodLevel.fromValue(after).emoji}")
                }
            }
            Text(
                text = formatTimestamp(session.session.startedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${session.totalSets} serie · ${formatDuration(session.session.totalDurationSec)} · ${session.totalVolumeKg.toInt()} kg volume",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun moodAverageEmoji(state: HomeUiState): String {
    if (state.recentMood.isEmpty()) return "–"
    val avg = state.recentMood.map { it.mood }.average().toInt().coerceIn(1, 5)
    return MoodLevel.fromValue(avg).emoji
}
