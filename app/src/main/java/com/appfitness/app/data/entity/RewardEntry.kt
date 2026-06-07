package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A persisted reward earned from any activity, forming the user's total score and
 * level across the whole app.
 */
@Entity(tableName = "reward_entries")
data class RewardEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    /** Origin of the reward, e.g. "workout", "gps", "cardio", "mood". */
    val source: String,
    val label: String,
    val points: Int,
)
