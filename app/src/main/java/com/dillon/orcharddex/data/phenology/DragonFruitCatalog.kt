package com.dillon.orcharddex.data.phenology

internal object DragonFruitCatalog {
    val cultivarProfiles = listOf(
        dragonFruit("American Beauty", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Asunta 6",
            aliases = setOf("A6", "Wild Berry Skittles", "Asunta 6 (Paco)"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("AX", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Condor", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Connie Mayer", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Cosmic Charlie", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        dragonFruit("Dark Star", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Delight", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Dennis Pale Pink",
            aliases = setOf("Dennis's Pale Pink"),
            pollinationRequirement = PollinationRequirement.UNKNOWN
        ),
        dragonFruit(
            "Edgar's Baby",
            aliases = setOf("Edgars Baby"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Frankie's Red",
            aliases = setOf("Frankies Red"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit(
            "Fruit Punch",
            aliases = setOf("Punch", "Paul Thomson #3.5S", "Paul Thompson #3.5S", "3.5S"),
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        dragonFruit(
            "Georges White",
            aliases = setOf("George's White"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit(
            "Halley's Comet",
            aliases = setOf("Halleys Comet", "Haley's Comet", "Haleys Comet"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("La Verne", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Lisa", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit(
            "Maria Rosa",
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        dragonFruit(
            "Medusa",
            aliases = setOf("Jellyfish"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("Moroccan Red", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Natural Mystic", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Neon", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "NOID Lowe's variety",
            aliases = setOf("NOID Lowes variety", "Lowe's variety", "Lowes variety"),
            pollinationRequirement = PollinationRequirement.UNKNOWN
        ),
        dragonFruit(
            "Palora",
            aliases = setOf("Ecuador Palora", "Yellow Dragon"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Physical Graffiti", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Purple Haze", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Rixford", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("San Ignacio", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Shayna", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Sin Espinas", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Sugar Dragon",
            aliases = setOf("S-8", "Sugar Dragon S-8"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit(
            "Thai Dragon",
            aliases = setOf("Thai Red"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Townsend Pink", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Tricia", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Valdivia Roja", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit(
            "Vietnamese Red",
            aliases = setOf("Vietnam Red"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit(
            "Vietnamese White",
            aliases = setOf("Vietnam White"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Voodoo Child", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("White Sapphire", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Yellow Thai", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Zamorano", pollinationRequirement = PollinationRequirement.SELF_FERTILE)
    )

    private fun dragonFruit(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement
    ) = CultivarBloomProfile(
        speciesKey = "dragon fruit",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Dragon Fruit",
        pollinationRequirement = pollinationRequirement
    )
}
