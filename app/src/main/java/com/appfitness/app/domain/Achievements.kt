package com.appfitness.app.domain

/** Aggregate stats used to evaluate unlockable achievements. */
data class UserStats(
    val totalPoints: Int,
    val totalWorkouts: Int,
    val totalDistanceKm: Double,
    val longestRunKm: Double,
    val currentStreak: Int,
)

/** A single unlockable badge. */
data class Achievement(
    val key: String,
    val title: String,
    val description: String,
    val emoji: String,
    val points: Int,
    val condition: (UserStats) -> Boolean,
)

/** The full badge catalogue. */
object AchievementCatalog {
    val ALL: List<Achievement> = listOf(
        Achievement("first_step", "Primo passo", "Completa la tua prima attività", "👟", 20) { it.totalPoints > 0 },
        Achievement("points_500", "Collezionista", "Raggiungi 500 punti", "⭐", 50) { it.totalPoints >= 500 },
        Achievement("points_2000", "Macchina da punti", "Raggiungi 2000 punti", "💎", 150) { it.totalPoints >= 2000 },
        Achievement("workouts_10", "Costante", "Completa 10 allenamenti", "🏋️", 100) { it.totalWorkouts >= 10 },
        Achievement("workouts_25", "Ferrea disciplina", "Completa 25 allenamenti", "🥇", 250) { it.totalWorkouts >= 25 },
        Achievement("distance_10", "Esploratore", "Percorri 10 km totali", "🧭", 80) { it.totalDistanceKm >= 10 },
        Achievement("distance_42", "Maratoneta", "Percorri 42 km totali", "🏆", 250) { it.totalDistanceKm >= 42 },
        Achievement("long_run_5", "5K", "Una singola attività di almeno 5 km", "🏃", 80) { it.longestRunKm >= 5 },
        Achievement("streak_3", "Tre di fila", "Allenati 3 giorni di seguito", "🔥", 60) { it.currentStreak >= 3 },
        Achievement("streak_7", "Settimana perfetta", "Allenati 7 giorni di seguito", "🔥🔥", 200) { it.currentStreak >= 7 },
    )

    fun byKey(key: String): Achievement? = ALL.firstOrNull { it.key == key }
}

object AchievementEvaluator {
    /** Achievements whose condition is now met and that aren't already unlocked. */
    fun newlyUnlocked(stats: UserStats, alreadyUnlocked: Set<String>): List<Achievement> =
        AchievementCatalog.ALL.filter { it.key !in alreadyUnlocked && it.condition(stats) }
}
