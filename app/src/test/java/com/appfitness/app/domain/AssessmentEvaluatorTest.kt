package com.appfitness.app.domain

import com.appfitness.app.data.model.FitnessLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentEvaluatorTest {

    @Test
    fun `low results map to beginner`() {
        val outcome = AssessmentEvaluator.evaluate(pushUps = 5, squats = 10, plankSec = 20)
        assertEquals(FitnessLevel.PRINCIPIANTE, outcome.level)
        assertTrue(outcome.startingLoad < 1.0f)
    }

    @Test
    fun `mid results map to intermediate`() {
        val outcome = AssessmentEvaluator.evaluate(pushUps = 20, squats = 30, plankSec = 45)
        assertEquals(FitnessLevel.INTERMEDIO, outcome.level)
        assertEquals(1.0f, outcome.startingLoad)
    }

    @Test
    fun `high results map to advanced`() {
        val outcome = AssessmentEvaluator.evaluate(pushUps = 45, squats = 60, plankSec = 120)
        assertEquals(FitnessLevel.AVANZATO, outcome.level)
        assertTrue(outcome.startingLoad > 1.0f)
    }

    @Test
    fun `score increases with better results`() {
        val weak = AssessmentEvaluator.evaluate(5, 5, 10).score
        val strong = AssessmentEvaluator.evaluate(40, 40, 90).score
        assertTrue(strong > weak)
    }
}
