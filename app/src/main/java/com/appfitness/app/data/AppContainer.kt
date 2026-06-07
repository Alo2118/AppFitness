package com.appfitness.app.data

import android.content.Context
import com.appfitness.app.ble.HeartRateMonitor

/** Exposes app-wide singletons. See [AppFitnessApplication]. */
interface AppContainer {
    val repository: FitnessRepository
    val heartRateMonitor: HeartRateMonitor
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)

    override val repository: FitnessRepository by lazy {
        FitnessRepository(
            exerciseDao = database.exerciseDao(),
            workoutDao = database.workoutDao(),
            moodDao = database.moodDao(),
            programDao = database.programDao(),
            assessmentDao = database.assessmentDao(),
            cardioAssessmentDao = database.cardioAssessmentDao(),
            gpsDao = database.gpsDao(),
            rewardDao = database.rewardDao(),
            achievementDao = database.achievementDao(),
            settingsRepository = settingsRepository,
        )
    }

    override val heartRateMonitor: HeartRateMonitor by lazy { HeartRateMonitor(appContext) }

    override val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }
}
