package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One workout session. Captures the emotional and energy state both *before* and
 * *after* training — the core differentiator of AppFitness — so the user can see
 * how exercise changes how they feel over time.
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    /** Active training time in seconds (excludes long pauses if tracked). */
    val totalDurationSec: Int = 0,
    val moodBefore: Int? = null,
    val moodAfter: Int? = null,
    val energyBefore: Int? = null,
    val energyAfter: Int? = null,
    val note: String = "",
    /** Set when this session belongs to a [TrainingProgram]. */
    val programId: Long? = null,
) {
    val isCompleted: Boolean get() = endedAt != null
}
