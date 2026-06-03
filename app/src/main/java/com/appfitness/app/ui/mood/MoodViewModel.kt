package com.appfitness.app.ui.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.MoodEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoodViewModel(private val repository: FitnessRepository) : ViewModel() {

    val entries: StateFlow<List<MoodEntry>> = repository.moodEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addEntry(mood: Int, energy: Int, tags: String, note: String) {
        viewModelScope.launch {
            repository.addMood(
                MoodEntry(
                    timestamp = System.currentTimeMillis(),
                    mood = mood,
                    energy = energy,
                    tags = tags,
                    note = note,
                )
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteMood(id) }
    }
}
