package com.appfitness.app.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.WorkoutSession

/** A workout session together with all of its logged sets. */
data class SessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val sets: List<SetLog>,
) {
    val totalVolumeKg: Float
        get() = sets.sumOf { (it.reps * it.weightKg).toDouble() }.toFloat()

    val totalSets: Int get() = sets.count { it.completed }
}
