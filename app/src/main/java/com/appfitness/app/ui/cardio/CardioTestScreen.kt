package com.appfitness.app.ui.cardio

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.ble.HeartRateMonitor
import com.appfitness.app.ble.HrConnectionState
import com.appfitness.app.data.entity.CardioAssessment
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioTestScreen(
    onBack: () -> Unit,
    viewModel: CardioTestViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val connection by viewModel.connectionState.collectAsStateWithLifecycle()
    val bpm by viewModel.heartRate.collectAsStateWithLifecycle()
    val device by viewModel.deviceName.collectAsStateWithLifecycle()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.secondsLeft.collectAsStateWithLifecycle()
    val age by viewModel.age.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.all { it }) viewModel.connect()
    }

    fun onConnect() {
        if (viewModel.hasPermissions()) viewModel.connect()
        else permissionLauncher.launch(HeartRateMonitor.requiredPermissions)
    }

    Scaffold(
        topBar = { com.appfitness.app.ui.components.BackTopBar("Test cardio con fascia", onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeartRateCard(bpm = bpm, connection = connection, device = device)

            if (phase == CardioPhase.RESULT && result != null) {
                ResultCard(result!!)
                Button(onClick = viewModel::reset) { Text("Rifai il test") }
                OutlinedButton(onClick = onBack) { Text("Chiudi") }
                return@Column
            }

            if (phase == CardioPhase.SETUP) {
                if (!viewModel.isBluetoothEnabled()) {
                    InfoText("Attiva il Bluetooth e indossa la fascia cardio per iniziare.")
                }
                AgeSelector(age = age, onChange = viewModel::setAge)

                when (connection) {
                    HrConnectionState.CONNECTED -> {
                        InfoText("Fascia collegata ✔ — pronto per il test (riposo, sforzo, recupero).")
                        Button(onClick = viewModel::startTest, modifier = Modifier.fillMaxWidth()) {
                            Text("Inizia test")
                        }
                    }
                    HrConnectionState.SCANNING, HrConnectionState.CONNECTING -> {
                        CircularProgressIndicator()
                        InfoText("Ricerca della fascia in corso…")
                    }
                    HrConnectionState.ERROR -> {
                        InfoText("Connessione non riuscita. Verifica permessi, Bluetooth e fascia, poi riprova.")
                        Button(onClick = ::onConnect) { Text("Riprova") }
                    }
                    else -> {
                        Button(onClick = ::onConnect, modifier = Modifier.fillMaxWidth()) {
                            Text("Collega cardiofrequenzimetro")
                        }
                    }
                }
            } else {
                // Test in progress.
                PhaseCard(phase = phase, secondsLeft = secondsLeft)
                OutlinedButton(onClick = viewModel::reset) { Text("Annulla test") }
            }
        }
    }
}

@Composable
private fun HeartRateCard(bpm: Int?, connection: HrConnectionState, device: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = bpm?.toString() ?: "--",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = " bpm",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Text(
            text = statusLabel(connection, device),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun PhaseCard(phase: CardioPhase, secondsLeft: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(phase.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = formatDuration(secondsLeft),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ResultCard(result: CardioAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Condizione fisica", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            BigStat("VO₂max", "${result.vo2max}", "ml/kg/min · ${result.category}")
            BigStat("Recupero (HRR)", "${result.hrr}", "bpm in 1 min")
            BigStat("Livello consigliato", result.level.label, "")
            Spacer(Modifier.height(8.dp))
            Text(
                "Riposo ${result.restingHr} · Picco ${result.peakHr} · Recupero ${result.recoveryHr} bpm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BigStat(label: String, value: String, unit: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (unit.isNotBlank()) {
                Text(" $unit", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun AgeSelector(age: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Età", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(16.dp))
        IconButton(onClick = { onChange(age - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuisci età")
        }
        Text("$age", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        IconButton(onClick = { onChange(age + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = "Aumenta età")
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

private fun statusLabel(connection: HrConnectionState, device: String?): String = when (connection) {
    HrConnectionState.CONNECTED -> "Connesso${device?.let { " · $it" } ?: ""}"
    HrConnectionState.SCANNING -> "Ricerca dispositivo…"
    HrConnectionState.CONNECTING -> "Connessione…"
    HrConnectionState.DISCONNECTED -> "Disconnesso"
    HrConnectionState.ERROR -> "Errore di connessione"
    HrConnectionState.IDLE -> "Non collegato"
}
