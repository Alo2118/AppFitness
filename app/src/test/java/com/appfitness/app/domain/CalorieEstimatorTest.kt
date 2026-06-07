package com.appfitness.app.domain

import com.appfitness.app.data.model.GpsActivityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieEstimatorTest {

    @Test
    fun `more distance burns more calories`() {
        val short = CalorieEstimator.forGps(GpsActivityType.RUN, 5.0, 1800, 70f)
        val long = CalorieEstimator.forGps(GpsActivityType.RUN, 10.0, 3600, 70f)
        assertTrue(long > short)
        assertTrue(short > 0)
    }

    @Test
    fun `heavier runner burns more`() {
        val light = CalorieEstimator.forGps(GpsActivityType.RUN, 5.0, 1800, 60f)
        val heavy = CalorieEstimator.forGps(GpsActivityType.RUN, 5.0, 1800, 90f)
        assertTrue(heavy > light)
    }

    @Test
    fun `zero duration or weight yields zero`() {
        assertEquals(0, CalorieEstimator.forGps(GpsActivityType.RUN, 5.0, 0, 70f))
        assertEquals(0, CalorieEstimator.forWorkout(0, 70f))
        assertEquals(0, CalorieEstimator.forWorkout(1800, 0f))
    }

    @Test
    fun `workout calories grow with duration`() {
        val a = CalorieEstimator.forWorkout(1800, 70f)
        val b = CalorieEstimator.forWorkout(3600, 70f)
        assertTrue(b > a && a > 0)
    }
}
