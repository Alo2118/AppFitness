package com.appfitness.app.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunCoachEngineTest {

    @Test
    fun `start phrase mentions target pace when set`() {
        val engine = RunCoachEngine()
        assertTrue(engine.startPhrase(330.0).contains("5:30"))
        assertTrue(engine.startPhrase(null).isNotBlank())
    }

    @Test
    fun `announces each kilometre once`() {
        val engine = RunCoachEngine()
        val first = engine.onUpdate(RunState(elapsedSec = 300, distanceM = 1000.0, currentPaceSecPerKm = 300.0))
        assertNotNull(first)
        assertTrue(first!!.contains("1 chilometro"))
        // Still in the first km -> no repeat.
        val again = engine.onUpdate(RunState(elapsedSec = 305, distanceM = 1010.0, currentPaceSecPerKm = 300.0))
        assertNull(again)
        // Second km announced.
        val second = engine.onUpdate(RunState(elapsedSec = 600, distanceM = 2000.0, currentPaceSecPerKm = 300.0))
        assertNotNull(second)
        assertTrue(second!!.contains("2 chilometri"))
    }

    @Test
    fun `milestone reports ghost status`() {
        val engine = RunCoachEngine()
        val ahead = engine.onUpdate(
            RunState(elapsedSec = 300, distanceM = 1000.0, currentPaceSecPerKm = 300.0, ghostLeadM = 30.0),
        )
        assertNotNull(ahead)
        assertTrue(ahead!!.contains("avanti"))
    }

    @Test
    fun `gives breathing guidance when heart rate is high`() {
        val engine = RunCoachEngine()
        val cue = engine.onUpdate(
            RunState(elapsedSec = 45, distanceM = 200.0, currentPaceSecPerKm = 360.0, heartRateElevated = true),
        )
        assertNotNull(cue)
        assertTrue(cue!!.lowercase().contains("respir"))
    }

    @Test
    fun `nudges to accelerate when behind target pace`() {
        val engine = RunCoachEngine()
        val cue = engine.onUpdate(
            RunState(elapsedSec = 30, distanceM = 100.0, currentPaceSecPerKm = 360.0, targetPaceSecPerKm = 330.0),
        )
        assertNotNull(cue)
        assertTrue(cue!!.lowercase().contains("accelera"))
    }

    @Test
    fun `stays quiet when on pace and not in a new km`() {
        val engine = RunCoachEngine()
        val cue = engine.onUpdate(
            RunState(elapsedSec = 30, distanceM = 100.0, currentPaceSecPerKm = 330.0, targetPaceSecPerKm = 330.0),
        )
        assertNull(cue)
    }

    @Test
    fun `pace nudge is throttled within the same window`() {
        val engine = RunCoachEngine()
        val first = engine.onUpdate(
            RunState(elapsedSec = 30, distanceM = 100.0, currentPaceSecPerKm = 360.0, targetPaceSecPerKm = 330.0),
        )
        val same = engine.onUpdate(
            RunState(elapsedSec = 35, distanceM = 110.0, currentPaceSecPerKm = 360.0, targetPaceSecPerKm = 330.0),
        )
        assertNotNull(first)
        assertNull(same)
    }
}
