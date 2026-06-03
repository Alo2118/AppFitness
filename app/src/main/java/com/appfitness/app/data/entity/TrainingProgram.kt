package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.Sport

/**
 * A multi-week, sport-specific training program. [loadMultiplier] is the adaptive
 * factor (1.0 = baseline) updated after each completed session based on how the
 * user performed — the engine of adaptive programming.
 */
@Entity(tableName = "training_programs")
data class TrainingProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sport: Sport,
    val name: String,
    val level: FitnessLevel,
    val weeks: Int,
    val sessionsPerWeek: Int,
    val startedAt: Long,
    val completedSessions: Int = 0,
    val loadMultiplier: Float = 1f,
    val lastSuggestion: String = "",
    val active: Boolean = true,
) {
    val totalSessions: Int get() = weeks * sessionsPerWeek
    val currentWeek: Int get() = (completedSessions / sessionsPerWeek + 1).coerceAtMost(weeks)
    val isFinished: Boolean get() = completedSessions >= totalSessions
    val progress: Float get() = if (totalSessions == 0) 0f else completedSessions.toFloat() / totalSessions
}
