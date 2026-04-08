package com.dillon.orcharddex.data.phenology

internal object SpecialtyTreeBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "american persimmon",
            aliases = setOf("american persimmon", "common persimmon", "diospyros virginiana"),
            referenceZoneCode = "6b",
            startMonth = 5,
            startDay = 18,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "American persimmons are often functionally dioecious or variably self-fruitful, so a separate species lane is safer than the old generic persimmon bucket."
        ),
        SpeciesBloomProfile(
            key = "japanese persimmon",
            aliases = setOf("japanese persimmon", "oriental persimmon", "asian persimmon", "kaki", "diospyros kaki"),
            referenceZoneCode = "7b",
            startMonth = 5,
            startDay = 10,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Oriental persimmons are usually more self-fruitful than American types, so they deserve their own species defaults."
        ),
        SpeciesBloomProfile(
            key = "cherimoya",
            aliases = setOf("cherimoya", "cherimolla", "custard apple", "annona cherimola"),
            referenceZoneCode = "10a",
            startMonth = 4,
            startDay = 1,
            durationDays = 85,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Cherimoya often benefits from hand pollination or strong beetle activity, so a separate lane is more honest than folding it into atemoya."
        )
    )

    val cultivarProfiles = listOf(
        americanPersimmon("Early Golden", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        americanPersimmon("Meader", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        americanPersimmon("Prok", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        americanPersimmon("Yates", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),

        japanesePersimmon("Chocolate", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        japanesePersimmon("Coffee Cake", aliases = setOf("Nishimura Wase"), pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        japanesePersimmon("Fuyu", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        japanesePersimmon("Hachiya", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        japanesePersimmon("Izu", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        japanesePersimmon("Jiro", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        japanesePersimmon("Saijo", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),

        cherimoya("Booth", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        cherimoya("Chaffey", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        cherimoya("El Bumpo", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        cherimoya("Honeyhart", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        cherimoya("Pierce", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    )

    private fun americanPersimmon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "american persimmon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "American persimmon",
        pollinationRequirement = pollinationRequirement
    )

    private fun japanesePersimmon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "japanese persimmon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Japanese persimmon",
        pollinationRequirement = pollinationRequirement
    )

    private fun cherimoya(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "cherimoya",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Cherimoya",
        pollinationRequirement = pollinationRequirement
    )
}
