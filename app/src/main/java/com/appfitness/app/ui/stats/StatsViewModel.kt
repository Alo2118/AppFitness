package com.appfitness.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.domain.WeeklyGoals
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StatsUiState(
    val weeklyWorkouts: List<Pair<String, Float>> = emptyList(),
    val weeklyKm: List<Pair<String, Float>> = emptyList(),
    val vo2max: List<Float> = emptyList(),
    val mood: List<Float> = emptyList(),
    val totalWorkouts: Int = 0,
    val totalKm: Float = 0f,
)

class StatsViewModel(repository: FitnessRepository) : ViewModel() {

    private val dayFmt = SimpleDateFormat("d/M", Locale.ITALIAN)

    val uiState: StateFlow<StatsUiState> = combine(
        repository.completedSessions(),
        repository.gpsActivities,
        repository.allCardioAssessments,
        repository.moodEntries,
    ) { sessions, gps, cardio, mood ->
        val weekMs = 7L * 24 * 60 * 60 * 1000
        val week0 = WeeklyGoals.startOfWeekMillis()

        val weeklyWorkouts = (5 downTo 0).map { i ->
            val start = week0 - i * weekMs
            val end = start + weekMs
            val label = dayFmt.format(Date(start))
            label to sessions.count { it.session.startedAt in start until end }.toFloat()
        }
        val weeklyKm = (5 downTo 0).map { i ->
            val start = week0 - i * weekMs
            val end = start + weekMs
            val label = dayFmt.format(Date(start))
            val km = gps.filter { it.startedAt in start until end }.sumOf { it.distanceM.toDouble() } / 1000.0
            label to (km * 10).toInt() / 10f
        }
        StatsUiState(
            weeklyWorkouts = weeklyWorkouts,
            weeklyKm = weeklyKm,
            vo2max = cardio.sortedBy { it.timestamp }.map { it.vo2max },
            mood = mood.sortedBy { it.timestamp }.takeLast(14).map { it.mood.toFloat() },
            totalWorkouts = sessions.size,
            totalKm = (gps.sumOf { it.distanceM.toDouble() } / 1000.0).toFloat(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )
}
