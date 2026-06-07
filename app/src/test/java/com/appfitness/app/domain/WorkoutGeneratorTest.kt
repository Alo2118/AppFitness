package com.appfitness.app.domain

import com.appfitness.app.data.ExerciseSeed
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.WorkoutGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Assign stable ids to the seed library for deterministic tests. */
private fun library(): List<Exercise> =
    ExerciseSeed.exercises.mapIndexed { index, e -> e.copy(id = (index + 1).toLong()) }

class WorkoutGeneratorTest {

    @Test
    fun `generates more exercises for longer sessions`() {
        val lib = library()
        val short = WorkoutGenerator.generate(WorkoutGoal.TOTAL_BODY, FitnessLevel.INTERMEDIO, 20, null, lib, Random(1))
        val long = WorkoutGenerator.generate(WorkoutGoal.TOTAL_BODY, FitnessLevel.INTERMEDIO, 60, null, lib, Random(1))
        assertTrue("longer workout should have at least as many exercises", long.size >= short.size)
        assertTrue(short.isNotEmpty())
    }

    @Test
    fun `never repeats an exercise within a plan`() {
        val plan = WorkoutGenerator.generate(
            WorkoutGoal.DIMAGRIMENTO, FitnessLevel.AVANZATO, 60, 4, library(), Random(7),
        )
        val ids = plan.map { it.exercise.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `low energy reduces volume versus high energy`() {
        val lib = library()
        val tired = WorkoutGenerator.generate(WorkoutGoal.CARDIO, FitnessLevel.INTERMEDIO, 45, 1, lib, Random(3))
        val fresh = WorkoutGenerator.generate(WorkoutGoal.CARDIO, FitnessLevel.INTERMEDIO, 45, 5, lib, Random(3))
        assertTrue("low energy should not produce more exercises", tired.size <= fresh.size)
    }

    @Test
    fun `strength goal rests longer than fat-loss goal`() {
        val lib = library()
        val strength = WorkoutGenerator.generate(WorkoutGoal.FORZA, FitnessLevel.INTERMEDIO, 45, null, lib, Random(2))
        val fatLoss = WorkoutGenerator.generate(WorkoutGoal.DIMAGRIMENTO, FitnessLevel.INTERMEDIO, 45, null, lib, Random(2))
        val avgStrengthRest = strength.map { it.restSec }.average()
        val avgFatLossRest = fatLoss.map { it.restSec }.average()
        assertTrue(avgStrengthRest > avgFatLossRest)
    }

    @Test
    fun `every prescription is valid`() {
        val plan = WorkoutGenerator.generate(WorkoutGoal.TOTAL_BODY, FitnessLevel.PRINCIPIANTE, 30, 3, library(), Random(9))
        plan.forEach { item ->
            assertTrue(item.sets in 2..6)
            assertTrue(item.restSec >= 15)
            if (item.exercise.isTimeBased) {
                assertTrue(item.durationSec >= 15)
            } else {
                assertTrue(item.reps >= 5)
            }
        }
    }
}
