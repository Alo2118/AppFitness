package com.appfitness.app.data

import android.content.Context

/** Exposes app-wide singletons. See [AppFitnessApplication]. */
interface AppContainer {
    val repository: FitnessRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database = AppDatabase.getInstance(context)

    override val repository: FitnessRepository by lazy {
        FitnessRepository(
            exerciseDao = database.exerciseDao(),
            workoutDao = database.workoutDao(),
            moodDao = database.moodDao(),
            programDao = database.programDao(),
            assessmentDao = database.assessmentDao(),
        )
    }
}
