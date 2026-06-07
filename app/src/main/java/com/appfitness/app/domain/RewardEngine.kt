package com.appfitness.app.domain

import kotlin.random.Random

/** How big a reward is — drives voice, haptics and the visual celebration. */
enum class RewardTier { SMALL, MEDIUM, EPIC }

/** A dopamine hit: immediate positive reinforcement for something the user did. */
data class RewardEvent(
    val title: String,
    val points: Int,
    val tier: RewardTier,
)

/** Snapshot fed to the reward engine each update. */
data class RunRewardState(
    val distanceM: Double,
    val elapsedSec: Int,
    val ghostLeadM: Double? = null,
    val hrInTargetZone: Boolean = false,
)

/**
 * In-session reward system designed around immediate, performance-linked
 * positive reinforcement (the dopamine loop): kilometre splits, faster-than-last
 * "negative split" combos, surprise bonuses on a **variable ratio** (the core of
 * habit-forming reward schedules), distance milestones, overtaking the ghost and
 * staying in the target heart-rate zone.
 *
 * Pure and deterministic (inject a seeded [Random] in tests).
 */
class RewardEngine(private val random: Random = Random.Default) {

    var totalScore = 0
        private set
    var combo = 0
        private set

    private var lastKm = 0
    private var lastSplitElapsed = 0
    private var prevSplitSec: Int? = null
    private var prevGhostAhead: Boolean? = null
    private val firedMilestones = mutableSetOf<Int>()
    private var lastZoneBucket = -1

    /** Returns a reward to celebrate for this update, or null. */
    fun onUpdate(state: RunRewardState): RewardEvent? {
        milestoneEvent(state)?.let { return award(it) }
        ghostOvertakeEvent(state)?.let { return award(it) }
        splitEvent(state)?.let { return award(it) }
        zoneEvent(state)?.let { return award(it) }
        return null
    }

    /** End-of-session epic reward when a personal record is beaten. */
    fun personalRecord(title: String, points: Int = 500): RewardEvent =
        award(RewardEvent(title, points, RewardTier.EPIC))

    fun reset() {
        totalScore = 0
        combo = 0
        lastKm = 0
        lastSplitElapsed = 0
        prevSplitSec = null
        prevGhostAhead = null
        firedMilestones.clear()
        lastZoneBucket = -1
    }

    private fun award(event: RewardEvent): RewardEvent {
        totalScore += event.points
        return event
    }

    private fun milestoneEvent(state: RunRewardState): RewardEvent? {
        val reached = MILESTONES.firstOrNull { it.meters <= state.distanceM && it.meters !in firedMilestones }
            ?: return null
        firedMilestones.add(reached.meters)
        return RewardEvent(reached.title, reached.points, reached.tier)
    }

    private fun ghostOvertakeEvent(state: RunRewardState): RewardEvent? {
        val lead = state.ghostLeadM ?: return null
        val ahead = lead >= 0
        val was = prevGhostAhead
        prevGhostAhead = ahead
        return if (was == false && ahead) {
            RewardEvent("Hai superato il fantasma! 👻", 150, RewardTier.MEDIUM)
        } else null
    }

    private fun splitEvent(state: RunRewardState): RewardEvent? {
        val km = (state.distanceM / 1000.0).toInt()
        if (km < 1 || km <= lastKm) return null
        val splitSec = state.elapsedSec - lastSplitElapsed
        val faster = prevSplitSec?.let { splitSec in 1 until it } ?: false
        lastKm = km
        lastSplitElapsed = state.elapsedSec
        prevSplitSec = splitSec

        var points = SPLIT_POINTS
        val parts = StringBuilder("Chilometro $km!")
        val tier: RewardTier
        if (faster) {
            combo += 1
            points += COMBO_BONUS * combo
            parts.append(" Split più veloce, combo x$combo! 🔥")
            tier = RewardTier.MEDIUM
        } else {
            combo = 0
            tier = RewardTier.SMALL
        }
        // Variable-ratio surprise bonus (~20% of splits).
        if (random.nextInt(100) < SURPRISE_CHANCE) {
            points += SURPRISE_BONUS
            parts.append(" Bonus a sorpresa +$SURPRISE_BONUS! 🎁")
        }
        return RewardEvent(parts.toString(), points, tier)
    }

    private fun zoneEvent(state: RunRewardState): RewardEvent? {
        if (!state.hrInTargetZone || state.elapsedSec <= 0) return null
        val bucket = state.elapsedSec / 60
        if (bucket <= lastZoneBucket) return null
        lastZoneBucket = bucket
        return RewardEvent("Un minuto in zona, ottimo! 💚", ZONE_POINTS, RewardTier.SMALL)
    }

    private data class Milestone(val meters: Int, val title: String, val points: Int, val tier: RewardTier)

    companion object {
        private const val SPLIT_POINTS = 50
        private const val COMBO_BONUS = 20
        private const val SURPRISE_BONUS = 40
        private const val SURPRISE_CHANCE = 20
        private const val ZONE_POINTS = 20

        private val MILESTONES = listOf(
            Milestone(1000, "Primo chilometro! 🎉", 100, RewardTier.MEDIUM),
            Milestone(5000, "5 km, grande! 💪", 250, RewardTier.MEDIUM),
            Milestone(10000, "10 km, fenomeno! 🏅", 500, RewardTier.EPIC),
            Milestone(21097, "Mezza maratona! 🤯", 1000, RewardTier.EPIC),
            Milestone(42195, "MARATONA! 🏆", 2000, RewardTier.EPIC),
        )
    }
}
