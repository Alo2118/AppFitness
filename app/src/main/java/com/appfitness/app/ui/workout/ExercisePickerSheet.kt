package com.appfitness.app.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfitness.app.data.entity.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    exercises: List<Exercise>,
    onPick: (Exercise) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "Scegli un esercizio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
            items(exercises, key = { it.id }) { exercise ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(exercise) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                    val target = if (exercise.isTimeBased) {
                        "${exercise.defaultSets} × ${exercise.defaultDurationSec}s"
                    } else {
                        "${exercise.defaultSets} × ${exercise.defaultReps} rip"
                    }
                    Text(
                        text = "${exercise.category.label} · ${exercise.muscleGroup} · $target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
