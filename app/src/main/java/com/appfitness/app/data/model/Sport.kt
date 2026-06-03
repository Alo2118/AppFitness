package com.appfitness.app.data.model

/**
 * Sport disciplines that drive sport-specific training programs. Each maps to a
 * primary and secondary [WorkoutGoal] plus a short focus shown in the UI.
 */
enum class Sport(
    val label: String,
    val emoji: String,
    val focus: String,
    val primaryGoal: WorkoutGoal,
    val secondaryGoal: WorkoutGoal,
) {
    PALLAVOLO("Pallavolo", "🏐", "Salto, esplosività, core e spalle", WorkoutGoal.TOTAL_BODY, WorkoutGoal.FORZA),
    CICLISMO("Ciclismo", "🚴", "Forza gambe e resistenza", WorkoutGoal.FORZA, WorkoutGoal.CARDIO),
    FITNESS("Fitness generale", "💪", "Forza e ipertrofia total body", WorkoutGoal.FORZA, WorkoutGoal.TOTAL_BODY),
    CALCIO("Calcio", "⚽", "Resistenza, esplosività, agilità", WorkoutGoal.CARDIO, WorkoutGoal.FORZA),
    RUNNING("Running", "🏃", "Resistenza e mobilità", WorkoutGoal.CARDIO, WorkoutGoal.MOBILITA),
    NUOTO("Nuoto", "🏊", "Spalle, core e mobilità", WorkoutGoal.TOTAL_BODY, WorkoutGoal.MOBILITA),
    TENNIS("Tennis", "🎾", "Core ed esplosività laterale", WorkoutGoal.TOTAL_BODY, WorkoutGoal.FORZA),
    BASKET("Basket", "🏀", "Salto, agilità, resistenza", WorkoutGoal.TOTAL_BODY, WorkoutGoal.CARDIO);

    /** Goal to use for the n-th session of a program (alternates primary/secondary). */
    fun goalForSession(sessionIndex: Int): WorkoutGoal =
        if (sessionIndex % 2 == 0) primaryGoal else secondaryGoal
}
