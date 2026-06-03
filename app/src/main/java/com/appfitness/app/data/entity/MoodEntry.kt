package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A standalone emotional check-in for the mood journal. Can optionally be linked
 * to a workout session via [relatedSessionId] (e.g. the "after" check-in).
 */
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val mood: Int,
    val energy: Int,
    /** Comma-separated free tags, e.g. "motivato,stanco". */
    val tags: String = "",
    val note: String = "",
    val relatedSessionId: Long? = null,
)
