package com.dillon.orcharddex.data.phenology

internal object FigBloomCatalog {
    val subgroupSpeciesKeys = setOf(
        "common fig",
        "smyrna fig",
        "san pedro fig"
    )

    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "fig",
            aliases = setOf("fig", "fig tree", "ficus carica"),
            referenceZoneCode = "8a",
            startMonth = 5,
            startDay = 10,
            durationDays = 20,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Most home gardeners mean common figs when they say fig. Smyrna and San Pedro types behave differently and need their own lanes."
        ),
        SpeciesBloomProfile(
            key = "common fig",
            aliases = setOf("common fig", "edible fig", "common-type fig"),
            referenceZoneCode = "8a",
            startMonth = 5,
            startDay = 10,
            durationDays = 20,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Common figs do not require caprification and are the standard backyard fig type."
        ),
        SpeciesBloomProfile(
            key = "smyrna fig",
            aliases = setOf("smyrna fig", "smyrna-type fig", "calimyrna type fig"),
            referenceZoneCode = "8a",
            startMonth = 5,
            startDay = 15,
            durationDays = 20,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Smyrna figs need pollination from caprifigs for the edible crop, so they should not share the common-fig fertility rule."
        ),
        SpeciesBloomProfile(
            key = "san pedro fig",
            aliases = setOf("san pedro fig", "san pedro-type fig"),
            referenceZoneCode = "8a",
            startMonth = 5,
            startDay = 12,
            durationDays = 20,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "San Pedro figs can set a breba crop without pollination, but the main crop depends on caprification, so the species lane should warn about pollination."
        )
    )

    val cultivarProfiles = listOf(
        common(
            cultivar = "Adriatic",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Black Mission",
            aliases = setOf("Mission"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Brown Turkey",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Celeste",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Flanders",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Italian Everbearing",
            aliases = setOf("Italian Honey"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Osborn's Prolific",
            aliases = setOf("Osborn Prolific"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "Violette de Bordeaux",
            aliases = setOf("VdB"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        common(
            cultivar = "White Genoa",
            aliases = setOf("Genoa"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        smyrna(
            cultivar = "Calimyrna",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        sanPedro(
            cultivar = "Desert King",
            aliases = setOf("King"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        sanPedro(
            cultivar = "Lampeira",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        sanPedro(
            cultivar = "San Pedro",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        )
    )

    private fun common(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "common fig",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Common Fig",
        pollinationRequirement = pollinationRequirement
    )

    private fun smyrna(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "smyrna fig",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Smyrna Fig",
        pollinationRequirement = pollinationRequirement
    )

    private fun sanPedro(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "san pedro fig",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "San Pedro Fig",
        pollinationRequirement = pollinationRequirement
    )
}
