package com.appfitness.app.ui.gps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appfitness.app.domain.GeoUtils
import com.appfitness.app.tracking.TrackingService
import com.appfitness.app.tracking.TrackingState
import com.appfitness.app.ui.util.formatDuration
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GpsTrackingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current

    val isTracking by TrackingState.isTracking.collectAsStateWithLifecycle()
    val type by TrackingState.type.collectAsStateWithLifecycle()
    val elapsed by TrackingState.elapsedSec.collectAsStateWithLifecycle()
    val distance by TrackingState.distanceM.collectAsStateWithLifecycle()
    val pace by TrackingState.paceSecPerKm.collectAsStateWithLifecycle()
    val ghostLead by TrackingState.ghostLeadM.collectAsStateWithLifecycle()
    val hasFix by TrackingState.hasGpsFix.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "${type.emoji} ${type.label}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            if (hasFix) "GPS attivo" else "In attesa del segnale GPS…",
            style = MaterialTheme.typography.bodySmall,
            color = if (hasFix) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = GeoUtils.formatKm(distance),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("km", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))
        Metric("Tempo", formatDuration(elapsed))
        Metric("Ritmo", "${GeoUtils.formatPace(pace)} /km")

        ghostLead?.let { lead ->
            Spacer(Modifier.height(16.dp))
            GhostBanner(lead)
        }

        Spacer(Modifier.height(32.dp))
        if (isTracking) {
            Button(
                onClick = { context.startService(TrackingService.stopIntent(context)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Termina e salva") }
        } else {
            Text("Attività salvata ✔", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("Chiudi") }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GhostBanner(leadMeters: Double) {
    val ahead = leadMeters >= 0
    val meters = abs(leadMeters).roundToInt()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ahead) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (ahead) "👻 Sei avanti!" else "👻 Sei indietro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$meters m ${if (ahead) "di vantaggio" else "da recuperare"}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
