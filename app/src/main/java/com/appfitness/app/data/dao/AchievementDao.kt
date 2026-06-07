package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appfitness.app.data.entity.AchievementUnlock
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(unlock: AchievementUnlock): Long

    @Query("SELECT achievementKey FROM achievement_unlocks")
    fun observeUnlockedKeys(): Flow<List<String>>

    @Query("SELECT achievementKey FROM achievement_unlocks")
    suspend fun unlockedKeys(): List<String>
}
