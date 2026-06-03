package com.appfitness.app.domain

import com.appfitness.app.data.model.FitnessLevel
import kotlin.math.roundToInt

/** Result of the heart-rate based fitness assessment. */
data class CardioFitnessOutcome(
    val vo2max: Float,
    val hrr: Int,
    val level: FitnessLevel,
    val category: String,
    val message: String,
)

/**
 * Estimates cardiovascular fitness from heart-rate data collected with a
 * connected monitor during a guided rest → effort → recovery protocol.
 *
 * - **VO₂max** via the resting-HR method: `15.3 × (HRmax / HRrest)`,
 *   with `HRmax = 220 − age` (Uth–Sørensen–Overgaard–Pedersen).
 * - **HRR** (heart-rate recovery) = peak HR − HR one minute after stopping;
 *   a larger drop indicates better cardiovascular fitness.
 */
object HrFitnessEvaluator {

    fun evaluate(age: Int, restingHr: Int, peakHr: Int, recoveryHr: Int): CardioFitnessOutcome {
        val safeAge = age.coerceIn(10, 100)
        val safeResting = restingHr.coerceIn(30, 120)
        val hrMax = (220 - safeAge).coerceAtLeast(120)

        val vo2max = (15.3f * hrMax / safeResting)
        val vo2maxRounded = (vo2max * 10).roundToInt() / 10f
        val hrr = (peakHr - recoveryHr).coerceAtLeast(0)

        val level = when {
            vo2max < 35f -> FitnessLevel.PRINCIPIANTE
            vo2max <= 45f -> FitnessLevel.INTERMEDIO
            else -> FitnessLevel.AVANZATO
        }
        val category = when {
            vo2max < 30f -> "Da migliorare"
            vo2max < 40f -> "Sufficiente"
            vo2max < 50f -> "Buona"
            else -> "Ottima"
        }
        val recoveryNote = when {
            hrr >= 25 -> "recupero cardiaco eccellente"
            hrr >= 12 -> "recupero cardiaco nella norma"
            else -> "recupero cardiaco lento: lavora sulla resistenza"
        }
        val message = "VO₂max stimato $vo2maxRounded ml/kg/min ($category) · HRR $hrr bpm — $recoveryNote."

        return CardioFitnessOutcome(vo2maxRounded, hrr, level, category, message)
    }
}
