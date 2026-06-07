package com.appfitness.app.domain

/** Snapshot of the live set used to decide coaching feedback. */
data class CoachingState(
    val repCount: Int,
    val targetReps: Int,
    /** current cadence / initial cadence; < 1 means the user is slowing down. */
    val cadenceRatio: Float = 1f,
    val heartRateElevated: Boolean = false,
    val finished: Boolean = false,
)

/**
 * Decides *when* and *what* to say during a guided set: encouragement, rep
 * milestones, a final countdown ("ancora 2 ripetizioni") and extra push when it
 * detects difficulty (cadence dropping or heart rate high). Stateful to avoid
 * repeating the same cue; deterministic so it can be unit-tested.
 */
class CoachingEngine {

    private val announced = mutableSetOf<String>()

    /** Phrase for the very start of the set. */
    fun startPhrase(): String {
        announced.add("start")
        return "Iniziamo, dai il massimo!"
    }

    /** Returns a phrase to speak for this update, or null if nothing new to say. */
    fun onUpdate(state: CoachingState): String? {
        val remaining = (state.targetReps - state.repCount).coerceAtLeast(0)

        if (state.finished || (state.targetReps > 0 && state.repCount >= state.targetReps)) {
            return once("done", "Serie completata! Ottimo lavoro.")
        }

        // Final countdown — the requested "forza, ancora N ripetizioni".
        if (remaining in 1..FINAL_COUNTDOWN) {
            val word = if (remaining == 1) "ripetizione" else "ripetizioni"
            once("left-$remaining", "Forza, ancora $remaining $word!")?.let { return it }
        }

        // Difficulty help: fires once per remaining value while struggling.
        val struggling = state.cadenceRatio < SLOWING_RATIO || state.heartRateElevated
        if (struggling && remaining > 0) {
            once("push-$remaining", pick(PUSH_PHRASES, state.repCount).format(remaining))?.let { return it }
        }

        // Periodic milestone every few reps.
        if (state.repCount > 0 && state.repCount % MILESTONE_EVERY == 0) {
            once("milestone-${state.repCount}", pick(MILESTONE_PHRASES, state.repCount).format(state.repCount))
                ?.let { return it }
        }
        return null
    }

    fun reset() = announced.clear()

    /** Returns [phrase] the first time [key] is seen, otherwise null. */
    private fun once(key: String, phrase: String): String? =
        if (announced.add(key)) phrase else null

    private fun pick(pool: List<String>, seed: Int): String = pool[seed % pool.size]

    companion object {
        private const val FINAL_COUNTDOWN = 3
        private const val MILESTONE_EVERY = 5
        private const val SLOWING_RATIO = 0.6f

        private val PUSH_PHRASES = listOf(
            "Tieni duro, ce la fai! Ancora %d.",
            "Non mollare, ultime %d!",
            "Spingi, mancano solo %d!",
        )
        private val MILESTONE_PHRASES = listOf(
            "Grande, %d fatte! Continua così.",
            "Ottimo ritmo, %d completate!",
        )
    }
}
