package com.appfitness.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfitness.app.ui.AppViewModelProvider
import com.appfitness.app.ui.components.BackTopBar
import com.appfitness.app.ui.components.BarChart
import com.appfitness.app.ui.components.LineChart
import com.appfitness.app.ui.components.SectionTitle
import com.appfitness.app.ui.components.StatTile

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { BackTopBar("Andamento", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("${state.totalWorkouts}", "Allenamenti totali", Modifier.weight(1f))
                StatTile("${state.totalKm.toInt()} km", "Distanza totale", Modifier.weight(1f), MaterialTheme.colorScheme.secondaryContainer)
            }

            ChartCard("Allenamenti per settimana") {
                BarChart(state.weeklyWorkouts, barColor = MaterialTheme.colorScheme.primary)
            }

            ChartCard("Distanza per settimana (km)") {
                BarChart(state.weeklyKm, barColor = MaterialTheme.colorScheme.secondary)
            }

            ChartCard("VO₂max nel tempo") {
                LineChart(state.vo2max, lineColor = MaterialTheme.colorScheme.tertiary)
            }

            ChartCard("Umore (ultime registrazioni)") {
                LineChart(state.mood, lineColor = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title)
            Column(modifier = Modifier.padding(top = 12.dp)) { content() }
        }
    }
}
