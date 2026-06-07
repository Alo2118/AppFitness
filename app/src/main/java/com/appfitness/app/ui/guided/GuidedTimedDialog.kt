package com.appfitness.app.ui.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.appfitness.app.audio.SpeechCoach
import com.appfitness.app.domain.TimedCueEngine
import com.appfitness.app.ui.util.formatDuration
import kotlinx.coroutines.delay

/**
 * Full-screen countdown for a time-based exercise (plank, wall-sit, …). It starts
 * automatically, counts down the target duration with a big timer + progress
 * ring, coaches the user out loud (start, mid-point, final 5s countdown) and then
 * hands control back via [onDone] so the caller can mark the set complete and
 * begin the rest timer.
 */
@Composable
fun GuidedTimedDialog(
    exerciseName: String,
    durationSec: Int,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    imageAsset: String? = null,
    voiceEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val coach = remember { if (voiceEnabled) SpeechCoach(context) else null }
    val cues = remember { TimedCueEngine() }

    var remaining by remember { mutableIntStateOf(durationSec) }
    var message by remember { mutableStateOf("Preparati…") }

    DisposableEffect(Unit) {
        onDispose { coach?.shutdown() }
    }

    LaunchedEffect(Unit) {
        cues.reset()
        var r = durationSec
        cues.onTick(r, durationSec)?.let { message = it; coach?.speak(it) }
        while (r > 0) {
            delay(1000)
            r--
            remaining = r
            cues.onTick(r, durationSec)?.let { message = it; coach?.speak(it) }
        }
        delay(900)
        onDone()
    }

    val progress = if (durationSec == 0) 1f else 1f - remaining.toFloat() / durationSec

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
                Spacer(Modifier.height(28.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(220.dp),
                        strokeWidth = 10.dp,
                    )
                    Text(
                        text = formatDuration(remaining),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(28.dp))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Annulla")
                    }
                    Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                        Text("Completa")
                    }
                }
            }
        }
    }
}
