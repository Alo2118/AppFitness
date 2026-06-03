package com.appfitness.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.data.model.MoodLevel
import com.appfitness.app.data.relation.SessionWithSets
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.components.ActionCard
import com.appfitness.app.ui.components.EmptyHint
import com.appfitness.app.ui.components.SectionTitle
import com.appfitness.app.ui.components.StatTile
import com.appfitness.app.ui.util.formatDuration
import com.appfitness.app.ui.util.formatTimestamp

@Composable
fun HomeScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenSession: (Long) -> Unit,
    onOpenGps: () -> Unit,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val totalPoints by viewModel.totalPoints.collectAsStateWithLifecycle()
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
                HeroHeader(totalPoints = totalPoints, onClick = onOpenAchievements)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        value = state.weekWorkouts.toString(),
                        label = "Allenamenti\nquesta settimana",
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        value = "${state.weekMinutes}'",
                        label = "Minuti\nattivi",
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        value = moodAverageEmoji(state),
                        label = "Umore\nrecente",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                ActionCard(
                    title = "✨ Genera un percorso completo",
                    subtitle = "Scegli obiettivo, livello e durata: costruiamo noi l'allenamento, adattato alla tua energia di oggi.",
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { showGenerateDialog = true },
                )
            }

            item {
                ActionCard(
                    title = "🏃 Corsa & Bici (GPS)",
                    subtitle = "Traccia il percorso e sfida un fantasma: una tua sessione, un ritmo costante o un tempo da battere.",
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onOpenGps,
                )
            }

            item {
                SectionTitle("Allenamenti recenti", modifier = Modifier.padding(top = 8.dp))
            }

            if (state.recentSessions.isEmpty()) {
                item {
                    EmptyHint("Nessun allenamento ancora. Tocca \"Inizia allenamento\" per cominciare!")
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
private fun HeroHeader(totalPoints: Int, onClick: () -> Unit) {
    val level = com.appfitness.app.domain.RewardLevels.levelFor(totalPoints)
    val progress = com.appfitness.app.domain.RewardLevels.levelProgress(totalPoints)
    val toNext = com.appfitness.app.domain.RewardLevels.pointsToNextLevel(totalPoints)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        com.appfitness.app.ui.theme.GradientStart,
                        com.appfitness.app.ui.theme.GradientEnd,
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Column {
            Text(
                "Ciao! 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
            )
            Text(
                "Pronto a muoverti e sentirti meglio?",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "🏆 Livello $level",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                )
                Text(
                    "$totalPoints pt",
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                color = androidx.compose.ui.graphics.Color.White,
                trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            )
            Text(
                "Ancora $toNext pt al livello ${level + 1} · tocca per i Traguardi",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 6.dp),
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
