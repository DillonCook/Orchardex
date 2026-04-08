package com.dillon.orcharddex.data.phenology

internal object MediterraneanBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "olive",
            aliases = setOf("olive", "olives", "olea europaea"),
            referenceZoneCode = "9a",
            startMonth = 4,
            startDay = 25,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Many olives will set with a single cultivar, but mixed plantings are still a safer default because bloom overlap and compatibility vary by type."
        ),
        SpeciesBloomProfile(
            key = "feijoa",
            aliases = setOf("feijoa", "pineapple guava", "guavasteen", "acca sellowiana", "feijoa sellowiana"),
            referenceZoneCode = "8b",
            startMonth = 5,
            startDay = 18,
            durationDays = 20,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Pineapple guavas often crop better with another cultivar nearby even when a single variety can set some fruit."
        )
    )

    val cultivarProfiles = listOf(
        olive("Arbequina", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        olive("Mission", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        olive("Frantoio", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        olive("Leccino", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        olive("Koroneiki", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        olive("Pendolino", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        olive("Manzanillo", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        olive("Sevillano", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),

        feijoa("Apollo", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        feijoa("Coolidge", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        feijoa("Gemini", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        feijoa("Mammoth", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        feijoa("Nazemetz", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        feijoa("Triumph", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        feijoa("Trask", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    )

    private fun olive(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "olive",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Olive",
        pollinationRequirement = pollinationRequirement
    )

    private fun feijoa(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "feijoa",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Feijoa",
        pollinationRequirement = pollinationRequirement
    )
}
