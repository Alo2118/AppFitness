package com.appfitness.app.data

import androidx.room.TypeConverter
import com.appfitness.app.data.model.Equipment
import com.appfitness.app.data.model.ExerciseCategory
import com.appfitness.app.data.model.FitnessLevel
import com.appfitness.app.data.model.GpsActivityType
import com.appfitness.app.data.model.Sport

/** Room type converters for the enums we persist. */
class Converters {
    @TypeConverter
    fun categoryToString(category: ExerciseCategory): String = category.name

    @TypeConverter
    fun stringToCategory(value: String): ExerciseCategory =
        runCatching { ExerciseCategory.valueOf(value) }.getOrDefault(ExerciseCategory.FULL_BODY)

    @TypeConverter
    fun sportToString(sport: Sport): String = sport.name

    @TypeConverter
    fun stringToSport(value: String): Sport =
        runCatching { Sport.valueOf(value) }.getOrDefault(Sport.FITNESS)

    @TypeConverter
    fun levelToString(level: FitnessLevel): String = level.name

    @TypeConverter
    fun stringToLevel(value: String): FitnessLevel =
        runCatching { FitnessLevel.valueOf(value) }.getOrDefault(FitnessLevel.INTERMEDIO)

    @TypeConverter
    fun gpsTypeToString(type: GpsActivityType): String = type.name

    @TypeConverter
    fun stringToGpsType(value: String): GpsActivityType =
        runCatching { GpsActivityType.valueOf(value) }.getOrDefault(GpsActivityType.RUN)

    @TypeConverter
    fun equipmentToString(equipment: Equipment): String = equipment.name

    @TypeConverter
    fun stringToEquipment(value: String): Equipment =
        runCatching { Equipment.valueOf(value) }.getOrDefault(Equipment.BODYWEIGHT)
}
