package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class RepDetectorTest {

    /** Feeds a sine wave (one rep per cycle) and checks the count is close. */
    private fun countSineReps(cycles: Int, freqHz: Double, amplitude: Float): Int {
        val detector = RepDetector()
        val dtMs = 20L
        val samplesPerCycle = (1000.0 / freqHz / dtMs).toInt()
        val totalSamples = samplesPerCycle * cycles
        var t = 0L
        repeat(totalSamples) { i ->
            val phase = 2 * PI * freqHz * (i * dtMs) / 1000.0
            val magnitude = 9.8f + amplitude * sin(phase).toFloat()
            detector.onSample(magnitude, t)
            t += dtMs
        }
        return detector.repCount
    }

    @Test
    fun `counts roughly one rep per cycle`() {
        val reps = countSineReps(cycles = 10, freqHz = 0.5, amplitude = 3f)
        assertTrue("expected ~10 reps, got $reps", reps in 8..11)
    }

    @Test
    fun `flat signal produces no reps`() {
        val detector = RepDetector()
        var t = 0L
        repeat(500) { detector.onSample(9.8f, t); t += 20 }
        assertEquals(0, detector.repCount)
    }

    @Test
    fun `reset clears the count`() {
        val detector = RepDetector()
        var t = 0L
        repeat(300) { i ->
            detector.onSample(9.8f + 3f * sin(2 * PI * 0.5 * (i * 20) / 1000.0).toFloat(), t)
            t += 20
        }
        assertTrue(detector.repCount > 0)
        detector.reset()
        assertEquals(0, detector.repCount)
    }

    @Test
    fun `small jitter below threshold is ignored`() {
        val reps = countSineReps(cycles = 10, freqHz = 0.5, amplitude = 0.3f)
        assertEquals(0, reps)
    }
}
