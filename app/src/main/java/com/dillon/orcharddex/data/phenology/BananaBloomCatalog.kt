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
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY,
            pollinationRequirement = PollinationRequirement.POLLINATION_NOT_REQUIRED,
            uncertaintyNote = "Edible bananas are best treated as continuously active rather than date-precise bloomers, because flowering depends more on plant age and heat than a stable annual window."
        )
    )

    val cultivarProfiles = listOf(
        banana("Apple", aliases = setOf("Apple Banana", "Manzana", "Silk")),
        banana("Blue Java", aliases = setOf("Ice Cream", "Ice Cream Banana")),
        banana("Brazilian", aliases = setOf("Pome", "Prata", "Hawaiian Apple", "Tall Apple")),
        banana("Burro", aliases = setOf("Platano Burro")),
        banana("Cavendish"),
        banana("Costa Rican Hybrid Globe"),
        banana("Cardaba"),
        banana("Cuban Red", aliases = setOf("Red", "Red Dacca", "Red Jamaican")),
        banana("Double Banana", aliases = setOf("Double")),
        banana("Dwarf Brazilian", aliases = setOf("Santa Catarina", "Dwarf Apple", "Prata Ana")),
        banana("Dwarf Cavendish"),
        banana(
            "Dwarf Namwa",
            aliases = setOf("Dwarf Namwah", "Dwarf Ducasse", "Kluai Namwa Khom", "Klue Namwa Khom")
        ),
        banana("Dwarf Red"),
        banana("FHIA-17"),
        banana("FHIA-21"),
        banana("Giant Cavendish"),
        banana("Goldfinger", aliases = setOf("FHIA-01", "FHIA 01")),
        banana("Grand Nain"),
        banana("Gros Michel"),
        banana("Lady Finger"),
        banana("Lacatan"),
        banana("Mona Lisa"),
        banana("Mysore", aliases = setOf("Poovan", "Pisang Keling", "Thousand Grain")),
        banana("Namwa", aliases = setOf("Namwah", "Pisang Awak", "Kluai Namwa", "Ducasse")),
        banana("Orinoco", aliases = setOf("Horse Banana", "Hog Banana")),
        banana("Pelipita"),
        banana("Pisang Raja", aliases = setOf("Raja", "Rajah")),
        banana("Popoulu"),
        banana("Praying Hands"),
        banana("Rajapuri", aliases = setOf("Raja Puri")),
        banana("Robusta", aliases = setOf("Valery")),
        banana("Saba"),
        banana("Super Dwarf Cavendish", aliases = setOf("Dwarf Parfitt", "Chinese Extra-Dwarf")),
        banana("Sweetheart", aliases = setOf("FHIA-03")),
        banana("Thousand Fingers"),
        banana("Williams", aliases = setOf("Williams Hybrid")),
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
