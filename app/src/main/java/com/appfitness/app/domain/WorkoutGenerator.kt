package com.appfitness.app.domain

import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.model.ExerciseCategory
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.WorkoutGoal
import kotlin.random.Random

/** One exercise of a generated plan, with concrete prescription. */
data class GeneratedExercise(
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val durationSec: Int,
    val restSec: Int,
) {
    /** Rough time cost in seconds: work + rest across all sets. */
    val estimatedSeconds: Int
        get() {
            val workPerSet = if (exercise.isTimeBased) durationSec else reps * 3 // ~3s per rep
            return sets * (workPerSet + restSec)
        }
}

/**
 * Builds a complete, ready-to-start workout from the exercise library based on
 * goal, level, available time and — uniquely for AppFitness — the user's current
 * energy. Pure and deterministic given a [random], so it is easy to preview and
 * to unit-test.
 */
object WorkoutGenerator {

    /** Ordered category pattern cycled through when picking exercises per goal. */
    private fun pattern(goal: WorkoutGoal): List<ExerciseCategory> = when (goal) {
        WorkoutGoal.FORZA -> listOf(
            ExerciseCategory.STRENGTH, ExerciseCategory.STRENGTH,
            ExerciseCategory.CORE, ExerciseCategory.FULL_BODY,
        )
        WorkoutGoal.CARDIO -> listOf(
            ExerciseCategory.CARDIO, ExerciseCategory.FULL_BODY,
            ExerciseCategory.CARDIO, ExerciseCategory.CORE,
        )
        WorkoutGoal.DIMAGRIMENTO -> listOf(
            ExerciseCategory.CARDIO, ExerciseCategory.STRENGTH,
            ExerciseCategory.FULL_BODY, ExerciseCategory.CORE,
        )
        WorkoutGoal.MOBILITA -> listOf(
            ExerciseCategory.MOBILITY, ExerciseCategory.CORE,
            ExerciseCategory.MOBILITY, ExerciseCategory.MOBILITY,
        )
        WorkoutGoal.TOTAL_BODY -> listOf(
            ExerciseCategory.STRENGTH, ExerciseCategory.CARDIO,
            ExerciseCategory.CORE, ExerciseCategory.FULL_BODY, ExerciseCategory.MOBILITY,
        )
    }

    fun generate(
        goal: WorkoutGoal,
        level: FitnessLevel,
        durationMin: Int,
        energy: Int?,
        library: List<Exercise>,
        random: Random = Random.Default,
    ): List<GeneratedExercise> {
        if (library.isEmpty()) return emptyList()

        // How many exercises fit the requested duration (~5 min each), nudged by energy.
        val energyAdj = when (energy) {
            null -> 0
            in 1..2 -> -1
            5 -> 1
            else -> 0
        }
        val count = (durationMin / 5 + energyAdj).coerceIn(3, 8)

        val chosen = pickExercises(goal, count, library, random)
        return chosen.map { prescribe(it, goal, level, energy) }
    }

    private fun pickExercises(
        goal: WorkoutGoal,
        count: Int,
        library: List<Exercise>,
        random: Random,
    ): List<Exercise> {
        val pattern = pattern(goal)
        val pools = library.groupBy { it.category }
            .mapValues { (_, list) -> list.shuffled(random).toMutableList() }
        val remaining = library.shuffled(random).toMutableList()
        val result = mutableListOf<Exercise>()

        var i = 0
        while (result.size < count && (remaining.isNotEmpty() || pools.any { it.value.isNotEmpty() })) {
            val category = pattern[i % pattern.size]
            val fromCategory = pools[category]?.removeFirstOrNull()
            val pick = fromCategory ?: remaining.removeFirstOrNull()
            if (pick != null && result.none { it.id == pick.id }) {
                result.add(pick)
                remaining.removeAll { it.id == pick.id }
            }
            i++
            if (i > count * pattern.size + library.size) break // safety
        }
        return result
    }

    private fun prescribe(
        exercise: Exercise,
        goal: WorkoutGoal,
        level: FitnessLevel,
        energy: Int?,
    ): GeneratedExercise {
        val setDelta = when (level) {
            FitnessLevel.PRINCIPIANTE -> -1
            FitnessLevel.INTERMEDIO -> 0
            FitnessLevel.AVANZATO -> 1
        }
        val sets = (exercise.defaultSets + setDelta).coerceIn(2, 6)

        val lowEnergy = energy != null && energy <= 2
        val intensity = if (lowEnergy) 0.8f else 1f

        // Goal-driven rep/duration tuning.
        val reps = if (exercise.isTimeBased) 0 else run {
            val goalDelta = when (goal) {
                WorkoutGoal.FORZA -> -2
                WorkoutGoal.CARDIO, WorkoutGoal.DIMAGRIMENTO -> 3
                else -> 0
            }
            val levelDelta = when (level) {
                FitnessLevel.PRINCIPIANTE -> -2
                FitnessLevel.AVANZATO -> 2
                else -> 0
            }
            ((exercise.defaultReps + goalDelta + levelDelta) * intensity).toInt().coerceAtLeast(5)
        }

        val durationSec = if (!exercise.isTimeBased) 0 else run {
            val levelDelta = when (level) {
                FitnessLevel.PRINCIPIANTE -> -10
                FitnessLevel.AVANZATO -> 10
                else -> 0
            }
            ((exercise.defaultDurationSec + levelDelta) * intensity).toInt().coerceAtLeast(15)
        }

        val restFactor = when (goal) {
            WorkoutGoal.FORZA -> 1.2f
            WorkoutGoal.DIMAGRIMENTO -> 0.6f
            WorkoutGoal.CARDIO -> 0.7f
            else -> 1f
        }
        val restLevelDelta = when (level) {
            FitnessLevel.PRINCIPIANTE -> 10
            FitnessLevel.AVANZATO -> -10
            else -> 0
        }
        val restSec = (exercise.defaultRestSec * restFactor + restLevelDelta).toInt().coerceAtLeast(15)

        return GeneratedExercise(
            exercise = exercise,
            sets = sets,
            reps = reps,
            durationSec = durationSec,
            restSec = restSec,
        )
    }
}
