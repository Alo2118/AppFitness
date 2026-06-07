package com.appfitness.app.domain

import com.appfitness.app.data.model.FitnessLevel

/** Outcome of the initial fitness test. */
data class AssessmentOutcome(
    val score: Int,
    val level: FitnessLevel,
    val startingLoad: Float,
    val message: String,
)

/**
 * Turns raw initial-test inputs (max push-ups, max squats in 60s and plank hold)
 * into a recommended starting level and baseline adaptive load.
 */
object AssessmentEvaluator {

    fun evaluate(pushUps: Int, squats: Int, plankSec: Int): AssessmentOutcome {
        // Plank weighed lighter (it accumulates seconds quickly).
        val score = pushUps + squats + plankSec / 3

        val level = when {
            score < 40 -> FitnessLevel.PRINCIPIANTE
            score <= 85 -> FitnessLevel.INTERMEDIO
            else -> FitnessLevel.AVANZATO
        }
        val startingLoad = when (level) {
            FitnessLevel.PRINCIPIANTE -> 0.9f
            FitnessLevel.INTERMEDIO -> 1.0f
            FitnessLevel.AVANZATO -> 1.1f
        }
        val message = when (level) {
            FitnessLevel.PRINCIPIANTE -> "Ottimo punto di partenza! Inizieremo con un carico graduale."
            FitnessLevel.INTERMEDIO -> "Buona base: programmi a intensità media calibrati su di te."
            FitnessLevel.AVANZATO -> "Livello avanzato: partiamo con volumi e carichi più alti."
        }
        return AssessmentOutcome(score, level, startingLoad, message)
    }
}
