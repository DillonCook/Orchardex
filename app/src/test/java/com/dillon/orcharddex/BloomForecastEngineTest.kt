package com.dillon.orcharddex

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.PollinationRequirement
import com.google.common.truth.Truth.assertThat
import java.time.YearMonth
import org.junit.Test

class BloomForecastEngineTest {
    @Test
    fun supportedCultivarCatalog_includesCommonBananas() {
        val bananaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Banana" }
            .map { it.cultivar }

        assertThat(bananaCultivars).containsAtLeast(
            "Dwarf Cavendish",
            "Blue Java",
            "Goldfinger",
            "Mona Lisa",
            "Rajapuri",
            "Sweetheart"
        )
    }

    @Test
    fun supportedCultivarCatalog_includesDragonFruitAndPollinationMetadata() {
        val dragonFruitCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Dragon Fruit" }
            .associateBy { it.cultivar }

        assertThat(dragonFruitCultivars.keys).containsAtLeast(
            "American Beauty",
            "Asunta 1",
            "Asunta 5 Paco",
            "Asunta 5 Patricia",
            "Asunta 5 Starburst",
            "Asunta 5 Sunset Sherbet",
            "Asunta 5 Ventura",
            "Asunta 6",
            "AX",
            "Cosmic Charlie",
            "Edgar's Baby",
            "Fruit Punch",
            "Medusa",
            "Sugar Dragon",
            "Thai Dragon",
            "Townsend Pink",
            "Tricia",
            "Vietnamese White",
            "Voodoo Child"
        )
        assertThat(dragonFruitCultivars.getValue("American Beauty").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Asunta 1").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Asunta 5 Paco").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("AX").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Townsend Pink").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Tricia").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Fruit Punch").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(dragonFruitCultivars.getValue("Dennis Pale Pink").pollinationRequirement)
            .isEqualTo(PollinationRequirement.UNKNOWN)
    }

    @Test
    fun supportedCultivarCatalog_includesStarFruitSeedSetAndPollinationMetadata() {
        val starFruitCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Star Fruit" }
            .associateBy { it.cultivar }

        assertThat(starFruitCultivars.keys).containsAtLeast(
            "Arkin",
            "Golden Star",
            "Fwang Tung",
            "Kary",
            "Sri Kembangan",
            "B-10",
            "B-17",
            "Tean Ma",
            "Mih Tao",
            "Thai Knight",
            "Wheeler"
        )
        assertThat(starFruitCultivars.getValue("Arkin").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(starFruitCultivars.getValue("Golden Star").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(starFruitCultivars.getValue("B-10").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(starFruitCultivars.getValue("Mih Tao").pollinationRequirement)
            .isEqualTo(PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY)
        assertThat(starFruitCultivars.getValue("Kajang").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(starFruitCultivars.getValue("Fwang Tung").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesSugarAppleSeedSetAndPollinationMetadata() {
        val sugarAppleCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Sugar Apple" }
            .associateBy { it.cultivar }

        assertThat(sugarAppleCultivars.keys).containsAtLeast(
            "Lessard Thai",
            "Kampong Mauve",
            "Brazilian Seedless",
            "Thai Seedless",
            "Mammoth",
            "APK-1",
            "NMK-1 Golden",
            "Beni Mazar",
            "Abd El Razik"
        )
        assertThat(sugarAppleCultivars.getValue("Brazilian Seedless").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(sugarAppleCultivars.getValue("Lessard Thai").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(sugarAppleCultivars.getValue("Kampong Mauve").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesJackfruitSeedSetAndPollinationMetadata() {
        val jackfruitCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Jackfruit" }
            .associateBy { it.cultivar }

        assertThat(jackfruitCultivars.keys).containsAtLeast(
            "Black Gold",
            "Dang Rasimi",
            "Golden Nugget",
            "J-31",
            "NS1",
            "Golden Pillow",
            "Fairchild First",
            "PLR 1",
            "PPI 1",
            "Siddu",
            "Sindhoor"
        )
        assertThat(jackfruitCultivars.getValue("Dang Rasimi").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(jackfruitCultivars.getValue("PLR 1").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesTamarindSeedSetAndPollinationMetadata() {
        val tamarindCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Tamarind" }
            .associateBy { it.cultivar }

        assertThat(tamarindCultivars.keys).containsAtLeast(
            "Manila Sweet",
            "Makham Waan",
            "PKM-1",
            "Prathisthan",
            "T-263",
            "Sichomphu",
            "Nam Phueng",
            "Aglibut Sweet",
            "PSAU Sour 2"
        )
        assertThat(tamarindCultivars.getValue("Manila Sweet").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(tamarindCultivars.getValue("Sichomphu").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedCultivarCatalog_includesPineappleSeedSetAndPollinationMetadata() {
        val pineappleCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Pineapple" }
            .associateBy { it.cultivar }

        assertThat(pineappleCultivars.keys).containsAtLeast(
            "Smooth Cayenne",
            "Kew",
            "Red Spanish",
            "Victoria",
            "Mauritius",
            "Sugarloaf",
            "MD-2",
            "N36",
            "Josapine",
            "BRS Vitória",
            "Gold Barrel",
            "Amritha"
        )
        assertThat(pineappleCultivars.getValue("MD-2").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(pineappleCultivars.getValue("Sugarloaf").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun supportedCultivarCatalog_includesBarbadosCherrySeedSetAndPollinationMetadata() {
        val barbadosCherryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Barbados Cherry" }
            .associateBy { it.cultivar }

        assertThat(barbadosCherryCultivars.keys).containsAtLeast(
            "Florida Sweet",
            "B-17",
            "J.H. Beaumont",
            "Tropical Ruby",
            "BRS Cabocla",
            "BRS 236 Cereja",
            "Olivier",
            "UEL 5 - Natalia"
        )
        assertThat(barbadosCherryCultivars.getValue("Florida Sweet").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(barbadosCherryCultivars.getValue("Olivier").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesJamaicanCherryLightSeedSet() {
        val jamaicanCherryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Jamaican Cherry" }
            .associateBy { it.cultivar }

        assertThat(jamaicanCherryCultivars.keys).containsExactly(
            "Standard red-fruited type",
            "Yellow-fruited form"
        )
        assertThat(jamaicanCherryCultivars.getValue("Standard red-fruited type").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(jamaicanCherryCultivars.getValue("Yellow-fruited form").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun supportedCultivarCatalog_includesPassionFruitSeedSetAndPollinationMetadata() {
        val passionFruitCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Passion Fruit" }
            .associateBy { it.cultivar }

        assertThat(passionFruitCultivars.keys).containsAtLeast(
            "Possum Purple",
            "Sweet Sunrise",
            "Whitman Yellow",
            "Frederick",
            "Panama Gold",
            "Pandora",
            "BRS Gigante Amarelo",
            "BRS Rubi do Cerrado",
            "UENF Rio Dourado",
            "Round Yellow"
        )
        assertThat(passionFruitCultivars.getValue("Possum Purple").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(passionFruitCultivars.getValue("Sweet Sunrise").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(passionFruitCultivars.getValue("Panama Red").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesPapayaSeedSetAndPollinationMetadata() {
        val papayaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Papaya" }
            .associateBy { it.cultivar }

        assertThat(papayaCultivars.keys).containsAtLeast(
            "Kapoho Solo",
            "Sunrise Solo",
            "Improved Sunrise Solo 72/12",
            "Tainung No. 1",
            "Red Lady 786",
            "Pusa Nanha",
            "CO.3",
            "Arka Surya",
            "Golden",
            "UENF/Caliman 01"
        )
        assertThat(papayaCultivars.getValue("Kapoho Solo").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(papayaCultivars.getValue("Pusa Nanha").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(papayaCultivars.getValue("Golden").pollinationRequirement)
            .isNull()
    }

    @Test
    fun supportedCultivarCatalog_includesWhiteSapoteSeedSetAndPollinationMetadata() {
        val whiteSapoteCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "White Sapote" }
            .associateBy { it.cultivar }

        assertThat(whiteSapoteCultivars.keys).containsAtLeast(
            "Blumenthal",
            "Dade",
            "Denzler",
            "Golden",
            "Lemon Gold",
            "Reinecke Commercial",
            "Suebelle",
            "Vernon",
            "Yellow"
        )
        assertThat(whiteSapoteCultivars.getValue("Vernon").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(whiteSapoteCultivars.getValue("Blumenthal").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(whiteSapoteCultivars.getValue("Suebelle").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun resolveCultivarAutocomplete_matchesBananaAliases() {
        val match = BloomForecastEngine.resolveCultivarAutocomplete("Ice Cream", "Banana")

        assertThat(match).isNotNull()
        assertThat(match?.species).isEqualTo("Banana")
        assertThat(match?.cultivar).isEqualTo("Blue Java")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesDragonFruitAliases() {
        val cometMatch = BloomForecastEngine.resolveCultivarAutocomplete("Haley's Comet", "Dragon fruit")
        val thaiMatch = BloomForecastEngine.resolveCultivarAutocomplete("Thai Red", "Dragon fruit")
        val asunta5Match = BloomForecastEngine.resolveCultivarAutocomplete("La Palma", "Dragon fruit")
        val asunta5RenameMatch = BloomForecastEngine.resolveCultivarAutocomplete("Asunta 5 Edgar", "Dragon fruit")
        val asunta6Match = BloomForecastEngine.resolveCultivarAutocomplete("Wild Berry Skittles", "Dragon fruit")

        assertThat(cometMatch?.cultivar).isEqualTo("Halley's Comet")
        assertThat(thaiMatch?.cultivar).isEqualTo("Thai Dragon")
        assertThat(asunta5Match?.cultivar).isEqualTo("Asunta 5 Paco")
        assertThat(asunta5RenameMatch?.cultivar).isEqualTo("Asunta 5 Sunset Sherbet")
        assertThat(asunta6Match?.cultivar).isEqualTo("Asunta 6")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesStarFruitAliases() {
        val kariMatch = BloomForecastEngine.resolveCultivarAutocomplete("Kari", "Star fruit")
        val sriMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sri Kambangan", "Star fruit")
        val teanMaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Team Ma", "Carambola")
        val b17Match = BloomForecastEngine.resolveCultivarAutocomplete("Belimbing Madu", "Star fruit")

        assertThat(kariMatch?.cultivar).isEqualTo("Kary")
        assertThat(sriMatch?.cultivar).isEqualTo("Sri Kembangan")
        assertThat(teanMaMatch?.cultivar).isEqualTo("Tean Ma")
        assertThat(b17Match?.cultivar).isEqualTo("B-17")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesSugarAppleAliases() {
        val lessardMatch = BloomForecastEngine.resolveCultivarAutocomplete("Thai Lessard", "Sugar apple")
        val cubanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Seedless Cuban", "Sweetsop")
        val balangarMatch = BloomForecastEngine.resolveCultivarAutocomplete("Balangar", "Annona squamosa")
        val apkMatch = BloomForecastEngine.resolveCultivarAutocomplete("APK1", "Sugar apple")

        assertThat(lessardMatch?.cultivar).isEqualTo("Lessard Thai")
        assertThat(cubanMatch?.cultivar).isEqualTo("Cuban Seedless")
        assertThat(balangarMatch?.cultivar).isEqualTo("Balanagar")
        assertThat(apkMatch?.cultivar).isEqualTo("APK-1")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesJackfruitAliases() {
        val nuggetMatch = BloomForecastEngine.resolveCultivarAutocomplete("Gold Nugget", "Jackfruit")
        val nansiMatch = BloomForecastEngine.resolveCultivarAutocomplete("Nansi", "Jack")
        val pillowMatch = BloomForecastEngine.resolveCultivarAutocomplete("Mong Tong", "Artocarpus heterophyllus")
        val plrMatch = BloomForecastEngine.resolveCultivarAutocomplete("Palur 1", "Jack fruit")

        assertThat(nuggetMatch?.cultivar).isEqualTo("Golden Nugget")
        assertThat(nansiMatch?.cultivar).isEqualTo("N.A.N.S.I.")
        assertThat(pillowMatch?.cultivar).isEqualTo("Golden Pillow")
        assertThat(plrMatch?.cultivar).isEqualTo("PLR 1")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesTamarindAliases() {
        val pkmMatch = BloomForecastEngine.resolveCultivarAutocomplete("Periyakulam 1", "Tamarind")
        val t263Match = BloomForecastEngine.resolveCultivarAutocomplete("T 263", "Tamarindo")
        val sichomphuMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sri Chompoo", "Tamarindus indica")
        val namPhuengMatch = BloomForecastEngine.resolveCultivarAutocomplete("Namphueng", "Makham")

        assertThat(pkmMatch?.cultivar).isEqualTo("PKM-1")
        assertThat(t263Match?.cultivar).isEqualTo("T-263")
        assertThat(sichomphuMatch?.cultivar).isEqualTo("Sichomphu")
        assertThat(namPhuengMatch?.cultivar).isEqualTo("Nam Phueng")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesPineappleAliases() {
        val victoriaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Queen Victoria", "Pineapple")
        val mauritiusMatch = BloomForecastEngine.resolveCultivarAutocomplete("Moris", "Piña")
        val sugarloafMatch = BloomForecastEngine.resolveCultivarAutocomplete("Kona Sugarloaf", "Ananas")
        val md2Match = BloomForecastEngine.resolveCultivarAutocomplete("Del Monte Gold", "Ananas comosus")

        assertThat(victoriaMatch?.cultivar).isEqualTo("Victoria")
        assertThat(mauritiusMatch?.cultivar).isEqualTo("Mauritius")
        assertThat(sugarloafMatch?.cultivar).isEqualTo("Sugarloaf")
        assertThat(md2Match?.cultivar).isEqualTo("MD-2")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesBarbadosCherryAliases() {
        val beaumontMatch = BloomForecastEngine.resolveCultivarAutocomplete("Beaumont", "Acerola")
        val rehnborgMatch = BloomForecastEngine.resolveCultivarAutocomplete("Rehnborg", "Barbados cherry")
        val apodiMatch = BloomForecastEngine.resolveCultivarAutocomplete("Apodi", "Malpighia emarginata")
        val uelMatch = BloomForecastEngine.resolveCultivarAutocomplete("UEL4 Ligia", "West Indian cherry")

        assertThat(beaumontMatch?.cultivar).isEqualTo("J.H. Beaumont")
        assertThat(rehnborgMatch?.cultivar).isEqualTo("C.F. Rehnborg")
        assertThat(apodiMatch?.cultivar).isEqualTo("BRS 235 Apodi")
        assertThat(uelMatch?.cultivar).isEqualTo("UEL 4 - Ligia")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesJamaicanCherryAliases() {
        val yellowMatch = BloomForecastEngine.resolveCultivarAutocomplete("Yellow Panama Berry", "Jamaican cherry")
        val yellowTreeMatch = BloomForecastEngine.resolveCultivarAutocomplete("Yellow Strawberry Tree", "Panama berry")

        assertThat(yellowMatch?.cultivar).isEqualTo("Yellow-fruited form")
        assertThat(yellowTreeMatch?.cultivar).isEqualTo("Yellow-fruited form")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesPassionFruitAliases() {
        val whitmanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Lilikoi")
        val flamencoMatch = BloomForecastEngine.resolveCultivarAutocomplete("Red Flemenco", "Passion fruit")
        val b74Match = BloomForecastEngine.resolveCultivarAutocomplete("B-74", "Passiflora edulis")
        val uenfMatch = BloomForecastEngine.resolveCultivarAutocomplete("UENF Golden River", "Maracuja azedo")

        assertThat(whitmanMatch?.cultivar).isEqualTo("Whitman Yellow")
        assertThat(flamencoMatch?.cultivar).isEqualTo("Flamenco")
        assertThat(b74Match?.cultivar).isEqualTo("University Selection No. B-74")
        assertThat(uenfMatch?.cultivar).isEqualTo("UENF Rio Dourado")
    }


    @Test
    fun resolveCultivarAutocomplete_matchesPapayaAliases() {
        val sunriseMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sunrise", "Papaya")
        val sunUpMatch = BloomForecastEngine.resolveCultivarAutocomplete("UH SunUp", "Carica papaya")
        val arkaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Arka Prabhat", "Lechosa")
        val calimanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Caliman 01", "Mamão")

        assertThat(sunriseMatch?.cultivar).isEqualTo("Sunrise Solo")
        assertThat(sunUpMatch?.cultivar).isEqualTo("SunUp")
        assertThat(arkaMatch?.cultivar).isEqualTo("Arka Prabhath")
        assertThat(calimanMatch?.cultivar).isEqualTo("UENF/Caliman 01")
    }


    @Test
    fun resolveCultivarAutocomplete_matchesWhiteSapoteAliases() {
        val goldenMatch = BloomForecastEngine.resolveCultivarAutocomplete("Max Golden", "White sapote")
        val denzlerMatch = BloomForecastEngine.resolveCultivarAutocomplete("Densler", "Casimiroa edulis")
        val reineckeMatch = BloomForecastEngine.resolveCultivarAutocomplete("Reinikie", "Zapote blanco")
        val suebelleMatch = BloomForecastEngine.resolveCultivarAutocomplete("Hubbell", "Casimiroa")

        assertThat(goldenMatch?.cultivar).isEqualTo("Golden")
        assertThat(denzlerMatch?.cultivar).isEqualTo("Denzler")
        assertThat(reineckeMatch?.cultivar).isEqualTo("Reinecke Commercial")
        assertThat(suebelleMatch?.cultivar).isEqualTo("Suebelle")
    }

    @Test
    fun pollinationRequirementFor_resolvesCultivarAndSpeciesDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Banana", "Goldfinger"))
            .isEqualTo(PollinationRequirement.POLLINATION_NOT_REQUIRED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Sugar Dragon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Asunta 5 Paco"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Asunta 5 Edgar"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Asunta 6"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "AX"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Townsend Pink"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Fruit Punch"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Dennis Pale Pink"))
            .isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Golden Delicious"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Honeycrisp"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee", "Tai So"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee", "Fay Zee Siu"))
            .isEqualTo(PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Carambola"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Star fruit", "Arkin"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Star fruit", "Belimbing Madu"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Star fruit", "Fwang Tung"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Star fruit", "Mei Tao"))
            .isEqualTo(PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Annona squamosa"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Sugar apple", "Brazilian seedless"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Sweetsop", "Seedless Cuban"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Jackfruit"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Jack fruit", "Gold Nugget"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Artocarpus heterophyllus", "Nansi"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Tamarind"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Tamarindo", "Periyakulam 1"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Tamarindus indica", "Namphueng"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pineapple"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Piña", "Moris"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Ananas comosus", "Del Monte Gold"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Acerola"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Barbados cherry", "Beaumont"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Malpighia emarginata", "Olivier"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Jamaican cherry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Panama berry", "Yellow Panama Berry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Muntingia calabura", "Standard red-fruited type"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lilikoi"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Passion fruit", "Possum Purple"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Passiflora edulis", "Sweet Sunrise"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Maracuja azedo", "BRS OV1"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Papaya")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Papaya", "Kapoho Solo"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Carica papaya", "Pusa Dwarf"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mamão", "Golden")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("White sapote"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Casimiroa edulis", "Vernon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Zapote blanco", "Reinikie"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Casimiroa", "Hubbell"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun predictMonth_usesStarFruitPrimaryBloomWindow() {
        val starFruitTree = TreeEntity(
            id = "star-fruit-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Carambola",
            cultivar = "Arkin",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val aprilWindow = BloomForecastEngine.predictMonth(
            trees = listOf(starFruitTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val septemberWindow = BloomForecastEngine.predictMonth(
            trees = listOf(starFruitTree),
            yearMonth = YearMonth.of(2026, 9),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 15))
        assertThat(septemberWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesSugarApplePrimaryBloomWindow() {
        val sugarAppleTree = TreeEntity(
            id = "sugar-apple-1",
            orchardName = "Home",
            sectionName = "Annona row",
            nickname = null,
            species = "Sweetsop",
            cultivar = "Lessard Thai",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val aprilWindow = BloomForecastEngine.predictMonth(
            trees = listOf(sugarAppleTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val augustWindow = BloomForecastEngine.predictMonth(
            trees = listOf(sugarAppleTree),
            yearMonth = YearMonth.of(2026, 8),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 3, 15))
        assertThat(augustWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesJackfruitPrimaryBloomWindow() {
        val jackfruitTree = TreeEntity(
            id = "jackfruit-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Jack fruit",
            cultivar = "Black Gold",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val februaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(jackfruitTree),
            yearMonth = YearMonth.of(2026, 2),
            zoneCode = "10b"
        )
        val julyWindow = BloomForecastEngine.predictMonth(
            trees = listOf(jackfruitTree),
            yearMonth = YearMonth.of(2026, 7),
            zoneCode = "10b"
        )

        assertThat(februaryWindow).hasSize(1)
        assertThat(februaryWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 1, 20))
        assertThat(julyWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesTamarindPrimaryBloomWindow() {
        val tamarindTree = TreeEntity(
            id = "tamarind-1",
            orchardName = "Home",
            sectionName = "Legumes",
            nickname = null,
            species = "Tamarindo",
            cultivar = "Manila Sweet",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val juneWindow = BloomForecastEngine.predictMonth(
            trees = listOf(tamarindTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )
        val octoberWindow = BloomForecastEngine.predictMonth(
            trees = listOf(tamarindTree),
            yearMonth = YearMonth.of(2026, 10),
            zoneCode = "10b"
        )

        assertThat(juneWindow).hasSize(1)
        assertThat(juneWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 5, 15))
        assertThat(octoberWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesPineappleWeakPrimaryBloomWindow() {
        val pineappleTree = TreeEntity(
            id = "pineapple-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Piña",
            cultivar = "Sugarloaf",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val marchWindow = BloomForecastEngine.predictMonth(
            trees = listOf(pineappleTree),
            yearMonth = YearMonth.of(2026, 3),
            zoneCode = "10b"
        )
        val novemberWindow = BloomForecastEngine.predictMonth(
            trees = listOf(pineappleTree),
            yearMonth = YearMonth.of(2026, 11),
            zoneCode = "10b"
        )

        assertThat(marchWindow).hasSize(1)
        assertThat(marchWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 2, 15))
        assertThat(novemberWindow).isEmpty()
    }

    @Test
    fun predictMonth_skipsAutomaticBarbadosCherryForecasts() {
        val barbadosCherryTree = TreeEntity(
            id = "acerola-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Acerola",
            cultivar = "Florida Sweet",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(barbadosCherryTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun predictMonth_skipsAutomaticJamaicanCherryForecasts() {
        val jamaicanCherryTree = TreeEntity(
            id = "jamaican-cherry-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Panama berry",
            cultivar = "Standard red-fruited type",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(jamaicanCherryTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun predictMonth_usesPassionFruitBroadPrimaryBloomWindow() {
        val passionFruitTree = TreeEntity(
            id = "passion-fruit-1",
            orchardName = "Home",
            sectionName = "Vines",
            nickname = null,
            species = "Lilikoi",
            cultivar = "Possum Purple",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val aprilWindow = BloomForecastEngine.predictMonth(
            trees = listOf(passionFruitTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val februaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(passionFruitTree),
            yearMonth = YearMonth.of(2026, 2),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 3, 15))
        assertThat(februaryWindow).isEmpty()
    }

    @Test
    fun predictMonth_skipsAutomaticPapayaForecasts() {
        val papayaTree = TreeEntity(
            id = "papaya-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Papaya",
            cultivar = "Kapoho Solo",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(papayaTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun predictMonth_usesWhiteSapotePrimaryBloomWindow() {
        val whiteSapoteTree = TreeEntity(
            id = "white-sapote-1",
            orchardName = "Home",
            sectionName = "Subtropics",
            nickname = null,
            species = "Zapote blanco",
            cultivar = "Vernon",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val novemberWindow = BloomForecastEngine.predictMonth(
            trees = listOf(whiteSapoteTree),
            yearMonth = YearMonth.of(2026, 11),
            zoneCode = "10b"
        )
        val julyWindow = BloomForecastEngine.predictMonth(
            trees = listOf(whiteSapoteTree),
            yearMonth = YearMonth.of(2026, 7),
            zoneCode = "10b"
        )

        assertThat(novemberWindow).hasSize(1)
        assertThat(novemberWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 11, 15))
        assertThat(julyWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesRegionalLycheeOverrides() {
        val lycheeTree = TreeEntity(
            id = "lychee-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Lychee",
            cultivar = "Mauritius",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val southFlorida = BloomForecastEngine.predictMonth(
            trees = listOf(lycheeTree),
            yearMonth = YearMonth.of(2026, 2),
            zoneCode = "10b",
            orchardRegionCode = "south_florida"
        )
        val hawaii = BloomForecastEngine.predictMonth(
            trees = listOf(lycheeTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "11a",
            orchardRegionCode = "hawaii"
        )
        val californiaWinter = BloomForecastEngine.predictMonth(
            trees = listOf(lycheeTree),
            yearMonth = YearMonth.of(2026, 2),
            zoneCode = "10a",
            orchardRegionCode = "california"
        )
        val californiaSpring = BloomForecastEngine.predictMonth(
            trees = listOf(lycheeTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10a",
            orchardRegionCode = "california"
        )

        assertThat(southFlorida).hasSize(1)
        assertThat(southFlorida.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 2, 5))
        assertThat(hawaii).hasSize(1)
        assertThat(hawaii.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 2, 1))
        assertThat(californiaWinter).isEmpty()
        assertThat(californiaSpring).hasSize(1)
        assertThat(californiaSpring.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 3, 15))
    }

    @Test
    fun predictMonth_skipsAutomaticBananaForecasts() {
        val bananaTree = TreeEntity(
            id = "banana-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Banana",
            cultivar = "Goldfinger",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(bananaTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun everbearingPlants_returnsTrackedBananasForSeparateDashboardListing() {
        val bananaTree = TreeEntity(
            id = "banana-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = "Plant 2",
            species = "Banana",
            cultivar = "Goldfinger",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val appleTree = bananaTree.copy(
            id = "apple-1",
            species = "Apple",
            cultivar = "Anna",
            nickname = "Back row"
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(bananaTree, appleTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("banana-1")
        assertThat(everbearing.single().treeLabel).isEqualTo("Plant 2 (Goldfinger)")
        assertThat(everbearing.single().speciesLabel).isEqualTo("Banana • Goldfinger")
        assertThat(everbearing.single().detailLabel).isEqualTo("Continuous / repeat-bearing")
    }

    @Test
    fun everbearingPlants_returnsTrackedBarbadosCherryForSeparateDashboardListing() {
        val acerolaTree = TreeEntity(
            id = "acerola-1",
            orchardName = "Home",
            sectionName = "Berry hedge",
            nickname = "Ruby",
            species = "Acerola",
            cultivar = "Florida Sweet",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val tamarindTree = acerolaTree.copy(
            id = "tamarind-1",
            species = "Tamarind",
            cultivar = "Manila Sweet",
            nickname = null
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(acerolaTree, tamarindTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("acerola-1")
        assertThat(everbearing.single().treeLabel).isEqualTo("Ruby (Florida Sweet)")
        assertThat(everbearing.single().speciesLabel).isEqualTo("Acerola • Florida Sweet")
        assertThat(everbearing.single().detailLabel).isEqualTo("Continuous / repeat-bearing")
    }

    @Test
    fun everbearingPlants_returnsTrackedJamaicanCherryForSeparateDashboardListing() {
        val jamaicanCherryTree = TreeEntity(
            id = "jamaican-cherry-1",
            orchardName = "Home",
            sectionName = "Bird grove",
            nickname = "Canopy",
            species = "Panama berry",
            cultivar = "Yellow-fruited form",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val pineappleTree = jamaicanCherryTree.copy(
            id = "pineapple-1",
            species = "Pineapple",
            cultivar = "Sugarloaf",
            nickname = null
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(jamaicanCherryTree, pineappleTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("jamaican-cherry-1")
        assertThat(everbearing.single().treeLabel).isEqualTo("Canopy (Yellow-fruited form)")
        assertThat(everbearing.single().speciesLabel).isEqualTo("Panama berry • Yellow-fruited form")
        assertThat(everbearing.single().detailLabel).isEqualTo("Continuous / repeat-bearing")
    }

    @Test
    fun everbearingPlants_returnsTrackedPapayaForSeparateDashboardListing() {
        val papayaTree = TreeEntity(
            id = "papaya-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = "Main",
            species = "Papaya",
            cultivar = "Kapoho Solo",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val passionFruitTree = papayaTree.copy(
            id = "passion-fruit-1",
            species = "Passion fruit",
            cultivar = "Possum Purple",
            nickname = null
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(papayaTree, passionFruitTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("papaya-1")
        assertThat(everbearing.single().treeLabel).isEqualTo("Main (Kapoho Solo)")
        assertThat(everbearing.single().speciesLabel).isEqualTo("Papaya • Kapoho Solo")
        assertThat(everbearing.single().detailLabel).isEqualTo("Continuous / repeat-bearing")
    }
}
