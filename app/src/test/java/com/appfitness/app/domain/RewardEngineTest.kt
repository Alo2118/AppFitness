package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RewardEngineTest {

    @Test
    fun `first kilometre triggers a milestone, then a split, and score grows`() {
        val engine = RewardEngine(Random(1))
        val milestone = engine.onUpdate(RunRewardState(1000.0, 360))
        assertNotNull(milestone)
        assertTrue(milestone!!.title.contains("Primo chilometro"))

        val split = engine.onUpdate(RunRewardState(1000.0, 361))
        assertNotNull(split)
        assertTrue(split!!.title.contains("Chilometro 1"))
        assertTrue(engine.totalScore > 0)
    }

    @Test
    fun `negative split builds a combo`() {
        val engine = RewardEngine(Random(1))
        engine.onUpdate(RunRewardState(1000.0, 360))   // milestone
        engine.onUpdate(RunRewardState(1000.0, 361))   // split km1 (~361s)
        val faster = engine.onUpdate(RunRewardState(2000.0, 660)) // km2 ~299s, faster
        assertNotNull(faster)
        assertTrue(faster!!.title.contains("combo"))
        assertEquals(1, engine.combo)
    }

    @Test
    fun `a milestone fires only once`() {
        val engine = RewardEngine(Random(1))
        engine.onUpdate(RunRewardState(1000.0, 360))
        val again = engine.onUpdate(RunRewardState(1000.0, 360))
        assertFalse(again!!.title.contains("Primo chilometro"))
    }

    @Test
    fun `overtaking the ghost is rewarded once on transition`() {
        val engine = RewardEngine(Random(1))
        assertNull(engine.onUpdate(RunRewardState(100.0, 30, ghostLeadM = -10.0)))
        val overtake = engine.onUpdate(RunRewardState(150.0, 45, ghostLeadM = 5.0))
        assertNotNull(overtake)
        assertTrue(overtake!!.title.contains("fantasma"))
        // Still ahead -> no repeat.
        assertNull(engine.onUpdate(RunRewardState(200.0, 60, ghostLeadM = 8.0)))
    }

    @Test
    fun `staying in zone rewards each minute`() {
        val engine = RewardEngine(Random(1))
        val z = engine.onUpdate(RunRewardState(100.0, 60, hrInTargetZone = true))
        assertNotNull(z)
        assertTrue(z!!.title.contains("zona"))
    }

    @Test
    fun `personal record is an epic reward that adds points`() {
        val engine = RewardEngine()
        val before = engine.totalScore
        val pr = engine.personalRecord("Record!")
        assertEquals(RewardTier.EPIC, pr.tier)
        assertTrue(engine.totalScore > before)
    }
}
