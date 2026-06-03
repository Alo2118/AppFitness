package com.appfitness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appfitness.app.ui.exercises.ExerciseListScreen
import com.appfitness.app.ui.history.HistoryScreen
import com.appfitness.app.ui.home.HomeScreen
import com.appfitness.app.ui.mood.MoodScreen
import com.appfitness.app.ui.navigation.Routes
import com.appfitness.app.ui.navigation.TopDestination
import com.appfitness.app.ui.theme.AppFitnessTheme
import com.appfitness.app.ui.workout.WorkoutScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppFitnessTheme {
                AppFitnessApp()
            }
        }
    }
}

@Composable
fun AppFitnessApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = TopDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopDestination.entries.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(TopDestination.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label,
                                )
                            },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(TopDestination.HOME.route) {
                HomeScreen(
                    onStartWorkout = { sessionId ->
                        navController.navigate(Routes.workout(sessionId))
                    },
                    onOpenSession = { /* history detail lives in the History tab */ },
                )
            }
            composable(TopDestination.HISTORY.route) { HistoryScreen() }
            composable(TopDestination.MOOD.route) { MoodScreen() }
            composable(TopDestination.EXERCISES.route) { ExerciseListScreen() }

            composable(
                route = "${Routes.WORKOUT}/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) {
                WorkoutScreen(onFinished = { navController.popBackStack() })
            }
        }
    }
}
