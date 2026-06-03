package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One recorded GPS fix of a [GpsActivity], with elapsed time and cumulative distance. */
@Entity(
    tableName = "gps_points",
    foreignKeys = [
        ForeignKey(
            entity = GpsActivity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("activityId")],
)
data class GpsPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val elapsedSec: Int,
    val cumulativeDistanceM: Double,
    val lat: Double,
    val lon: Double,
)
