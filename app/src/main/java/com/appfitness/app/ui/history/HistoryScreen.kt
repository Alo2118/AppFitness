package com.appfitness.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Storico allenamenti",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        if (sessions.isEmpty()) {
            item {
                Text(
                    "Nessun allenamento completato.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(sessions, key = { it.session.id }) { session ->
                SessionDetailCard(session, onDelete = { viewModel.delete(session.session.id) })
            }
        }
    }
}

@Composable
private fun SessionDetailCard(session: SessionWithSets, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        formatTimestamp(session.session.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina")
                }
            }

            Text(
                text = "⏱ ${formatDuration(session.session.totalDurationSec)}  ·  ${session.totalSets} serie  ·  ${session.totalVolumeKg.toInt()} kg",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )

            val before = session.session.moodBefore
            val after = session.session.moodAfter
            if (before != null || after != null) {
                val beforeTxt = before?.let { MoodLevel.fromValue(it).emoji } ?: "–"
                val afterTxt = after?.let { MoodLevel.fromValue(it).emoji } ?: "–"
                Text(
                    text = "Umore: $beforeTxt → $afterTxt",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (session.sets.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                session.sets.groupBy { it.exerciseName }.forEach { (name, sets) ->
                    val summary = sets.joinToString("  ") {
                        if (it.isTimeBased) "${it.durationSec}s" else "${it.reps}×${it.weightKg.toInt()}kg"
                    }
                    Text(
                        text = "$name: $summary",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (session.session.note.isNotBlank()) {
                Text(
                    text = "📝 ${session.session.note}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
