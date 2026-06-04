package com.appfitness.app.ui.gps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.appfitness.app.AppFitnessApplication
import com.appfitness.app.data.relation.ActivityWithPoints
import com.appfitness.app.domain.GeoUtils
import com.appfitness.app.domain.GpxExporter
import com.appfitness.app.ui.util.formatDuration
import com.appfitness.app.ui.util.formatTimestamp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsActivityDetailScreen(activityId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as AppFitnessApplication).container
    val repository = container.repository
    val prefs by container.settingsRepository.preferences
        .collectAsState(initial = com.appfitness.app.data.model.UserPreferences())

    val data by produceState<ActivityWithPoints?>(initialValue = null, activityId) {
        value = repository.getActivityWithPoints(activityId)
    }

    Scaffold(
        topBar = {
            com.appfitness.app.ui.components.BackTopBar("Dettaglio attività", onBack) {
                data?.let { d ->
                    if (d.points.isNotEmpty()) {
                        IconButton(onClick = { shareGpx(context, d) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Esporta GPX")
                        }
                    }
                }
            }
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
                val kcal = com.appfitness.app.domain.CalorieEstimator.forGps(
                    activity.type, activity.distanceM / 1000.0, activity.durationSec, prefs.weightKg,
                )
                Text("🔥 ~$kcal kcal", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Writes the route to a GPX file and opens the system share sheet. */
private fun shareGpx(context: android.content.Context, data: ActivityWithPoints) {
    runCatching {
        val gpx = GpxExporter.toGpx(data.activity, data.points)
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "appfitness_${data.activity.id}.gpx")
        file.writeText(gpx)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Condividi percorso GPX"))
    }
}
