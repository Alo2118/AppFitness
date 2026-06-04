package com.appfitness.app.domain

import com.appfitness.app.data.model.GpsActivityType
import kotlin.math.roundToInt

/**
 * Rough calorie estimates using the MET method: kcal ≈ MET × weight(kg) × hours.
 * MET values are derived from pace/speed for GPS activities. Pure and testable.
 */
object CalorieEstimator {

    fun forGps(type: GpsActivityType, distanceKm: Double, durationSec: Int, weightKg: Float): Int {
        val hours = durationSec / 3600.0
        if (hours <= 0 || weightKg <= 0) return 0
        val speed = distanceKm / hours
        val met = when (type) {
            GpsActivityType.RUN -> (speed * 0.95).coerceIn(6.0, 23.0)   // ~9.5 MET at 10 km/h
            GpsActivityType.BIKE -> (speed * 0.45).coerceIn(4.0, 16.0)  // ~8 MET at ~18 km/h
        }
        return (met * weightKg * hours).roundToInt()
    }

    fun forWorkout(durationSec: Int, weightKg: Float, met: Double = 5.0): Int {
        val hours = durationSec / 3600.0
        if (hours <= 0 || weightKg <= 0) return 0
        return (met * weightKg * hours).roundToInt()
    }
}
