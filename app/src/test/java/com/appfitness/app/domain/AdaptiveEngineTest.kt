package com.appfitness.app.domain

import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.WorkoutSession
import com.appfitness.app.data.relation.SessionWithSets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun session(
    completed: Int,
    total: Int,
    energyAfter: Int?,
): SessionWithSets {
    val sets = (1..total).map {
        SetLog(
            id = it.toLong(),
            sessionId = 1,
            exerciseId = 1,
            exerciseName = "Test",
            setNumber = it,
            reps = 10,
            completed = it <= completed,
        )
    }
    return SessionWithSets(
        session = WorkoutSession(id = 1, title = "T", startedAt = 0, energyAfter = energyAfter),
        sets = sets,
    )
}

class AdaptiveEngineTest {

    @Test
    fun `full completion with high energy increases load`() {
        val result = AdaptiveEngine.recompute(1.0f, session(completed = 10, total = 10, energyAfter = 5))
        assertTrue("load should increase", result.loadMultiplier > 1.0f)
    }

    @Test
    fun `poor completion decreases load`() {
        val result = AdaptiveEngine.recompute(1.0f, session(completed = 4, total = 10, energyAfter = 2))
        assertTrue("load should decrease", result.loadMultiplier < 1.0f)
    }

    @Test
    fun `load stays within bounds`() {
        val high = AdaptiveEngine.recompute(1.5f, session(10, 10, 5))
        val low = AdaptiveEngine.recompute(0.8f, session(0, 10, 1))
        assertTrue(high.loadMultiplier <= 1.5f)
        assertTrue(low.loadMultiplier >= 0.8f)
    }

    @Test
    fun `empty session is treated as fully completed`() {
        val result = AdaptiveEngine.recompute(1.0f, session(0, 0, 3))
        assertTrue(result.loadMultiplier >= 1.0f)
    }

    @Test
    fun `suggestion is never blank`() {
        val result = AdaptiveEngine.recompute(1.0f, session(9, 10, 4))
        assertEquals(false, result.suggestion.isBlank())
    }
}
