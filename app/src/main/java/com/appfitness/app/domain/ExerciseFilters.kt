package com.appfitness.app.domain

import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.model.Equipment

/** Filters used when generating workouts/programs so they respect what the user owns. */
object ExerciseFilters {

    /**
     * Keeps exercises doable with the user's [owned] equipment. Bodyweight is
     * always available; an empty [owned] set means "no restriction" (use all).
     */
    fun forEquipment(exercises: List<Exercise>, owned: Set<Equipment>): List<Exercise> {
        if (owned.isEmpty()) return exercises
        return exercises.filter { it.equipment == Equipment.BODYWEIGHT || it.equipment in owned }
    }
}
