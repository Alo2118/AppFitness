package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appfitness.app.data.entity.CardioAssessment
import kotlinx.coroutines.flow.Flow

@Dao
interface CardioAssessmentDao {

    @Insert
    suspend fun insert(result: CardioAssessment): Long

    @Query("SELECT * FROM cardio_assessments ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<CardioAssessment?>

    @Query("SELECT * FROM cardio_assessments ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CardioAssessment>>
}
