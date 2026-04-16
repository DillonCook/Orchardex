package com.dillon.orcharddex.data.phenology

internal object DragonFruitCatalog {
    val cultivarProfiles = listOf(
        dragonFruit("American Beauty", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Asunta 1", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Asunta 2", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Asunta 3", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Asunta 4", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit(
            "Asunta 5 Paco",
            aliases = setOf("A5 Paco", "Asunta 5 Kevin", "Asunta 5 La Palma", "Kevin", "La Palma"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Asunta 5 Patricia",
            aliases = setOf("A5 Patricia", "A5 Patrecia", "Asunta 5 Patrecia"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Asunta 5 Starburst",
            aliases = setOf("A5 Starburst"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Asunta 5 Sunset Sherbet",
            aliases = setOf("A5 Sunset Sherbet", "Asunta 5 Edgar", "Sunset Sherbet", "Sunset Sherbert"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Asunta 5 Ventura",
            aliases = setOf("A5 Ventura"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Asunta 6",
            aliases = setOf("A6", "Wild Berry Skittles", "Asunta 6 Wild Berry Skittles"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("Alice", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Armando", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Arizona Purple", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("AX", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Aztec Gem", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("B B Red", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("B B White", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Baby Cerrado", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Blush", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Bones Purple", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Bruni", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Capistrano Valley", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Cebra", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Cerise",
            aliases = setOf("Cerise Red"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Chameleon", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit(
            "Colombian Supreme",
            aliases = setOf("Colombian Supreme Red", "Columbian Supreme", "Columbian Supreme Red", "Common Red"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        dragonFruit(
            "Columbian Red",
            aliases = setOf("Colombian Red"),
            pollinationRequirement = PollinationRequirement.UNKNOWN
        ),
        dragonFruit("Commercial White", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Condor", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Connie Gee", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Connie Mayer", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Costa Rican Sunset", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Cosmic Charlie", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        dragonFruit("Crimson", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Dark Star", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Delight", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Desert King",
            aliases = setOf("DK16", "DK 16", "Desert King 16"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
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
        dragonFruit("Florida Sweet White", pollinationRequirement = PollinationRequirement.UNKNOWN),
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
            "Giant Columbian Yellow",
            aliases = setOf("Columbian Yellow", "Colombian Yellow"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Giant Orange", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Godzilla", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Halley's Comet",
            aliases = setOf("Halleys Comet", "Haley's Comet", "Haleys Comet"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("Hana", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Hawaiian Orange", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Honey White", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Isis Gold",
            aliases = setOf("Aussie Gold", "Ozzie Gold", "Australian Gold", "Israeli Yellow", "Golden Yellow"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Jade Red",
            aliases = setOf("Red Jade"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit(
            "Kathy Van Arum",
            aliases = setOf("Kathie Van Arum"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("King Kong", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Lake Atitlan", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("La Verne", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Lisa", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit(
            "Lucille Lemonade",
            aliases = setOf("Lucille Lemonaid"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit(
            "Malaysian Purple",
            aliases = setOf("Malay Purple"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        dragonFruit("Maria Rosa", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        dragonFruit(
            "Medusa",
            aliases = setOf("Jellyfish"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("Moroccan Red", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Natural Mystic", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Neon", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("NOID", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Palora",
            aliases = setOf("Ecuador Palora"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Pink Beauty", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Pink Champagne", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Pink Diamond", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Pink Lemonade", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Pink Panther", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Physical Graffiti", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Purple Haze", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Purple Heart", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Purple Megalanthus",
            aliases = setOf("Strawberry Megalanthus"),
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        dragonFruit("Rixford", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("San Ignacio", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Scott's Purple",
            aliases = setOf("Scotts Purple"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Shayna", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Sin Espinas", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Sugar Dragon",
            aliases = setOf("S-8", "Sugar Dragon S-8"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit("Southern Cross", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Sunshine White", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Supernova", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit("Taiwan Magenta", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Thai Dragon",
            aliases = setOf("Thai Red"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        dragonFruit(
            "Thomson G2",
            aliases = setOf("Paul Thomson G2", "Tompson G2"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        dragonFruit("Townsend Pink", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit(
            "Trish Red",
            aliases = setOf("Trish"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
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
        dragonFruit("Wongarra Red", pollinationRequirement = PollinationRequirement.UNKNOWN),
        dragonFruit(
            "Yellow Colombiana",
            aliases = setOf("Thorny Yellow", "Yellow Colombiana Megalanthus", "Yellow Megalanthus", "Yellow Dragon", "Common Yellow"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
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
