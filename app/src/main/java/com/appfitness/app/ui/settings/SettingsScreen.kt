package com.appfitness.app.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.data.model.Equipment
import com.appfitness.app.data.model.Goal
import com.appfitness.app.data.model.Sex
import com.appfitness.app.data.model.ThemeMode
import com.appfitness.app.data.model.UnitSystem
import com.appfitness.app.domain.HeartRateZone
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.components.BackTopBar
import com.appfitness.app.ui.components.SectionTitle

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

    Scaffold(topBar = { BackTopBar("Profilo e impostazioni", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Profilo")
            Stepper("Peso", "${prefs.weightKg.toInt()} kg", { viewModel.setWeight(prefs.weightKg - 1) }, { viewModel.setWeight(prefs.weightKg + 1) })
            Stepper("Altezza", "${prefs.heightCm} cm", { viewModel.setHeight(prefs.heightCm - 1) }, { viewModel.setHeight(prefs.heightCm + 1) })
            Stepper("Età", "${prefs.age}", { viewModel.setAge(prefs.age - 1) }, { viewModel.setAge(prefs.age + 1) })

            Label("Sesso")
            ChipRow {
                Sex.entries.forEach { s ->
                    FilterChip(selected = prefs.sex == s, onClick = { viewModel.setSex(s) }, label = { Text(s.label) })
                }
            }
            Label("Obiettivo")
            ChipRow {
                Goal.entries.forEach { g ->
                    FilterChip(selected = prefs.goal == g, onClick = { viewModel.setGoal(g) }, label = { Text(g.label) })
                }
            }

            SectionTitle("Preferenze", modifier = Modifier.padding(top = 8.dp))
            Label("Tema")
            ChipRow {
                ThemeMode.entries.forEach { t ->
                    FilterChip(selected = prefs.theme == t, onClick = { viewModel.setTheme(t) }, label = { Text(t.label) })
                }
            }
            Label("Unità")
            ChipRow {
                UnitSystem.entries.forEach { u ->
                    FilterChip(selected = prefs.unit == u, onClick = { viewModel.setUnit(u) }, label = { Text(u.label) })
                }
            }
            Label("Zona cardio target")
            ChipRow {
                HeartRateZone.entries.forEach { z ->
                    FilterChip(selected = prefs.targetZone == z, onClick = { viewModel.setTargetZone(z) }, label = { Text(z.label) })
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Coach vocale", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = prefs.voiceCoach, onCheckedChange = { viewModel.setVoice(it) })
            }

            SectionTitle("I miei attrezzi", modifier = Modifier.padding(top = 8.dp))
            Text(
                "Seleziona ciò che possiedi: nella libreria potrai filtrare solo gli esercizi compatibili.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChipRow {
                Equipment.entries.forEach { eq ->
                    FilterChip(
                        selected = eq in prefs.equipment,
                        onClick = { viewModel.toggleEquipment(eq) },
                        label = { Text("${eq.emoji} ${eq.label}") },
                    )
                }
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus) { Icon(Icons.Filled.Remove, contentDescription = "Diminuisci") }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onPlus) { Icon(Icons.Filled.Add, contentDescription = "Aumenta") }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}
