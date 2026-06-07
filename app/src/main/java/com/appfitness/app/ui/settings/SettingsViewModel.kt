package com.appfitness.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.SettingsRepository
import com.appfitness.app.data.model.Equipment
import com.appfitness.app.data.model.Goal
import com.appfitness.app.data.model.Sex
import com.appfitness.app.data.model.ThemeMode
import com.appfitness.app.data.model.UnitSystem
import com.appfitness.app.data.model.UserPreferences
import com.appfitness.app.domain.HeartRateZone
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val prefs: StateFlow<UserPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setWeight(kg: Float) = launch { repository.setWeight(kg.coerceIn(30f, 250f)) }
    fun setHeight(cm: Int) = launch { repository.setHeight(cm.coerceIn(120, 230)) }
    fun setAge(age: Int) = launch { repository.setAge(age.coerceIn(10, 100)) }
    fun setSex(sex: Sex) = launch { repository.setSex(sex) }
    fun setGoal(goal: Goal) = launch { repository.setGoal(goal) }
    fun setUnit(unit: UnitSystem) = launch { repository.setUnit(unit) }
    fun setTheme(theme: ThemeMode) = launch { repository.setTheme(theme) }
    fun setVoice(enabled: Boolean) = launch { repository.setVoiceCoach(enabled) }
    fun setTargetZone(zone: HeartRateZone) = launch { repository.setTargetZone(zone) }

    fun toggleEquipment(eq: Equipment) = launch {
        val current = prefs.value.equipment
        repository.setEquipment(if (eq in current) current - eq else current + eq)
    }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
