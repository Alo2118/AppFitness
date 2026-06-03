package com.appfitness.app.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.AssessmentResult
import com.appfitness.app.data.entity.CardioAssessment
import com.appfitness.app.data.entity.TrainingProgram
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.Sport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgramsViewModel(private val repository: FitnessRepository) : ViewModel() {

    val programs: StateFlow<List<TrainingProgram>> = repository.programs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val latestAssessment: StateFlow<AssessmentResult?> = repository.latestAssessment.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val latestCardio: StateFlow<CardioAssessment?> = repository.latestCardioAssessment.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun saveAssessment(pushUps: Int, squats: Int, plankSec: Int) {
        viewModelScope.launch { repository.saveAssessment(pushUps, squats, plankSec) }
    }

    fun createProgram(sport: Sport, level: FitnessLevel, weeks: Int, sessionsPerWeek: Int) {
        viewModelScope.launch { repository.createProgram(sport, level, weeks, sessionsPerWeek) }
    }

    fun deleteProgram(id: Long) {
        viewModelScope.launch { repository.deleteProgram(id) }
    }

    /** Generates and starts the program's next session, returning its id. */
    fun startNextSession(programId: Long, energy: Int?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            repository.startProgramSession(programId, energy)?.let(onCreated)
        }
    }
}
