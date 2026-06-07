package com.appfitness.app.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachingEngineTest {

    @Test
    fun `start phrase is not blank`() {
        assertTrue(CoachingEngine().startPhrase().isNotBlank())
    }

    @Test
    fun `final countdown announces remaining reps`() {
        val engine = CoachingEngine()
        val msg = engine.onUpdate(CoachingState(repCount = 8, targetReps = 10))
        assertNotNull(msg)
        assertTrue("should mention the 2 remaining reps", msg!!.contains("2"))
    }

    @Test
    fun `countdown cue is not repeated for the same remaining count`() {
        val engine = CoachingEngine()
        val first = engine.onUpdate(CoachingState(repCount = 9, targetReps = 10))
        val second = engine.onUpdate(CoachingState(repCount = 9, targetReps = 10))
        assertNotNull(first)
        assertNull(second)
    }

    @Test
    fun `completion is announced when target reached`() {
        val engine = CoachingEngine()
        val msg = engine.onUpdate(CoachingState(repCount = 10, targetReps = 10))
        assertNotNull(msg)
    }

    @Test
    fun `difficulty triggers an extra push when slowing down`() {
        val engine = CoachingEngine()
        val msg = engine.onUpdate(
            CoachingState(repCount = 4, targetReps = 12, cadenceRatio = 0.5f),
        )
        assertNotNull(msg)
        // Same state again should not repeat the same push cue.
        val again = engine.onUpdate(
            CoachingState(repCount = 4, targetReps = 12, cadenceRatio = 0.5f),
        )
        assertNull(again)
    }

    @Test
    fun `steady mid-set produces no chatter`() {
        val engine = CoachingEngine()
        val msg = engine.onUpdate(
            CoachingState(repCount = 4, targetReps = 12, cadenceRatio = 1f),
        )
        assertNull(msg)
    }

    @Test
    fun `high heart rate also triggers encouragement`() {
        val engine = CoachingEngine()
        val msg = engine.onUpdate(
            CoachingState(repCount = 6, targetReps = 12, heartRateElevated = true),
        )
        assertNotNull(msg)
    }
}
