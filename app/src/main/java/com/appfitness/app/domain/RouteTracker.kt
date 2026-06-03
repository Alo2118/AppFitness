package com.appfitness.app.domain

/** One recorded point along a route. */
data class RouteSample(
    val elapsedSec: Int,
    val cumulativeDistanceM: Double,
    val lat: Double,
    val lon: Double,
)

/**
 * Accumulates GPS fixes into a route: running total distance (Haversine between
 * consecutive fixes) plus current and average pace. Pure and stateful so it can
 * be unit-tested with synthetic coordinates.
 */
class RouteTracker(
    /** Ignore sub-metre jitter between fixes to reduce GPS noise drift. */
    private val minStepMeters: Double = 1.0,
) {
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    var totalDistanceM = 0.0
        private set

    val samples = mutableListOf<RouteSample>()

    /** Adds a fix; returns the resulting sample. */
    fun onLocation(lat: Double, lon: Double, elapsedSec: Int): RouteSample {
        val pLat = lastLat
        val pLon = lastLon
        if (pLat != null && pLon != null) {
            val step = GeoUtils.haversineMeters(pLat, pLon, lat, lon)
            if (step >= minStepMeters) {
                totalDistanceM += step
                lastLat = lat
                lastLon = lon
            }
        } else {
            lastLat = lat
            lastLon = lon
        }
        return RouteSample(elapsedSec, totalDistanceM, lat, lon).also { samples.add(it) }
    }

    /** Pace over the most recent [windowSec] seconds, in seconds per km. */
    fun currentPaceSecPerKm(windowSec: Int = 15): Double {
        if (samples.size < 2) return 0.0
        val last = samples.last()
        val ref = samples.lastOrNull { last.elapsedSec - it.elapsedSec >= windowSec } ?: samples.first()
        val dDist = last.cumulativeDistanceM - ref.cumulativeDistanceM
        val dTime = last.elapsedSec - ref.elapsedSec
        if (dDist <= 0 || dTime <= 0) return 0.0
        return dTime / (dDist / 1000.0)
    }

    /** Average pace over the whole route, in seconds per km. */
    fun averagePaceSecPerKm(): Double {
        val last = samples.lastOrNull() ?: return 0.0
        if (totalDistanceM <= 0) return 0.0
        return last.elapsedSec / (totalDistanceM / 1000.0)
    }
}
