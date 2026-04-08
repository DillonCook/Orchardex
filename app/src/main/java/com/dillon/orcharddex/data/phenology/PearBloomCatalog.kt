package com.dillon.orcharddex.data.phenology

internal object PearBloomCatalog {
    val subgroupSpeciesKeys = setOf(
        "european pear",
        "asian pear"
    )

    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "pear",
            aliases = setOf("pear", "pears", "pear tree"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 30,
            durationDays = 10,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "European pears, Asian pears, and hybrid pears do not all share one pollination rule, so the generic pear lane should stay a cautious fallback."
        ),
        SpeciesBloomProfile(
            key = "european pear",
            aliases = setOf("european pear", "common pear", "pyrus communis"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 31,
            durationDays = 10,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "European pears often benefit from or require a second compatible cultivar, even though partially self-fruitful exceptions like Bartlett and Anjou exist."
        ),
        SpeciesBloomProfile(
            key = "asian pear",
            aliases = setOf("asian pear", "nashi", "sand pear", "apple pear", "pyrus pyrifolia"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 27,
            durationDays = 10,
            defaultPhase = BloomPhase.EARLY,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Asian pears are usually best planted with two or more compatible cultivars, while partially self-fruitful exceptions like Hosui still crop better with overlap."
        )
    )

    val cultivarProfiles = listOf(
        generic(
            cultivar = "Kieffer",
            phase = BloomPhase.LATE
        ),
        generic(
            cultivar = "Summercrisp",
            phase = BloomPhase.MID_LATE
        ),
        generic(
            cultivar = "Ure",
            phase = BloomPhase.MID_LATE
        ),

        european(
            cultivar = "Anjou",
            aliases = setOf("Red Anjou", "d'Anjou", "D'Anjou"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        european(
            cultivar = "Bartlett",
            aliases = setOf("Max-Red Bartlett"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        european(
            cultivar = "Bosc",
            aliases = setOf("Beurre Bosc"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        european(
            cultivar = "Comice",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        european(
            cultivar = "Harrow Sweet",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        european(
            cultivar = "Moonglow",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        european(
            cultivar = "Seckel",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),

        asian(
            cultivar = "Chojuro",
            phase = BloomPhase.EARLY
        ),
        asian(
            cultivar = "Hosui",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        asian(
            cultivar = "Korean Giant",
            aliases = setOf("Olympic", "A-Ri-Rang", "Ari-Rang"),
            phase = BloomPhase.MID
        ),
        asian(
            cultivar = "Kosui",
            phase = BloomPhase.EARLY
        ),
        asian(
            cultivar = "Shinseiki",
            aliases = setOf("Shinseike", "New Century"),
            phase = BloomPhase.EARLY
        ),
        asian(
            cultivar = "Twentieth Century",
            aliases = setOf("20th Century", "20th Century Pear", "Nijisseiki"),
            phase = BloomPhase.EARLY_MID
        )
    )

    private fun generic(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "pear",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Pear",
        pollinationRequirement = pollinationRequirement
    )

    private fun european(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "european pear",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "European Pear",
        pollinationRequirement = pollinationRequirement
    )

    private fun asian(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "asian pear",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Asian Pear",
        pollinationRequirement = pollinationRequirement
    )
}
