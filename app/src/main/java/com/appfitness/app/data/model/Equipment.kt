package com.appfitness.app.data.model

/** Equipment an exercise needs — lets users filter by what they own. */
enum class Equipment(val label: String, val emoji: String) {
    BODYWEIGHT("Corpo libero", "🤸"),
    DUMBBELL("Manubri", "🏋️"),
    BARBELL("Bilanciere", "🏋️‍♂️"),
    BENCH("Panca", "🛋️"),
    LAT_MACHINE("Lat machine", "🎚️"),
    LEG_MACHINE("Leg machine", "🦵"),
    KETTLEBELL("Kettlebell", "🔔"),
    BAND("Elastico", "➰"),
    MACHINE("Macchina", "⚙️"),
    OTHER("Altro", "🧰");
}
