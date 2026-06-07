package com.appfitness.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.entity.GpsPoint
import com.appfitness.app.data.relation.ActivityWithPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsDao {

    @Insert
    suspend fun insertActivity(activity: GpsActivity): Long

    @Insert
    suspend fun insertPoints(points: List<GpsPoint>)

    @Query("SELECT * FROM gps_activities ORDER BY startedAt DESC")
    fun observeActivities(): Flow<List<GpsActivity>>

    @Transaction
    @Query("SELECT * FROM gps_activities WHERE id = :id")
    suspend fun getActivityWithPoints(id: Long): ActivityWithPoints?

    @Query("DELETE FROM gps_activities WHERE id = :id")
    suspend fun deleteActivity(id: Long)
}
