package com.dillon.orcharddex.data.local

import androidx.room.TypeConverter
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistPriority

class Converters {
    @TypeConverter
    fun fromPlantType(value: PlantType): String = value.name

    @TypeConverter
    fun toPlantType(value: String): PlantType = PlantType.valueOf(value)

    @TypeConverter
    fun fromTreeStatus(value: TreeStatus): String = value.name

    @TypeConverter
    fun toTreeStatus(value: String): TreeStatus = TreeStatus.valueOf(value)

    @TypeConverter
    fun fromFrostSensitivity(value: FrostSensitivityLevel): String = value.name

    @TypeConverter
    fun toFrostSensitivity(value: String): FrostSensitivityLevel = FrostSensitivityLevel.valueOf(value)

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String = value.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType = RecurrenceType.valueOf(value)

    @TypeConverter
    fun fromLeadTimeMode(value: LeadTimeMode): String = value.name

    @TypeConverter
    fun toLeadTimeMode(value: String): LeadTimeMode = LeadTimeMode.valueOf(value)

    @TypeConverter
    fun fromWishlistPriority(value: WishlistPriority): String = value.name

    @TypeConverter
    fun toWishlistPriority(value: String): WishlistPriority = WishlistPriority.valueOf(value)
}
