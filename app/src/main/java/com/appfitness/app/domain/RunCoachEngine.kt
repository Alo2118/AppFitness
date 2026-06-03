package com.appfitness.app.domain

import kotlin.math.roundToInt

/** Live snapshot of the run used to decide spoken coaching. */
data class RunState(
    val elapsedSec: Int,
    val distanceM: Double,
    val currentPaceSecPerKm: Double,
    val targetPaceSecPerKm: Double? = null,
    val ghostLeadM: Double? = null,
    val heartRateElevated: Boolean = false,
)

/**
 * Voice coaching for GPS runs/rides: periodic status against the objective
 * (ghost / target pace), pace-keeping nudges, and breathing guidance when it
 * detects difficulty (high heart rate, slipping well behind, or pace far slower
 * than target). Pure, deterministic and deduped so it never talks over itself.
 */
class RunCoachEngine {

    private val announced = mutableSetOf<String>()
    private var lastKm = 0

    /** Opening cue, optionally stating the target pace. */
    fun startPhrase(targetPaceSecPerKm: Double?): String =
        if (targetPaceSecPerKm != null && targetPaceSecPerKm > 0) {
            "Partenza! Obiettivo ritmo ${GeoUtils.formatPace(targetPaceSecPerKm)} al chilometro."
        } else {
            "Partenza! Buon allenamento."
        }

    fun onUpdate(state: RunState): String? {
        // 1) Kilometre milestone: status against the objective.
        val km = (state.distanceM / 1000.0).toInt()
        if (km >= 1 && km > lastKm) {
            lastKm = km
            val word = if (km == 1) "chilometro" else "chilometri"
            val pace = "Ritmo ${GeoUtils.formatPace(state.currentPaceSecPerKm)} al chilometro."
            val status = objectiveStatus(state)?.let { " $it" } ?: ""
            return "$km $word. $pace$status"
        }

        // 2) Breathing help when struggling (throttled ~ every 60 s).
        if (isStruggling(state)) {
            once("breath-${state.elapsedSec / 60}")?.let {
                return pick(BREATHING, state.elapsedSec / 60)
            }
        }

        // 3) Pace-keeping nudge (throttled ~ every 30 s).
        paceNudge(state)?.let { nudge ->
            once("pace-${state.elapsedSec / 30}")?.let { return nudge }
        }
        return null
    }

    fun reset() {
        announced.clear()
        lastKm = 0
    }

    /** Where the user stands relative to ghost or target pace. */
    private fun objectiveStatus(state: RunState): String? {
        state.ghostLeadM?.let { lead ->
            val m = kotlin.math.abs(lead).roundToInt()
            return if (lead >= 0) "Sei avanti di $m metri." else "Sei indietro di $m metri."
        }
        val target = state.targetPaceSecPerKm ?: return null
        val cur = state.currentPaceSecPerKm
        if (cur <= 0) return null
        return when {
            cur > target + PACE_TOLERANCE -> "Sei sotto ritmo, prova ad accelerare."
            cur < target - PACE_TOLERANCE -> "Sei sopra ritmo, ottimo."
            else -> "Sei in linea con l'obiettivo."
        }
    }

    private fun paceNudge(state: RunState): String? {
        state.ghostLeadM?.let { lead ->
            return when {
                lead < -GHOST_TOLERANCE_M -> "Stai perdendo terreno, spingi un po'!"
                lead > GHOST_TOLERANCE_M -> "Ottimo vantaggio, mantieni il ritmo."
                else -> null
            }
        }
        val target = state.targetPaceSecPerKm ?: return null
        val cur = state.currentPaceSecPerKm
        if (cur <= 0) return null
        return when {
            cur > target + PACE_TOLERANCE -> "Accelera leggermente per restare in ritmo."
            cur < target - PACE_TOLERANCE -> "Puoi rallentare un po', sei in anticipo."
            else -> null
        }
    }

    private fun isStruggling(state: RunState): Boolean {
        if (state.heartRateElevated) return true
        if (state.ghostLeadM != null && state.ghostLeadM < -STRUGGLE_BEHIND_M) return true
        val target = state.targetPaceSecPerKm
        return target != null && state.currentPaceSecPerKm > target * 1.15 && state.currentPaceSecPerKm > 0
    }

    private fun once(key: String): Unit? = if (announced.add(key)) Unit else null

    private fun pick(pool: List<String>, seed: Int): String = pool[seed % pool.size]

    companion object {
        private const val PACE_TOLERANCE = 8.0       // seconds per km
        private const val GHOST_TOLERANCE_M = 25.0
        private const val STRUGGLE_BEHIND_M = 60.0

        private val BREATHING = listOf(
            "Respira con calma: inspira per tre passi, espira per tre passi.",
            "Controlla il respiro, inspirazioni profonde e regolari col diaframma.",
            "Mantieni la calma: respiro lungo e costante, rilassa le spalle.",
        )
    }
}
