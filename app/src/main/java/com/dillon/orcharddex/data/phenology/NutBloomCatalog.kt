package com.dillon.orcharddex.data.phenology

internal object NutBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "pecan",
            aliases = setOf("pecan", "pecans", "carya illinoinensis"),
            referenceZoneCode = "8a",
            startMonth = 4,
            startDay = 28,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Pecans need a compatible partner because protandrous and protogynous cultivars do not self-cover reliably."
        )
    )

    val cultivarProfiles = listOf(
        pecan("Caddo"),
        pecan("Cape Fear"),
        pecan("Desirable"),
        pecan("Elliott", aliases = setOf("Elliot")),
        pecan("Lakota"),
        pecan("Oconee"),
        pecan("Pawnee"),
        pecan("Sumner")
    )

    private fun pecan(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarBloomProfile(
        speciesKey = "pecan",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Pecan",
        pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
    )
}
