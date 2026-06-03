package com.appfitness.app.ui.gps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.domain.GeoUtils
import com.appfitness.app.tracking.GhostConfig
import com.appfitness.app.tracking.TrackingService
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.util.formatDuration
import com.appfitness.app.ui.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsScreen(
    onBack: () -> Unit,
    onStartTracking: () -> Unit,
    viewModel: GpsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNew by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Pair<com.appfitness.app.data.model.GpsActivityType, GhostConfig>?>(null) }

    fun startTracking(type: com.appfitness.app.data.model.GpsActivityType, ghost: GhostConfig) {
        ContextCompat.startForegroundService(context, TrackingService.startIntent(context, type, ghost))
        onStartTracking()
    }

    val locationPermission = rememberLocationPermissionLauncher { granted ->
        val p = pending
        pending = null
        if (granted && p != null) startTracking(p.first, p.second)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Corsa & Bici") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNew = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuova attività") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (activities.isEmpty()) {
                item {
                    Text(
                        "Nessuna attività registrata. Tocca \"Nuova attività\" per la tua prima corsa o giro in bici.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(activities, key = { it.id }) { activity ->
                    ActivityCard(activity, onDelete = { viewModel.delete(activity.id) })
                }
            }
        }
    }

    if (showNew) {
        NewActivityDialog(
            pastActivities = activities,
            onDismiss = { showNew = false },
            onConfirm = { type, ghost ->
                showNew = false
                if (hasLocationPermission(context)) {
                    startTracking(type, ghost)
                } else {
                    pending = type to ghost
                    locationPermission.launch(LOCATION_PERMISSIONS)
                }
            },
        )
    }
}

@Composable
private fun ActivityCard(activity: GpsActivity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${activity.type.emoji} ${activity.type.label} · ${GeoUtils.formatKm(activity.distanceM.toDouble())} km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${formatDuration(activity.durationSec)} · ${GeoUtils.formatPace(activity.avgPaceSecPerKm.toDouble())}/km",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    formatTimestamp(activity.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Elimina")
            }
        }
    }
}
