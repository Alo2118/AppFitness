package com.appfitness.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.appfitness.app.AppFitnessApplication
import com.appfitness.app.data.model.UserPreferences

/** Observes the user's saved profile/preferences from anywhere in the UI tree. */
@Composable
fun rememberUserPreferences(): UserPreferences {
    val context = LocalContext.current
    val repository = remember {
        (context.applicationContext as AppFitnessApplication).container.settingsRepository
    }
    val prefs by repository.preferences.collectAsState(initial = UserPreferences())
    return prefs
}
