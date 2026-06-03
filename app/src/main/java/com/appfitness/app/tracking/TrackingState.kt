package com.appfitness.app.tracking

import com.appfitness.app.data.model.GpsActivityType
import kotlinx.coroutines.flow.MutableStateFlow

/** Where a ghost opponent comes from. */
enum class GhostMode { NONE, PACE, TARGET_TIME, REPLAY }

/** Ghost configuration passed from the UI to the tracking service. */
data class GhostConfig(
    val mode: GhostMode = GhostMode.NONE,
    val paceSecPerKm: Double = 0.0,
    val targetDistanceM: Double = 0.0,
    val targetTimeSec: Int = 0,
    val replayActivityId: Long = -1L,
)

/**
 * App-wide live state of the current GPS session, written by [TrackingService]
 * and observed by the tracking UI. A simple shared holder keeps the
 * foreground service and Compose screen in sync without binding.
 */
object TrackingState {
    val isTracking = MutableStateFlow(false)
    val type = MutableStateFlow(GpsActivityType.RUN)
    val elapsedSec = MutableStateFlow(0)
    val distanceM = MutableStateFlow(0.0)
    val paceSecPerKm = MutableStateFlow(0.0)
    val ghostLeadM = MutableStateFlow<Double?>(null)
    val hasGpsFix = MutableStateFlow(false)
    val lastSavedActivityId = MutableStateFlow<Long?>(null)

    /** Live route as (lat, lon) pairs, for the map. */
    val path = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())

    fun resetForStart(activityType: GpsActivityType) {
        type.value = activityType
        elapsedSec.value = 0
        distanceM.value = 0.0
        paceSecPerKm.value = 0.0
        ghostLeadM.value = null
        hasGpsFix.value = false
        lastSavedActivityId.value = null
        path.value = emptyList()
        isTracking.value = true
    }
}
