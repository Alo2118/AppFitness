package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appfitness.app.data.model.FitnessLevel

/**
 * Result of a heart-rate based fitness test performed with a connected monitor.
 * Stores the raw readings plus the derived VO₂max, recovery and level.
 */
@Entity(tableName = "cardio_assessments")
data class CardioAssessment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val age: Int,
    val restingHr: Int,
    val peakHr: Int,
    val recoveryHr: Int,
    val hrr: Int,
    val vo2max: Float,
    val category: String,
    val level: FitnessLevel,
    val deviceName: String = "",
)
