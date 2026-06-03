package com.appfitness.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.entity.WorkoutSession
import com.appfitness.app.data.relation.SessionWithSets
import com.appfitness.app.domain.GeneratedExercise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentSessions: List<SessionWithSets> = emptyList(),
    val recentMood: List<MoodEntry> = emptyList(),
    val weekWorkouts: Int = 0,
    val weekMinutes: Int = 0,
)

class HomeViewModel(private val repository: FitnessRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(
            repository.completedSessions(),
            repository.recentMood(7),
        ) { sessions, mood ->
            val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            val thisWeek = sessions.filter { it.session.startedAt >= weekAgo }
            HomeUiState(
                recentSessions = sessions.take(5),
                recentMood = mood,
                weekWorkouts = thisWeek.size,
                weekMinutes = thisWeek.sumOf { it.session.totalDurationSec } / 60,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    /** Creates a new in-progress session and returns its id via [onCreated]. */
    fun startWorkout(title: String, moodBefore: Int?, energyBefore: Int?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.startSession(
                WorkoutSession(
                    title = title.ifBlank { "Allenamento" },
                    startedAt = System.currentTimeMillis(),
                    moodBefore = moodBefore,
                    energyBefore = energyBefore,
                )
            )
            onCreated(id)
        }
    }

    /** Persists a generated plan as a new in-progress session. */
    fun startGenerated(
        title: String,
        moodBefore: Int?,
        energyBefore: Int?,
        plan: List<GeneratedExercise>,
        onCreated: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val id = repository.startGeneratedSession(title, moodBefore, energyBefore, plan)
            onCreated(id)
        }
    }
}
