package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appfitness.app.data.model.GpsActivityType

/** A recorded outdoor GPS activity (run or ride). */
@Entity(tableName = "gps_activities")
data class GpsActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: GpsActivityType,
    val title: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationSec: Int,
    val distanceM: Float,
    val avgPaceSecPerKm: Float,
)
