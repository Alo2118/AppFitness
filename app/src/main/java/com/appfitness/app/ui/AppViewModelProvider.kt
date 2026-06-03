package com.appfitness.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfitness.app.AppFitnessApplication
import com.appfitness.app.ui.achievements.AchievementsViewModel
import com.appfitness.app.ui.cardio.CardioTestViewModel
import com.appfitness.app.ui.exercises.ExerciseViewModel
import com.appfitness.app.ui.gps.GpsViewModel
import com.appfitness.app.ui.history.HistoryViewModel
import com.appfitness.app.ui.home.HomeViewModel
import com.appfitness.app.ui.mood.MoodViewModel
import com.appfitness.app.ui.programs.ProgramsViewModel
import com.appfitness.app.ui.workout.WorkoutViewModel

/** Central place wiring every ViewModel to the shared repository. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { HomeViewModel(repository()) }
        initializer { ExerciseViewModel(repository()) }
        initializer { MoodViewModel(repository()) }
        initializer { HistoryViewModel(repository()) }
        initializer { ProgramsViewModel(repository()) }
        initializer { CardioTestViewModel(repository(), application().container.heartRateMonitor) }
        initializer { GpsViewModel(repository()) }
        initializer { AchievementsViewModel(repository()) }
        initializer { WorkoutViewModel(repository(), this.createSavedStateHandle()) }
    }
}

private fun CreationExtras.application(): AppFitnessApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AppFitnessApplication

private fun CreationExtras.repository() = application().container.repository
