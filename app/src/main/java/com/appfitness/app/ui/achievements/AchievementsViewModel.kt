package com.appfitness.app.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.domain.Achievement
import com.appfitness.app.domain.AchievementCatalog
import com.appfitness.app.domain.RewardLevels
import com.appfitness.app.domain.StreakCalculator
import com.appfitness.app.domain.WeeklyGoal
import com.appfitness.app.domain.WeeklyGoals
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class BadgeUi(val achievement: Achievement, val unlocked: Boolean)

data class AchievementsUiState(
    val level: Int = 1,
    val totalPoints: Int = 0,
    val streak: Int = 0,
    val weeklyGoals: List<WeeklyGoal> = emptyList(),
    val badges: List<BadgeUi> = emptyList(),
)

class AchievementsViewModel(repository: FitnessRepository) : ViewModel() {

    val uiState: StateFlow<AchievementsUiState> = combine(
        repository.totalRewardPoints,
        repository.completedSessions(),
        repository.gpsActivities,
        repository.allRewards,
        repository.unlockedAchievements,
    ) { points, sessions, gps, rewards, unlocked ->
        val weekStart = WeeklyGoals.startOfWeekMillis()
        val workoutsWeek = sessions.count { it.session.startedAt >= weekStart }
        val kmWeek = (gps.filter { it.startedAt >= weekStart }.sumOf { it.distanceM.toDouble() } / 1000.0).toInt()
        val pointsWeek = rewards.filter { it.timestamp >= weekStart }.sumOf { it.points }
        val streak = StreakCalculator.currentStreak(
            StreakCalculator.activeDaysFromMillis(rewards.map { it.timestamp }),
            LocalDate.now(),
        )
        val unlockedSet = unlocked.toSet()
        AchievementsUiState(
            level = RewardLevels.levelFor(points),
            totalPoints = points,
            streak = streak,
            weeklyGoals = WeeklyGoals.goals(workoutsWeek, kmWeek, pointsWeek),
            badges = AchievementCatalog.ALL.map { BadgeUi(it, it.key in unlockedSet) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementsUiState(),
    )
}
