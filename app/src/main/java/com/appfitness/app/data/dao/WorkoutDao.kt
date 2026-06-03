package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Query
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.WorkoutSession
import com.appfitness.app.data.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSession(id: Long): WorkoutSession?

    @Insert
    suspend fun insertSet(set: SetLog): Long

    @Update
    suspend fun updateSet(set: SetLog)

    @Query("DELETE FROM set_logs WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeSessionWithSets(id: Long): Flow<SessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionWithSets(id: Long): SessionWithSets?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE endedAt IS NOT NULL ORDER BY startedAt DESC")
    fun observeCompletedSessions(): Flow<List<SessionWithSets>>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}
