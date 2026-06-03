package com.appfitness.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Computes the consecutive-day activity streak. Pure and testable. */
object StreakCalculator {

    /**
     * Length of the streak ending today (or yesterday, so a streak isn't lost
     * until a full day is missed). Returns 0 if neither today nor yesterday is active.
     */
    fun currentStreak(activeDays: Set<LocalDate>, today: LocalDate): Int {
        if (today !in activeDays && today.minusDays(1) !in activeDays) return 0
        var cursor = if (today in activeDays) today else today.minusDays(1)
        var streak = 0
        while (cursor in activeDays) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun activeDaysFromMillis(millis: List<Long>, zone: ZoneId = ZoneId.systemDefault()): Set<LocalDate> =
        millis.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.toSet()
}
