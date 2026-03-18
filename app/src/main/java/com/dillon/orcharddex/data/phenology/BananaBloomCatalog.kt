package com.dillon.orcharddex.data.phenology

internal object BananaBloomCatalog {
    // Banana flowering is heavily driven by plant age, heat, and management rather than a stable
    // cultivar-by-month window. We catalog common cultivars for autocomplete and tracking, but keep
    // automatic calendar forecasting disabled for bananas so warm-climate growers do not get nearly
    // year-round bloom spam.
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "banana",
            aliases = setOf("banana", "bananas", "dessert banana", "fruit banana", "plantain"),
            referenceZoneCode = "10b",
            startMonth = 5,
            startDay = 1,
            durationDays = 90,
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY
        )
    )

    val cultivarProfiles = listOf(
        banana("Apple", aliases = setOf("Apple Banana", "Manzana", "Silk")),
        banana("Blue Java", aliases = setOf("Ice Cream", "Ice Cream Banana")),
        banana("Burro", aliases = setOf("Platano Burro")),
        banana("Cavendish"),
        banana("Costa Rican Hybrid Globe"),
        banana("Cuban Red"),
        banana("Double Banana", aliases = setOf("Double")),
        banana("Dwarf Cavendish"),
        banana("Dwarf Red"),
        banana("Giant Cavendish"),
        banana("Goldfinger", aliases = setOf("FHIA-01", "FHIA 01")),
        banana("Grand Nain"),
        banana("Gros Michel"),
        banana("Lady Finger"),
        banana("Lacatan"),
        banana("Mona Lisa"),
        banana("Orinoco", aliases = setOf("Horse Banana", "Hog Banana")),
        banana("Popoulu"),
        banana("Praying Hands"),
        banana("Rajapuri"),
        banana("Robusta", aliases = setOf("Valery")),
        banana("Sweetheart"),
        banana("Thousand Fingers"),
        banana("African Rhino Horn")
    )

    private fun banana(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarBloomProfile(
        speciesKey = "banana",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Banana"
    )
}
