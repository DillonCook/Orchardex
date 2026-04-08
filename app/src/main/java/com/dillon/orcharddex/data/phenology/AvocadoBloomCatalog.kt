package com.dillon.orcharddex.data.phenology

internal object AvocadoBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "avocado",
            aliases = setOf(
                "avocado",
                "avocado pear",
                "alligator pear",
                "aguacate",
                "persea americana"
            ),
            referenceZoneCode = "10a",
            startMonth = 3,
            startDay = 1,
            durationDays = 45,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Avocado fruit set depends heavily on temperature-distorted A/B flowering overlap. Many cultivars can fruit alone, but opposite-type companions usually improve reliability."
        )
    )

    val cultivarProfiles = listOf(
        typeA(
            cultivar = "Donnie",
            phase = BloomPhase.EARLY
        ),
        typeA(
            cultivar = "Simmonds",
            phase = BloomPhase.EARLY
        ),
        typeA(
            cultivar = "Choquette",
            phase = BloomPhase.EARLY_MID
        ),
        typeA(
            cultivar = "GEM",
            aliases = setOf("Gem"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Gwen",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Hass",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Lamb Hass",
            aliases = setOf("Lamb"),
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Lula",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Pinkerton",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Reed",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Taylor",
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeA(
            cultivar = "Waldin",
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        typeB(
            cultivar = "Bacon",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Booth 8",
            aliases = setOf("Booth No. 8"),
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Brogdon",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Ettinger",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Fuerte",
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Monroe",
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Pollock",
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Sharwil",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Sir Prize",
            aliases = setOf("SirPrize"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Winter Mexican",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        typeB(
            cultivar = "Zutano",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        )
    )

    private fun typeA(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "avocado",
        cultivar = cultivar,
        aliases = aliases + setOf("Type A $cultivar", "A-type $cultivar"),
        phase = phase,
        catalogSpeciesLabel = "Avocado",
        pollinationRequirement = pollinationRequirement
    )

    private fun typeB(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "avocado",
        cultivar = cultivar,
        aliases = aliases + setOf("Type B $cultivar", "B-type $cultivar"),
        phase = phase,
        catalogSpeciesLabel = "Avocado",
        pollinationRequirement = pollinationRequirement
    )
}
