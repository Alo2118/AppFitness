package com.appfitness.app.domain

import com.appfitness.app.data.relation.SessionWithSets

/** New adaptive load plus a human-readable explanation. */
data class AdaptiveResult(
    val loadMultiplier: Float,
    val suggestion: String,
)

/**
 * Adaptive programming engine. After each completed session it adjusts the
 * program's load based on **set completion + post-workout energy** (the approach
 * chosen for AppFitness): finishing everything while still energised pushes the
 * load up (progressive overload), struggling or feeling drained pulls it back
 * down (deload / autoregulation).
 */
object AdaptiveEngine {

    private const val MIN_LOAD = 0.8f
    private const val MAX_LOAD = 1.5f

    fun recompute(currentLoad: Float, session: SessionWithSets): AdaptiveResult {
        val totalSets = session.sets.size
        val completionRate = if (totalSets == 0) 1f else session.sets.count { it.completed }.toFloat() / totalSets
        val energyAfter = session.session.energyAfter // 1..5 or null

        var delta = when {
            completionRate >= 0.95f -> 0.05f   // nailed it → push harder
            completionRate >= 0.85f -> 0.02f
            completionRate >= 0.60f -> -0.03f  // struggled a bit
            else -> -0.08f                     // clearly too hard → deload
        }

        // Energy feedback nudges the load further.
        when (energyAfter) {
            5 -> delta += 0.03f
            4 -> delta += 0.01f
            2 -> delta -= 0.03f
            1 -> delta -= 0.05f
        }

        val newLoad = (currentLoad + delta).coerceIn(MIN_LOAD, MAX_LOAD)
        return AdaptiveResult(newLoad, suggestionFor(newLoad, currentLoad, completionRate))
    }

    private fun suggestionFor(newLoad: Float, oldLoad: Float, completionRate: Float): String {
        val pct = (newLoad * 100).toInt()
        return when {
            newLoad > oldLoad + 0.001f ->
                "Ottimo lavoro (${(completionRate * 100).toInt()}% serie completate): aumento il carico a $pct%."
            newLoad < oldLoad - 0.001f ->
                "Sessione impegnativa: riduco il carico a $pct% per farti recuperare."
            else ->
                "Carico mantenuto al $pct%: stai progredendo con costanza."
        }
    }
}
