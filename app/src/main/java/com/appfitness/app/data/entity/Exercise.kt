package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appfitness.app.data.model.ExerciseCategory

/**
 * A single exercise in the library. Exercises can be time-based (e.g. plank,
 * running) or rep-based (e.g. squats). The defaults are used to pre-fill a
 * workout but can always be overridden during a session.
 */
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ExerciseCategory,
    val muscleGroup: String,
    val description: String,
    val isTimeBased: Boolean,
    val defaultSets: Int = 3,
    val defaultReps: Int = 12,
    val defaultDurationSec: Int = 30,
    val defaultRestSec: Int = 60,
    val isCustom: Boolean = false,
    /** Optional guide image bundled in assets, e.g. "exercises/squat.jpg". */
    val imageAsset: String? = null,
)
