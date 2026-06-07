package com.appfitness.app.domain

import com.appfitness.app.data.model.ExerciseCategory

/**
 * Tuning for [RepDetector] that depends on the exercise: big slow movements
 * (squat, stacco) have a larger acceleration swing and a longer cadence than
 * small fast ones (crunch) or explosive ones (burpee, jumping jack).
 *
 * [minProminence] is the absolute peak height (m/s²) a movement must reach to be
 * counted, and [thresholdK] scales the adaptive part on top of the measured
 * noise — larger values reject more borderline movements.
 */
data class RepProfile(
    val minProminence: Float,
    val thresholdK: Float,
    val minRepIntervalMs: Long,
)

object RepProfiles {

    /** Balanced default used when the exercise category is unknown. */
    val DEFAULT = RepProfile(minProminence = 2.0f, thresholdK = 1.2f, minRepIntervalMs = 400L)

    // Big, slow lifts: large swing, generous spacing, strict floor.
    private val STRENGTH = RepProfile(minProminence = 2.6f, thresholdK = 1.2f, minRepIntervalMs = 600L)
    // Small core movements: lower swing, but a stricter k to reject wobble.
    private val CORE = RepProfile(minProminence = 1.6f, thresholdK = 1.6f, minRepIntervalMs = 500L)
    // Explosive moves: big swing, fastest cadence allowed.
    private val EXPLOSIVE = RepProfile(minProminence = 3.0f, thresholdK = 1.1f, minRepIntervalMs = 300L)

    /** Picks a detection profile for the exercise's category. */
    fun forCategory(category: ExerciseCategory?): RepProfile = when (category) {
        ExerciseCategory.STRENGTH -> STRENGTH
        ExerciseCategory.CORE -> CORE
        ExerciseCategory.CARDIO, ExerciseCategory.FULL_BODY -> EXPLOSIVE
        ExerciseCategory.MOBILITY -> DEFAULT
        null -> DEFAULT
    }
}
