package com.appfitness.app.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Geographic helpers for GPS tracking. */
object GeoUtils {

    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Great-circle distance between two coordinates, in metres (Haversine). */
    fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return EARTH_RADIUS_M * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /** Formats a pace (seconds per km) as "m:ss/km"; blank-ish when unknown. */
    fun formatPace(secPerKm: Double): String {
        if (secPerKm <= 0 || secPerKm.isInfinite() || secPerKm.isNaN()) return "--:--"
        val total = secPerKm.toInt()
        return "%d:%02d".format(total / 60, total % 60)
    }

    /** Formats metres as km with two decimals. */
    fun formatKm(meters: Double): String = "%.2f".format(meters / 1000.0)
}
