package com.appfitness.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level tabs shown in the bottom navigation bar. */
enum class TopDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    HISTORY("history", "Storico", Icons.Filled.History, Icons.Outlined.History),
    MOOD("mood", "Umore", Icons.Filled.Mood, Icons.Outlined.Mood),
    EXERCISES("exercises", "Esercizi", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
}

object Routes {
    const val WORKOUT = "workout"
    fun workout(sessionId: Long) = "$WORKOUT/$sessionId"
}
