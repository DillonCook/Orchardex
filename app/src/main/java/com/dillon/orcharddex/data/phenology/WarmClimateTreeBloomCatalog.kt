package com.dillon.orcharddex.data.phenology

import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.PhenologyModelType

internal object WarmClimateTreeBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "loquat",
            aliases = setOf("loquat", "eriobotrya japonica", "nispero", "japanese medlar", "biwa"),
            referenceZoneCode = "9b",
            startMonth = 11,
            startDay = 20,
            durationDays = 45,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Loquat is often self-fruitful, but several named cultivars still crop better with cross-pollination, so the species lane should stay conservative."
        ),
        SpeciesBloomProfile(
            key = "guava",
            aliases = setOf("guava", "common guava", "apple guava", "psidium guajava", "guajava"),
            referenceZoneCode = "10a",
            startMonth = 4,
            startDay = 1,
            durationDays = 240,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Common guava often flowers in repeated warm-season flushes rather than a single bloom burst, so the forecast should act more like an orchard-aware active season than one exact annual window."
        ),
        SpeciesBloomProfile(
            key = "sapodilla",
            aliases = setOf("sapodilla", "manilkara zapota", "naseberry", "naseberry tree", "chikoo", "chiku"),
            referenceZoneCode = "10b",
            startMonth = 3,
            startDay = 15,
            durationDays = 290,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Sapodilla can flower across a long warm season or nearly continuously in the tropics, so the model should behave like a repeat-bloom climate season instead of one short spring window."
        )
    )

    val cultivarProfiles = listOf(
        loquat("Advance", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        loquat("Big Jim", aliases = setOf("BigJim")),
        loquat("Champagne", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        loquat("Gold Nugget", aliases = setOf("Goldnugget")),
        loquat("Oliver"),
        loquat("Tanaka"),

        guava("Barbie Pink"),
        guava("Mexican Cream"),
        guava("Red Malaysian"),
        guava("Ruby Supreme"),
        guava("Thai Maroon"),
        guava("Thai White"),

        sapodilla("Alano"),
        sapodilla("Brown Sugar"),
        sapodilla("Hasya"),
        sapodilla("Makok"),
        sapodilla("Molix"),
        sapodilla("Tikal")
    )

    private fun loquat(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "loquat",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Loquat",
        pollinationRequirement = pollinationRequirement
    )

    private fun guava(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "guava",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Guava",
        pollinationRequirement = pollinationRequirement
    )

    private fun sapodilla(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "sapodilla",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Sapodilla",
        pollinationRequirement = pollinationRequirement
    )
}
