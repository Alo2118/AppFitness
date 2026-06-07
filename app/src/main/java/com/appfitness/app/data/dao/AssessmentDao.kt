package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appfitness.app.data.entity.AssessmentResult
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {

    @Insert
    suspend fun insert(result: AssessmentResult): Long

    @Query("SELECT * FROM assessment_results ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<AssessmentResult?>

    @Query("SELECT * FROM assessment_results ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AssessmentResult>>
}
