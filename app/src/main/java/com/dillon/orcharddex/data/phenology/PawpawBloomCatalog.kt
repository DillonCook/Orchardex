package com.dillon.orcharddex.data.phenology

internal object PawpawBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "pawpaw",
            aliases = setOf(
                "pawpaw",
                "american pawpaw",
                "papaw",
                "indiana banana",
                "poor man's banana",
                "prairie banana",
                "asimina triloba"
            ),
            referenceZoneCode = "6b",
            startMonth = 4,
            startDay = 18,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Most pawpaws crop better with a genetically different tree nearby and often depend on beetle or fly pollination, so the species lane should assume cross-pollination."
        )
    )

    val cultivarProfiles = listOf(
        pawpaw("Allegheny"),
        pawpaw("KSU-Atwood", aliases = setOf("KSU Atwood")),
        pawpaw("KSU-Benson", aliases = setOf("KSU Benson")),
        pawpaw("NC-1", aliases = setOf("NC1")),
        pawpaw("Potomac"),
        pawpaw("Shenandoah"),
        pawpaw("Sunflower", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        pawpaw("Susquehanna")
    )

    private fun pawpaw(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "pawpaw",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Pawpaw",
        pollinationRequirement = pollinationRequirement
    )
}
