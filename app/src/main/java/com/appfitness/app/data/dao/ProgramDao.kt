package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appfitness.app.data.entity.TrainingProgram
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    @Query("SELECT * FROM training_programs ORDER BY active DESC, startedAt DESC")
    fun observeAll(): Flow<List<TrainingProgram>>

    @Query("SELECT * FROM training_programs WHERE id = :id")
    suspend fun getById(id: Long): TrainingProgram?

    @Insert
    suspend fun insert(program: TrainingProgram): Long

    @Update
    suspend fun update(program: TrainingProgram)

    @Query("DELETE FROM training_programs WHERE id = :id")
    suspend fun delete(id: Long)
}
