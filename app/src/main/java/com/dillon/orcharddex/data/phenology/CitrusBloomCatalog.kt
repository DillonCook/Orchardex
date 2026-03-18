package com.dillon.orcharddex.data.phenology

internal object CitrusBloomCatalog {
    val speciesProfiles = listOf(
        SpeciesBloomProfile("citrus", setOf("citrus"), "9b", 3, 6, 25),
        SpeciesBloomProfile(
            "lemon",
            setOf(
                "lemon",
                "meyer lemon",
                "improved meyer lemon",
                "eureka lemon",
                "lisbon lemon",
                "ponderosa lemon"
            ),
            "9b",
            3,
            1,
            32
        ),
        SpeciesBloomProfile(
            "lime",
            setOf(
                "lime",
                "key lime",
                "mexican lime",
                "persian lime",
                "bearss lime",
                "tahiti lime",
                "finger lime",
                "australian finger lime",
                "kaffir lime",
                "makrut lime",
                "rangpur lime",
                "sweet lime",
                "palestine sweet lime"
            ),
            "9b",
            3,
            6,
            34
        ),
        SpeciesBloomProfile(
            "orange",
            setOf(
                "orange",
                "sweet orange",
                "navel orange",
                "blood orange",
                "valencia orange",
                "acidless orange",
                "sour orange"
            ),
            "9b",
            3,
            10,
            24
        ),
        SpeciesBloomProfile(
            "mandarin",
            setOf(
                "mandarin",
                "mandarin orange",
                "tangerine",
                "clementine",
                "satsuma",
                "satsuma mandarin",
                "mandarin hybrid",
                "tangelo",
                "tangor"
            ),
            "9a",
            3,
            5,
            22
        ),
        SpeciesBloomProfile(
            "grapefruit",
            setOf(
                "grapefruit",
                "pomelo",
                "pummelo",
                "grapefruit hybrid",
                "oroblanco",
                "oro blanco",
                "melogold"
            ),
            "9b",
            3,
            8,
            24
        )
    )

    val cultivarProfiles = listOf(
        orange("Fukumoto", phase = BloomPhase.EARLY),
        orange("Golden Buckeye", aliases = setOf("Buckeye"), phase = BloomPhase.EARLY),
        orange("Navelina", aliases = setOf("Smith's Early"), phase = BloomPhase.EARLY),
        orange("Newhall", aliases = setOf("Newhall Nucellar"), phase = BloomPhase.EARLY),
        orange("Skaggs Bonanza", aliases = setOf("Bonanza"), phase = BloomPhase.EARLY),
        orange("UCR Early", phase = BloomPhase.EARLY),
        orange("Hamlin", phase = BloomPhase.EARLY_MID),
        orange("Marrs", phase = BloomPhase.EARLY_MID),
        orange("Parson Brown", phase = BloomPhase.EARLY_MID),
        orange("Robertson", phase = BloomPhase.EARLY_MID),
        orange("Atwood", phase = BloomPhase.MID),
        orange("Bahianinha", phase = BloomPhase.MID),
        orange("Cara Cara", phase = BloomPhase.MID),
        orange("Homosassa", phase = BloomPhase.MID),
        orange("Midsweet", phase = BloomPhase.MID),
        orange("Pineapple", phase = BloomPhase.MID),
        orange("Salustiana", phase = BloomPhase.MID),
        orange("Trovita", phase = BloomPhase.MID),
        orange("Washington", aliases = setOf("Washington Navel"), phase = BloomPhase.MID),
        orange("Cogan", phase = BloomPhase.MID_LATE),
        orange("Moro", phase = BloomPhase.MID_LATE),
        orange("Ruby Blood", aliases = setOf("Ruby"), phase = BloomPhase.MID_LATE),
        orange("Smith Red", phase = BloomPhase.MID_LATE),
        orange("Tarocco", aliases = setOf("Tarroco"), phase = BloomPhase.MID_LATE),
        orange("Chislett", phase = BloomPhase.LATE),
        orange("Cutter Valencia", phase = BloomPhase.LATE),
        orange("Lane Late", aliases = setOf("Lane's Late"), phase = BloomPhase.LATE),
        orange("Midknight Valencia", aliases = setOf("Midknight"), phase = BloomPhase.LATE),
        orange("Olinda Valencia", phase = BloomPhase.LATE),
        orange("Sanguinelli", phase = BloomPhase.LATE),
        orange("Seville", aliases = setOf("Sour Orange"), phase = BloomPhase.MID),
        orange("Valencia", phase = BloomPhase.LATE),

        lemon("Fino 49", phase = BloomPhase.EARLY),
        lemon("Fino 95", phase = BloomPhase.EARLY),
        lemon("Interdonato", phase = BloomPhase.EARLY),
        lemon("Primofiori", phase = BloomPhase.EARLY),
        lemon("Lapithkiotiki", phase = BloomPhase.EARLY_MID),
        lemon("Meyer", aliases = setOf("Improved Meyer"), phase = BloomPhase.EARLY_MID),
        lemon("Bearss", aliases = setOf("Bearss Lemon"), phase = BloomPhase.MID),
        lemon("Eureka", phase = BloomPhase.MID),
        lemon("Genoa", aliases = setOf("Genova"), phase = BloomPhase.MID),
        lemon("Lisbon", phase = BloomPhase.MID),
        lemon("Lunario", aliases = setOf("Four Seasons", "Quattro Stagioni"), phase = BloomPhase.MID),
        lemon("Monachello", phase = BloomPhase.MID),
        lemon("Ponderosa", phase = BloomPhase.MID_LATE),
        lemon("Villafranca", aliases = setOf("Villa Franca"), phase = BloomPhase.MID_LATE),
        lemon("Yen Ben", phase = BloomPhase.MID_LATE),
        lemon("Verna", phase = BloomPhase.LATE),

        lime("Key Lime", aliases = setOf("Mexican", "Mexican Lime"), phase = BloomPhase.EARLY_MID),
        lime("Rangpur", phase = BloomPhase.EARLY),
        lime("Bearss", aliases = setOf("Bearss Lime", "Persian", "Persian Lime", "Tahiti", "Tahiti Lime"), phase = BloomPhase.MID),
        lime("Palestine Sweet", aliases = setOf("Palestinian Sweet"), phase = BloomPhase.MID),
        lime("Sweet Lime", phase = BloomPhase.MID),
        lime("Australian Finger", aliases = setOf("Finger Lime"), phase = BloomPhase.MID_LATE),
        lime("Kaffir", aliases = setOf("Makrut"), phase = BloomPhase.MID_LATE),

        grapefruit("Oro Blanco", aliases = setOf("Oroblanco"), phase = BloomPhase.EARLY),
        grapefruit("Melogold", phase = BloomPhase.EARLY_MID),
        grapefruit("Duncan", phase = BloomPhase.MID),
        grapefruit("Marsh", phase = BloomPhase.MID),
        grapefruit("Thompson", phase = BloomPhase.MID),
        grapefruit("Flame", phase = BloomPhase.MID_LATE),
        grapefruit("Redblush", phase = BloomPhase.MID_LATE),
        grapefruit("Ruby Red", phase = BloomPhase.MID_LATE),
        grapefruit("Rio Red", phase = BloomPhase.LATE),
        grapefruit("Star Ruby", phase = BloomPhase.LATE),

        clementine("Algerian", aliases = setOf("Clementine"), phase = BloomPhase.EARLY),
        clementine("Arrufatina", phase = BloomPhase.EARLY),
        clementine("Fina", phase = BloomPhase.EARLY),
        clementine("Fina Sodea", phase = BloomPhase.EARLY),
        clementine("Marisol", phase = BloomPhase.EARLY),
        clementine("Oroval", phase = BloomPhase.EARLY),
        clementine("Clemenules", aliases = setOf("Nules", "Nules Clementine"), phase = BloomPhase.EARLY_MID),
        clementine("Corsica #1", aliases = setOf("Corsica 1"), phase = BloomPhase.MID),
        clementine("Monreal", phase = BloomPhase.MID),
        clementine("Hernandina", phase = BloomPhase.LATE),
        clementine("Nour", phase = BloomPhase.LATE),

        tangerine("Daisy", phase = BloomPhase.EARLY),
        tangerine("Fairchild", phase = BloomPhase.EARLY),
        tangerine("Fallglo", phase = BloomPhase.EARLY),
        tangerine("Page", phase = BloomPhase.EARLY),
        tangerine("Sunburst", phase = BloomPhase.EARLY_MID),
        tangerine("Dancy", phase = BloomPhase.EARLY_MID),
        tangerine("Lee", phase = BloomPhase.EARLY_MID),
        tangerine("Robinson", phase = BloomPhase.EARLY_MID),
        mandarin("Orlando", phase = BloomPhase.EARLY_MID, catalogSpecies = "Tangelo"),
        mandarin("Fremont", phase = BloomPhase.MID),
        mandarin("Kincy", phase = BloomPhase.MID_LATE),
        mandarin("Nova", phase = BloomPhase.MID),
        mandarin("Ponkan", phase = BloomPhase.MID),
        mandarin("Seminole", phase = BloomPhase.MID, catalogSpecies = "Tangelo"),
        mandarin("Shiranui", aliases = setOf("Dekopon", "Sumo", "Sumo Citrus", "Sumo Orange"), phase = BloomPhase.MID_LATE),
        mandarin("Temple", phase = BloomPhase.MID_LATE, catalogSpecies = "Tangor"),
        mandarin("Wilking", phase = BloomPhase.MID),
        mandarin("Honey", aliases = setOf("Murcott", "Honey Murcott"), phase = BloomPhase.MID_LATE),
        mandarin("Minneola", aliases = setOf("Honeybell", "Honey Bell"), phase = BloomPhase.MID_LATE, catalogSpecies = "Tangelo"),
        mandarin("Osceola", phase = BloomPhase.MID_LATE),
        mandarin("Encore", phase = BloomPhase.LATE),
        mandarin("Gold Nugget", phase = BloomPhase.LATE),
        mandarin("Kinnow", phase = BloomPhase.LATE),
        mandarin("Ortanique", phase = BloomPhase.LATE, catalogSpecies = "Tangelo"),
        mandarin("Pixie", phase = BloomPhase.LATE),
        mandarin("Shasta Gold", aliases = setOf("TDE 2", "TDE #2"), phase = BloomPhase.LATE),
        mandarin("Tahoe Gold", aliases = setOf("TDE 3", "TDE #3"), phase = BloomPhase.LATE),
        mandarin("Tango", phase = BloomPhase.LATE),
        mandarin("W. Murcott", aliases = setOf("W Murcott", "Afourer"), phase = BloomPhase.LATE),
        mandarin("Yosemite Gold", aliases = setOf("TDE 4", "TDE #4"), phase = BloomPhase.LATE),

        satsuma("Early St. Ann", aliases = setOf("Early St Ann"), phase = BloomPhase.EARLY),
        satsuma("Xie Shan", phase = BloomPhase.EARLY),
        satsuma("Brown Select", phase = BloomPhase.EARLY),
        satsuma("Kimbrough", phase = BloomPhase.MID),
        satsuma("Miho", phase = BloomPhase.MID),
        satsuma("Miyagawa", phase = BloomPhase.MID),
        satsuma("Obawase", phase = BloomPhase.MID),
        satsuma("Owari", phase = BloomPhase.MID),
        satsuma("Seto", phase = BloomPhase.MID),
        satsuma("Silverhill", phase = BloomPhase.MID),
        satsuma("Armstrong", phase = BloomPhase.MID_LATE),
        satsuma("Okitsu", phase = BloomPhase.MID_LATE),
        satsuma("Aoshima", phase = BloomPhase.LATE)
    )

    private fun orange(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("orange", cultivar, aliases, phase, catalogSpeciesLabel = "Orange")

    private fun lemon(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("lemon", cultivar, aliases, phase, catalogSpeciesLabel = "Lemon")

    private fun lime(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("lime", cultivar, aliases, phase, catalogSpeciesLabel = "Lime")

    private fun grapefruit(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("grapefruit", cultivar, aliases, phase, catalogSpeciesLabel = "Grapefruit")

    private fun mandarin(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase,
        catalogSpecies: String = "Mandarin"
    ) = CultivarBloomProfile("mandarin", cultivar, aliases, phase, catalogSpeciesLabel = catalogSpecies)

    private fun tangerine(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("mandarin", cultivar, aliases, phase, catalogSpeciesLabel = "Tangerine")

    private fun clementine(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("mandarin", cultivar, aliases, phase, catalogSpeciesLabel = "Clementine")

    private fun satsuma(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        phase: BloomPhase
    ) = CultivarBloomProfile("mandarin", cultivar, aliases, phase, catalogSpeciesLabel = "Satsuma")
}
