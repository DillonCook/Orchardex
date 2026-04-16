package com.dillon.orcharddex.data.phenology

internal object KiwiBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "kiwi",
            aliases = setOf(
                "kiwi",
                "kiwifruit",
                "fuzzy kiwi",
                "fuzzy kiwifruit",
                "green kiwi",
                "gold kiwi",
                "actinidia deliciosa",
                "actinidia chinensis"
            ),
            referenceZoneCode = "8a",
            startMonth = 5,
            startDay = 12,
            durationDays = 10,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Most fuzzy kiwifruit cultivars need a compatible pollinizer, and bloom overlap matters more than the broad seasonal window."
        )
    )

    val cultivarProfiles = listOf(
        kiwi("Hayward", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        kiwi("Bruno", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        kiwi("Abbott", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        kiwi("Monty", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        kiwi("Blake", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        kiwi("Saanichton", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        kiwi("Jenny", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    )

    private fun kiwi(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "kiwi",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Kiwi",
        pollinationRequirement = pollinationRequirement
    )
}
