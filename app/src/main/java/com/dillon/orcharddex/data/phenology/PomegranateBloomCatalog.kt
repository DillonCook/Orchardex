package com.dillon.orcharddex.data.phenology

internal object PomegranateBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "pomegranate",
            aliases = setOf("pomegranate", "pomegranates", "punica granatum"),
            referenceZoneCode = "8b",
            startMonth = 5,
            startDay = 8,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Pomegranates are usually self-fruitful, but climate, heat, and insect activity still affect set quality enough that the species bloom window should stay approximate."
        )
    )

    val cultivarProfiles = listOf(
        pomegranate(
            cultivar = "Wonderful",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        pomegranate(
            cultivar = "Salavatski",
            aliases = setOf("Salavatski-Russian", "Russian", "Russian Pomegranate"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        pomegranate(
            cultivar = "Russian 26",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        )
    )

    private fun pomegranate(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "pomegranate",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Pomegranate",
        pollinationRequirement = pollinationRequirement
    )
}
