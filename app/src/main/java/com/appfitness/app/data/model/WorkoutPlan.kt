package com.appfitness.app.data.model

/** The training goal that drives how a generated workout is built. */
enum class WorkoutGoal(val label: String, val emoji: String) {
    FORZA("Forza", "🏋️"),
    CARDIO("Cardio", "🏃"),
    DIMAGRIMENTO("Dimagrimento", "🔥"),
    MOBILITA("Mobilità", "🧘"),
    TOTAL_BODY("Total body", "💪");
}

/** User experience level, used to scale sets, reps/duration and rest. */
enum class FitnessLevel(val label: String) {
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZATO("Avanzato");
}
