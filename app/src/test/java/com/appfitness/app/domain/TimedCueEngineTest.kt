package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimedCueEngineTest {

    @Test
    fun `first tick announces the start`() {
        val engine = TimedCueEngine()
        assertEquals("Via! Tieni la posizione", engine.onTick(30, 30))
    }

    @Test
    fun `counts down the final five seconds, once each`() {
        val engine = TimedCueEngine()
        engine.onTick(10, 10) // start
        val spoken = (9 downTo 0).mapNotNull { engine.onTick(it, 10) }
        assertEquals(listOf("5", "4", "3", "2", "1", "Finito! Ottimo lavoro"), spoken)
    }

    @Test
    fun `midpoint cue fires once for a long enough set`() {
        val engine = TimedCueEngine()
        engine.onTick(40, 40)
        val half = (39 downTo 20).mapNotNull { engine.onTick(it, 40) }
        assertTrue("Metà! Resisti" in half)
        assertEquals(1, half.count { it == "Metà! Resisti" })
    }

    @Test
    fun `nothing is said between cues`() {
        val engine = TimedCueEngine()
        engine.onTick(30, 30)
        assertNull(engine.onTick(29, 30))
    }

    @Test
    fun `reset clears state so cues fire again`() {
        val engine = TimedCueEngine()
        engine.onTick(20, 20)
        engine.reset()
        assertEquals("Via! Tieni la posizione", engine.onTick(20, 20))
    }
}
