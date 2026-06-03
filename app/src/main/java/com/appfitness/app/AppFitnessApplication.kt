package com.appfitness.app

import android.app.Application
import com.appfitness.app.data.AppContainer
import com.appfitness.app.data.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom [Application] that owns the app-wide dependency container.
 *
 * We use a lightweight manual dependency-injection container instead of a DI
 * framework: it keeps the project easy to read and removes annotation-processing
 * surprises while still giving every ViewModel a single shared repository.
 */
class AppFitnessApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        // Populate the starter exercise library on first launch.
        applicationScope.launch {
            container.repository.seedExercisesIfEmpty()
        }
    }
}
