package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.appfitness.app.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun observeAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("DELETE FROM exercises WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustom(id: Long)
}
