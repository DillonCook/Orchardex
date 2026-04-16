package com.dillon.orcharddex.data.phenology

internal object PlumBloomCatalog {
    val subgroupSpeciesKeys = setOf(
        "japanese plum",
        "european plum",
        "hardy hybrid plum"
    )

    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "plum",
            aliases = setOf("plum", "plums", "plum tree"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 18,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Japanese, European, and hardy hybrid plums do not share identical bloom timing or fertility behavior, so the generic plum lane should stay a cautious fallback."
        ),
        SpeciesBloomProfile(
            key = "japanese plum",
            aliases = setOf("japanese plum", "asian plum", "prunus salicina"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 15,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Japanese plums usually bloom earlier than European plums and many need or strongly benefit from a compatible pollinizer."
        ),
        SpeciesBloomProfile(
            key = "european plum",
            aliases = setOf("european plum", "prune plum", "prunus domestica", "domestic plum"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 28,
            durationDays = 12,
            defaultPhase = BloomPhase.MID_LATE,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "European plums usually bloom later than Japanese plums and are more often self-fruitful, but nearby pollinizers still improve consistency."
        ),
        SpeciesBloomProfile(
            key = "hardy hybrid plum",
            aliases = setOf("hardy hybrid plum", "hybrid plum", "american hybrid plum", "canadian hybrid plum"),
            referenceZoneCode = "4b",
            startMonth = 4,
            startDay = 22,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Hardy hybrid plums are self-incompatible and need a compatible second variety; they should not be folded into the Japanese or European plum lanes."
        )
    )

    val cultivarProfiles = listOf(
        japanese(
            cultivar = "Methley",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        japanese(
            cultivar = "Shiro",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        japanese(
            cultivar = "Beauty",
            phase = BloomPhase.EARLY
        ),
        japanese(
            cultivar = "Gulfbeauty",
            aliases = setOf("Gulf Beauty"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        japanese(
            cultivar = "Gulfblaze",
            aliases = setOf("Gulf Blaze"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        japanese(
            cultivar = "Gulfrose",
            aliases = setOf("Gulf Rose"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        japanese(
            cultivar = "Santa Rosa",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        japanese(
            cultivar = "Satsuma",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        japanese(
            cultivar = "Mariposa",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        japanese(
            cultivar = "Wickson",
            phase = BloomPhase.MID
        ),

        european(
            cultivar = "Stanley",
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        european(
            cultivar = "Damson",
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        european(
            cultivar = "Italian Prune",
            aliases = setOf("Italian", "Italian Plum"),
            phase = BloomPhase.MID_LATE
        ),
        european(
            cultivar = "Green Gage",
            aliases = setOf("Greengage"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        european(
            cultivar = "Mount Royal",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        european(
            cultivar = "President",
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),

        hardyHybrid(
            cultivar = "Alderman",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        hardyHybrid(
            cultivar = "BlackIce",
            aliases = setOf("Black Ice"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        hardyHybrid(
            cultivar = "Pipestone",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        hardyHybrid(
            cultivar = "Superior",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        hardyHybrid(
            cultivar = "Toka",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        hardyHybrid(
            cultivar = "Waneta",
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        )
    )

    private fun japanese(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "japanese plum",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Japanese Plum",
        pollinationRequirement = pollinationRequirement
    )

    private fun european(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "european plum",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "European Plum",
        pollinationRequirement = pollinationRequirement
    )

    private fun hardyHybrid(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "hardy hybrid plum",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Hardy Hybrid Plum",
        pollinationRequirement = pollinationRequirement
    )
}
