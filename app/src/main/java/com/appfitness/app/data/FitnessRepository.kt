package com.appfitness.app.data

import com.appfitness.app.data.dao.ExerciseDao
import com.appfitness.app.data.dao.MoodDao
import com.appfitness.app.data.dao.WorkoutDao
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.WorkoutSession
import com.appfitness.app.data.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for all app data. ViewModels talk only to this class,
 * never directly to the DAOs.
 */
class FitnessRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val moodDao: MoodDao,
) {
    // ----- Exercises -----
    val exercises: Flow<List<Exercise>> = exerciseDao.observeAll()
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
}
