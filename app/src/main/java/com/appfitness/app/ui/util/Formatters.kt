package com.appfitness.app.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ITALIAN)
private val dayFormat = SimpleDateFormat("EEE d MMM", Locale.ITALIAN)

/** Formats seconds as mm:ss, or h:mm:ss when over an hour. */
fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

fun formatTimestamp(millis: Long): String = dateFormat.format(Date(millis))

fun formatDay(millis: Long): String = dayFormat.format(Date(millis))
