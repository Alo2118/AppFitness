package com.appfitness.app.domain

/**
 * Heart-rate training zones as a fraction of estimated max HR (220 − age).
 * Ordered from lightest to hardest; [lowerBound] is the lower %HRmax of the zone.
 */
enum class HeartRateZone(val label: String, val lowerBound: Float) {
    RIPOSO("Riposo", 0.0f),
    RISCALDAMENTO("Riscaldamento", 0.5f),
    BRUCIA_GRASSI("Brucia grassi", 0.6f),
    AEROBICA("Aerobica", 0.7f),
    ANAEROBICA("Anaerobica", 0.8f),
    MASSIMALE("Massimale", 0.9f);
}

object HeartRateZoneEvaluator {

    fun maxHr(age: Int): Int = (220 - age.coerceIn(10, 100)).coerceAtLeast(120)

    /** Returns the training zone for [hr] given the user's [age]. */
    fun zoneFor(hr: Int, age: Int): HeartRateZone {
        val pct = hr.toFloat() / maxHr(age)
        return HeartRateZone.entries.last { pct >= it.lowerBound }
    }
}

/** Relationship between the current zone and the target zone. */
enum class ZoneStatus { BELOW, IN_ZONE, ABOVE }

/**
 * Emits a voice cue only when the user *crosses* into/out of the target zone, so
 * the coach doesn't repeat itself every heartbeat. Stateful and deterministic.
 */
class ZoneAlerter(private val targetZone: HeartRateZone) {

    private var lastStatus: ZoneStatus? = null

    fun onZone(zone: HeartRateZone): String? {
        val status = when {
            zone.ordinal < targetZone.ordinal -> ZoneStatus.BELOW
            zone.ordinal > targetZone.ordinal -> ZoneStatus.ABOVE
            else -> ZoneStatus.IN_ZONE
        }
        if (status == lastStatus) return null
        lastStatus = status
        return when (status) {
            ZoneStatus.BELOW -> "Aumenta il ritmo: sei sotto la zona ${targetZone.label}."
            ZoneStatus.ABOVE -> "Rallenta: sei oltre la zona ${targetZone.label}."
            ZoneStatus.IN_ZONE -> "Perfetto, sei in zona ${targetZone.label}!"
        }
    }

    fun reset() { lastStatus = null }
}
