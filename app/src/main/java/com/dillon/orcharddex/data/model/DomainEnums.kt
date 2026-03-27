package com.dillon.orcharddex.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlantType {
    IN_GROUND,
    CONTAINER
}

@Serializable
enum class TreeStatus {
    ACTIVE,
    REMOVED,
    DEAD,
    GIFTED,
    SOLD
}

@Serializable
enum class FrostSensitivityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CUSTOM
}

@Serializable
enum class BloomTimingMode {
    AUTO,
    CUSTOM
}

@Serializable
enum class EventType {
    PLANTED,
    REPOTTED,
    PRUNED,
    FERTILIZED,
    SPRAYED,
    BUD,
    BLOOM,
    FRUIT_SET,
    HARVEST,
    PEST_OBSERVED,
    DISEASE_OBSERVED,
    FROST_DAMAGE,
    HEAT_STRESS,
    GRAFTED,
    WATERED,
    NOTE
}

@Serializable
enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    EVERY_X_DAYS
}

@Serializable
enum class LeadTimeMode {
    SAME_DAY,
    ONE_DAY_BEFORE,
    CUSTOM_HOURS
}

@Serializable
enum class WishlistPriority {
    LOW,
    MEDIUM,
    HIGH
}

val CommonSpeciesSuggestions = listOf(
    "Mango",
    "Avocado",
    "Citrus",
    "Lychee",
    "Longan",
    "Guava",
    "Fig",
    "Loquat",
    "Sapodilla",
    "Jaboticaba",
    "Dragon fruit",
    "Mulberry",
    "Persimmon",
    "Banana",
    "Papaya",
    "Passionfruit"
)
