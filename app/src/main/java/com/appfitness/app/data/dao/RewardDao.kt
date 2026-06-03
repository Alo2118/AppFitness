package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appfitness.app.data.entity.RewardEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Insert
    suspend fun insert(entry: RewardEntry): Long

    @Query("SELECT COALESCE(SUM(points), 0) FROM reward_entries")
    fun observeTotalPoints(): Flow<Int>

    @Query("SELECT * FROM reward_entries ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RewardEntry>>
}
