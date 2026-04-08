package com.dillon.orcharddex.data.phenology

internal object BlueberryBloomCatalog {
    val subgroupSpeciesKeys = setOf(
        "rabbiteye blueberry",
        "southern highbush blueberry",
        "northern highbush blueberry"
    )

    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "rabbiteye blueberry",
            aliases = setOf("rabbiteye blueberry", "rabbiteye", "vaccinium virgatum", "vaccinium ashei"),
            referenceZoneCode = "8a",
            startMonth = 4,
            startDay = 5,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Rabbiteyes need a second compatible rabbiteye cultivar for reliable crops, so they should not share one fertility rule with highbush blueberries."
        ),
        SpeciesBloomProfile(
            key = "southern highbush blueberry",
            aliases = setOf("southern highbush blueberry", "southern highbush", "shb blueberry", "southern highbush blueberries"),
            referenceZoneCode = "8b",
            startMonth = 3,
            startDay = 10,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Southern highbush can set with self-pollination, but mixed plantings improve fruit size, timing, and consistency."
        ),
        SpeciesBloomProfile(
            key = "northern highbush blueberry",
            aliases = setOf("northern highbush blueberry", "northern highbush", "highbush blueberry", "highbush blueberries", "vaccinium corymbosum"),
            referenceZoneCode = "5b",
            startMonth = 5,
            startDay = 1,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Northern highbush cultivars are often self-fruitful, but cross-pollination still improves berry size and earliness."
        )
    )

    val cultivarProfiles = listOf(
        northernHighbush("Patriot", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        northernHighbush("Bluecrop", aliases = setOf("Blue Crop"), phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        northernHighbush("Chandler", phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        northernHighbush("Legacy", phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        northernHighbush("Rubel", phase = BloomPhase.MID_LATE, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        northernHighbush("Duke", phase = BloomPhase.LATE, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        northernHighbush("Elliott", aliases = setOf("Elliot"), phase = BloomPhase.LATE, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),

        southernHighbush("Snowchaser", aliases = setOf("Snow Chaser"), phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Arcadia", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Chickadee", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Kestrel", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Optimus", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Emerald", phase = BloomPhase.EARLY_MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Farthing", phase = BloomPhase.EARLY_MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Jewel", phase = BloomPhase.EARLY_MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Springhigh", aliases = setOf("Spring High"), phase = BloomPhase.EARLY_MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Star", phase = BloomPhase.EARLY_MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Biloxi", phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Misty", phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("O'Neal", aliases = setOf("ONeal"), phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Sharpblue", aliases = setOf("Sharp Blue"), phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Sweetcrisp", aliases = setOf("Sweet Crisp"), phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        southernHighbush("Windsor", phase = BloomPhase.MID, pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),

        rabbiteye("Beckyblue", aliases = setOf("Becky Blue"), phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Bonita", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Climax", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Alapaha", phase = BloomPhase.MID_LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Austin", phase = BloomPhase.MID_LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Brightwell", phase = BloomPhase.LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Powderblue", aliases = setOf("Powder Blue"), phase = BloomPhase.LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Premier", phase = BloomPhase.MID_LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Tifblue", aliases = setOf("Tif Blue"), phase = BloomPhase.LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Vernon", phase = BloomPhase.MID_LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        rabbiteye("Woodard", phase = BloomPhase.LATE, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION)
    )

    private fun northernHighbush(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "northern highbush blueberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Northern Highbush Blueberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun southernHighbush(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "southern highbush blueberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Southern Highbush Blueberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun rabbiteye(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "rabbiteye blueberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Rabbiteye Blueberry",
        pollinationRequirement = pollinationRequirement
    )
}
