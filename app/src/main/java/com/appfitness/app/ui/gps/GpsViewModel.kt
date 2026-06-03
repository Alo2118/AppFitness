package com.appfitness.app.ui.gps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.GpsActivity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GpsViewModel(private val repository: FitnessRepository) : ViewModel() {

    val activities: StateFlow<List<GpsActivity>> = repository.gpsActivities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteGpsActivity(id) }
    }
}
