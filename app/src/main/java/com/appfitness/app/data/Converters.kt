package com.appfitness.app.data

import androidx.room.TypeConverter
import com.appfitness.app.data.model.ExerciseCategory

/** Room type converters for the enums we persist. */
class Converters {
    @TypeConverter
    fun categoryToString(category: ExerciseCategory): String = category.name

    @TypeConverter
    fun stringToCategory(value: String): ExerciseCategory =
        runCatching { ExerciseCategory.valueOf(value) }.getOrDefault(ExerciseCategory.FULL_BODY)
}
