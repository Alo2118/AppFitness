package com.appfitness.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appfitness.app.data.dao.AssessmentDao
import com.appfitness.app.data.dao.AchievementDao
import com.appfitness.app.data.dao.CardioAssessmentDao
import com.appfitness.app.data.dao.ExerciseDao
import com.appfitness.app.data.dao.GpsDao
import com.appfitness.app.data.dao.MoodDao
import com.appfitness.app.data.dao.ProgramDao
import com.appfitness.app.data.dao.RewardDao
import com.appfitness.app.data.dao.WorkoutDao
import com.appfitness.app.data.entity.AchievementUnlock
import com.appfitness.app.data.entity.AssessmentResult
import com.appfitness.app.data.entity.CardioAssessment
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.entity.GpsPoint
import com.appfitness.app.data.entity.MoodEntry
import com.appfitness.app.data.entity.RewardEntry
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.entity.TrainingProgram
import com.appfitness.app.data.entity.WorkoutSession

@Database(
    entities = [
        Exercise::class, WorkoutSession::class, SetLog::class, MoodEntry::class,
        TrainingProgram::class, AssessmentResult::class, CardioAssessment::class,
        GpsActivity::class, GpsPoint::class, RewardEntry::class, AchievementUnlock::class,
    ],
    version = 7,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun moodDao(): MoodDao
    abstract fun programDao(): ProgramDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun cardioAssessmentDao(): CardioAssessmentDao
    abstract fun gpsDao(): GpsDao
    abstract fun rewardDao(): RewardDao
    abstract fun achievementDao(): AchievementDao

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
