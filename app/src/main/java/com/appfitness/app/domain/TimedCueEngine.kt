package com.appfitness.app.domain

/**
 * Picks spoken cues for a time-based exercise (plank, wall-sit, …) as it counts
 * down. Pure and stateful so it can be unit-tested. Returns null when nothing
 * should be said on this tick; each cue fires at most once per set.
 */
class TimedCueEngine {

    private var started = false
    private var halfSpoken = false
    private val counted = mutableSetOf<Int>()

    /**
     * @param remaining seconds left (counts down to 0).
     * @param total the starting duration in seconds.
     */
    fun onTick(remaining: Int, total: Int): String? {
        if (!started) {
            started = true
            return "Via! Tieni la posizione"
        }
        if (remaining == 0) {
            if (counted.add(0)) return "Finito! Ottimo lavoro"
            return null
        }
        // Mid-point encouragement, but only for sets long enough to matter and
        // not so close to the final countdown that the two would overlap.
        if (!halfSpoken && total >= 16 && remaining <= total / 2 && remaining > 5) {
            halfSpoken = true
            return "Metà! Resisti"
        }
        // Final countdown.
        if (remaining in 1..5 && remaining < total && counted.add(remaining)) {
            return remaining.toString()
        }
        return null
    }

    fun reset() {
        started = false
        halfSpoken = false
        counted.clear()
    }
}
