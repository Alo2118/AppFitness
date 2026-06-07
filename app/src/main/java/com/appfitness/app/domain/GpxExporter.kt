package com.appfitness.app.domain

import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.entity.GpsPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Serialises a recorded activity to the standard GPX 1.1 track format. */
object GpxExporter {

    fun toGpx(activity: GpsActivity, points: List<GpsPoint>): String {
        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val sorted = points.sortedBy { it.elapsedSec }
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<gpx version=\"1.1\" creator=\"AppFitness\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
            append("  <trk>\n")
            append("    <name>${activity.type.label} - ${iso.format(java.util.Date(activity.startedAt))}</name>\n")
            append("    <trkseg>\n")
            sorted.forEach { p ->
                val time = iso.format(java.util.Date(activity.startedAt + p.elapsedSec * 1000L))
                append("      <trkpt lat=\"${p.lat}\" lon=\"${p.lon}\"><time>$time</time></trkpt>\n")
            }
            append("    </trkseg>\n")
            append("  </trk>\n")
            append("</gpx>\n")
        }
    }
}
