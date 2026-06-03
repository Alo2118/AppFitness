package com.appfitness.app.domain

import com.appfitness.app.data.model.ExerciseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepProfilesTest {

    @Test
    fun `strength has a higher threshold and slower cadence than core`() {
        val strength = RepProfiles.forCategory(ExerciseCategory.STRENGTH)
        val core = RepProfiles.forCategory(ExerciseCategory.CORE)
        assertTrue(strength.highThreshold > core.highThreshold)
        assertTrue(strength.minRepIntervalMs > core.minRepIntervalMs)
    }

    @Test
    fun `explosive categories allow the fastest cadence`() {
        val explosive = RepProfiles.forCategory(ExerciseCategory.CARDIO)
        val strength = RepProfiles.forCategory(ExerciseCategory.STRENGTH)
        assertTrue(explosive.minRepIntervalMs < strength.minRepIntervalMs)
    }

    @Test
    fun `null category falls back to default`() {
        assertEquals(RepProfiles.DEFAULT, RepProfiles.forCategory(null))
    }
}
