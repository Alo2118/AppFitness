package com.appfitness.app.ui.gps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfitness.app.AppFitnessApplication
import com.appfitness.app.data.relation.ActivityWithPoints
import com.appfitness.app.domain.GeoUtils
import com.appfitness.app.ui.util.formatDuration
import com.appfitness.app.ui.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsActivityDetailScreen(activityId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = (context.applicationContext as AppFitnessApplication).container.repository

    val data by produceState<ActivityWithPoints?>(initialValue = null, activityId) {
        value = repository.getActivityWithPoints(activityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio attività") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
            )
        },
    ) { padding ->
        val activity = data?.activity
        if (activity == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val points = data?.points
            ?.sortedBy { it.elapsedSec }
            ?.map { it.lat to it.lon }
            .orEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (points.size >= 2) {
                OsmRouteMap(
                    points = points,
                    fitBounds = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
            } else {
                Text(
                    "Percorso non disponibile per questa attività.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "${activity.type.emoji} ${activity.type.label}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(formatTimestamp(activity.startedAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Distanza: ${GeoUtils.formatKm(activity.distanceM.toDouble())} km",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Durata: ${formatDuration(activity.durationSec)}")
                Text("Ritmo medio: ${GeoUtils.formatPace(activity.avgPaceSecPerKm.toDouble())}/km")
            }
        }
    }
}
