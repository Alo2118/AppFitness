package com.appfitness.app.domain

import com.appfitness.app.data.model.ExerciseCategory

/**
 * Tuning for [RepDetector] that depends on the exercise: big slow movements
 * (squat, stacco) have a larger acceleration swing and a longer cadence than
 * small fast ones (crunch) or explosive ones (burpee, jumping jack).
 */
data class RepProfile(
    val highThreshold: Float,
    val lowThreshold: Float,
    val minRepIntervalMs: Long,
)

object RepProfiles {

    /** Balanced default used when the exercise category is unknown. */
    val DEFAULT = RepProfile(highThreshold = 1.5f, lowThreshold = 0.6f, minRepIntervalMs = 400L)

    private val STRENGTH = RepProfile(highThreshold = 2.0f, lowThreshold = 0.8f, minRepIntervalMs = 600L)
    private val CORE = RepProfile(highThreshold = 1.2f, lowThreshold = 0.5f, minRepIntervalMs = 500L)
    private val EXPLOSIVE = RepProfile(highThreshold = 2.2f, lowThreshold = 0.9f, minRepIntervalMs = 300L)

    /** Picks a detection profile for the exercise's category. */
    fun forCategory(category: ExerciseCategory?): RepProfile = when (category) {
        ExerciseCategory.STRENGTH -> STRENGTH
        ExerciseCategory.CORE -> CORE
        ExerciseCategory.CARDIO, ExerciseCategory.FULL_BODY -> EXPLOSIVE
        ExerciseCategory.MOBILITY -> DEFAULT
        null -> DEFAULT
    }
}
