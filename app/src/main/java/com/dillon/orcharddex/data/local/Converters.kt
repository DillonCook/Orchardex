package com.dillon.orcharddex.data.local

import androidx.room.TypeConverter
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.MicroclimateFlag
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.PollinationMode
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.SelfCompatibility
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
    fun fromBloomTimingMode(value: BloomTimingMode): String = value.name

    @TypeConverter
    fun toBloomTimingMode(value: String): BloomTimingMode = BloomTimingMode.valueOf(value)

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

    @TypeConverter
    fun fromSelfCompatibility(value: SelfCompatibility?): String? = value?.name

    @TypeConverter
    fun toSelfCompatibility(value: String?): SelfCompatibility? = value?.let(SelfCompatibility::valueOf)

    @TypeConverter
    fun fromPollinationMode(value: PollinationMode?): String? = value?.name

    @TypeConverter
    fun toPollinationMode(value: String?): PollinationMode? = value?.let(PollinationMode::valueOf)

    @TypeConverter
    fun fromHemisphere(value: Hemisphere): String = value.name

    @TypeConverter
    fun toHemisphere(value: String): Hemisphere = Hemisphere.valueOf(value)

    @TypeConverter
    fun fromChillHoursBand(value: ChillHoursBand): String = value.name

    @TypeConverter
    fun toChillHoursBand(value: String): ChillHoursBand = ChillHoursBand.valueOf(value)

    @TypeConverter
    fun fromMicroclimateFlags(value: Set<MicroclimateFlag>): String =
        value.joinToString("|") { it.name }

    @TypeConverter
    fun toMicroclimateFlags(value: String): Set<MicroclimateFlag> =
        value.split("|")
            .mapNotNull { token -> token.takeIf(String::isNotBlank) }
            .mapNotNull { token -> runCatching { MicroclimateFlag.valueOf(token) }.getOrNull() }
            .toSet()

    @TypeConverter
    fun fromDoubleList(value: List<Double>): String =
        value.joinToString("|") { number -> number.toString() }

    @TypeConverter
    fun toDoubleList(value: String): List<Double> =
        value.split("|")
            .mapNotNull { token -> token.takeIf(String::isNotBlank) }
            .mapNotNull(String::toDoubleOrNull)
}
