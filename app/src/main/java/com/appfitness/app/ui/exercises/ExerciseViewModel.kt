package com.appfitness.app.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.Exercise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseViewModel(private val repository: FitnessRepository) : ViewModel() {

    val exercises: StateFlow<List<Exercise>> = repository.exercises.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun save(exercise: Exercise) {
        viewModelScope.launch { repository.upsertExercise(exercise) }
    }

    fun deleteCustom(id: Long) {
        viewModelScope.launch { repository.deleteCustomExercise(id) }
    }
}
