package com.appfitness.app.domain

import com.appfitness.app.data.model.GpsActivityType
import kotlin.math.abs
import kotlin.math.roundToInt

/** Live snapshot of the run/ride used to decide spoken coaching. */
data class RunState(
    val activityType: GpsActivityType,
    val elapsedSec: Int,
    val distanceM: Double,
    val currentPaceSecPerKm: Double,
    val targetPaceSecPerKm: Double? = null,
    val ghostLeadM: Double? = null,
    val heartRate: Int? = null,
    val age: Int = 30,
    val targetZone: HeartRateZone = HeartRateZone.AEROBICA,
)

/**
 * Voice coaching for GPS runs and rides, calibrated to how the user is actually
 * performing. It **intervenes** when something needs attention — heart rate out
 * of the target zone, falling well behind the objective, or losing the target
 * pace — and otherwise just gives a routine status **every kilometre**. The
 * language and units adapt to the activity (pace for running, speed for cycling).
 *
 * Pure, deterministic and deduped so it never talks over itself.
 */
class RunCoachEngine {

    private val announced = mutableSetOf<String>()
    private var lastKm = 0
    private var lastZoneStatus: ZoneStatus? = null

    /** Opening cue, optionally stating the target pace. */
    fun startPhrase(activityType: GpsActivityType, targetPaceSecPerKm: Double?): String {
        val target = if (targetPaceSecPerKm != null && targetPaceSecPerKm > 0) {
            " Obiettivo ritmo ${GeoUtils.formatPace(targetPaceSecPerKm)} al chilometro."
        } else ""
        val opener = if (activityType == GpsActivityType.BIKE) "Partenza! Buona pedalata." else "Partenza! Buona corsa."
        return opener + target
    }

    /**
     * Returns the next cue to speak, or null. Problems are checked first (more
     * responsive), the per-kilometre status only when everything is fine.
     */
    fun onUpdate(state: RunState): String? {
        heartRateCue(state)?.let { return it }       // HR out of zone (high HR -> breathing)
        performanceCue(state)?.let { return it }     // far from objective / losing pace
        return kilometreCue(state)                    // all good -> status each km
    }

    fun reset() {
        announced.clear()
        lastKm = 0
        lastZoneStatus = null
    }

    // --- Heart-rate zone ---

    private fun heartRateCue(state: RunState): String? {
        val hr = state.heartRate ?: return null
        val zone = HeartRateZoneEvaluator.zoneFor(hr, state.age)
        val status = when {
            zone.ordinal < state.targetZone.ordinal -> ZoneStatus.BELOW
            zone.ordinal > state.targetZone.ordinal -> ZoneStatus.ABOVE
            else -> ZoneStatus.IN_ZONE
        }
        val previous = lastZoneStatus
        val changed = status != previous
        lastZoneStatus = status

        return when (status) {
            ZoneStatus.IN_ZONE -> {
                val wasOutOfZone = previous == ZoneStatus.BELOW || previous == ZoneStatus.ABOVE
                if (wasOutOfZone) "Sei rientrato in zona ${state.targetZone.label}." else null
            }
            else -> {
                // Alert on change, and remind periodically while still out of zone.
                val periodicNew = announced.add("hr-$status-${state.elapsedSec / 45}")
                if (!changed && !periodicNew) return null
                if (status == ZoneStatus.BELOW) {
                    "Battito basso, sei sotto la zona ${state.targetZone.label}: aumenta l'intensità."
                } else {
                    // High HR = real difficulty -> breathing guidance.
                    "Battito alto, fuori zona. " + pick(breathingPool(state.activityType), state.elapsedSec / 45)
                }
            }
        }
    }

    // --- Pace / objective ---

    private fun performanceCue(state: RunState): String? {
        state.ghostLeadM?.let { lead ->
            val behind = (-lead).roundToInt()
            return when {
                lead <= -FAR_BEHIND_M ->
                    throttled("gapfar-${state.elapsedSec / 25}", "Sei molto indietro, $behind metri: ${push(state.activityType)}")
                lead <= -MODERATE_BEHIND_M ->
                    throttled("gap-${state.elapsedSec / 30}", "Stai perdendo terreno, $behind metri dietro.")
                else -> null
            }
        }
        val target = state.targetPaceSecPerKm ?: return null
        val cur = state.currentPaceSecPerKm
        if (cur <= 0) return null
        val diff = cur - target
        return when {
            diff >= FAR_PACE -> throttled("pacefar-${state.elapsedSec / 25}", "Sei molto sotto ritmo: ${accelerate(state.activityType)}")
            diff >= PACE_TOLERANCE -> throttled("pace-${state.elapsedSec / 30}", accelerate(state.activityType))
            diff <= -PACE_TOLERANCE -> throttled("ease-${state.elapsedSec / 30}", ease(state.activityType))
            else -> null
        }
    }

    // --- Routine per-kilometre status ---

    private fun kilometreCue(state: RunState): String? {
        val km = (state.distanceM / 1000.0).toInt()
        if (km < 1 || km <= lastKm) return null
        lastKm = km
        val word = if (km == 1) "chilometro" else "chilometri"
        val speed = speedReadout(state)
        val status = objectiveStatus(state)?.let { " $it" } ?: ""
        return "$km $word. $speed$status"
    }

    private fun speedReadout(state: RunState): String =
        if (state.activityType == GpsActivityType.BIKE) {
            val kmh = if (state.currentPaceSecPerKm > 0) 3600.0 / state.currentPaceSecPerKm else 0.0
            "Velocità ${"%.1f".format(kmh)} chilometri orari."
        } else {
            "Ritmo ${GeoUtils.formatPace(state.currentPaceSecPerKm)} al chilometro."
        }

    private fun objectiveStatus(state: RunState): String? {
        state.ghostLeadM?.let { lead ->
            val m = abs(lead).roundToInt()
            return if (lead >= 0) "Sei avanti di $m metri." else "Sei indietro di $m metri."
        }
        val target = state.targetPaceSecPerKm ?: return null
        val cur = state.currentPaceSecPerKm
        if (cur <= 0) return null
        return when {
            cur > target + PACE_TOLERANCE -> "Sei sotto ritmo."
            cur < target - PACE_TOLERANCE -> "Sei sopra ritmo, ottimo."
            else -> "Sei in linea con l'obiettivo."
        }
    }

    // --- Helpers ---

    private fun throttled(key: String, phrase: String): String? =
        if (announced.add(key)) phrase else null

    private fun pick(pool: List<String>, seed: Int): String = pool[seed % pool.size]

    private fun accelerate(type: GpsActivityType): String =
        if (type == GpsActivityType.BIKE) "spingi sui pedali per restare in ritmo."
        else "accelera leggermente per restare in ritmo."

    private fun ease(type: GpsActivityType): String =
        if (type == GpsActivityType.BIKE) "Puoi alleggerire la pedalata, sei in anticipo."
        else "Puoi rallentare un po', sei in anticipo."

    private fun push(type: GpsActivityType): String =
        if (type == GpsActivityType.BIKE) "aumenta la cadenza!" else "spingi il passo!"

    private fun breathingPool(type: GpsActivityType): List<String> =
        if (type == GpsActivityType.BIKE) BIKE_RECOVER else RUN_BREATHING

    companion object {
        private const val PACE_TOLERANCE = 8.0   // seconds per km
        private const val FAR_PACE = 20.0
        private const val MODERATE_BEHIND_M = 25.0
        private const val FAR_BEHIND_M = 60.0

        private val RUN_BREATHING = listOf(
            "Respira con calma: inspira per tre passi, espira per tre passi.",
            "Controlla il respiro, inspirazioni profonde e regolari col diaframma.",
            "Mantieni la calma: respiro lungo e costante, rilassa le spalle.",
        )
        private val BIKE_RECOVER = listOf(
            "Respira profondamente e mantieni una pedalata fluida e costante.",
            "Sciogli le gambe, respiro regolare e busto rilassato.",
            "Allenta la presa sul manubrio e respira con calma.",
        )
    }
}
