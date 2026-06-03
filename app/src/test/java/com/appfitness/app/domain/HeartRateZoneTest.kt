package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HeartRateZoneTest {

    @Test
    fun `max hr follows 220 minus age`() {
        assertEquals(190, HeartRateZoneEvaluator.maxHr(30))
    }

    @Test
    fun `low hr is rest, high hr is maximal`() {
        // age 30 -> maxHr 190
        assertEquals(HeartRateZone.RIPOSO, HeartRateZoneEvaluator.zoneFor(80, 30))   // 42%
        assertEquals(HeartRateZone.AEROBICA, HeartRateZoneEvaluator.zoneFor(140, 30)) // ~74%
        assertEquals(HeartRateZone.MASSIMALE, HeartRateZoneEvaluator.zoneFor(180, 30)) // ~95%
    }

    @Test
    fun `alerter speaks when leaving target and stays silent while in zone`() {
        val alerter = ZoneAlerter(HeartRateZone.AEROBICA)
        assertNotNull(alerter.onZone(HeartRateZone.RISCALDAMENTO)) // below -> alert
        assertNull(alerter.onZone(HeartRateZone.RISCALDAMENTO))    // same status -> silent
        assertNotNull(alerter.onZone(HeartRateZone.AEROBICA))      // back in zone -> alert
        assertNull(alerter.onZone(HeartRateZone.AEROBICA))         // still in zone -> silent
        assertNotNull(alerter.onZone(HeartRateZone.ANAEROBICA))    // above -> alert
    }

    @Test
    fun `reset clears alerter memory`() {
        val alerter = ZoneAlerter(HeartRateZone.AEROBICA)
        assertNotNull(alerter.onZone(HeartRateZone.MASSIMALE))
        alerter.reset()
        assertNotNull(alerter.onZone(HeartRateZone.MASSIMALE))
    }
}
