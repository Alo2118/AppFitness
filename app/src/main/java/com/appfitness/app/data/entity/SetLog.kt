package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single logged set inside a [WorkoutSession]. We denormalise [exerciseName]
 * so history stays readable even if the source exercise is later edited/deleted.
 */
@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId")],
)
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int = 0,
    val weightKg: Float = 0f,
    val durationSec: Int = 0,
    val restSec: Int = 0,
    val isTimeBased: Boolean = false,
    val completed: Boolean = false,
)
