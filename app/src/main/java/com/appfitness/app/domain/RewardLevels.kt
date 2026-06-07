package com.appfitness.app.domain

/** Maps a cumulative point total to a level and progress within that level. */
object RewardLevels {
    const val PER_LEVEL = 500

    fun levelFor(points: Int): Int = points / PER_LEVEL + 1

    fun pointsIntoLevel(points: Int): Int = points % PER_LEVEL

    fun pointsToNextLevel(points: Int): Int = PER_LEVEL - pointsIntoLevel(points)

    /** 0..1 progress toward the next level. */
    fun levelProgress(points: Int): Float = pointsIntoLevel(points).toFloat() / PER_LEVEL
}
