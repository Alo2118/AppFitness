package com.appfitness.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appfitness.app.data.dao.ExerciseDao
import com.appfitness.app.data.dao.MoodDao
import com.appfitness.app.data.dao.WorkoutDao
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.WorkoutSession

@Database(
    entities = [Exercise::class, WorkoutSession::class, SetLog::class, MoodEntry::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun moodDao(): MoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appfitness.db",
                ).build().also { INSTANCE = it }
            }
    }
}
