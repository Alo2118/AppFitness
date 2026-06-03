package com.appfitness.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Records that an achievement (by catalogue key) has been unlocked. */
@Entity(tableName = "achievement_unlocks")
data class AchievementUnlock(
    @PrimaryKey val achievementKey: String,
    val unlockedAt: Long,
)
