package com.dillon.orcharddex.data.phenology

internal object AppleBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "apple",
            aliases = setOf("apple", "apples", "malus", "malus domestica"),
            referenceZoneCode = "7a",
            startMonth = 4,
            startDay = 5,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Most apples still need a second compatible cultivar or crabapple pollinizer, even though a few backyard standards are partially self-fruitful."
        )
    )

    val cultivarProfiles = listOf(
        apple("Akane", phase = BloomPhase.EARLY),
        apple("Ambrosia", phase = BloomPhase.MID),
        apple("Anna", phase = BloomPhase.EARLY),
        apple("Arkansas Black", phase = BloomPhase.MID_LATE),
        apple("Braeburn", phase = BloomPhase.MID),
        apple("Cortland", phase = BloomPhase.MID),
        apple("Empire", phase = BloomPhase.MID),
        apple("Enterprise", phase = BloomPhase.MID_LATE),
        apple("Fuji", phase = BloomPhase.MID),
        apple("Gala", aliases = setOf("Royal Gala"), phase = BloomPhase.MID),
        apple(
            "Golden Delicious",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        apple("Ginger Gold", phase = BloomPhase.MID),
        apple("Granny Smith", phase = BloomPhase.MID),
        apple("Grimes Golden", phase = BloomPhase.MID_LATE),
        apple("Haralson", phase = BloomPhase.MID_LATE),
        apple("Honeycrisp", phase = BloomPhase.MID_LATE),
        apple(
            "Jonagold",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        apple("Liberty", phase = BloomPhase.MID),
        apple("Macoun", phase = BloomPhase.MID_LATE),
        apple("McIntosh", phase = BloomPhase.EARLY_MID),
        apple(
            "Mutsu",
            aliases = setOf("Crispin"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        apple("Pink Lady", aliases = setOf("Cripps Pink"), phase = BloomPhase.MID),
        apple("Pristine", phase = BloomPhase.MID),
        apple("Red Delicious", phase = BloomPhase.MID),
        apple("Rome Beauty", phase = BloomPhase.MID_LATE),
        apple("SunCrisp", phase = BloomPhase.MID_LATE),
        apple("Sweet Sixteen", phase = BloomPhase.MID_LATE),
        apple("Wealthy", phase = BloomPhase.MID),
        apple("Winesap", phase = BloomPhase.MID),
        apple("Zestar!", aliases = setOf("Zestar"), phase = BloomPhase.MID)
    )

    private fun apple(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "apple",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Apple",
        pollinationRequirement = pollinationRequirement
    )
}
