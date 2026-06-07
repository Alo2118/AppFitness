package com.appfitness.app.domain

import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.TrainingProgram
import kotlin.random.Random

/**
 * Builds the next workout of a sport-specific [TrainingProgram], combining the
 * sport's goal rotation with the program's current adaptive [loadMultiplier].
 */
object SportProgramGenerator {

    private const val SESSION_DURATION_MIN = 40

    /** Generates the workout for the program's next (not-yet-completed) session. */
    fun nextWorkout(
        program: TrainingProgram,
        library: List<Exercise>,
        energy: Int? = null,
    ): List<GeneratedExercise> {
        val sessionIndex = program.completedSessions
        val goal = program.sport.goalForSession(sessionIndex)
        // Deterministic per program+session so the preview matches what is started.
        val seed = program.id * 1000L + sessionIndex
        return WorkoutGenerator.generate(
            goal = goal,
            level = program.level,
            durationMin = SESSION_DURATION_MIN,
            energy = energy,
            library = library,
            random = Random(seed),
            loadMultiplier = program.loadMultiplier,
        )
    }

    fun sessionTitle(program: TrainingProgram): String {
        val sessionInWeek = program.completedSessions % program.sessionsPerWeek + 1
        return "${program.sport.emoji} ${program.sport.label} · Sett. ${program.currentWeek} · Sessione $sessionInWeek"
    }
}
