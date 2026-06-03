package com.appfitness.app.data

import com.appfitness.app.data.dao.AssessmentDao
import com.appfitness.app.data.dao.CardioAssessmentDao
import com.appfitness.app.data.dao.ExerciseDao
import com.appfitness.app.data.dao.MoodDao
import com.appfitness.app.data.dao.ProgramDao
import com.appfitness.app.data.dao.WorkoutDao
import com.appfitness.app.data.entity.AssessmentResult
import com.appfitness.app.data.entity.CardioAssessment
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.TrainingProgram
import com.appfitness.app.data.entity.WorkoutSession
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.Sport
import com.appfitness.app.data.relation.SessionWithSets
import com.appfitness.app.domain.AdaptiveEngine
import com.appfitness.app.domain.AssessmentEvaluator
import com.appfitness.app.domain.GeneratedExercise
import com.appfitness.app.domain.HrFitnessEvaluator
import com.appfitness.app.domain.SportProgramGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Single source of truth for all app data. ViewModels talk only to this class,
 * never directly to the DAOs.
 */
class FitnessRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val moodDao: MoodDao,
    private val programDao: ProgramDao,
    private val assessmentDao: AssessmentDao,
    private val cardioAssessmentDao: CardioAssessmentDao,
) {
    // ----- Exercises -----
    val exercises: Flow<List<Exercise>> = exerciseDao.observeAll()
    suspend fun exercisesSnapshot(): List<Exercise> = exerciseDao.observeAll().first()
    suspend fun getExercise(id: Long) = exerciseDao.getById(id)
    suspend fun upsertExercise(exercise: Exercise) = exerciseDao.upsert(exercise)
    suspend fun deleteCustomExercise(id: Long) = exerciseDao.deleteCustom(id)

    /** Populate the starter library the first time the app runs. */
    suspend fun seedExercisesIfEmpty() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(ExerciseSeed.exercises)
        }
    }

    // ----- Workouts -----
    fun completedSessions(): Flow<List<SessionWithSets>> = workoutDao.observeCompletedSessions()
    fun observeSession(id: Long): Flow<SessionWithSets?> = workoutDao.observeSessionWithSets(id)
    suspend fun getSession(id: Long) = workoutDao.getSession(id)
    suspend fun startSession(session: WorkoutSession): Long = workoutDao.insertSession(session)

    /**
     * Creates a new in-progress session pre-populated with the sets of a
     * generated plan, returning the session id ready for the workout screen.
     */
    suspend fun startGeneratedSession(
        title: String,
        moodBefore: Int?,
        energyBefore: Int?,
        plan: List<GeneratedExercise>,
        programId: Long? = null,
    ): Long {
        val sessionId = workoutDao.insertSession(
            WorkoutSession(
                title = title,
                startedAt = System.currentTimeMillis(),
                moodBefore = moodBefore,
                energyBefore = energyBefore,
                programId = programId,
            )
        )
        plan.forEach { item ->
            repeat(item.sets) { index ->
                workoutDao.insertSet(
                    SetLog(
                        sessionId = sessionId,
                        exerciseId = item.exercise.id,
                        exerciseName = item.exercise.name,
                        setNumber = index + 1,
                        reps = item.reps,
                        durationSec = item.durationSec,
                        restSec = item.restSec,
                        isTimeBased = item.exercise.isTimeBased,
                    )
                )
            }
        }
        return sessionId
    }
    suspend fun updateSession(session: WorkoutSession) = workoutDao.updateSession(session)
    suspend fun deleteSession(id: Long) = workoutDao.deleteSession(id)

    suspend fun addSet(set: SetLog): Long = workoutDao.insertSet(set)
    suspend fun updateSet(set: SetLog) = workoutDao.updateSet(set)
    suspend fun deleteSet(id: Long) = workoutDao.deleteSet(id)

    // ----- Mood -----
    val moodEntries: Flow<List<MoodEntry>> = moodDao.observeAll()
    fun recentMood(limit: Int): Flow<List<MoodEntry>> = moodDao.observeRecent(limit)
    suspend fun addMood(entry: MoodEntry): Long = moodDao.insert(entry)
    suspend fun deleteMood(id: Long) = moodDao.delete(id)

    // ----- Initial test (assessment) -----
    val latestAssessment: Flow<AssessmentResult?> = assessmentDao.observeLatest()

    /** Scores the initial test, stores it, and returns the derived level. */
    suspend fun saveAssessment(pushUps: Int, squats: Int, plankSec: Int): FitnessLevel {
        val outcome = AssessmentEvaluator.evaluate(pushUps, squats, plankSec)
        assessmentDao.insert(
            AssessmentResult(
                timestamp = System.currentTimeMillis(),
                pushUps = pushUps,
                squats = squats,
                plankSec = plankSec,
                score = outcome.score,
                level = outcome.level,
            )
        )
        return outcome.level
    }

    // ----- Cardio test (heart-rate monitor) -----
    val latestCardioAssessment: Flow<CardioAssessment?> = cardioAssessmentDao.observeLatest()

    /** Evaluates and stores a heart-rate based fitness test, returning the result. */
    suspend fun saveCardioAssessment(
        age: Int,
        restingHr: Int,
        peakHr: Int,
        recoveryHr: Int,
        deviceName: String,
    ): CardioAssessment {
        val outcome = HrFitnessEvaluator.evaluate(age, restingHr, peakHr, recoveryHr)
        val record = CardioAssessment(
            timestamp = System.currentTimeMillis(),
            age = age,
            restingHr = restingHr,
            peakHr = peakHr,
            recoveryHr = recoveryHr,
            hrr = outcome.hrr,
            vo2max = outcome.vo2max,
            category = outcome.category,
            level = outcome.level,
            deviceName = deviceName,
        )
        val id = cardioAssessmentDao.insert(record)
        return record.copy(id = id)
    }

    // ----- Sport programs -----
    val programs: Flow<List<TrainingProgram>> = programDao.observeAll()
    suspend fun getProgram(id: Long) = programDao.getById(id)
    suspend fun deleteProgram(id: Long) = programDao.delete(id)

    /** Creates a sport program, seeding its load from the latest test if any. */
    suspend fun createProgram(
        sport: Sport,
        level: FitnessLevel,
        weeks: Int,
        sessionsPerWeek: Int,
    ): Long {
        val startingLoad = when (level) {
            FitnessLevel.PRINCIPIANTE -> 0.9f
            FitnessLevel.INTERMEDIO -> 1.0f
            FitnessLevel.AVANZATO -> 1.1f
        }
        return programDao.insert(
            TrainingProgram(
                sport = sport,
                name = "${sport.label} • ${weeks} settimane",
                level = level,
                weeks = weeks,
                sessionsPerWeek = sessionsPerWeek,
                startedAt = System.currentTimeMillis(),
                loadMultiplier = startingLoad,
            )
        )
    }

    /** Generates and starts the program's next adaptive session. */
    suspend fun startProgramSession(programId: Long, energy: Int?): Long? {
        val program = programDao.getById(programId) ?: return null
        val plan = SportProgramGenerator.nextWorkout(program, exercisesSnapshot(), energy)
        if (plan.isEmpty()) return null
        return startGeneratedSession(
            title = SportProgramGenerator.sessionTitle(program),
            moodBefore = null,
            energyBefore = energy,
            plan = plan,
            programId = programId,
        )
    }

    /**
     * Called when a program session is completed: recomputes the adaptive load
     * from completion + post-workout energy and advances the program.
     */
    suspend fun applyAdaptiveUpdate(programId: Long, sessionId: Long) {
        val program = programDao.getById(programId) ?: return
        val session = workoutDao.getSessionWithSets(sessionId) ?: return
        val result = AdaptiveEngine.recompute(program.loadMultiplier, session)
        val newCompleted = program.completedSessions + 1
        programDao.update(
            program.copy(
                loadMultiplier = result.loadMultiplier,
                completedSessions = newCompleted,
                lastSuggestion = result.suggestion,
                active = newCompleted < program.totalSessions,
            )
        )
    }
}
