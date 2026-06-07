package com.appfitness.app.domain

/**
 * A virtual opponent: tells how far it has travelled at a given elapsed time.
 * Used for the "ghost" mode — beat a previous run, hold a pace, or chase a time.
 */
interface Ghost {
    fun distanceAt(elapsedSec: Int): Double
}

/** Steady pace, e.g. 5:30/km. */
class ConstantPaceGhost(private val paceSecPerKm: Double) : Ghost {
    override fun distanceAt(elapsedSec: Int): Double =
        if (paceSecPerKm <= 0) 0.0 else elapsedSec / paceSecPerKm * 1000.0
}

/** Replays a previous route by interpolating its (time, distance) samples. */
class ReplayGhost(samples: List<RouteSample>) : Ghost {
    private val points = samples.sortedBy { it.elapsedSec }

    override fun distanceAt(elapsedSec: Int): Double {
        if (points.isEmpty()) return 0.0
        if (elapsedSec <= points.first().elapsedSec) return points.first().cumulativeDistanceM
        if (elapsedSec >= points.last().elapsedSec) return points.last().cumulativeDistanceM
        for (i in 1 until points.size) {
            val b = points[i]
            if (b.elapsedSec >= elapsedSec) {
                val a = points[i - 1]
                val span = (b.elapsedSec - a.elapsedSec).toDouble()
                if (span <= 0) return b.cumulativeDistanceM
                val f = (elapsedSec - a.elapsedSec) / span
                return a.cumulativeDistanceM + f * (b.cumulativeDistanceM - a.cumulativeDistanceM)
            }
        }
        return points.last().cumulativeDistanceM
    }
}

object Ghosts {
    fun pace(secPerKm: Double): Ghost = ConstantPaceGhost(secPerKm)

    /** Beat [distanceM] in [totalSec]: reduces to the required constant pace. */
    fun targetTime(distanceM: Double, totalSec: Int): Ghost {
        if (distanceM <= 0) return ConstantPaceGhost(0.0)
        return ConstantPaceGhost(totalSec / (distanceM / 1000.0))
    }

    fun replay(samples: List<RouteSample>): Ghost = ReplayGhost(samples)
}

object GhostComparator {
    /** Positive => the user is ahead of the ghost (more metres covered). */
    fun leadMeters(ghost: Ghost, myDistanceM: Double, elapsedSec: Int): Double =
        myDistanceM - ghost.distanceAt(elapsedSec)
}
