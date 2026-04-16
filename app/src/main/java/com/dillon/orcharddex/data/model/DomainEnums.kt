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
enum class NurseryStage(val label: String) {
    NONE("None"),
    MOTHER("Mother"),
    PROPAGATING("Propagating"),
    ROOTING("Rooting"),
    LINER("Liner"),
    GROWING_ON("Growing on"),
    SALE_READY("Sale ready"),
    HOLD("Hold")
}

@Serializable
enum class TreeOriginType(val label: String) {
    PURCHASED("Purchased"),
    GIFTED("Gifted"),
    PROPAGATED("Propagated"),
    SEED_GROWN("Seed-grown"),
    UNKNOWN("Unknown")
}

@Serializable
enum class PropagationMethod(val label: String) {
    AIR_LAYER("Air layer"),
    CUTTING("Cutting"),
    GRAFT("Graft"),
    SEEDLING("Seedling"),
    DIVISION("Division"),
    SUCKER("Sucker"),
    OTHER("Other")
}

@Serializable
enum class SaleKind {
    TREE,
    HARVEST
}

@Serializable
enum class SaleChannel(val label: String) {
    DIRECT("Direct"),
    FARM_STAND("Farm stand"),
    MARKET("Market"),
    NURSERY("Nursery"),
    ONLINE("Online"),
    OTHER("Other")
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
    "Apple",
    "Mango",
    "Avocado",
    "Apricot",
    "Coffee",
    "Citrus",
    "Pomelo",
    "Lemon",
    "Lime",
    "Orange",
    "Mandarin",
    "Grapefruit",
    "Kumquat",
    "Calamondin",
    "Yuzu",
    "Citron",
    "Lychee",
    "Longan",
    "Guava",
    "Cherimoya",
    "Fig",
    "Blueberry",
    "Strawberry",
    "Cranberry",
    "Grape",
    "Muscadine",
    "Kiwi",
    "Kiwiberry",
    "Olive",
    "Feijoa",
    "Pecan",
    "Honeyberry",
    "Nectarine",
    "Pear",
    "European Pear",
    "Asian Pear",
    "Plum",
    "Japanese Plum",
    "European Plum",
    "Pomegranate",
    "Raspberry",
    "Black Raspberry",
    "Blackberry",
    "Black Mulberry",
    "White Mulberry",
    "Loquat",
    "Pawpaw",
    "Sapodilla",
    "Jaboticaba",
    "Dragon fruit",
    "Mulberry",
    "Persimmon",
    "Banana",
    "Papaya",
    "Passionfruit",
    "Watermelon",
    "Cantaloupe",
    "Honeydew",
    "Canary Melon",
    "Galia Melon",
    "Casaba Melon",
    "Persian Melon"
)
