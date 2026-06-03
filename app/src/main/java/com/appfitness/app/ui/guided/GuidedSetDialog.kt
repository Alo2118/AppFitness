package com.appfitness.app.ui.guided

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appfitness.app.AppFitnessApplication
import com.appfitness.app.audio.SpeechCoach
import com.appfitness.app.ble.HeartRateMonitor
import com.appfitness.app.ble.HrConnectionState
import com.appfitness.app.domain.CoachingEngine
import com.appfitness.app.domain.CoachingState
import com.appfitness.app.domain.HeartRateZone
import com.appfitness.app.domain.HeartRateZoneEvaluator
import com.appfitness.app.domain.RepProfile
import com.appfitness.app.domain.RepProfiles
import com.appfitness.app.domain.ZoneAlerter
import com.appfitness.app.sensor.SensorRepCounter
import kotlinx.coroutines.delay

private const val HR_ELEVATED_BPM = 150

/**
 * Full-screen guided set: counts real reps from the wrist accelerometer, reads
 * heart rate from the connected monitor, and coaches the user out loud — extra
 * push when it detects slowing or a high heart rate, plus the final "ancora N
 * ripetizioni" countdown. Writes the actual rep count back via [onDone].
 */
@Composable
fun GuidedSetDialog(
    exerciseName: String,
    targetReps: Int,
    onDone: (actualReps: Int) -> Unit,
    onCancel: () -> Unit,
    age: Int = 30,
    repProfile: RepProfile = RepProfiles.DEFAULT,
    targetZone: HeartRateZone = HeartRateZone.AEROBICA,
    imageAsset: String? = null,
) {
    val context = LocalContext.current
    val monitor = remember {
        (context.applicationContext as AppFitnessApplication).container.heartRateMonitor
    }
    val counter = remember(repProfile) { SensorRepCounter(context, repProfile) }
    val coach = remember { SpeechCoach(context) }
    val engine = remember { CoachingEngine() }
    val zoneAlerter = remember(targetZone) { ZoneAlerter(targetZone) }

    val reps by counter.reps.collectAsStateWithLifecycle()
    val cadence by counter.cadence.collectAsStateWithLifecycle()
    val hr by monitor.heartRate.collectAsStateWithLifecycle()
    val hrState by monitor.state.collectAsStateWithLifecycle()

    var message by remember { mutableStateOf("Preparati…") }
    var initialCadence by remember { mutableFloatStateOf(0f) }

    val currentZone = hr?.let { HeartRateZoneEvaluator.zoneFor(it, age) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants -> if (grants.values.all { it }) monitor.connect() }

    DisposableEffect(Unit) {
        counter.reset()
        engine.reset()
        counter.start()
        message = engine.startPhrase()
        coach.speak(message)
        onDispose {
            counter.stop()
            coach.shutdown()
        }
    }

    val finished = targetReps > 0 && reps >= targetReps

    // Live coaching while the set is in progress.
    LaunchedEffect(reps, hr, cadence) {
        if (finished) return@LaunchedEffect
        if (reps >= 3 && initialCadence == 0f && cadence > 0f) initialCadence = cadence
        val ratio = if (initialCadence > 0f) cadence / initialCadence else 1f
        val state = CoachingState(
            repCount = reps,
            targetReps = targetReps,
            cadenceRatio = ratio,
            heartRateElevated = hr != null && hr!! >= HR_ELEVATED_BPM,
        )
        engine.onUpdate(state)?.let { message = it; coach.speak(it) }
    }

    // Live heart-rate zone alerts: speak only when crossing the target zone.
    LaunchedEffect(currentZone) {
        currentZone?.let { zone ->
            zoneAlerter.onZone(zone)?.let { message = it; coach.speak(it) }
        }
    }

    // Completion: announce, then hand the actual count back.
    LaunchedEffect(finished) {
        if (finished) {
            engine.onUpdate(CoachingState(reps, targetReps, finished = true))?.let {
                message = it
                coach.speak(it)
            }
            delay(1600)
            onDone(reps)
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    exerciseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                imageAsset?.let { asset ->
                    Spacer(Modifier.height(12.dp))
                    com.appfitness.app.ui.components.AnimatedExerciseImage(
                        assetPath = asset,
                        contentDescription = exerciseName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "$reps",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("di $targetReps ripetizioni", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(16.dp))
                Text(
                    text = hr?.let { "❤️ $it bpm" } ?: "Fascia non collegata",
                    style = MaterialTheme.typography.bodyLarge,
                )
                currentZone?.let { zone ->
                    val inTarget = zone == targetZone
                    Text(
                        text = "Zona: ${zone.label}${if (inTarget) " ✔ (target)" else " · target ${targetZone.label}"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (inTarget) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                    )
                }
                if (hrState != HrConnectionState.CONNECTED) {
                    OutlinedButton(
                        onClick = {
                            if (monitor.hasPermissions()) monitor.connect()
                            else permissionLauncher.launch(HeartRateMonitor.requiredPermissions)
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) { Text("Collega fascia cardio") }
                }

                Spacer(Modifier.height(24.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "🗣 $message",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { counter.addManualRep() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("+1 (conteggio manuale)") }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Annulla")
                    }
                    Button(onClick = { onDone(reps) }, modifier = Modifier.weight(1f)) {
                        Text("Completa")
                    }
                }

                if (!counter.hasSensor()) {
                    Text(
                        "Accelerometro non disponibile: usa il conteggio manuale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}
