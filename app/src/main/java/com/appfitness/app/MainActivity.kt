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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.appfitness.app.reward.RewardCenter
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appfitness.app.ui.achievements.AchievementsScreen
import com.appfitness.app.ui.exercises.ExerciseListScreen
import com.appfitness.app.ui.history.HistoryScreen
import com.appfitness.app.ui.home.HomeScreen
import com.appfitness.app.ui.cardio.CardioTestScreen
import com.appfitness.app.ui.gps.GpsActivityDetailScreen
import com.appfitness.app.ui.gps.GpsScreen
import com.appfitness.app.ui.gps.GpsTrackingScreen
import com.appfitness.app.ui.mood.MoodScreen
import com.appfitness.app.ui.programs.ProgramsScreen
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

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        RewardCenter.events.collect { event ->
            rewardHaptic(context)
            snackbarHostState.showSnackbar("🏆 ${event.title}  +${event.points}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    onOpenGps = { navController.navigate(Routes.GPS) },
                    onOpenAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                )
            }
            composable(Routes.ACHIEVEMENTS) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }
            composable(TopDestination.PROGRAMS.route) {
                ProgramsScreen(
                    onStartSession = { sessionId ->
                        navController.navigate(Routes.workout(sessionId))
                    },
                    onOpenCardioTest = { navController.navigate(Routes.CARDIO_TEST) },
                )
            }
            composable(Routes.CARDIO_TEST) {
                CardioTestScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.GPS) {
                GpsScreen(
                    onBack = { navController.popBackStack() },
                    onStartTracking = { navController.navigate(Routes.GPS_TRACKING) },
                    onOpenActivity = { id -> navController.navigate(Routes.gpsDetail(id)) },
                )
            }
            composable(Routes.GPS_TRACKING) {
                GpsTrackingScreen(onFinished = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.GPS_DETAIL}/{activityId}",
                arguments = listOf(navArgument("activityId") { type = NavType.LongType }),
            ) { entry ->
                GpsActivityDetailScreen(
                    activityId = entry.arguments?.getLong("activityId") ?: 0L,
                    onBack = { navController.popBackStack() },
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

/** Short celebratory vibration when a reward is earned anywhere in the app. */
private fun rewardHaptic(context: android.content.Context) {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE)
            as? android.os.VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
    }
    runCatching {
        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(120, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
