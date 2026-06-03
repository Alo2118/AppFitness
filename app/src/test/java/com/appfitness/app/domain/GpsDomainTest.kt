package com.appfitness.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoUtilsTest {

    @Test
    fun `one degree of latitude is about 111 km`() {
        val d = GeoUtils.haversineMeters(0.0, 0.0, 1.0, 0.0)
        assertEquals(111_195.0, d, 500.0)
    }

    @Test
    fun `same point is zero distance`() {
        assertEquals(0.0, GeoUtils.haversineMeters(45.07, 7.68, 45.07, 7.68), 0.001)
    }

    @Test
    fun `pace formats as minutes and seconds`() {
        assertEquals("5:30", GeoUtils.formatPace(330.0))
        assertEquals("--:--", GeoUtils.formatPace(0.0))
    }
}

class RouteTrackerTest {

    @Test
    fun `accumulates distance across fixes`() {
        val tracker = RouteTracker()
        tracker.onLocation(45.0000, 7.0000, 0)
        tracker.onLocation(45.0009, 7.0000, 60)  // ~100 m north
        assertTrue("expected ~100m, got ${tracker.totalDistanceM}", tracker.totalDistanceM in 90.0..110.0)
    }

    @Test
    fun `average pace reflects distance over time`() {
        val tracker = RouteTracker()
        tracker.onLocation(45.0, 7.0, 0)
        tracker.onLocation(45.0090, 7.0, 300) // ~1000 m in 300 s -> 5:00/km
        val pace = tracker.averagePaceSecPerKm()
        assertEquals(300.0, pace, 30.0)
    }

    @Test
    fun `sub-metre jitter does not inflate distance`() {
        val tracker = RouteTracker()
        tracker.onLocation(45.0, 7.0, 0)
        repeat(20) { tracker.onLocation(45.0000001, 7.0000001, it + 1) }
        assertTrue(tracker.totalDistanceM < 1.0)
    }
}

class GhostTest {

    @Test
    fun `constant pace covers expected distance`() {
        val ghost = Ghosts.pace(300.0) // 5:00/km
        assertEquals(1000.0, ghost.distanceAt(300), 0.01)
        assertEquals(2000.0, ghost.distanceAt(600), 0.01)
    }

    @Test
    fun `target time reduces to required pace`() {
        val ghost = Ghosts.targetTime(distanceM = 1000.0, totalSec = 300)
        assertEquals(500.0, ghost.distanceAt(150), 0.01)
    }

    @Test
    fun `replay interpolates between samples`() {
        val ghost = Ghosts.replay(
            listOf(
                RouteSample(0, 0.0, 0.0, 0.0),
                RouteSample(100, 200.0, 0.0, 0.0),
            )
        )
        assertEquals(100.0, ghost.distanceAt(50), 0.01)
        assertEquals(200.0, ghost.distanceAt(999), 0.01) // clamps to last
    }

    @Test
    fun `lead is positive when ahead of ghost`() {
        val ghost = Ghosts.pace(300.0) // 1000 m at 300 s
        val lead = GhostComparator.leadMeters(ghost, myDistanceM = 1100.0, elapsedSec = 300)
        assertEquals(100.0, lead, 0.01)
    }
}
