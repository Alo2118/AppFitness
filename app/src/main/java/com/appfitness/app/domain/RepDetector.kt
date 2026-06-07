package com.appfitness.app.domain

import kotlin.math.sqrt

/**
 * Detects repetitions from a stream of accelerometer magnitude samples, assuming
 * the phone is worn on the wrist. Pure and stateful so it can be unit-tested with
 * synthetic signals.
 *
 * Approach (robust against jitter / "every small movement" false counts):
 *  1. Smooth the signal with a fast EMA to kill high-frequency noise.
 *  2. Track a slow running mean and standard deviation of the smoothed signal,
 *     so the trigger threshold self-calibrates to the movement's own intensity.
 *  3. Count a rep only on a *full* up-and-down cycle: the signal must rise above
 *     an adaptive high threshold (`mean + k·std`, never below an absolute
 *     [minProminence] floor), reach a peak, and fall back below a release level.
 *     A rep is registered on the falling edge only if the peak's prominence
 *     (peak − mean) clears [minProminence] and the minimum interval has elapsed.
 *
 * Counting on the falling edge of a prominent peak — rather than the instant the
 * signal first crosses a fixed threshold — is what rejects small repeated
 * movements: a tiny wobble never reaches the adaptive peak height, and a single
 * spike that doesn't complete a cycle is never counted.
 */
class RepDetector(
    private val smoothing: Float = 0.3f,
    // Mean/std adapt slowly so brief reps don't wash out the baseline.
    private val statsAlpha: Float = 0.02f,
    // Peak must exceed mean by this many standard deviations…
    private val thresholdK: Float = 1.2f,
    // …but always by at least this absolute amount (m/s²), to reject tiny moves.
    private val minProminence: Float = 2.0f,
    // Re-arm once the signal falls back to this fraction of the trigger height.
    private val releaseFraction: Float = 0.5f,
    private val minRepIntervalMs: Long = 400L,
    // Let the stats settle before the first count can fire.
    private val warmupSamples: Int = 8,
) {
    private var ema = Float.NaN
    private var mean = Float.NaN
    private var varEma = 0f
    private var samples = 0
    private var rising = false
    private var peak = 0f
    // 0 (not MIN_VALUE) so the first interval check can't overflow.
    private var lastRepTime = 0L

    var repCount = 0
        private set

    /** Feeds one sample; returns true if a repetition was just completed. */
    fun onSample(magnitude: Float, timestampMs: Long): Boolean {
        if (ema.isNaN()) {
            ema = magnitude
            mean = magnitude
            varEma = 0f
            samples = 1
            return false
        }
        ema += smoothing * (magnitude - ema)
        samples++

        // Adaptive trigger geometry from the running statistics.
        val std = sqrt(varEma.coerceAtLeast(0f))
        val height = maxOf(thresholdK * std, minProminence)
        val high = mean + height
        val low = mean + releaseFraction * height

        if (!rising) {
            // Keep the baseline statistics tracking the "quiet" signal.
            mean += statsAlpha * (ema - mean)
            val d = ema - mean
            varEma += statsAlpha * (d * d - varEma)

            if (samples > warmupSamples && ema > high) {
                rising = true
                peak = ema
            }
        } else {
            if (ema > peak) peak = ema
            if (ema < low) {
                rising = false
                val prominence = peak - mean
                if (prominence >= minProminence &&
                    timestampMs - lastRepTime >= minRepIntervalMs
                ) {
                    lastRepTime = timestampMs
                    repCount++
                    return true
                }
            }
        }
        return false
    }

    fun reset() {
        ema = Float.NaN
        mean = Float.NaN
        varEma = 0f
        samples = 0
        rising = false
        peak = 0f
        lastRepTime = 0L
        repCount = 0
    }
}
