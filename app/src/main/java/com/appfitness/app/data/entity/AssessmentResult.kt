package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appfitness.app.data.model.FitnessLevel

/**
 * Result of the initial fitness test ("test di inizio"). Used to recommend a
 * starting [level] and a baseline load for new programs, and to track progress
 * if the test is repeated over time.
 */
@Entity(tableName = "assessment_results")
data class AssessmentResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val pushUps: Int,
    val squats: Int,
    val plankSec: Int,
    val score: Int,
    val level: FitnessLevel,
)
