package com.appfitness.app.domain

/**
 * Reward formulas for completing any kind of activity, so the dopamine system is
 * active app-wide — not only during GPS sessions. Pure and testable.
 */
object ActivityRewardRules {

    fun workout(setsCompleted: Int, volumeKg: Float, moodDelta: Int?): RewardEvent {
        var points = 30 + setsCompleted * 10 + (volumeKg / 50).toInt()
        val improvedMood = moodDelta != null && moodDelta > 0
        if (improvedMood) points += 40
        val title = if (improvedMood) {
            "Allenamento completato e umore su! 💪🙂"
        } else {
            "Allenamento completato! 💪"
        }
        val tier = if (setsCompleted >= 12 || improvedMood) RewardTier.MEDIUM else RewardTier.SMALL
        return RewardEvent(title, points.coerceAtLeast(20), tier)
    }

    fun guidedSet(reps: Int, target: Int): RewardEvent =
        if (target > 0 && reps >= target) {
            RewardEvent("Serie completata, obiettivo raggiunto! 🎯", 30 + (reps - target) * 2, RewardTier.MEDIUM)
        } else {
            RewardEvent("Serie completata!", 15, RewardTier.SMALL)
        }

    fun timedSet(durationSec: Int): RewardEvent =
        RewardEvent("Tempo completato! ⏱", (15 + durationSec / 6).coerceAtMost(60), RewardTier.SMALL)

    fun cardioTest(): RewardEvent =
        RewardEvent("Test cardio completato! ❤️", 80, RewardTier.MEDIUM)

    fun moodCheckIn(): RewardEvent =
        RewardEvent("Check-in emozionale registrato 📝", 10, RewardTier.SMALL)

    /** Wraps a finished GPS session's in-session score into the global ledger. */
    fun gpsSession(sessionScore: Int): RewardEvent {
        val tier = when {
            sessionScore >= 800 -> RewardTier.EPIC
            sessionScore >= 200 -> RewardTier.MEDIUM
            else -> RewardTier.SMALL
        }
        return RewardEvent("Sessione GPS completata! 🏃", sessionScore.coerceAtLeast(20), tier)
    }
}
