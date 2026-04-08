package com.dillon.orcharddex.data.phenology

internal object MulberryBloomCatalog {
    val subgroupSpeciesKeys = setOf(
        "black mulberry",
        "white mulberry"
    )

    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "mulberry",
            aliases = setOf("mulberry", "mulberries", "morus"),
            referenceZoneCode = "7a",
            startMonth = 4,
            startDay = 15,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Fruiting mulberries are often self-fruitful in home gardens, but white mulberry also includes many fruitless male trees and some trees carry separate sexes."
        ),
        SpeciesBloomProfile(
            key = "black mulberry",
            aliases = setOf(
                "black mulberry",
                "persian mulberry",
                "morus nigra"
            ),
            referenceZoneCode = "8a",
            startMonth = 4,
            startDay = 18,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Fruiting black mulberries are generally treated as self-fruitful backyard trees, but the app should still keep the bloom window broad."
        ),
        SpeciesBloomProfile(
            key = "white mulberry",
            aliases = setOf(
                "white mulberry",
                "fruiting white mulberry",
                "morus alba"
            ),
            referenceZoneCode = "7a",
            startMonth = 4,
            startDay = 12,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "White mulberry can carry male and female flowers on separate trees or on the same tree, so fruiting selections are best treated as usually self-fruitful but not perfectly uniform."
        )
    )

    val cultivarProfiles = listOf(
        generic(
            cultivar = "Illinois Everbearing",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        generic(
            cultivar = "Dwarf Everbearing",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        black(
            cultivar = "Persian Fruiting",
            aliases = setOf("Persian"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        white(
            cultivar = "Pakistan",
            aliases = setOf("Pakistani mulberry", "Pakistani"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        white(
            cultivar = "White Fruiting",
            aliases = setOf("White Mulberry"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        )
    )

    private fun generic(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "mulberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Mulberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun black(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "black mulberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Black Mulberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun white(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "white mulberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "White Mulberry",
        pollinationRequirement = pollinationRequirement
    )
}
