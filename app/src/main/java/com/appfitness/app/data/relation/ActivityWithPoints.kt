package com.appfitness.app.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.entity.GpsPoint

/** A GPS activity together with its recorded points (used for the ghost replay). */
data class ActivityWithPoints(
    @Embedded val activity: GpsActivity,
    @Relation(parentColumn = "id", entityColumn = "activityId")
    val points: List<GpsPoint>,
)
