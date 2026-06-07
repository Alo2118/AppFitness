package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 6, 3)

    @Test
    fun `consecutive days ending today count`() {
        val days = setOf(today, today.minusDays(1), today.minusDays(2))
        assertEquals(3, StreakCalculator.currentStreak(days, today))
    }

    @Test
    fun `streak survives if yesterday was active but not yet today`() {
        val days = setOf(today.minusDays(1), today.minusDays(2))
        assertEquals(2, StreakCalculator.currentStreak(days, today))
    }

    @Test
    fun `gap breaks the streak`() {
        val days = setOf(today, today.minusDays(2), today.minusDays(3))
        assertEquals(1, StreakCalculator.currentStreak(days, today))
    }

    @Test
    fun `no recent activity means no streak`() {
        val days = setOf(today.minusDays(5))
        assertEquals(0, StreakCalculator.currentStreak(days, today))
    }
}

class WeeklyGoalsTest {

    @Test
    fun `progress is clamped and reached flag works`() {
        val goals = WeeklyGoals.goals(workouts = 5, km = 0, points = 150)
        val workouts = goals.first { it.key == "workouts" }
        assertEquals(1f, workouts.progress, 0.001f)
        assertTrue(workouts.reached)
        val km = goals.first { it.key == "km" }
        assertEquals(0f, km.progress, 0.001f)
        assertFalse(km.reached)
    }
}

class AchievementEvaluatorTest {

    private fun stats(points: Int = 0, workouts: Int = 0, km: Double = 0.0, longest: Double = 0.0, streak: Int = 0) =
        UserStats(points, workouts, km, longest, streak)

    @Test
    fun `first activity unlocks the first badge`() {
        val unlocked = AchievementEvaluator.newlyUnlocked(stats(points = 20), emptySet())
        assertTrue(unlocked.any { it.key == "first_step" })
    }

    @Test
    fun `already unlocked badges are not returned again`() {
        val unlocked = AchievementEvaluator.newlyUnlocked(stats(points = 20), setOf("first_step"))
        assertFalse(unlocked.any { it.key == "first_step" })
    }

    @Test
    fun `thresholds gate higher badges`() {
        val none = AchievementEvaluator.newlyUnlocked(stats(points = 100, streak = 2), emptySet())
        assertFalse(none.any { it.key == "streak_3" })
        val withStreak = AchievementEvaluator.newlyUnlocked(stats(points = 100, streak = 3), emptySet())
        assertTrue(withStreak.any { it.key == "streak_3" })
    }

    @Test
    fun `distance badges unlock from kilometres`() {
        val unlocked = AchievementEvaluator.newlyUnlocked(stats(points = 10, km = 42.0, longest = 6.0), emptySet())
        assertTrue(unlocked.any { it.key == "distance_42" })
        assertTrue(unlocked.any { it.key == "long_run_5" })
    }
}
