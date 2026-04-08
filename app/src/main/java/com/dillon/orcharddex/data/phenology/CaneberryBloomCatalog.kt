package com.dillon.orcharddex.data.phenology

internal object CaneberryBloomCatalog {
    val subgroupSpeciesKeys = setOf(
        "red raspberry",
        "black raspberry"
    )

    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "raspberry",
            aliases = setOf("raspberry", "raspberries", "raspberry cane"),
            referenceZoneCode = "6a",
            startMonth = 5,
            startDay = 10,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Most raspberries are self-fruitful, but primocane versus floricane production and black-raspberry timing differences make the generic species lane intentionally broad."
        ),
        SpeciesBloomProfile(
            key = "red raspberry",
            aliases = setOf(
                "red raspberry",
                "summer red raspberry",
                "everbearing raspberry",
                "primocane raspberry",
                "floricane raspberry",
                "yellow raspberry",
                "rubus idaeus"
            ),
            referenceZoneCode = "6a",
            startMonth = 5,
            startDay = 8,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Red raspberries are usually self-fruitful, but home growers still need cultivar-aware cane-type expectations because summer-bearing and primocane selections do not fruit the same way."
        ),
        SpeciesBloomProfile(
            key = "black raspberry",
            aliases = setOf("black raspberry", "blackcap raspberry", "black cap raspberry", "rubus occidentalis"),
            referenceZoneCode = "5b",
            startMonth = 5,
            startDay = 12,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Black raspberries are generally self-fruitful, but they bloom and manage differently enough from red raspberries that they should keep their own lane."
        ),
        SpeciesBloomProfile(
            key = "blackberry",
            aliases = setOf("blackberry", "blackberries", "thornless blackberry", "rubus fruticosus"),
            referenceZoneCode = "7a",
            startMonth = 5,
            startDay = 12,
            durationDays = 20,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Most blackberries are self-fruitful, but cane habit and primocane-fruiting selections still matter more than the coarse species window suggests."
        )
    )

    val cultivarProfiles = listOf(
        redRaspberry("Boyne", phase = BloomPhase.EARLY),
        redRaspberry("Killarney", phase = BloomPhase.EARLY),
        redRaspberry("Latham", phase = BloomPhase.EARLY),
        redRaspberry("Nova", phase = BloomPhase.EARLY_MID),
        redRaspberry("Anne", phase = BloomPhase.MID_LATE),
        redRaspberry("Caroline", phase = BloomPhase.MID_LATE),
        redRaspberry("Fallgold", phase = BloomPhase.MID_LATE),
        redRaspberry("Heritage", phase = BloomPhase.MID_LATE),

        blackRaspberry("Bristol", phase = BloomPhase.MID),
        blackRaspberry("Cumberland", phase = BloomPhase.MID),
        blackRaspberry("Jewel", phase = BloomPhase.MID_LATE),

        blackberry("Apache", phase = BloomPhase.MID),
        blackberry("Arapaho", phase = BloomPhase.EARLY),
        blackberry("Boysen", aliases = setOf("Boysenberry"), phase = BloomPhase.MID),
        blackberry("Chester", aliases = setOf("Chester Thornless"), phase = BloomPhase.LATE),
        blackberry("Illini Hardy", phase = BloomPhase.EARLY_MID),
        blackberry("Logan", aliases = setOf("Loganberry"), phase = BloomPhase.EARLY_MID),
        blackberry("Marion", aliases = setOf("Marionberry"), phase = BloomPhase.MID),
        blackberry("Natchez", phase = BloomPhase.EARLY),
        blackberry("Navaho", phase = BloomPhase.MID_LATE),
        blackberry("Ouachita", phase = BloomPhase.MID),
        blackberry("Prime-Ark Freedom", aliases = setOf("Prime Ark Freedom"), phase = BloomPhase.EARLY_MID),
        blackberry("Triple Crown", phase = BloomPhase.MID_LATE)
    )

    private fun redRaspberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile(
        speciesKey = "red raspberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Red Raspberry",
        pollinationRequirement = PollinationRequirement.SELF_FERTILE
    )

    private fun blackRaspberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile(
        speciesKey = "black raspberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Black Raspberry",
        pollinationRequirement = PollinationRequirement.SELF_FERTILE
    )

    private fun blackberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile(
        speciesKey = "blackberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = "Blackberry",
        pollinationRequirement = PollinationRequirement.SELF_FERTILE
    )
}
