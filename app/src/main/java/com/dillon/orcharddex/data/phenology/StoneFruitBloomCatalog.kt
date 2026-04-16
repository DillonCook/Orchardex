package com.dillon.orcharddex.data.phenology

internal object StoneFruitBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "nectarine",
            aliases = setOf("nectarine", "prunus persica var. nucipersica", "prunus persica var nucipersica"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 18,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Most nectarines are self-fruitful, but chill fulfillment and frost exposure can move the real bloom window more than the catalog baseline suggests."
        ),
        SpeciesBloomProfile(
            key = "apricot",
            aliases = setOf("apricot", "prunus armeniaca"),
            referenceZoneCode = "7a",
            startMonth = 3,
            startDay = 8,
            durationDays = 10,
            defaultPhase = BloomPhase.EARLY,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Apricots bloom very early and are frost-sensitive, so even reviewed cultivar timing should be treated as seasonal guidance rather than a hard date promise."
        )
    )

    val cultivarProfiles = listOf(
        apricot("Blenheim", phase = BloomPhase.EARLY),
        apricot("Goldcot", phase = BloomPhase.MID),
        apricot("Harcot", phase = BloomPhase.MID),
        apricot("Harglow", phase = BloomPhase.EARLY_MID),
        apricot("Moorpark", phase = BloomPhase.MID),
        apricot("Moongold", phase = BloomPhase.EARLY, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        apricot("Patterson", phase = BloomPhase.MID_LATE),
        apricot("Sungold", phase = BloomPhase.EARLY_MID, pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        apricot("Westcot", phase = BloomPhase.MID_LATE),

        nectarine("Early Glo", phase = BloomPhase.EARLY_MID),
        nectarine("Fantasia", phase = BloomPhase.MID),
        nectarine("Flavortop", phase = BloomPhase.MID),
        nectarine("Redgold", aliases = setOf("Red Gold", "RedGold"), phase = BloomPhase.MID_LATE),
        nectarine("Snow Queen", phase = BloomPhase.MID_LATE),
        nectarine("Sunglo", phase = BloomPhase.EARLY_MID),
        nectarine("Sunbest", phase = BloomPhase.EARLY_MID),
        nectarine("Suncoast", phase = BloomPhase.MID_LATE),
        nectarine("Sunmist", phase = BloomPhase.MID_LATE),
        nectarine("Sunraycer", phase = BloomPhase.MID),
        nectarine("UFQueen", aliases = setOf("UF Queen"), phase = BloomPhase.MID),
        nectarine("UFRoyal", aliases = setOf("UF Royal"), phase = BloomPhase.EARLY_MID)
    )

    private fun apricot(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "apricot",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Apricot",
        pollinationRequirement = pollinationRequirement
    )

    private fun nectarine(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "nectarine",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Nectarine",
        pollinationRequirement = pollinationRequirement
    )
}
