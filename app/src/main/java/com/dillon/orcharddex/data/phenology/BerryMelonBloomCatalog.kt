package com.dillon.orcharddex.data.phenology

internal object BerryMelonBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile(
            key = "cranberry",
            aliases = setOf("cranberry", "american cranberry", "vaccinium macrocarpon"),
            referenceZoneCode = "4b",
            startMonth = 6,
            startDay = 10,
            durationDays = 18,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Cranberries are generally self-fruitful, but bee activity and cross-pollination often improve set enough that the species lane should not imply complete pollination independence."
        ),
        SpeciesBloomProfile(
            key = "watermelon",
            aliases = setOf("watermelon", "citrullus lanatus"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 20,
            durationDays = 40,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Watermelon bloom timing depends heavily on planting date and maturity class, so automatic orchard-style season forecasts would be fake precision."
        ),
        SpeciesBloomProfile(
            key = "cantaloupe",
            aliases = setOf("cantaloupe", "muskmelon", "melon", "netted melon", "cucumis melo"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 15,
            durationDays = 35,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Cantaloupe bloom timing is driven by sowing date and season length more than a perennial climate baseline, so the app should not pretend to know exact bloom windows automatically."
        ),
        SpeciesBloomProfile(
            key = "honeydew",
            aliases = setOf("honeydew", "honeydew melon", "winter melon", "inodorus melon"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 20,
            durationDays = 35,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Honeydew behaves like other annual melons: flowering depends mostly on planting date and maturity, so auto bloom forecasting should stay suppressed."
        ),
        SpeciesBloomProfile(
            key = "canary melon",
            aliases = setOf("canary melon", "canary", "juan canary", "yellow melon"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 20,
            durationDays = 35,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Canary melon is another annual dessert melon where sowing date and maturity class matter more than a perennial orchard baseline, so auto bloom should remain suppressed."
        ),
        SpeciesBloomProfile(
            key = "galia melon",
            aliases = setOf("galia melon", "galia"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 20,
            durationDays = 35,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Galia is a muskmelon-type annual crop; bloom timing follows planting date and heat accumulation, so the app should avoid fake automatic bloom windows."
        ),
        SpeciesBloomProfile(
            key = "casaba melon",
            aliases = setOf("casaba melon", "casaba", "winter melon type"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 20,
            durationDays = 40,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Casaba is a late annual winter-melon type, so flowering depends far more on sowing date than on orchard climate; suppression is the honest default."
        ),
        SpeciesBloomProfile(
            key = "persian melon",
            aliases = setOf("persian melon", "persian", "persian muskmelon"),
            referenceZoneCode = "8a",
            startMonth = 6,
            startDay = 20,
            durationDays = 35,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            reviewTier = SpeciesReviewTier.REVIEWED,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Persian melons are annual muskmelon types with bloom tied to planting timing and heat, so orchard-style automatic bloom forecasts should stay suppressed."
        )
    )

    val cultivarProfiles = listOf(
        cranberry("Ben Lear"),
        cranberry("Early Black"),
        cranberry("Howes"),
        cranberry("Pilgrim"),
        cranberry("Stevens"),

        watermelon("Charleston Gray"),
        watermelon("Crimson Sweet"),
        watermelon("Jubilee"),
        watermelon("Moon and Stars", aliases = setOf("Moon & Stars")),
        watermelon("Sugar Baby"),

        cantaloupe("Ambrosia"),
        cantaloupe("Athena"),
        cantaloupe("Hale's Best", aliases = setOf("Hales Best", "Hale's Best Jumbo")),
        cantaloupe("Minnesota Midget"),
        cantaloupe("Sugar Cube"),

        honeydew("Earli Dew", aliases = setOf("Earlidew", "Early Dew")),
        honeydew("Honey Brew"),
        honeydew("Honey Pearl"),
        honeydew("Honeydew Green Flesh"),
        honeydew("Orange Flesh Honey Dew", aliases = setOf("Orange Flesh Honeydew")),

        canaryMelon("Juan Canary", aliases = setOf("Canary")),
        canaryMelon("Sugarnut"),

        galiaMelon("Galia"),
        galiaMelon("Sivan"),

        casabaMelon("Golden Beauty"),
        casabaMelon("Santa Claus"),

        persianMelon("Persian"),
        persianMelon("Caspian")
    )

    private fun cranberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "cranberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Cranberry",
        pollinationRequirement = pollinationRequirement
    )

    private fun watermelon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "watermelon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Watermelon",
        pollinationRequirement = pollinationRequirement
    )

    private fun cantaloupe(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "cantaloupe",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Cantaloupe",
        pollinationRequirement = pollinationRequirement
    )

    private fun honeydew(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "honeydew",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Honeydew",
        pollinationRequirement = pollinationRequirement
    )

    private fun canaryMelon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "canary melon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Canary melon",
        pollinationRequirement = pollinationRequirement
    )

    private fun galiaMelon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "galia melon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Galia melon",
        pollinationRequirement = pollinationRequirement
    )

    private fun casabaMelon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "casaba melon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Casaba melon",
        pollinationRequirement = pollinationRequirement
    )

    private fun persianMelon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "persian melon",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Persian melon",
        pollinationRequirement = pollinationRequirement
    )
}
