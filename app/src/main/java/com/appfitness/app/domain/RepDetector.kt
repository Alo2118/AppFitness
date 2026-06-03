package com.appfitness.app.domain

/**
 * Detects repetitions from a stream of accelerometer magnitude samples, assuming
 * the phone is worn on the wrist. Pure and stateful so it can be unit-tested with
 * synthetic signals.
 *
 * Approach: smooth the signal (fast EMA), track a slow baseline, and count a rep
 * each time the deviation from baseline rises above [highThreshold] and then
 * falls back below [lowThreshold] (hysteresis), respecting a minimum interval to
 * reject jitter and double-counts.
 */
class RepDetector(
    private val smoothing: Float = 0.3f,
    // Baseline must adapt slowly (posture drift) without flattening the rep
    // signal itself, so its time-constant spans several reps.
    private val baselineAlpha: Float = 0.005f,
    private val highThreshold: Float = 1.5f,
    private val lowThreshold: Float = 0.6f,
    private val minRepIntervalMs: Long = 400L,
) {
    private var ema = Float.NaN
    private var baseline = Float.NaN
    private var armed = true
    // 0 (not MIN_VALUE) so the first interval check can't overflow.
    private var lastRepTime = 0L

    var repCount = 0
        private set

    /** Feeds one sample; returns true if a repetition was just completed. */
    fun onSample(magnitude: Float, timestampMs: Long): Boolean {
        if (ema.isNaN()) {
            ema = magnitude
            baseline = magnitude
            return false
        }
        ema += smoothing * (magnitude - ema)
        baseline += baselineAlpha * (ema - baseline)
        val delta = ema - baseline

        if (armed && delta > highThreshold && timestampMs - lastRepTime >= minRepIntervalMs) {
            armed = false
            lastRepTime = timestampMs
            repCount++
            return true
        }
        if (!armed && delta < lowThreshold) {
            armed = true
        }
        return false
    }

    fun reset() {
        ema = Float.NaN
        baseline = Float.NaN
        armed = true
        lastRepTime = 0L
        repCount = 0
    }
}
