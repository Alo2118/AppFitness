package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityRewardRulesTest {

    @Test
    fun `workout points grow with sets and volume`() {
        val small = ActivityRewardRules.workout(setsCompleted = 3, volumeKg = 0f, moodDelta = null)
        val big = ActivityRewardRules.workout(setsCompleted = 15, volumeKg = 1000f, moodDelta = null)
        assertTrue(big.points > small.points)
    }

    @Test
    fun `improved mood adds a bonus and upgrades the message`() {
        val plain = ActivityRewardRules.workout(3, 0f, moodDelta = 0)
        val happy = ActivityRewardRules.workout(3, 0f, moodDelta = 2)
        assertTrue(happy.points > plain.points)
        assertTrue(happy.title.contains("umore", ignoreCase = true))
    }

    @Test
    fun `guided set hitting target is worth more than missing it`() {
        val hit = ActivityRewardRules.guidedSet(reps = 12, target = 10)
        val miss = ActivityRewardRules.guidedSet(reps = 6, target = 10)
        assertTrue(hit.points > miss.points)
        assertEquals(RewardTier.MEDIUM, hit.tier)
    }

    @Test
    fun `gps session tier scales with score`() {
        assertEquals(RewardTier.EPIC, ActivityRewardRules.gpsSession(1000).tier)
        assertEquals(RewardTier.SMALL, ActivityRewardRules.gpsSession(50).tier)
    }

    @Test
    fun `every reward gives at least some points`() {
        assertTrue(ActivityRewardRules.moodCheckIn().points > 0)
        assertTrue(ActivityRewardRules.cardioTest().points > 0)
        assertTrue(ActivityRewardRules.gpsSession(0).points > 0)
    }
}

class RewardLevelsTest {

    @Test
    fun `level increases every 500 points`() {
        assertEquals(1, RewardLevels.levelFor(0))
        assertEquals(1, RewardLevels.levelFor(499))
        assertEquals(2, RewardLevels.levelFor(500))
        assertEquals(3, RewardLevels.levelFor(1000))
    }

    @Test
    fun `progress and points-to-next are consistent`() {
        assertEquals(250, RewardLevels.pointsIntoLevel(750))
        assertEquals(250, RewardLevels.pointsToNextLevel(750))
        assertEquals(0.5f, RewardLevels.levelProgress(750), 0.001f)
    }
}
