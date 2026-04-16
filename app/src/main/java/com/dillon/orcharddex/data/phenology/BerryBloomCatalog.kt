package com.dillon.orcharddex.data.phenology

internal object BerryBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "muscadine",
            aliases = setOf("muscadine", "muscadine grape", "muscadine grapes", "scuppernong", "vitis rotundifolia"),
            referenceZoneCode = "8a",
            startMonth = 5,
            startDay = 18,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Muscadines need their own lane because female and self-fertile cultivars do not share one pollination rule."
        ),
        SpeciesBloomProfile(
            key = "elderberry",
            aliases = setOf("elderberry", "elderberries", "american elderberry", "black elderberry", "sambucus canadensis", "sambucus nigra"),
            referenceZoneCode = "5b",
            startMonth = 6,
            startDay = 1,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Elderberries often crop better with another compatible cultivar nearby, so the baseline should favor cross-pollination."
        ),
        SpeciesBloomProfile(
            key = "currant",
            aliases = setOf("currant", "currants", "red currant", "white currant", "black currant", "ribes"),
            referenceZoneCode = "4b",
            startMonth = 4,
            startDay = 24,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Most currants set alone, but mixed plantings still help stabilize yield and are common in home orchards."
        ),
        SpeciesBloomProfile(
            key = "gooseberry",
            aliases = setOf("gooseberry", "gooseberries", "american gooseberry", "european gooseberry", "ribes hirtellum", "ribes uva-crispa"),
            referenceZoneCode = "4b",
            startMonth = 4,
            startDay = 24,
            durationDays = 12,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Gooseberries are usually self-fruitful, so the species model can stay simple unless cultivar-specific data proves otherwise."
        ),
        SpeciesBloomProfile(
            key = "honeyberry",
            aliases = setOf("honeyberry", "honeyberries", "haskap", "haskap berry", "blue honeysuckle", "lonicera caerulea"),
            referenceZoneCode = "3b",
            startMonth = 4,
            startDay = 18,
            durationDays = 14,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Haskaps need a compatible second cultivar for reliable crops, so the species default should stay cross-pollinated."
        ),
        SpeciesBloomProfile(
            key = "serviceberry",
            aliases = setOf("serviceberry", "serviceberries", "juneberry", "juneberries", "saskatoon", "saskatoon berry", "amelanchier", "shadbush", "shadblow"),
            referenceZoneCode = "4b",
            startMonth = 4,
            startDay = 20,
            durationDays = 10,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Serviceberries often set alone, but cross-pollination can still improve fruit size and consistency in mixed plantings."
        )
    )

    val cultivarProfiles = listOf(
        muscadine("Carlos", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Cowart", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Doreen", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Lane", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Magnolia", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Nesbitt", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Tara", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        muscadine("Triumph", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        muscadine("Fry", aliases = setOf("Fry Seedless"), pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        muscadine("Summit", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        muscadine("Supreme", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        muscadine("Black Beauty", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),

        elderberry("Adams", aliases = setOf("Adams 1", "Adams 2"), pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        elderberry("Bob Gordon", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        elderberry("Johns", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        elderberry("Nova", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        elderberry("Ranch", aliases = setOf("Wyldewood Ranch"), pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        elderberry("York", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),

        currant("Red Lake", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        currant("Rovada", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        currant("White Grape", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        currant("Consort", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        currant("Titania", pollinationRequirement = PollinationRequirement.SELF_FERTILE),

        gooseberry("Hinnomaki Red", aliases = setOf("Hinnonmaki Red"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        gooseberry("Hinnomaki Yellow", aliases = setOf("Hinnonmaki Yellow"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        gooseberry("Invicta", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        gooseberry("Pixwell", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        gooseberry("Poorman", pollinationRequirement = PollinationRequirement.SELF_FERTILE),

        honeyberry("Aurora", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        honeyberry("Borealis", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        honeyberry("Honey Bee", aliases = setOf("Honeybee"), pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        honeyberry("Indigo Gem", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        honeyberry("Tundra", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),

        serviceberry("Honeywood", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        serviceberry("Northline", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        serviceberry("Regent", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        serviceberry("Smoky", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        serviceberry("Thiessen", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    )

    private fun muscadine(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "muscadine",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Muscadine",
        pollinationRequirement = pollinationRequirement
    )

    private fun elderberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "elderberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Elderberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun currant(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "currant",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Currant",
        pollinationRequirement = pollinationRequirement
    )

    private fun gooseberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "gooseberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Gooseberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun honeyberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "honeyberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Honeyberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun serviceberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "serviceberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Serviceberry",
        pollinationRequirement = pollinationRequirement
    )
}
