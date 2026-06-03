package com.appfitness.app.domain

import com.appfitness.app.data.model.FitnessLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HrFitnessEvaluatorTest {

    @Test
    fun `lower resting hr yields higher vo2max`() {
        val fit = HrFitnessEvaluator.evaluate(age = 30, restingHr = 50, peakHr = 170, recoveryHr = 140)
        val unfit = HrFitnessEvaluator.evaluate(age = 30, restingHr = 80, peakHr = 170, recoveryHr = 150)
        assertTrue(fit.vo2max > unfit.vo2max)
    }

    @Test
    fun `hrr is peak minus recovery`() {
        val outcome = HrFitnessEvaluator.evaluate(age = 40, restingHr = 60, peakHr = 175, recoveryHr = 150)
        assertEquals(25, outcome.hrr)
    }

    @Test
    fun `high fitness maps to advanced level`() {
        val outcome = HrFitnessEvaluator.evaluate(age = 25, restingHr = 45, peakHr = 190, recoveryHr = 150)
        assertEquals(FitnessLevel.AVANZATO, outcome.level)
    }

    @Test
    fun `negative recovery difference is clamped to zero`() {
        val outcome = HrFitnessEvaluator.evaluate(age = 50, restingHr = 70, peakHr = 120, recoveryHr = 140)
        assertEquals(0, outcome.hrr)
    }

    @Test
    fun `handles out-of-range inputs without crashing`() {
        val outcome = HrFitnessEvaluator.evaluate(age = 0, restingHr = 0, peakHr = 0, recoveryHr = 0)
        assertTrue(outcome.vo2max > 0f)
        assertEquals(false, outcome.message.isBlank())
    }
}
