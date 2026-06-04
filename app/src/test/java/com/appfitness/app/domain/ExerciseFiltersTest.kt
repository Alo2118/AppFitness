package com.appfitness.app.domain

import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.model.Equipment
import com.appfitness.app.data.model.ExerciseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseFiltersTest {

    private fun ex(name: String, eq: Equipment) = Exercise(
        name = name,
        category = ExerciseCategory.STRENGTH,
        muscleGroup = "x",
        description = "",
        isTimeBased = false,
        equipment = eq,
    )

    private val library = listOf(
        ex("Squat", Equipment.BODYWEIGHT),
        ex("Panca", Equipment.BENCH),
        ex("Bilanciere curl", Equipment.BARBELL),
        ex("Manubri curl", Equipment.DUMBBELL),
        ex("Lat machine", Equipment.LAT_MACHINE),
    )

    @Test
    fun `empty owned set keeps everything`() {
        assertEquals(library.size, ExerciseFilters.forEquipment(library, emptySet()).size)
    }

    @Test
    fun `owning a barbell keeps bodyweight and barbell only`() {
        val result = ExerciseFilters.forEquipment(library, setOf(Equipment.BARBELL))
        val equips = result.map { it.equipment }.toSet()
        assertEquals(setOf(Equipment.BODYWEIGHT, Equipment.BARBELL), equips)
    }

    @Test
    fun `bodyweight is always available`() {
        val result = ExerciseFilters.forEquipment(library, setOf(Equipment.DUMBBELL))
        assertTrue(result.any { it.equipment == Equipment.BODYWEIGHT })
        assertTrue(result.none { it.equipment == Equipment.LAT_MACHINE })
    }
}
