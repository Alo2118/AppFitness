package com.appfitness.app.domain

import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.entity.GpsPoint
import com.appfitness.app.data.model.GpsActivityType
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxExporterTest {

    private fun sample(): String {
        val activity = GpsActivity(
            id = 1, type = GpsActivityType.RUN, title = "t",
            startedAt = 0L, endedAt = 1000L, durationSec = 1, distanceM = 10f, avgPaceSecPerKm = 300f,
        )
        val points = listOf(
            GpsPoint(1, 1, 0, 0.0, 45.0, 7.0),
            GpsPoint(2, 1, 1, 10.0, 45.001, 7.001),
        )
        return GpxExporter.toGpx(activity, points)
    }

    @Test
    fun `produces a valid gpx skeleton`() {
        val gpx = sample()
        assertTrue(gpx.contains("<?xml"))
        assertTrue(gpx.contains("<gpx"))
        assertTrue(gpx.trim().endsWith("</gpx>"))
    }

    @Test
    fun `includes a track point per coordinate with timestamps`() {
        val gpx = sample()
        assertTrue(gpx.contains("lat=\"45.0\""))
        assertTrue(gpx.contains("lon=\"7.001\""))
        assertTrue(gpx.contains("<time>"))
        assertEquals(2, Regex("<trkpt").findAll(gpx).count())
    }

    private fun assertEquals(expected: Int, actual: Int) =
        org.junit.Assert.assertEquals(expected.toLong(), actual.toLong())
}
