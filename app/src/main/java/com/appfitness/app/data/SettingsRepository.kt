package com.appfitness.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appfitness.app.data.model.Equipment
import com.appfitness.app.data.model.Goal
import com.appfitness.app.data.model.Sex
import com.appfitness.app.data.model.ThemeMode
import com.appfitness.app.data.model.UnitSystem
import com.appfitness.app.data.model.UserPreferences
import com.appfitness.app.domain.HeartRateZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

/** DataStore-backed store for the user profile and app preferences. */
class SettingsRepository(private val context: Context) {

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { it.toUserPreferences() }

    suspend fun setWeight(kg: Float) = edit { it[WEIGHT] = kg }
    suspend fun setHeight(cm: Int) = edit { it[HEIGHT] = cm }
    suspend fun setAge(age: Int) = edit { it[AGE] = age }
    suspend fun setSex(sex: Sex) = edit { it[SEX] = sex.name }
    suspend fun setGoal(goal: Goal) = edit { it[GOAL] = goal.name }
    suspend fun setUnit(unit: UnitSystem) = edit { it[UNIT] = unit.name }
    suspend fun setTheme(theme: ThemeMode) = edit { it[THEME] = theme.name }
    suspend fun setVoiceCoach(enabled: Boolean) = edit { it[VOICE] = enabled }
    suspend fun setTargetZone(zone: HeartRateZone) = edit { it[ZONE] = zone.name }
    suspend fun setEquipment(equipment: Set<Equipment>) =
        edit { it[EQUIPMENT] = equipment.map { e -> e.name }.toSet() }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private fun Preferences.toUserPreferences() = UserPreferences(
        weightKg = this[WEIGHT] ?: 70f,
        heightCm = this[HEIGHT] ?: 175,
        age = this[AGE] ?: 30,
        sex = enumOrDefault(this[SEX], Sex.UNSPECIFIED),
        goal = enumOrDefault(this[GOAL], Goal.FITNESS),
        unit = enumOrDefault(this[UNIT], UnitSystem.METRIC),
        theme = enumOrDefault(this[THEME], ThemeMode.SYSTEM),
        voiceCoach = this[VOICE] ?: true,
        targetZone = enumOrDefault(this[ZONE], HeartRateZone.AEROBICA),
        equipment = (this[EQUIPMENT] ?: emptySet()).mapNotNull {
            runCatching { Equipment.valueOf(it) }.getOrNull()
        }.toSet(),
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
        value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

    private companion object {
        val WEIGHT = floatPreferencesKey("weight")
        val HEIGHT = intPreferencesKey("height")
        val AGE = intPreferencesKey("age")
        val SEX = stringPreferencesKey("sex")
        val GOAL = stringPreferencesKey("goal")
        val UNIT = stringPreferencesKey("unit")
        val THEME = stringPreferencesKey("theme")
        val VOICE = booleanPreferencesKey("voice")
        val ZONE = stringPreferencesKey("zone")
        val EQUIPMENT = stringSetPreferencesKey("equipment")
    }
}
