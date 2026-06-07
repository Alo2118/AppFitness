package com.appfitness.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** One weekly target with current progress. */
data class WeeklyGoal(
    val key: String,
    val label: String,
    val unit: String,
    val target: Int,
    val current: Int,
) {
    val progress: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)
    val reached: Boolean get() = current >= target
}

object WeeklyGoals {
    const val WORKOUTS_TARGET = 3
    const val KM_TARGET = 15
    const val POINTS_TARGET = 300

    fun goals(workouts: Int, km: Int, points: Int): List<WeeklyGoal> = listOf(
        WeeklyGoal("workouts", "Allenamenti", "", WORKOUTS_TARGET, workouts),
        WeeklyGoal("km", "Distanza", "km", KM_TARGET, km),
        WeeklyGoal("points", "Punti", "pt", POINTS_TARGET, points),
    )

    /** Epoch millis of the start (Monday 00:00) of the current week. */
    fun startOfWeekMillis(zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): Long =
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
}
