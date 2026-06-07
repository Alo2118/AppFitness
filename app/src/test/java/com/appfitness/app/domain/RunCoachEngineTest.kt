package com.appfitness.app.domain

import com.appfitness.app.data.model.GpsActivityType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private fun run(
    elapsedSec: Int,
    distanceM: Double,
    pace: Double,
    target: Double? = null,
    ghostLead: Double? = null,
    hr: Int? = null,
    age: Int = 30,
) = RunState(
    activityType = GpsActivityType.RUN,
    elapsedSec = elapsedSec,
    distanceM = distanceM,
    currentPaceSecPerKm = pace,
    targetPaceSecPerKm = target,
    ghostLeadM = ghostLead,
    heartRate = hr,
    age = age,
)

class RunCoachEngineTest {

    @Test
    fun `start phrase adapts to activity and mentions target`() {
        val engine = RunCoachEngine()
        assertTrue(engine.startPhrase(GpsActivityType.BIKE, null).contains("pedalata"))
        assertTrue(engine.startPhrase(GpsActivityType.RUN, 330.0).contains("5:30"))
    }

    @Test
    fun `announces each kilometre once when all is well`() {
        val engine = RunCoachEngine()
        val first = engine.onUpdate(run(300, 1000.0, 300.0))
        assertNotNull(first)
        assertTrue(first!!.contains("1 chilometro"))
        assertNull(engine.onUpdate(run(305, 1010.0, 300.0)))
        val second = engine.onUpdate(run(600, 2000.0, 300.0))
        assertTrue(second!!.contains("2 chilometri"))
    }

    @Test
    fun `bike kilometre status uses speed in km per hour`() {
        val engine = RunCoachEngine()
        val cue = engine.onUpdate(
            RunState(GpsActivityType.BIKE, elapsedSec = 120, distanceM = 1000.0, currentPaceSecPerKm = 120.0),
        )
        assertNotNull(cue)
        assertTrue(cue!!.contains("orari")) // 120 s/km == 30.0 km/h
    }

    @Test
    fun `intervenes when heart rate is above the target zone`() {
        val engine = RunCoachEngine()
        // age 30 -> maxHr 190; 185 ~ 97% -> well above AEROBICA target.
        val cue = engine.onUpdate(run(40, 150.0, 360.0, hr = 185))
        assertNotNull(cue)
        assertTrue(cue!!.lowercase().contains("battito"))
    }

    @Test
    fun `intervenes when far behind the ghost`() {
        val engine = RunCoachEngine()
        val cue = engine.onUpdate(run(40, 150.0, 360.0, ghostLead = -120.0))
        assertNotNull(cue)
        assertTrue(cue!!.contains("indietro"))
    }

    @Test
    fun `nudges to accelerate when losing target pace`() {
        val engine = RunCoachEngine()
        val cue = engine.onUpdate(run(40, 150.0, 345.0, target = 330.0))
        assertNotNull(cue)
        assertTrue(cue!!.lowercase().contains("accelera"))
    }

    @Test
    fun `stays quiet mid-kilometre when everything is on track`() {
        val engine = RunCoachEngine()
        assertNull(engine.onUpdate(run(40, 150.0, 330.0, target = 330.0, hr = 150)))
    }

    @Test
    fun `interventions are throttled within their window`() {
        val engine = RunCoachEngine()
        val first = engine.onUpdate(run(30, 100.0, 345.0, target = 330.0))
        val same = engine.onUpdate(run(35, 110.0, 345.0, target = 330.0))
        assertNotNull(first)
        assertNull(same)
    }
}
