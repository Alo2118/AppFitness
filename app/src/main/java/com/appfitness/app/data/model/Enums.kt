package com.appfitness.app.data.model

/** High level grouping used to filter the exercise library. */
enum class ExerciseCategory(val label: String) {
    STRENGTH("Forza"),
    CARDIO("Cardio"),
    MOBILITY("Mobilità"),
    CORE("Core"),
    FULL_BODY("Total body");
}

/**
 * Emotional / energy scale used for mood tracking, from 1 (very low) to 5 (very
 * high). We store the raw [value] in the database and expose helpers for the UI.
 */
enum class MoodLevel(val value: Int, val emoji: String, val label: String) {
    VERY_LOW(1, "😫", "Molto giù"),
    LOW(2, "😕", "Giù"),
    NEUTRAL(3, "😐", "Neutro"),
    GOOD(4, "🙂", "Bene"),
    GREAT(5, "😄", "Alla grande");

    companion object {
        fun fromValue(value: Int): MoodLevel = entries.firstOrNull { it.value == value } ?: NEUTRAL
    }
}
