package com.appfitness.app.data.model

import com.appfitness.app.domain.HeartRateZone

enum class ThemeMode(val label: String) {
    SYSTEM("Sistema"), LIGHT("Chiaro"), DARK("Scuro"), DYNAMIC("Material You");
}

enum class UnitSystem(val label: String) { METRIC("Metrico (km)"), IMPERIAL("Imperiale (mi)") }

enum class Sex(val label: String) { MALE("Uomo"), FEMALE("Donna"), UNSPECIFIED("Non specificato") }

enum class Goal(val label: String) {
    LOSE_WEIGHT("Dimagrire"), MAINTAIN("Mantenere"), BUILD_MUSCLE("Massa"), FITNESS("Forma fisica");
}

/** All user profile + app preferences, persisted via DataStore. */
data class UserPreferences(
    val weightKg: Float = 70f,
    val heightCm: Int = 175,
    val age: Int = 30,
    val sex: Sex = Sex.UNSPECIFIED,
    val goal: Goal = Goal.FITNESS,
    val unit: UnitSystem = UnitSystem.METRIC,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val voiceCoach: Boolean = true,
    val targetZone: HeartRateZone = HeartRateZone.AEROBICA,
    /** Equipment the user owns; empty means "show everything". */
    val equipment: Set<Equipment> = emptySet(),
)
