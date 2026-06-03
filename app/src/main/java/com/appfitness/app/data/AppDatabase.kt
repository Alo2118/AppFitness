package com.appfitness.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appfitness.app.data.dao.AssessmentDao
import com.appfitness.app.data.dao.ExerciseDao
import com.appfitness.app.data.dao.MoodDao
import com.appfitness.app.data.dao.ProgramDao
import com.appfitness.app.data.dao.WorkoutDao
import com.appfitness.app.data.entity.AssessmentResult
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.TrainingProgram
import com.appfitness.app.data.entity.WorkoutSession

@Database(
    entities = [
        Exercise::class, WorkoutSession::class, SetLog::class, MoodEntry::class,
        TrainingProgram::class, AssessmentResult::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun moodDao(): MoodDao
    abstract fun programDao(): ProgramDao
    abstract fun assessmentDao(): AssessmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appfitness.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
