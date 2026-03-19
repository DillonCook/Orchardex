package com.dillon.orcharddex.data.phenology

internal object DragonFruitCatalog {
    val cultivarProfiles = listOf(
        dragonFruit("American Beauty", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("AX", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Condor", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Connie Mayer", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Cosmic Charlie", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        dragonFruit("Dark Star", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Delight", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Frankie's Red", aliases = setOf("Frankies Red"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Halley's Comet", aliases = setOf("Halleys Comet"), pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("La Verne", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Lisa", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Natural Mystic", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Neon", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Palora", aliases = setOf("Ecuador Palora", "Yellow Dragon"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Physical Graffiti", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Purple Haze", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Rixford", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("San Ignacio", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Shayna", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Sin Espinas", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Sugar Dragon", aliases = setOf("S-8", "Sugar Dragon S-8"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Valdivia Roja", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        dragonFruit("Vietnamese Red", aliases = setOf("Vietnam Red"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Vietnamese White", aliases = setOf("Vietnam White"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        dragonFruit("Voodoo Child", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
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
