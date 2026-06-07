package com.appfitness.app.ui.mood

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.model.MoodLevel
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.util.formatTimestamp

@Composable
fun MoodScreen(
    modifier: Modifier = Modifier,
    viewModel: MoodViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Registra umore")
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
                    "Diario emozionale",
                    "Registra come ti senti e scopri il legame con i tuoi allenamenti.",
                )
            }

            if (entries.isNotEmpty()) {
                item { com.appfitness.app.ui.components.SectionTitle("Andamento", modifier = Modifier.padding(top = 4.dp)) }
                item { MoodTrend(entries.take(10).reversed()) }
            }

            if (entries.isEmpty()) {
                item { com.appfitness.app.ui.components.EmptyHint("Nessuna registrazione. Tocca + per il tuo primo check-in.") }
            } else {
                item { com.appfitness.app.ui.components.SectionTitle("Registrazioni", modifier = Modifier.padding(top = 8.dp)) }
                items(entries, key = { it.id }) { entry ->
                    MoodCard(entry, onDelete = { viewModel.delete(entry.id) })
                }
            }
        }
    }

    if (showAdd) {
        AddMoodDialog(
            onDismiss = { showAdd = false },
            onConfirm = { mood, energy, tags, note ->
                viewModel.addEntry(mood, energy, tags, note)
                showAdd = false
            },
        )
    }
}

@Composable
private fun MoodTrend(recent: List<MoodEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Andamento recente", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                recent.forEach { entry ->
                    Text(
                        text = MoodLevel.fromValue(entry.mood).emoji,
                        fontSize = 22.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodCard(entry: MoodEntry, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = MoodLevel.fromValue(entry.mood).emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${MoodLevel.fromValue(entry.mood).label} · energia ${entry.energy}/5",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (entry.tags.isNotBlank()) {
                    Text("🏷 ${entry.tags}", style = MaterialTheme.typography.bodySmall)
                }
                if (entry.note.isNotBlank()) {
                    Text(entry.note, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Elimina")
            }
        }
    }
}
