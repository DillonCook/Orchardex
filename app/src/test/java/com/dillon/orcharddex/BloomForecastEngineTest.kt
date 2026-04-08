package com.dillon.orcharddex

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.ForecastConfidence
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.ForecastSource
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.PollinationRequirement
import com.dillon.orcharddex.time.OrchardTime
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Test

class BloomForecastEngineTest {
    @Test
    fun supportedSpeciesCatalog_includesSugarCaneHybridDisplayLabel() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Sugarcane (cultivated hybrid complex)")
    }

    @Test
    fun supportedSpeciesCatalog_includesLongan() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Longan")
    }

    @Test
    fun supportedSpeciesCatalog_includesAtemoya() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Atemoya")
    }

    @Test
    fun supportedSpeciesCatalog_includesCaimitoStarApple() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Caimito (star apple)")
    }

    @Test
    fun supportedSpeciesCatalog_includesCoconut() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Coconut")
    }

    @Test
    fun supportedSpeciesCatalog_includesMango() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Mango")
    }

    @Test
    fun supportedSpeciesCatalog_includesSoursop() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Soursop")
    }

    @Test
    fun supportedSpeciesCatalog_includesCoffeeAndKiwiberry() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast("Coffee", "Kiwiberry")
    }

    @Test
    fun supportedSpeciesCatalog_includesSplitCitrusGroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Lemon",
            "Lime",
            "Orange",
            "Mandarin",
            "Grapefruit",
            "Pomelo",
            "Kumquat",
            "Calamondin",
            "Yuzu",
            "Citron"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesBackyardExpansionWave() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Kiwi",
            "Muscadine",
            "Olive",
            "Feijoa",
            "Pecan",
            "Cherimoya",
            "American Persimmon",
            "Japanese Persimmon",
            "Elderberry",
            "Currant",
            "Gooseberry",
            "Honeyberry",
            "Serviceberry"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesBlueberrySubgroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Blueberry",
            "Rabbiteye Blueberry",
            "Southern Highbush Blueberry",
            "Northern Highbush Blueberry"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesPlumSubgroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Plum",
            "Japanese Plum",
            "European Plum",
            "Hardy Hybrid Plum"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesMulberrySubgroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Mulberry",
            "Black Mulberry",
            "White Mulberry"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesFigSubgroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Fig",
            "Common Fig",
            "Smyrna Fig",
            "San Pedro Fig"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesApple() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Apple")
    }

    @Test
    fun supportedSpeciesCatalog_includesPomegranate() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Pomegranate")
    }

    @Test
    fun supportedSpeciesCatalog_includesWarmClimateStoneFruitAndPawpawBatch() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Loquat",
            "Guava",
            "Sapodilla",
            "Apricot",
            "Nectarine",
            "Pawpaw"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesBerryAndMelonBatch() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Cranberry",
            "Watermelon",
            "Cantaloupe",
            "Honeydew",
            "Canary Melon",
            "Galia Melon",
            "Casaba Melon",
            "Persian Melon"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesPearSubgroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Pear",
            "European Pear",
            "Asian Pear"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesCaneberrySubgroups() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Raspberry",
            "Red Raspberry",
            "Black Raspberry",
            "Blackberry"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesMamoncillo() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Mamoncillo")
    }

    @Test
    fun supportedSpeciesCatalog_includesAbiu() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Abiu")
    }

    @Test
    fun supportedSpeciesCatalog_includesAmbarellaJunePlum() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Ambarella (June plum)")
    }

    @Test
    fun supportedSpeciesCatalog_includesCashewApple() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).contains("Cashew (cashew apple)")
    }

    @Test
    fun supportedSpeciesCatalog_includesRemainingTropicalSinglesBackfillBatch() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Avocado",
            "Guava",
            "Loquat",
            "Jaboticaba",
            "Sapodilla",
            "Ambarella (June plum)"
        )
    }

    @Test
    fun supportedSpeciesCatalog_includesRemainingTemperateBaselineBatch() {
        val species = BloomForecastEngine.supportedSpeciesCatalog()

        assertThat(species).containsAtLeast(
            "Asian Pear",
            "Blackberry",
            "Fig",
            "Grape",
            "Mulberry",
            "Persimmon",
            "Pomegranate",
            "Raspberry",
            "Sour Cherry",
            "Strawberry"
        )
    }

    @Test
    fun speciesAutocompleteOptions_matchAliasesButReturnCanonicalDisplayLabels() {
        val starAppleSuggestions = BloomForecastEngine.speciesAutocompleteOptions("star apple")
        val scientificSuggestions = BloomForecastEngine.speciesAutocompleteOptions("cocos nucifera")
        val hybridSuggestions = BloomForecastEngine.speciesAutocompleteOptions("annona x atemoya")
        val guanabanaSuggestions = BloomForecastEngine.speciesAutocompleteOptions("guanabana")
        val genipSuggestions = BloomForecastEngine.speciesAutocompleteOptions("genip")
        val spanishLimeSuggestions = BloomForecastEngine.speciesAutocompleteOptions("spanish lime")
        val abioSuggestions = BloomForecastEngine.speciesAutocompleteOptions("abio")
        val caimitoAmarilloSuggestions = BloomForecastEngine.speciesAutocompleteOptions("caimito amarillo")
        val junePlumSuggestions = BloomForecastEngine.speciesAutocompleteOptions("june plum")
        val spondiasDulcisSuggestions = BloomForecastEngine.speciesAutocompleteOptions("spondias dulcis")
        val spondiasCythereaSuggestions = BloomForecastEngine.speciesAutocompleteOptions("spondias cytherea")
        val cashewAppleSuggestions = BloomForecastEngine.speciesAutocompleteOptions("cashew apple")
        val anacardiumSuggestions = BloomForecastEngine.speciesAutocompleteOptions("anacardium occidentale")

        assertThat(starAppleSuggestions).contains("Caimito (star apple)")
        assertThat(scientificSuggestions).contains("Coconut")
        assertThat(hybridSuggestions).contains("Atemoya")
        assertThat(guanabanaSuggestions).contains("Soursop")
        assertThat(genipSuggestions).contains("Mamoncillo")
        assertThat(spanishLimeSuggestions).contains("Mamoncillo")
        assertThat(abioSuggestions).contains("Abiu")
        assertThat(caimitoAmarilloSuggestions).contains("Abiu")
        assertThat(junePlumSuggestions).contains("Ambarella (June plum)")
        assertThat(spondiasDulcisSuggestions).contains("Ambarella (June plum)")
        assertThat(spondiasCythereaSuggestions).contains("Ambarella (June plum)")
        assertThat(cashewAppleSuggestions).contains("Cashew (cashew apple)")
        assertThat(anacardiumSuggestions).contains("Cashew (cashew apple)")
    }

    @Test
    fun resolveSpeciesAutocomplete_canonicalizesAliasesAndScientificNames() {
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("star apple"))
            .isEqualTo("Caimito (star apple)")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("cocos nucifera"))
            .isEqualTo("Coconut")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("annona x atemoya"))
            .isEqualTo("Atemoya")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("guanabana"))
            .isEqualTo("Soursop")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Melicocca bijuga"))
            .isEqualTo("Mamoncillo")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Pouteria caimito"))
            .isEqualTo("Abiu")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Achras caimito"))
            .isEqualTo("Abiu")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("June plum"))
            .isEqualTo("Ambarella (June plum)")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Spondias dulcis"))
            .isEqualTo("Ambarella (June plum)")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Spondias cytherea"))
            .isEqualTo("Ambarella (June plum)")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("cashew apple"))
            .isEqualTo("Cashew (cashew apple)")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Anacardium occidentale"))
            .isEqualTo("Cashew (cashew apple)")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("mamoncillo chino"))
            .isEqualTo("Longan")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("Mangifera indica"))
            .isEqualTo("Mango")
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("lime")).isNull()
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("genipa")).isNull()
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("jocote")).isNull()
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("yellow mombin")).isNull()
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("spondias")).isNull()
        assertThat(BloomForecastEngine.resolveSpeciesAutocomplete("caimito"))
            .isEqualTo("Caimito (star apple)")
    }

    @Test
    fun supportedCultivarCatalog_includesMangoCultivarsAndAtaulfoAliases() {
        val mangoCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Mango" }
            .associateBy { it.cultivar }

        assertThat(mangoCultivars).hasSize(51)
        assertThat(mangoCultivars.keys).containsAtLeast(
            "Rosigold",
            "Glenn",
            "Kent",
            "Sweet Tart",
            "Ataulfo",
            "Southern Blush"
        )
        assertThat(mangoCultivars.getValue("Ataulfo").aliases).containsExactly(
            "Champagne",
            "Champagne mango",
            "Honey",
            "Honey mango"
        )
        assertThat(mangoCultivars.getValue("Ataulfo").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(mangoCultivars.getValue("Southern Blush").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_returnsExactSpeciesLane() {
        val mangoCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Mango")

        assertThat(mangoCultivars).isNotEmpty()
        assertThat(mangoCultivars.all { it.species == "Mango" }).isTrue()
        assertThat(mangoCultivars.map { it.cultivar }).contains("Ataulfo")
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_resolvesScientificSpeciesAlias() {
        val mangoCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Mangifera indica")

        assertThat(mangoCultivars).isNotEmpty()
        assertThat(mangoCultivars.all { it.species == "Mango" }).isTrue()
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_respectsBlueberryFamilyCompatibility() {
        val genericBlueberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Blueberry", limit = 20)
        val southernHighbushCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Southern highbush blueberry", limit = 20)

        assertThat(genericBlueberryCultivars).isNotEmpty()
        assertThat(genericBlueberryCultivars.map { it.cultivar }).containsAtLeast("Bluecrop", "Alapaha")
        assertThat(southernHighbushCultivars).isNotEmpty()
        assertThat(southernHighbushCultivars.all { it.species == "Southern Highbush Blueberry" }).isTrue()
        assertThat(southernHighbushCultivars.map { it.cultivar }).containsAtLeast("Emerald", "Sweetcrisp", "Snowchaser")
        assertThat(southernHighbushCultivars.map { it.cultivar }).doesNotContain("Climax")
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_respectsPlumFamilyCompatibility() {
        val genericPlumCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Plum", limit = 20)
        val japanesePlumCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Japanese plum", limit = 20)
        val europeanPlumCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "European plum", limit = 20)

        assertThat(genericPlumCultivars).isNotEmpty()
        assertThat(genericPlumCultivars.map { it.cultivar }).containsAtLeast("Methley", "Stanley", "Toka")
        assertThat(japanesePlumCultivars).isNotEmpty()
        assertThat(japanesePlumCultivars.all { it.species == "Japanese Plum" }).isTrue()
        assertThat(japanesePlumCultivars.map { it.cultivar }).containsAtLeast("Methley", "Santa Rosa", "Gulfbeauty")
        assertThat(japanesePlumCultivars.map { it.cultivar }).doesNotContain("Stanley")
        assertThat(europeanPlumCultivars).isNotEmpty()
        assertThat(europeanPlumCultivars.all { it.species == "European Plum" }).isTrue()
        assertThat(europeanPlumCultivars.map { it.cultivar }).containsAtLeast("Stanley", "Damson", "Italian Prune")
        assertThat(europeanPlumCultivars.map { it.cultivar }).doesNotContain("Methley")
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_respectsMulberryFamilyCompatibility() {
        val genericMulberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Mulberry", limit = 20)
        val blackMulberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Black mulberry", limit = 20)
        val whiteMulberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "White mulberry", limit = 20)

        assertThat(genericMulberryCultivars).isNotEmpty()
        assertThat(genericMulberryCultivars.map { it.cultivar }).containsAtLeast("Illinois Everbearing", "Pakistan", "Persian Fruiting")
        assertThat(blackMulberryCultivars).isNotEmpty()
        assertThat(blackMulberryCultivars.all { it.species == "Black Mulberry" }).isTrue()
        assertThat(blackMulberryCultivars.map { it.cultivar }).contains("Persian Fruiting")
        assertThat(blackMulberryCultivars.map { it.cultivar }).doesNotContain("Pakistan")
        assertThat(whiteMulberryCultivars).isNotEmpty()
        assertThat(whiteMulberryCultivars.all { it.species == "White Mulberry" }).isTrue()
        assertThat(whiteMulberryCultivars.map { it.cultivar }).containsAtLeast("Pakistan", "White Fruiting")
        assertThat(whiteMulberryCultivars.map { it.cultivar }).doesNotContain("Persian Fruiting")
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_respectsFigFamilyCompatibility() {
        val genericFigCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Fig", limit = 20)
        val commonFigCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Common fig", limit = 20)
        val smyrnaFigCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Smyrna fig", limit = 20)

        assertThat(genericFigCultivars).isNotEmpty()
        assertThat(genericFigCultivars.map { it.cultivar }).containsAtLeast("Black Mission", "Calimyrna", "Desert King")
        assertThat(commonFigCultivars).isNotEmpty()
        assertThat(commonFigCultivars.all { it.species == "Common Fig" }).isTrue()
        assertThat(commonFigCultivars.map { it.cultivar }).containsAtLeast("Black Mission", "Brown Turkey", "White Genoa")
        assertThat(commonFigCultivars.map { it.cultivar }).doesNotContain("Calimyrna")
        assertThat(smyrnaFigCultivars).isNotEmpty()
        assertThat(smyrnaFigCultivars.all { it.species == "Smyrna Fig" }).isTrue()
        assertThat(smyrnaFigCultivars.map { it.cultivar }).contains("Calimyrna")
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_respectsPearFamilyCompatibility() {
        val genericPearCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Pear", limit = 20)
        val europeanPearCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "European pear", limit = 20)
        val asianPearCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Asian pear", limit = 20)

        assertThat(genericPearCultivars).isNotEmpty()
        assertThat(genericPearCultivars.map { it.cultivar }).containsAtLeast("Kieffer", "Bartlett", "Hosui")
        assertThat(europeanPearCultivars).isNotEmpty()
        assertThat(europeanPearCultivars.all { it.species == "European Pear" }).isTrue()
        assertThat(europeanPearCultivars.map { it.cultivar }).containsAtLeast("Bartlett", "Anjou", "Bosc")
        assertThat(europeanPearCultivars.map { it.cultivar }).doesNotContain("Hosui")
        assertThat(asianPearCultivars).isNotEmpty()
        assertThat(asianPearCultivars.all { it.species == "Asian Pear" }).isTrue()
        assertThat(asianPearCultivars.map { it.cultivar }).containsAtLeast("Hosui", "Shinseiki", "Twentieth Century")
        assertThat(asianPearCultivars.map { it.cultivar }).doesNotContain("Bartlett")
    }

    @Test
    fun cultivarAutocompleteOptions_blankQuery_respectsRaspberryFamilyCompatibility() {
        val genericRaspberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Raspberry", limit = 20)
        val redRaspberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Red raspberry", limit = 20)
        val blackRaspberryCultivars = BloomForecastEngine.cultivarAutocompleteOptions("", "Black raspberry", limit = 20)

        assertThat(genericRaspberryCultivars).isNotEmpty()
        assertThat(genericRaspberryCultivars.map { it.cultivar }).containsAtLeast("Heritage", "Bristol")
        assertThat(redRaspberryCultivars).isNotEmpty()
        assertThat(redRaspberryCultivars.all { it.species == "Red Raspberry" }).isTrue()
        assertThat(redRaspberryCultivars.map { it.cultivar }).containsAtLeast("Heritage", "Caroline", "Anne")
        assertThat(redRaspberryCultivars.map { it.cultivar }).doesNotContain("Bristol")
        assertThat(blackRaspberryCultivars).isNotEmpty()
        assertThat(blackRaspberryCultivars.all { it.species == "Black Raspberry" }).isTrue()
        assertThat(blackRaspberryCultivars.map { it.cultivar }).containsAtLeast("Bristol", "Cumberland", "Jewel")
        assertThat(blackRaspberryCultivars.map { it.cultivar }).doesNotContain("Heritage")
    }

    @Test
    fun supportedCultivarCatalog_includesJaboticabaVarietiesAndAliases() {
        val jaboticabaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Jaboticaba" }
            .associateBy { it.cultivar }

        assertThat(jaboticabaCultivars.keys).containsAtLeast(
            "Sabara",
            "Paulista",
            "Grimal",
            "Red Hybrid",
            "Escarlate",
            "Otto Andersen",
            "Esalq",
            "Pingo de Mel",
            "White Jaboticaba"
        )
        assertThat(jaboticabaCultivars.getValue("Escarlate").aliases).contains("Scarlet")
        assertThat(jaboticabaCultivars.getValue("Otto Andersen").aliases).contains("Otto Anderson")
        assertThat(jaboticabaCultivars.getValue("Pingo de Mel").aliases).contains("Honey Drop")
    }

    @Test
    fun supportedCultivarCatalog_includesCoffeeGrapeAndKiwiberryVarieties() {
        val coffeeCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Coffee" }
            .associateBy { it.cultivar }
        val grapeCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Grape" }
            .associateBy { it.cultivar }
        val kiwiberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Kiwiberry" }
            .associateBy { it.cultivar }

        assertThat(coffeeCultivars.keys).containsAtLeast(
            "Typica",
            "Bourbon",
            "Caturra",
            "Catuai",
            "Mundo Novo",
            "Geisha",
            "Villa Sarchi"
        )
        assertThat(coffeeCultivars.getValue("Geisha").aliases).contains("Gesha")
        assertThat(coffeeCultivars.getValue("Typica").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)

        assertThat(grapeCultivars.keys).containsAtLeast(
            "Concord",
            "Niagara",
            "Himrod",
            "Reliance",
            "Jupiter",
            "Mars",
            "Marquette"
        )
        assertThat(grapeCultivars.getValue("New York Muscat").aliases).contains("NY Muscat")
        assertThat(grapeCultivars.getValue("Jupiter").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)

        assertThat(kiwiberryCultivars.keys).containsAtLeast(
            "Issai",
            "Ananasnaya",
            "Ken's Red",
            "Jumbo",
            "Krupnoplodnaya"
        )
        assertThat(kiwiberryCultivars.getValue("Issai").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(kiwiberryCultivars.getValue("Ken's Red").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedCultivarCatalog_includesBackyardExpansionCultivars() {
        val kiwiCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Kiwi" }
            .associateBy { it.cultivar }
        val muscadineCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Muscadine" }
            .associateBy { it.cultivar }
        val mediterraneanCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Olive" || it.species == "Feijoa" }
            .groupBy { it.species }
        val pecanCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Pecan" }
            .associateBy { it.cultivar }
        val specialtyCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species in listOf("Cherimoya", "American persimmon", "Japanese persimmon") }
            .groupBy { it.species }
        val berryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species in listOf("Elderberry", "Currant", "Gooseberry", "Honeyberry", "Serviceberry") }

        assertThat(kiwiCultivars.keys).containsAtLeast("Hayward", "Jenny", "Monty")
        assertThat(kiwiCultivars.getValue("Jenny").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)

        assertThat(muscadineCultivars.keys).containsAtLeast("Carlos", "Fry", "Supreme")
        assertThat(muscadineCultivars.getValue("Carlos").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(muscadineCultivars.getValue("Fry").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)

        assertThat(mediterraneanCultivars.getValue("Olive").map { it.cultivar })
            .containsAtLeast("Arbequina", "Mission", "Manzanillo")
        assertThat(mediterraneanCultivars.getValue("Feijoa").map { it.cultivar })
            .containsAtLeast("Coolidge", "Mammoth", "Nazemetz")

        assertThat(pecanCultivars.keys).containsAtLeast("Desirable", "Pawnee", "Elliott")
        assertThat(specialtyCultivars.getValue("Cherimoya").map { it.cultivar })
            .containsAtLeast("Pierce", "El Bumpo", "Honeyhart")
        assertThat(specialtyCultivars.getValue("Japanese persimmon").map { it.cultivar })
            .containsAtLeast("Fuyu", "Hachiya", "Coffee Cake")
        assertThat(specialtyCultivars.getValue("American persimmon").map { it.cultivar })
            .containsAtLeast("Early Golden", "Meader", "Prok")
        assertThat(berryCultivars.map { it.cultivar }).containsAtLeast(
            "Nova",
            "Red Lake",
            "Invicta",
            "Borealis",
            "Smoky"
        )
    }

    @Test
    fun supportedCultivarCatalog_includesAppleCultivarsAndPollinationMetadata() {
        val appleCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Apple" }
            .associateBy { it.cultivar }

        assertThat(appleCultivars.keys).containsAtLeast(
            "Anna",
            "Gala",
            "Honeycrisp",
            "Golden Delicious",
            "Jonagold",
            "Mutsu",
            "Haralson",
            "Zestar!"
        )
        assertThat(appleCultivars.getValue("Golden Delicious").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(appleCultivars.getValue("Jonagold").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(appleCultivars.getValue("Mutsu").aliases).contains("Crispin")
        assertThat(appleCultivars.getValue("Pink Lady").aliases).contains("Cripps Pink")
    }

    @Test
    fun supportedCultivarCatalog_includesPomegranateCultivars() {
        val pomegranateCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Pomegranate" }
            .associateBy { it.cultivar }

        assertThat(pomegranateCultivars.keys).containsAtLeast(
            "Wonderful",
            "Salavatski",
            "Russian 26"
        )
        assertThat(pomegranateCultivars.getValue("Salavatski").aliases)
            .containsAtLeast("Salavatski-Russian", "Russian")
        assertThat(pomegranateCultivars.getValue("Wonderful").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun supportedCultivarCatalog_includesWarmClimateStoneFruitAndPawpawCultivars() {
        val loquatCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Loquat" }
            .associateBy { it.cultivar }
        val guavaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Guava" }
            .associateBy { it.cultivar }
        val sapodillaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Sapodilla" }
            .associateBy { it.cultivar }
        val apricotCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Apricot" }
            .associateBy { it.cultivar }
        val nectarineCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Nectarine" }
            .associateBy { it.cultivar }
        val pawpawCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Pawpaw" }
            .associateBy { it.cultivar }

        assertThat(loquatCultivars.keys).containsAtLeast("Advance", "Big Jim", "Champagne", "Oliver", "Tanaka")
        assertThat(loquatCultivars.getValue("Advance").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(loquatCultivars.getValue("Champagne").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)

        assertThat(guavaCultivars.keys).containsAtLeast("Ruby Supreme", "Mexican Cream", "Barbie Pink", "Thai White")
        assertThat(sapodillaCultivars.keys).containsAtLeast("Alano", "Hasya", "Makok", "Tikal")
        assertThat(sapodillaCultivars.getValue("Alano").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)

        assertThat(apricotCultivars.keys).containsAtLeast("Blenheim", "Harcot", "Moongold", "Sungold", "Westcot")
        assertThat(apricotCultivars.getValue("Moongold").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(apricotCultivars.getValue("Sungold").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)

        assertThat(nectarineCultivars.keys).containsAtLeast("Early Glo", "Fantasia", "Flavortop", "Redgold", "Sunglo")
        assertThat(nectarineCultivars.getValue("Redgold").aliases).contains("Red Gold")

        assertThat(pawpawCultivars.keys).containsAtLeast("Allegheny", "NC-1", "Shenandoah", "Sunflower", "Susquehanna")
        assertThat(pawpawCultivars.getValue("Sunflower").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesBerryAndMelonCultivars() {
        val cranberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Cranberry" }
            .associateBy { it.cultivar }
        val watermelonCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Watermelon" }
            .associateBy { it.cultivar }
        val cantaloupeCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Cantaloupe" }
            .associateBy { it.cultivar }
        val honeydewCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Honeydew" }
            .associateBy { it.cultivar }
        val canaryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Canary melon" }
            .associateBy { it.cultivar }
        val galiaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Galia melon" }
            .associateBy { it.cultivar }
        val casabaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Casaba melon" }
            .associateBy { it.cultivar }
        val persianCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Persian melon" }
            .associateBy { it.cultivar }

        assertThat(cranberryCultivars.keys).containsAtLeast("Stevens", "Ben Lear", "Pilgrim")
        assertThat(watermelonCultivars.keys).containsAtLeast("Sugar Baby", "Crimson Sweet", "Charleston Gray")
        assertThat(watermelonCultivars.keys).contains("Moon and Stars")
        assertThat(cantaloupeCultivars.keys).containsAtLeast("Ambrosia", "Athena", "Hale's Best")
        assertThat(cantaloupeCultivars.getValue("Hale's Best").aliases).contains("Hale's Best Jumbo")
        assertThat(honeydewCultivars.keys).containsAtLeast("Honey Brew", "Earli Dew", "Orange Flesh Honey Dew")
        assertThat(honeydewCultivars.getValue("Earli Dew").aliases).contains("Earlidew")
        assertThat(canaryCultivars.keys).containsAtLeast("Juan Canary", "Sugarnut")
        assertThat(galiaCultivars.keys).containsAtLeast("Galia", "Sivan")
        assertThat(casabaCultivars.keys).containsAtLeast("Golden Beauty", "Santa Claus")
        assertThat(persianCultivars.keys).containsAtLeast("Persian", "Caspian")
    }

    @Test
    fun supportedCultivarCatalog_includesAvocadoCultivarsAndPollinationMetadata() {
        val avocadoCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Avocado" }
            .associateBy { it.cultivar }

        assertThat(avocadoCultivars.keys).containsAtLeast(
            "Hass",
            "Lamb Hass",
            "Pinkerton",
            "Reed",
            "Lula",
            "Bacon",
            "Fuerte",
            "Zutano",
            "Monroe",
            "Pollock"
        )
        assertThat(avocadoCultivars.getValue("Hass").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(avocadoCultivars.getValue("Lula").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(avocadoCultivars.getValue("Fuerte").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(avocadoCultivars.getValue("Pollock").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesBlueberrySubgroupCultivars() {
        val northernHighbushCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Northern Highbush Blueberry" }
            .associateBy { it.cultivar }
        val southernHighbushCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Southern Highbush Blueberry" }
            .associateBy { it.cultivar }
        val rabbiteyeCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Rabbiteye Blueberry" }
            .associateBy { it.cultivar }

        assertThat(northernHighbushCultivars.keys).containsAtLeast("Bluecrop", "Duke", "Patriot")
        assertThat(southernHighbushCultivars.keys).containsAtLeast("Snowchaser", "Emerald", "Sweetcrisp")
        assertThat(rabbiteyeCultivars.keys).containsAtLeast("Climax", "Premier", "Tifblue")
        assertThat(rabbiteyeCultivars.getValue("Climax").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedCultivarCatalog_includesPlumSubgroupCultivars() {
        val japanesePlumCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Japanese Plum" }
            .associateBy { it.cultivar }
        val europeanPlumCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "European Plum" }
            .associateBy { it.cultivar }
        val hardyHybridPlumCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Hardy Hybrid Plum" }
            .associateBy { it.cultivar }

        assertThat(japanesePlumCultivars.keys).containsAtLeast("Methley", "Shiro", "Santa Rosa", "Gulfbeauty")
        assertThat(japanesePlumCultivars.getValue("Methley").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(japanesePlumCultivars.getValue("Santa Rosa").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)

        assertThat(europeanPlumCultivars.keys).containsAtLeast("Stanley", "Damson", "Italian Prune", "Green Gage")
        assertThat(europeanPlumCultivars.getValue("Stanley").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(europeanPlumCultivars.getValue("President").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)

        assertThat(hardyHybridPlumCultivars.keys).containsAtLeast("Alderman", "Superior", "Toka")
        assertThat(hardyHybridPlumCultivars.getValue("Superior").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedCultivarCatalog_includesMulberrySubgroupCultivars() {
        val genericMulberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Mulberry" }
            .associateBy { it.cultivar }
        val blackMulberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Black Mulberry" }
            .associateBy { it.cultivar }
        val whiteMulberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "White Mulberry" }
            .associateBy { it.cultivar }

        assertThat(genericMulberryCultivars.keys).containsAtLeast("Illinois Everbearing", "Dwarf Everbearing")
        assertThat(genericMulberryCultivars.getValue("Illinois Everbearing").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)

        assertThat(blackMulberryCultivars.keys).contains("Persian Fruiting")
        assertThat(blackMulberryCultivars.getValue("Persian Fruiting").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)

        assertThat(whiteMulberryCultivars.keys).containsAtLeast("Pakistan", "White Fruiting")
        assertThat(whiteMulberryCultivars.getValue("Pakistan").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesFigSubgroupCultivars() {
        val commonFigCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Common Fig" }
            .associateBy { it.cultivar }
        val smyrnaFigCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Smyrna Fig" }
            .associateBy { it.cultivar }
        val sanPedroFigCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "San Pedro Fig" }
            .associateBy { it.cultivar }

        assertThat(commonFigCultivars.keys).containsAtLeast("Black Mission", "Brown Turkey", "Celeste", "White Genoa")
        assertThat(commonFigCultivars.getValue("Black Mission").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(commonFigCultivars.getValue("Italian Everbearing").aliases).contains("Italian Honey")

        assertThat(smyrnaFigCultivars.keys).containsExactly("Calimyrna")
        assertThat(smyrnaFigCultivars.getValue("Calimyrna").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)

        assertThat(sanPedroFigCultivars.keys).containsAtLeast("Desert King", "Lampeira", "San Pedro")
        assertThat(sanPedroFigCultivars.getValue("Desert King").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedCultivarCatalog_includesPearSubgroupCultivars() {
        val genericPearCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Pear" }
            .associateBy { it.cultivar }
        val europeanPearCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "European Pear" }
            .associateBy { it.cultivar }
        val asianPearCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Asian Pear" }
            .associateBy { it.cultivar }

        assertThat(genericPearCultivars.keys).contains("Kieffer")

        assertThat(europeanPearCultivars.keys).containsAtLeast("Bartlett", "Anjou", "Bosc", "Comice")
        assertThat(europeanPearCultivars.getValue("Bartlett").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(europeanPearCultivars.getValue("Anjou").aliases).contains("d'Anjou")

        assertThat(asianPearCultivars.keys).containsAtLeast("Hosui", "Chojuro", "Shinseiki", "Twentieth Century")
        assertThat(asianPearCultivars.getValue("Hosui").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(asianPearCultivars.getValue("Twentieth Century").aliases).contains("Nijisseiki")
    }

    @Test
    fun supportedCultivarCatalog_includesCaneberryCultivars() {
        val redRaspberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Red Raspberry" }
            .associateBy { it.cultivar }
        val blackRaspberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Black Raspberry" }
            .associateBy { it.cultivar }
        val blackberryCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Blackberry" }
            .associateBy { it.cultivar }

        assertThat(redRaspberryCultivars.keys).containsAtLeast("Killarney", "Nova", "Heritage", "Anne")
        assertThat(redRaspberryCultivars.getValue("Heritage").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)

        assertThat(blackRaspberryCultivars.keys).containsAtLeast("Bristol", "Cumberland", "Jewel")
        assertThat(blackRaspberryCultivars.getValue("Jewel").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)

        assertThat(blackberryCultivars.keys).containsAtLeast("Chester", "Marion", "Prime-Ark Freedom", "Triple Crown")
        assertThat(blackberryCultivars.getValue("Marion").aliases).contains("Marionberry")
        assertThat(blackberryCultivars.getValue("Prime-Ark Freedom").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun supportedCultivarCatalog_includesLonganCultivarsAndPollinationMetadata() {
        val longanCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Longan" }
            .associateBy { it.cultivar }

        assertThat(longanCultivars.keys).containsAtLeast(
            "Kohala",
            "Edau",
            "Biew Kiew",
            "Chompoo I",
            "Haew",
            "Diamond River"
        )
        assertThat(longanCultivars.getValue("Kohala").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(longanCultivars.getValue("Diamond River").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesAtemoyaCultivarsAndPollinationMetadata() {
        val atemoyaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Atemoya" }
            .associateBy { it.cultivar }

        assertThat(atemoyaCultivars.keys).containsAtLeast(
            "Gefner",
            "Page",
            "African Pride",
            "Bradley",
            "Pink Mammoth",
            "Priestly"
        )
        assertThat(atemoyaCultivars.getValue("Gefner").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(atemoyaCultivars.getValue("African Pride").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesCaimitoCultivarsAndPollinationMetadata() {
        val caimitoCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Caimito (star apple)" }
            .associateBy { it.cultivar }

        assertThat(caimitoCultivars.keys).containsExactly(
            "Haitian Star",
            "Blanco Star"
        )
        assertThat(caimitoCultivars.getValue("Haitian Star").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(caimitoCultivars.getValue("Blanco Star").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun supportedCultivarCatalog_includesCoconutCultivarsAndPollinationMetadata() {
        val coconutCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Coconut" }
            .associateBy { it.cultivar }

        assertThat(coconutCultivars.keys).containsAtLeast(
            "Jamaican Tall",
            "Panama Tall",
            "Malayan Dwarf",
            "Green Malayan Dwarf",
            "Yellow Malayan Dwarf",
            "Golden Malayan Dwarf",
            "Red Malayan Dwarf",
            "Maypan"
        )
        assertThat(coconutCultivars.getValue("Jamaican Tall").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(coconutCultivars.getValue("Panama Tall").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(coconutCultivars.getValue("Yellow Malayan Dwarf").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(coconutCultivars.getValue("Golden Malayan Dwarf").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(coconutCultivars.getValue("Red Malayan Dwarf").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(coconutCultivars.getValue("Malayan Dwarf").pollinationRequirement)
            .isEqualTo(PollinationRequirement.UNKNOWN)
        assertThat(coconutCultivars.getValue("Maypan").pollinationRequirement)
            .isEqualTo(PollinationRequirement.UNKNOWN)
    }

    @Test
    fun supportedCultivarCatalog_includesSoursopCultivarsAndPollinationMetadata() {
        val soursopCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Soursop" }
            .associateBy { it.cultivar }

        assertThat(soursopCultivars.keys).containsExactly(
            "Sweet",
            "Bennett",
            "Cuban Fiberless",
            "Whitman Fiberless"
        )
        assertThat(soursopCultivars.getValue("Sweet").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(soursopCultivars.getValue("Whitman Fiberless").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesMamoncilloCultivarsAndPollinationMetadata() {
        val mamoncilloCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Mamoncillo" }
            .associateBy { it.cultivar }

        assertThat(mamoncilloCultivars.keys).containsExactly(
            "Montgomery",
            "Jose Pabon",
            "Large"
        )
        assertThat(mamoncilloCultivars.getValue("Montgomery").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(mamoncilloCultivars.getValue("Jose Pabon").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(mamoncilloCultivars.getValue("Large").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedCultivarCatalog_includesAbiuCultivarsAndPollinationMetadata() {
        val abiuCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Abiu" }
            .associateBy { it.cultivar }

        assertThat(abiuCultivars.keys).containsExactly(
            "Gray",
            "Z-2"
        )
        assertThat(abiuCultivars.getValue("Gray").pollinationRequirement).isNull()
        assertThat(abiuCultivars.getValue("Z-2").pollinationRequirement).isNull()
    }

    @Test
    fun supportedCultivarCatalog_includesLycheeCultivarsAndPollinationMetadata() {
        val lycheeCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Lychee" }
            .associateBy { it.cultivar }

        assertThat(lycheeCultivars.keys).containsAtLeast(
            "Mauritius",
            "Fei Zi Xiao",
            "Sweetheart"
        )
        assertThat(lycheeCultivars.getValue("Sweetheart").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesCashewTypeLanesAndPollinationMetadata() {
        val cashewCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Cashew (cashew apple)" }
            .associateBy { it.cultivar }

        assertThat(cashewCultivars.keys).containsExactly(
            "Gigante / Tardio",
            "Anão / Precoce"
        )
        assertThat(cashewCultivars.getValue("Gigante / Tardio").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(cashewCultivars.getValue("Anão / Precoce").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun supportedSpeciesReferenceCatalog_exposesModeledZoneBloomWindows() {
        val appleEntry = BloomForecastEngine.supportedSpeciesReferenceCatalog()
            .first { it.species == "Apple" }

        assertThat(appleEntry.referenceBloomTimingLabel).contains("USDA 7A")
        assertThat(appleEntry.zoneBloomTimings).hasSize(26)
        assertThat(appleEntry.zoneBloomTimings.first().zoneLabel).isEqualTo("USDA 1A")
        assertThat(appleEntry.zoneBloomTimings.last().zoneLabel).isEqualTo("USDA 13B")
    }

    @Test
    fun supportedSpeciesReferenceCatalog_shiftsBloomEarlierInWarmerZones() {
        val appleEntry = BloomForecastEngine.supportedSpeciesReferenceCatalog()
            .first { it.species == "Apple" }
        val colderZone = appleEntry.zoneBloomTimings.first { it.zoneCode == "5a" }
        val referenceZone = appleEntry.zoneBloomTimings.first { it.zoneCode == "7a" }
        val warmerZone = appleEntry.zoneBloomTimings.first { it.zoneCode == "9a" }

        assertThat(colderZone.timingLabel).isEqualTo("Apr 21 - May 3")
        assertThat(referenceZone.timingLabel).isEqualTo("Apr 5 - Apr 17")
        assertThat(warmerZone.timingLabel).isEqualTo("Mar 20 - Apr 1")
    }

    @Test
    fun supportedCultivarCatalog_includesCommonBananas() {
        val bananaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Banana" }
            .associateBy { it.cultivar }

        assertThat(bananaCultivars.keys).containsAtLeast(
            "Dwarf Cavendish",
            "Blue Java",
            "Goldfinger",
            "Mona Lisa",
            "Rajapuri",
            "Sweetheart",
            "Namwa",
            "Dwarf Namwa",
            "Mysore",
            "Brazilian",
            "Saba",
            "FHIA-17",
            "FHIA-21"
        )
        assertThat(bananaCultivars.getValue("Namwa").aliases).containsAtLeast(
            "Pisang Awak",
            "Kluai Namwa",
            "Namwah",
            "Ducasse"
        )
        assertThat(bananaCultivars.getValue("Sweetheart").aliases).contains("FHIA-03")
    }

    @Test
    fun supportedCultivarCatalog_includesDragonFruitAndPollinationMetadata() {
        val dragonFruitCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Dragon Fruit" }
            .associateBy { it.cultivar }

        assertThat(dragonFruitCultivars.keys).containsAtLeast(
            "American Beauty",
            "Alice",
            "Armando",
            "Arizona Purple",
            "Asunta 1",
            "Asunta 5 Paco",
            "Asunta 5 Patricia",
            "Asunta 5 Starburst",
            "Asunta 5 Sunset Sherbet",
            "Asunta 5 Ventura",
            "Asunta 6",
            "AX",
            "Aztec Gem",
            "B B Red",
            "B B White",
            "Baby Cerrado",
            "Blush",
            "Bones Purple",
            "Bruni",
            "Capistrano Valley",
            "Cebra",
            "Cerise",
            "Chameleon",
            "Colombian Supreme",
            "Columbian Red",
            "Commercial White",
            "Connie Gee",
            "Cosmic Charlie",
            "Costa Rican Sunset",
            "Crimson",
            "Desert King",
            "Edgar's Baby",
            "Florida Sweet White",
            "Fruit Punch",
            "Giant Columbian Yellow",
            "Giant Orange",
            "Godzilla",
            "Hana",
            "Hawaiian Orange",
            "Honey White",
            "Isis Gold",
            "Jade Red",
            "Kathy Van Arum",
            "King Kong",
            "Lake Atitlan",
            "Lucille Lemonade",
            "Malaysian Purple",
            "Medusa",
            "NOID",
            "Pink Beauty",
            "Pink Champagne",
            "Pink Diamond",
            "Pink Lemonade",
            "Pink Panther",
            "Purple Heart",
            "Purple Megalanthus",
            "Scott's Purple",
            "Southern Cross",
            "Sugar Dragon",
            "Sunshine White",
            "Supernova",
            "Taiwan Magenta",
            "Thai Dragon",
            "Thomson G2",
            "Townsend Pink",
            "Tricia",
            "Trish Red",
            "Vietnamese White",
            "Voodoo Child",
            "Wongarra Red",
            "Yellow Colombiana",
        )
        assertThat(dragonFruitCultivars.getValue("American Beauty").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Asunta 1").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Asunta 5 Paco").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("AX").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Bruni").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Capistrano Valley").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Cerise").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Chameleon").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Colombian Supreme").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(dragonFruitCultivars.getValue("Desert King").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Giant Columbian Yellow").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Isis Gold").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Jade Red").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Kathy Van Arum").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Lucille Lemonade").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Malaysian Purple").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(dragonFruitCultivars.getValue("Pink Beauty").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Pink Champagne").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Pink Diamond").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Pink Panther").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Purple Megalanthus").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(dragonFruitCultivars.getValue("Scott's Purple").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Sunshine White").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Thomson G2").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Townsend Pink").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Tricia").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Trish Red").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Yellow Colombiana").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Fruit Punch").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(dragonFruitCultivars.getValue("Dennis Pale Pink").pollinationRequirement)
            .isEqualTo(PollinationRequirement.UNKNOWN)
        assertThat(dragonFruitCultivars.getValue("NOID").pollinationRequirement)
            .isEqualTo(PollinationRequirement.UNKNOWN)
        assertThat(dragonFruitCultivars.getValue("Colombian Supreme").aliases)
            .containsAtLeast("Columbian Supreme", "Common Red")
        assertThat(dragonFruitCultivars.getValue("Desert King").aliases)
            .contains("DK16")
        assertThat(dragonFruitCultivars.getValue("Isis Gold").aliases)
            .contains("Aussie Gold")
        assertThat(dragonFruitCultivars.getValue("Palora").aliases)
            .doesNotContain("Yellow Dragon")
        assertThat(dragonFruitCultivars.getValue("Thomson G2").aliases)
            .contains("Paul Thomson G2")
        assertThat(dragonFruitCultivars.getValue("Yellow Colombiana").aliases)
            .containsAtLeast("Yellow Megalanthus", "Yellow Dragon", "Common Yellow")
        assertThat(dragonFruitCultivars.getValue("Purple Haze").aliases).doesNotContain("Scott's Purple")
        assertThat(dragonFruitCultivars.getValue("Scott's Purple").aliases).contains("Scotts Purple")
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
            "White Jade",
            "Amritha"
        )
        assertThat(pineappleCultivars.getValue("MD-2").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(pineappleCultivars.getValue("Sugarloaf").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(pineappleCultivars.getValue("White Jade").pollinationRequirement)
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
    fun supportedCultivarCatalog_includesSugarCaneGroupsAndNamedClones() {
        val sugarCaneCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Sugarcane (cultivated hybrid complex)" }
            .associateBy { it.cultivar }

        assertThat(sugarCaneCultivars.keys).containsAtLeast(
            "Chewing cane",
            "Syrup cane",
            "Crystal / commercial cane",
            "Yellow Gal",
            "White Transparent",
            "Louisiana Ribbon",
            "Green German",
            "CP 96-1252",
            "CP 01-1372",
            "CP 00-1101",
            "CP 89-2143"
        )
        assertThat(sugarCaneCultivars.getValue("Yellow Gal").pollinationRequirement).isNull()
        assertThat(sugarCaneCultivars.getValue("Crystal / commercial cane").pollinationRequirement).isNull()
    }

    @Test
    fun supportedCultivarCatalog_includesCanistelSeedSetAndPollinationMetadata() {
        val canistelCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Canistel" }
            .associateBy { it.cultivar }

        assertThat(canistelCultivars.keys).containsAtLeast(
            "Bruce",
            "Fairchild #1",
            "Fairchild #2",
            "Fitzpatrick",
            "Keisau",
            "Oro",
            "Trompo",
            "TREC 9680",
            "TREC 9681"
        )
        assertThat(canistelCultivars.getValue("Bruce").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(canistelCultivars.getValue("TREC 9680").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesMameySapoteSeedSetAndPollinationMetadata() {
        val mameySapoteCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Mamey Sapote" }
            .associateBy { it.cultivar }

        assertThat(mameySapoteCultivars.keys).containsAtLeast(
            "Pantin",
            "Magana",
            "Pace",
            "Tazumal",
            "Mayapan",
            "Copan",
            "Lara",
            "Florida",
            "Piloto",
            "Chenox",
            "Abuelo",
            "Francisco Fernandez",
            "Flores",
            "Viejo",
            "Lorito",
            "Cepeda Especial",
            "Akil Especial",
            "AREC No. 3"
        )
        assertThat(mameySapoteCultivars.getValue("Pantin").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(mameySapoteCultivars.getValue("AREC No. 3").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun supportedCultivarCatalog_includesBlackSapoteSeedSetAndPollinationMetadata() {
        val blackSapoteCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Black Sapote" }
            .associateBy { it.cultivar }

        assertThat(blackSapoteCultivars.keys).containsAtLeast(
            "Merida",
            "Bernicker",
            "Mossman",
            "Maher",
            "Ricks Late",
            "Superb",
            "Cocktail"
        )
        assertThat(blackSapoteCultivars.getValue("Merida").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(blackSapoteCultivars.getValue("Superb").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun supportedCultivarCatalog_includesGreenSapoteSparseSeedSet() {
        val greenSapoteCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Green Sapote" }
            .associateBy { it.cultivar }

        assertThat(greenSapoteCultivars.keys).containsExactly(
            "UF/TREC selection",
            "Whitman"
        )
        assertThat(greenSapoteCultivars.getValue("UF/TREC selection").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(greenSapoteCultivars.getValue("Whitman").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun resolveCultivarAutocomplete_matchesBananaAliases() {
        val match = BloomForecastEngine.resolveCultivarAutocomplete("Ice Cream", "Banana")
        val namwaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Pisang Awak", "Banana")
        val dwarfNamwaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Dwarf Ducasse", "Banana")
        val sweetheartMatch = BloomForecastEngine.resolveCultivarAutocomplete("FHIA-03", "Banana")

        assertThat(match).isNotNull()
        assertThat(match?.species).isEqualTo("Banana")
        assertThat(match?.cultivar).isEqualTo("Blue Java")
        assertThat(namwaMatch?.cultivar).isEqualTo("Namwa")
        assertThat(dwarfNamwaMatch?.cultivar).isEqualTo("Dwarf Namwa")
        assertThat(sweetheartMatch?.cultivar).isEqualTo("Sweetheart")
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
    fun resolveCultivarAutocomplete_matchesMangoAliases() {
        val honeyMatch = BloomForecastEngine.resolveCultivarAutocomplete("Honey", "Mango")
        val champagneMatch = BloomForecastEngine.resolveCultivarAutocomplete("Champagne mango", "Mangifera indica")

        assertThat(honeyMatch?.cultivar).isEqualTo("Ataulfo")
        assertThat(champagneMatch?.cultivar).isEqualTo("Ataulfo")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesJaboticabaAliases() {
        val scarletMatch = BloomForecastEngine.resolveCultivarAutocomplete("Scarlet", "Jaboticaba")
        val ottoMatch = BloomForecastEngine.resolveCultivarAutocomplete("Otto Anderson", "Jaboticaba")
        val honeyDropMatch = BloomForecastEngine.resolveCultivarAutocomplete("Honey Drop", "Jaboticaba")
        val sabaraAccentMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sabará", "Jaboticaba")

        assertThat(scarletMatch?.cultivar).isEqualTo("Escarlate")
        assertThat(ottoMatch?.cultivar).isEqualTo("Otto Andersen")
        assertThat(honeyDropMatch?.cultivar).isEqualTo("Pingo de Mel")
        assertThat(sabaraAccentMatch?.cultivar).isEqualTo("Sabara")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesCoffeeKiwiberryAndPassionFruitAliases() {
        val hardyKiwiSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("hardy kiwi")
        val coffeeSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("coffea arabica")
        val geshaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Gesha", "Coffee")
        val nyMuscatMatch = BloomForecastEngine.resolveCultivarAutocomplete("NY Muscat", "Grape")
        val possomMatch = BloomForecastEngine.resolveCultivarAutocomplete("Purple Possom", "Passion fruit")

        assertThat(hardyKiwiSpeciesMatch).isEqualTo("Kiwiberry")
        assertThat(coffeeSpeciesMatch).isEqualTo("Coffee")
        assertThat(geshaMatch?.cultivar).isEqualTo("Geisha")
        assertThat(nyMuscatMatch?.cultivar).isEqualTo("New York Muscat")
        assertThat(possomMatch?.cultivar).isEqualTo("Possum Purple")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesBackyardExpansionAliases() {
        val kiwiSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("kiwifruit")
        val feijoaSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("pineapple guava")
        val muscadineSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("scuppernong")
        val honeyberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("haskap")
        val serviceberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("juneberry")
        val cherimoyaSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("annona cherimola")
        val americanPersimmonMatch = BloomForecastEngine.resolveSpeciesAutocomplete("diospyros virginiana")
        val japanesePersimmonMatch = BloomForecastEngine.resolveSpeciesAutocomplete("diospyros kaki")
        val calamondinSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("calamansi")
        val pomeloSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("pummelo")
        val jennyMatch = BloomForecastEngine.resolveCultivarAutocomplete("Jenny", "Kiwi")
        val fryMatch = BloomForecastEngine.resolveCultivarAutocomplete("Fry Seedless", "Muscadine")
        val variegatedMatch = BloomForecastEngine.resolveCultivarAutocomplete("Variegated Kumquat", "Kumquat")
        val buddhaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Buddhas Hand", "Citron")
        val coffeeCakeMatch = BloomForecastEngine.resolveCultivarAutocomplete("Nishimura Wase", "Japanese persimmon")

        assertThat(kiwiSpeciesMatch).isEqualTo("Kiwi")
        assertThat(feijoaSpeciesMatch).isEqualTo("Feijoa")
        assertThat(muscadineSpeciesMatch).isEqualTo("Muscadine")
        assertThat(honeyberrySpeciesMatch).isEqualTo("Honeyberry")
        assertThat(serviceberrySpeciesMatch).isEqualTo("Serviceberry")
        assertThat(cherimoyaSpeciesMatch).isEqualTo("Cherimoya")
        assertThat(americanPersimmonMatch).isEqualTo("American Persimmon")
        assertThat(japanesePersimmonMatch).isEqualTo("Japanese Persimmon")
        assertThat(calamondinSpeciesMatch).isEqualTo("Calamondin")
        assertThat(pomeloSpeciesMatch).isEqualTo("Pomelo")
        assertThat(jennyMatch?.cultivar).isEqualTo("Jenny")
        assertThat(fryMatch?.cultivar).isEqualTo("Fry")
        assertThat(variegatedMatch?.cultivar).isEqualTo("Centennial Variegated")
        assertThat(buddhaMatch?.cultivar).isEqualTo("Buddha's Hand")
        assertThat(coffeeCakeMatch?.cultivar).isEqualTo("Coffee Cake")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesAvocadoAliases() {
        val avocadoSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("alligator pear")
        val scientificSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("persea americana")
        val lambMatch = BloomForecastEngine.resolveCultivarAutocomplete("Lamb", "Avocado")
        val sirPrizeMatch = BloomForecastEngine.resolveCultivarAutocomplete("SirPrize", "Avocado")
        val typeAMatch = BloomForecastEngine.resolveCultivarAutocomplete("Type A Hass", "Avocado")
        val typeBMatch = BloomForecastEngine.resolveCultivarAutocomplete("B-type Zutano", "Avocado")

        assertThat(avocadoSpeciesMatch).isEqualTo("Avocado")
        assertThat(scientificSpeciesMatch).isEqualTo("Avocado")
        assertThat(lambMatch?.cultivar).isEqualTo("Lamb Hass")
        assertThat(sirPrizeMatch?.cultivar).isEqualTo("Sir Prize")
        assertThat(typeAMatch?.cultivar).isEqualTo("Hass")
        assertThat(typeBMatch?.cultivar).isEqualTo("Zutano")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesBlueberrySubgroupAliases() {
        val rabbiteyeSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("rabbiteye")
        val southernHighbushSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("southern highbush blueberry")
        val northernHighbushSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("highbush blueberry")
        val blueCropMatch = BloomForecastEngine.resolveCultivarAutocomplete("Blue Crop", "Northern highbush blueberry")
        val sweetCrispMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sweet Crisp", "Blueberry")
        val powderBlueMatch = BloomForecastEngine.resolveCultivarAutocomplete("Powder Blue", "Blueberry")

        assertThat(rabbiteyeSpeciesMatch).isEqualTo("Rabbiteye Blueberry")
        assertThat(southernHighbushSpeciesMatch).isEqualTo("Southern Highbush Blueberry")
        assertThat(northernHighbushSpeciesMatch).isEqualTo("Northern Highbush Blueberry")
        assertThat(blueCropMatch?.cultivar).isEqualTo("Bluecrop")
        assertThat(sweetCrispMatch?.cultivar).isEqualTo("Sweetcrisp")
        assertThat(powderBlueMatch?.cultivar).isEqualTo("Powderblue")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesPlumSubgroupAliases() {
        val japanesePlumSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("prunus salicina")
        val europeanPlumSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("prunus domestica")
        val hybridPlumSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("american hybrid plum")
        val gulfBeautyMatch = BloomForecastEngine.resolveCultivarAutocomplete("Gulf Beauty", "Japanese plum")
        val greengageMatch = BloomForecastEngine.resolveCultivarAutocomplete("Greengage", "European plum")
        val blackIceMatch = BloomForecastEngine.resolveCultivarAutocomplete("Black Ice", "Plum")

        assertThat(japanesePlumSpeciesMatch).isEqualTo("Japanese Plum")
        assertThat(europeanPlumSpeciesMatch).isEqualTo("European Plum")
        assertThat(hybridPlumSpeciesMatch).isEqualTo("Hardy Hybrid Plum")
        assertThat(gulfBeautyMatch?.cultivar).isEqualTo("Gulfbeauty")
        assertThat(greengageMatch?.cultivar).isEqualTo("Green Gage")
        assertThat(blackIceMatch?.cultivar).isEqualTo("BlackIce")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesMulberrySubgroupAliases() {
        val blackMulberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("persian mulberry")
        val whiteMulberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("morus alba")
        val blackCultivarMatch = BloomForecastEngine.resolveCultivarAutocomplete("Persian", "Mulberry")
        val pakistanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Pakistani mulberry", "White mulberry")

        assertThat(blackMulberrySpeciesMatch).isEqualTo("Black Mulberry")
        assertThat(whiteMulberrySpeciesMatch).isEqualTo("White Mulberry")
        assertThat(blackCultivarMatch?.cultivar).isEqualTo("Persian Fruiting")
        assertThat(pakistanMatch?.cultivar).isEqualTo("Pakistan")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesFigSubgroupAliases() {
        val figSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("ficus carica")
        val commonFigSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("common fig")
        val smyrnaFigSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("calimyrna type fig")
        val missionMatch = BloomForecastEngine.resolveCultivarAutocomplete("Mission", "Fig")
        val vdbMatch = BloomForecastEngine.resolveCultivarAutocomplete("VdB", "Common fig")
        val kingMatch = BloomForecastEngine.resolveCultivarAutocomplete("King", "San Pedro fig")

        assertThat(figSpeciesMatch).isEqualTo("Fig")
        assertThat(commonFigSpeciesMatch).isEqualTo("Common Fig")
        assertThat(smyrnaFigSpeciesMatch).isEqualTo("Smyrna Fig")
        assertThat(missionMatch?.cultivar).isEqualTo("Black Mission")
        assertThat(vdbMatch?.cultivar).isEqualTo("Violette de Bordeaux")
        assertThat(kingMatch?.cultivar).isEqualTo("Desert King")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesAppleAliases() {
        val appleSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("malus domestica")
        val galaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Royal Gala", "Apple")
        val pinkLadyMatch = BloomForecastEngine.resolveCultivarAutocomplete("Cripps Pink", "Apple")
        val zestarMatch = BloomForecastEngine.resolveCultivarAutocomplete("Zestar", "Apple")
        val crispinMatch = BloomForecastEngine.resolveCultivarAutocomplete("Crispin", "Apple")

        assertThat(appleSpeciesMatch).isEqualTo("Apple")
        assertThat(galaMatch?.cultivar).isEqualTo("Gala")
        assertThat(pinkLadyMatch?.cultivar).isEqualTo("Pink Lady")
        assertThat(zestarMatch?.cultivar).isEqualTo("Zestar!")
        assertThat(crispinMatch?.cultivar).isEqualTo("Mutsu")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesPomegranateAliases() {
        val pomegranateSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("punica granatum")
        val salavatskiMatch = BloomForecastEngine.resolveCultivarAutocomplete("Russian", "Pomegranate")
        val russian26Match = BloomForecastEngine.resolveCultivarAutocomplete("Russian 26", "Pomegranate")

        assertThat(pomegranateSpeciesMatch).isEqualTo("Pomegranate")
        assertThat(salavatskiMatch?.cultivar).isEqualTo("Salavatski")
        assertThat(russian26Match?.cultivar).isEqualTo("Russian 26")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesWarmClimateStoneFruitAndPawpawAliases() {
        val loquatSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("eriobotrya japonica")
        val guavaSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("psidium guajava")
        val sapodillaSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("naseberry")
        val apricotSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("prunus armeniaca")
        val nectarineSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("prunus persica var. nucipersica")
        val pawpawSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("asimina triloba")
        val bigJimMatch = BloomForecastEngine.resolveCultivarAutocomplete("BigJim", "Loquat")
        val redGoldMatch = BloomForecastEngine.resolveCultivarAutocomplete("Red Gold", "Nectarine")
        val nc1Match = BloomForecastEngine.resolveCultivarAutocomplete("NC1", "Pawpaw")
        val ksuAtwoodMatch = BloomForecastEngine.resolveCultivarAutocomplete("KSU Atwood", "Pawpaw")

        assertThat(loquatSpeciesMatch).isEqualTo("Loquat")
        assertThat(guavaSpeciesMatch).isEqualTo("Guava")
        assertThat(sapodillaSpeciesMatch).isEqualTo("Sapodilla")
        assertThat(apricotSpeciesMatch).isEqualTo("Apricot")
        assertThat(nectarineSpeciesMatch).isEqualTo("Nectarine")
        assertThat(pawpawSpeciesMatch).isEqualTo("Pawpaw")
        assertThat(bigJimMatch?.cultivar).isEqualTo("Big Jim")
        assertThat(redGoldMatch?.cultivar).isEqualTo("Redgold")
        assertThat(nc1Match?.cultivar).isEqualTo("NC-1")
        assertThat(ksuAtwoodMatch?.cultivar).isEqualTo("KSU-Atwood")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesBerryAndMelonAliases() {
        val cranberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("vaccinium macrocarpon")
        val watermelonSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("citrullus lanatus")
        val cantaloupeSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("muskmelon")
        val honeydewSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("honeydew melon")
        val canarySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("juan canary")
        val galiaSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("galia")
        val casabaSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("casaba")
        val persianSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("persian muskmelon")
        val moonAndStarsMatch = BloomForecastEngine.resolveCultivarAutocomplete("Moon & Stars", "Watermelon")
        val halesBestMatch = BloomForecastEngine.resolveCultivarAutocomplete("Hales Best", "Cantaloupe")
        val earlidewMatch = BloomForecastEngine.resolveCultivarAutocomplete("Earlidew", "Honeydew")
        val canaryMatch = BloomForecastEngine.resolveCultivarAutocomplete("Canary", "Canary Melon")
        val santaClausMatch = BloomForecastEngine.resolveCultivarAutocomplete("Santa Claus", "Casaba Melon")
        val caspianMatch = BloomForecastEngine.resolveCultivarAutocomplete("Caspian", "Persian Melon")

        assertThat(cranberrySpeciesMatch).isEqualTo("Cranberry")
        assertThat(watermelonSpeciesMatch).isEqualTo("Watermelon")
        assertThat(cantaloupeSpeciesMatch).isEqualTo("Cantaloupe")
        assertThat(honeydewSpeciesMatch).isEqualTo("Honeydew")
        assertThat(canarySpeciesMatch).isEqualTo("Canary Melon")
        assertThat(galiaSpeciesMatch).isEqualTo("Galia Melon")
        assertThat(casabaSpeciesMatch).isEqualTo("Casaba Melon")
        assertThat(persianSpeciesMatch).isEqualTo("Persian Melon")
        assertThat(moonAndStarsMatch?.cultivar).isEqualTo("Moon and Stars")
        assertThat(halesBestMatch?.cultivar).isEqualTo("Hale's Best")
        assertThat(earlidewMatch?.cultivar).isEqualTo("Earli Dew")
        assertThat(canaryMatch?.cultivar).isEqualTo("Juan Canary")
        assertThat(santaClausMatch?.cultivar).isEqualTo("Santa Claus")
        assertThat(caspianMatch?.cultivar).isEqualTo("Caspian")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesPearSubgroupAliases() {
        val europeanPearSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("pyrus communis")
        val asianPearSpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("apple pear")
        val bartlettMatch = BloomForecastEngine.resolveCultivarAutocomplete("Max-Red Bartlett", "Pear")
        val anjouMatch = BloomForecastEngine.resolveCultivarAutocomplete("D'Anjou", "European pear")
        val twentiethCenturyMatch = BloomForecastEngine.resolveCultivarAutocomplete("Nijisseiki", "Asian pear")
        val olympicMatch = BloomForecastEngine.resolveCultivarAutocomplete("Olympic", "Pear")

        assertThat(europeanPearSpeciesMatch).isEqualTo("European Pear")
        assertThat(asianPearSpeciesMatch).isEqualTo("Asian Pear")
        assertThat(bartlettMatch?.cultivar).isEqualTo("Bartlett")
        assertThat(anjouMatch?.cultivar).isEqualTo("Anjou")
        assertThat(twentiethCenturyMatch?.cultivar).isEqualTo("Twentieth Century")
        assertThat(olympicMatch?.cultivar).isEqualTo("Korean Giant")
    }

    @Test
    fun resolveSpeciesAndCultivarAutocomplete_matchesCaneberryAliases() {
        val redRaspberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("yellow raspberry")
        val blackRaspberrySpeciesMatch = BloomForecastEngine.resolveSpeciesAutocomplete("blackcap raspberry")
        val marionMatch = BloomForecastEngine.resolveCultivarAutocomplete("Marionberry", "Blackberry")
        val boysenMatch = BloomForecastEngine.resolveCultivarAutocomplete("Boysenberry", "Blackberry")
        val primeArkMatch = BloomForecastEngine.resolveCultivarAutocomplete("Prime Ark Freedom", "Blackberry")

        assertThat(redRaspberrySpeciesMatch).isEqualTo("Red Raspberry")
        assertThat(blackRaspberrySpeciesMatch).isEqualTo("Black Raspberry")
        assertThat(marionMatch?.cultivar).isEqualTo("Marion")
        assertThat(boysenMatch?.cultivar).isEqualTo("Boysen")
        assertThat(primeArkMatch?.cultivar).isEqualTo("Prime-Ark Freedom")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesLonganAliases() {
        val edauMatch = BloomForecastEngine.resolveCultivarAutocomplete("Daw", "Longan")
        val biewKiewMatch = BloomForecastEngine.resolveCultivarAutocomplete("Beow Keow", "Dimocarpus longan")
        val diamondRiverMatch = BloomForecastEngine.resolveCultivarAutocomplete("Petch Sakorn", "Dragon eye")

        assertThat(edauMatch?.cultivar).isEqualTo("Edau")
        assertThat(biewKiewMatch?.cultivar).isEqualTo("Biew Kiew")
        assertThat(diamondRiverMatch?.cultivar).isEqualTo("Diamond River")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesLycheeCultivars() {
        val sweetheartMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sweetheart", "Lychee")

        assertThat(sweetheartMatch?.cultivar).isEqualTo("Sweetheart")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesAtemoyaAliases() {
        val gefnerMatch = BloomForecastEngine.resolveCultivarAutocomplete("Geffner", "Atemoya")
        val africanPrideMatch = BloomForecastEngine.resolveCultivarAutocomplete("Kaller", "Annona x atemoya")
        val mammothMatch = BloomForecastEngine.resolveCultivarAutocomplete("Mammoth", "Atemoya")
        val pinksMammothMatch = BloomForecastEngine.resolveCultivarAutocomplete("Pinks Mammoth", "Annona cherimola x annona squamosa")

        assertThat(gefnerMatch?.cultivar).isEqualTo("Gefner")
        assertThat(africanPrideMatch?.cultivar).isEqualTo("African Pride")
        assertThat(mammothMatch?.cultivar).isEqualTo("Pink Mammoth")
        assertThat(pinksMammothMatch?.cultivar).isEqualTo("Pink Mammoth")
    }

    @Test
    fun resolveCultivarAutocomplete_disambiguatesMammothBySpeciesQuery() {
        val atemoyaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Mammoth", "Atemoya")
        val sugarAppleMatch = BloomForecastEngine.resolveCultivarAutocomplete("Mammoth", "Sugar apple")
        val globalMatch = BloomForecastEngine.resolveCultivarAutocomplete("Mammoth")

        assertThat(atemoyaMatch?.species).isEqualTo("Atemoya")
        assertThat(atemoyaMatch?.cultivar).isEqualTo("Pink Mammoth")
        assertThat(sugarAppleMatch?.species).isEqualTo("Sugar Apple")
        assertThat(sugarAppleMatch?.cultivar).isEqualTo("Mammoth")
        assertThat(globalMatch).isNull()
    }

    @Test
    fun resolveCultivarAutocomplete_matchesCaimitoAliases() {
        val haitianMatch = BloomForecastEngine.resolveCultivarAutocomplete("Haitian Star Apple", "Caimito (star apple)")
        val blancoMatch = BloomForecastEngine.resolveCultivarAutocomplete("Blanco Star", "Star apple")

        assertThat(haitianMatch?.cultivar).isEqualTo("Haitian Star")
        assertThat(blancoMatch?.cultivar).isEqualTo("Blanco Star")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesCoconutAliases() {
        val jamaicanTallMatch = BloomForecastEngine.resolveCultivarAutocomplete("Atlantic Tall", "Cocos nucifera")
        val goldenMatch = BloomForecastEngine.resolveCultivarAutocomplete("Malayan Golden Dwarf", "Coconut palm")

        assertThat(jamaicanTallMatch?.cultivar).isEqualTo("Jamaican Tall")
        assertThat(goldenMatch?.cultivar).isEqualTo("Golden Malayan Dwarf")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesSoursopAliases() {
        val sweetMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sweet guanabana", "Soursop")
        val cubanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Cuban Fibreless", "Annona muricata")
        val whitmanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Guanabana")

        assertThat(sweetMatch?.cultivar).isEqualTo("Sweet")
        assertThat(cubanMatch?.cultivar).isEqualTo("Cuban Fiberless")
        assertThat(whitmanMatch?.cultivar).isEqualTo("Whitman Fiberless")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesMamoncilloAliases() {
        val josePabonMatch = BloomForecastEngine.resolveCultivarAutocomplete("JosÃ© PabÃ³n", "Mamoncillo")
        val largeMatch = BloomForecastEngine.resolveCultivarAutocomplete("Large", "Spanish lime")

        assertThat(josePabonMatch?.cultivar).isEqualTo("Jose Pabon")
        assertThat(largeMatch?.cultivar).isEqualTo("Large")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesAbiuAliases() {
        val grayMatch = BloomForecastEngine.resolveCultivarAutocomplete("Gray", "Abiu")
        val z2Match = BloomForecastEngine.resolveCultivarAutocomplete("Z2", "Pouteria caimito")

        assertThat(grayMatch?.cultivar).isEqualTo("Gray")
        assertThat(z2Match?.cultivar).isEqualTo("Z-2")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesCashewTypeLanes() {
        val giganteMatch = BloomForecastEngine.resolveCultivarAutocomplete("Gigante", "Cashew (cashew apple)")
        val anaoMatch = BloomForecastEngine.resolveCultivarAutocomplete("Anao/Precoce", "Anacardium occidentale")

        assertThat(giganteMatch?.cultivar).isEqualTo("Gigante / Tardio")
        assertThat(anaoMatch?.cultivar).isEqualTo("Anão / Precoce")
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
        val mauritiusMatch = BloomForecastEngine.resolveCultivarAutocomplete("Moris", "PiÃ±a")
        val sugarloafMatch = BloomForecastEngine.resolveCultivarAutocomplete("Kona Sugarloaf", "Ananas")
        val md2Match = BloomForecastEngine.resolveCultivarAutocomplete("Del Monte Gold", "Ananas comosus")
        val whiteJadeMatch = BloomForecastEngine.resolveCultivarAutocomplete("White Jade", "Pineapple")

        assertThat(victoriaMatch?.cultivar).isEqualTo("Victoria")
        assertThat(mauritiusMatch?.cultivar).isEqualTo("Mauritius")
        assertThat(sugarloafMatch?.cultivar).isEqualTo("Sugarloaf")
        assertThat(md2Match?.cultivar).isEqualTo("MD-2")
        assertThat(whiteJadeMatch?.cultivar).isEqualTo("White Jade")
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
    fun resolveCultivarAutocomplete_disambiguatesWhitmanBySpeciesQuery() {
        val passionFruitMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Lilikoi")
        val greenSapoteMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Green sapote")
        val soursopMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Soursop")
        val globalMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman")

        assertThat(passionFruitMatch?.species).isEqualTo("Passion Fruit")
        assertThat(passionFruitMatch?.cultivar).isEqualTo("Whitman Yellow")
        assertThat(greenSapoteMatch?.species).isEqualTo("Green Sapote")
        assertThat(greenSapoteMatch?.cultivar).isEqualTo("Whitman")
        assertThat(soursopMatch?.species).isEqualTo("Soursop")
        assertThat(soursopMatch?.cultivar).isEqualTo("Whitman Fiberless")
        assertThat(globalMatch).isNull()
    }

    @Test
    fun resolveCultivarAutocomplete_matchesPapayaAliases() {
        val sunriseMatch = BloomForecastEngine.resolveCultivarAutocomplete("Sunrise", "Papaya")
        val sunUpMatch = BloomForecastEngine.resolveCultivarAutocomplete("UH SunUp", "Carica papaya")
        val arkaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Arka Prabhat", "Lechosa")
        val calimanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Caliman 01", "MamÃ£o")

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
    fun resolveCultivarAutocomplete_matchesSugarCaneAliases() {
        val yellowGalMatch = BloomForecastEngine.resolveCultivarAutocomplete("F31-407", "Sugar cane")
        val commercialMatch = BloomForecastEngine.resolveCultivarAutocomplete("Commercial cane", "Saccharum spp.")
        val cloneMatch = BloomForecastEngine.resolveCultivarAutocomplete("CP01-1372", "Sugarcane (cultivated hybrid complex)")
        val transparentMatch = BloomForecastEngine.resolveCultivarAutocomplete("White Transparent", "Saccharum officinarum")

        assertThat(yellowGalMatch?.cultivar).isEqualTo("Yellow Gal")
        assertThat(commercialMatch?.cultivar).isEqualTo("Crystal / commercial cane")
        assertThat(cloneMatch?.cultivar).isEqualTo("CP 01-1372")
        assertThat(transparentMatch?.cultivar).isEqualTo("White Transparent")
    }


    @Test
    fun resolveCultivarAutocomplete_matchesCanistelAliases() {
        val fairchildOneMatch = BloomForecastEngine.resolveCultivarAutocomplete("Fairchild 1", "Eggfruit")
        val fairchildTwoMatch = BloomForecastEngine.resolveCultivarAutocomplete("Fairchild 2", "Pouteria campechiana")
        val trecMatch = BloomForecastEngine.resolveCultivarAutocomplete("TREC9680", "Lucuma nervosa")
        val bruceMatch = BloomForecastEngine.resolveCultivarAutocomplete("Bruce", "Zapote amarillo")

        assertThat(fairchildOneMatch?.cultivar).isEqualTo("Fairchild #1")
        assertThat(fairchildTwoMatch?.cultivar).isEqualTo("Fairchild #2")
        assertThat(trecMatch?.cultivar).isEqualTo("TREC 9680")
        assertThat(bruceMatch?.cultivar).isEqualTo("Bruce")
    }


    @Test
    fun resolveCultivarAutocomplete_matchesMameySapoteAliases() {
        val pantinMatch = BloomForecastEngine.resolveCultivarAutocomplete("Key West", "Mamey sapote")
        val maganaMatch = BloomForecastEngine.resolveCultivarAutocomplete("MagaÃ±a", "Pouteria sapota")
        val copanMatch = BloomForecastEngine.resolveCultivarAutocomplete("AREC No. 1", "Zapote mamey")
        val fernandezMatch = BloomForecastEngine.resolveCultivarAutocomplete("Francisco Fernancez", "Calocarpum sapota")
        val cepedaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Cepeda Special", "Mamey colorado")
        val akilMatch = BloomForecastEngine.resolveCultivarAutocomplete("Akil Special", "Lucuma mammosa")

        assertThat(pantinMatch?.cultivar).isEqualTo("Pantin")
        assertThat(maganaMatch?.cultivar).isEqualTo("Magana")
        assertThat(copanMatch?.cultivar).isEqualTo("Copan")
        assertThat(fernandezMatch?.cultivar).isEqualTo("Francisco Fernandez")
        assertThat(cepedaMatch?.cultivar).isEqualTo("Cepeda Especial")
        assertThat(akilMatch?.cultivar).isEqualTo("Akil Especial")
    }


    @Test
    fun resolveCultivarAutocomplete_matchesBlackSapoteAliases() {
        val meridaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Reineke", "Black sapote")
        val reineckeMatch = BloomForecastEngine.resolveCultivarAutocomplete("Reinecke", "Diospyros nigra")
        val bernickerMatch = BloomForecastEngine.resolveCultivarAutocomplete("Bernecker", "Zapote negro")
        val lateMatch = BloomForecastEngine.resolveCultivarAutocomplete("Rick's Late", "Diospyros digyna")

        assertThat(meridaMatch?.cultivar).isEqualTo("Merida")
        assertThat(reineckeMatch?.cultivar).isEqualTo("Merida")
        assertThat(bernickerMatch?.cultivar).isEqualTo("Bernicker")
        assertThat(lateMatch?.cultivar).isEqualTo("Ricks Late")
    }


    @Test
    fun resolveCultivarAutocomplete_matchesGreenSapoteAliases() {
        val selectionMatch = BloomForecastEngine.resolveCultivarAutocomplete("UF/TREC selection", "Green sapote")
        val whitmanMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Pouteria viridis")
        val calocarpumMatch = BloomForecastEngine.resolveCultivarAutocomplete("Whitman", "Calocarpum viride")
        val injertoMatch = BloomForecastEngine.resolveCultivarAutocomplete("UF/TREC selection", "Zapote injerto")

        assertThat(selectionMatch?.cultivar).isEqualTo("UF/TREC selection")
        assertThat(whitmanMatch?.cultivar).isEqualTo("Whitman")
        assertThat(calocarpumMatch?.cultivar).isEqualTo("Whitman")
        assertThat(injertoMatch?.cultivar).isEqualTo("UF/TREC selection")
    }

    @Test
    fun pollinationRequirementFor_resolvesLonganSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Longan"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lungan", "Daw"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mamoncillo chino", "Petch Sakorn"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_resolvesAtemoyaSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Atemoya"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Annona atemoya", "Geffner"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_resolvesCaimitoSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Caimito (star apple)"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Star apple"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cainito"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Chrysophyllum cainito", "Haitian Star"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Star fruit"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun pollinationRequirementFor_resolvesAbiuSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Abiu")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pouteria caimito")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Achras caimito")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Abio", "Gray")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lucuma caimito", "Z2")).isNull()
    }

    @Test
    fun pollinationRequirementFor_resolvesAmbarellaSpeciesDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Ambarella (June plum)")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("June plum")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Spondias dulcis")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Spondias cytherea")).isNull()
    }

    @Test
    fun pollinationRequirementFor_resolvesCashewSpeciesAndTypeDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cashew (cashew apple)"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cashew apple"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Anacardium occidentale"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cashew", "Gigante"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cashew apple", "Anao / Precoce"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun pollinationRequirementFor_resolvesCoconutSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut palm")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cocos nucifera")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut", "Jamaican Tall"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut tree", "Panama Tall"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut", "Yellow Malayan Dwarf"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut", "Golden Malayan Dwarf"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut", "Red Malayan Dwarf"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut", "Malayan Dwarf")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Coconut", "Maypan")).isNull()
    }

    @Test
    fun pollinationRequirementFor_resolvesSoursopSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Soursop"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Guanabana"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Annona muricata", "Whitman"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Graviola", "Sweet guanabana"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun pollinationRequirementFor_resolvesMamoncilloSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mamoncillo"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Genip"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Melicoccus bijugatus", "JosÃ© PabÃ³n"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Spanish lime", "Montgomery"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mamoncillo chino", "Petch Sakorn"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_usesSpeciesDefaultForMangoCultivarCatalogRows() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mango"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mangifera indica", "Honey"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mango", "Southern Blush"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
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
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Capistrano Valley"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Townsend Pink"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Fruit Punch"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Aussie Gold"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Common Red"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Yellow Dragon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Kathie Van Arum"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Dennis Pale Pink"))
            .isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "NOID"))
            .isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "DK16"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Golden Delicious"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Honeycrisp"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee", "Tai So"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee", "Fay Zee Siu"))
            .isEqualTo(PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lychee", "Sweetheart"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
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
        assertThat(BloomForecastEngine.pollinationRequirementFor("PiÃ±a", "Moris"))
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
        assertThat(BloomForecastEngine.pollinationRequirementFor("MamÃ£o", "Golden")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("White sapote"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Casimiroa edulis", "Vernon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Zapote blanco", "Reinikie"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Casimiroa", "Hubbell"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Sugar cane")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Saccharum spp.", "Yellow Gal")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Sugarcane (cultivated hybrid complex)", "CP01-1372")).isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Canistel"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Eggfruit", "Fairchild 1"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pouteria campechiana", "Bruce"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lucuma nervosa", "TREC9681"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mamey sapote"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pouteria sapota", "Key West"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Zapote colorado", "MagaÃ±a"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Calocarpum sapota", "AREC No. 2"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Lucuma mammosa", "Cepeda Special"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Black sapote"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Diospyros nigra", "Reineke"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Zapote negro", "Bernecker"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Diospyros digyna", "Superb"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Green sapote"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pouteria viridis", "UF/TREC selection"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Calocarpum viride", "Whitman"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Injerto", "Whitman"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun pollinationRequirementFor_resolvesBackyardExpansionSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Kiwi"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Kiwi", "Jenny"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Muscadine"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Muscadine", "Carlos"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Muscadine", "Supreme"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Feijoa"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Feijoa", "Coolidge"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Olive", "Manzanillo"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pecan"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Honeyberry", "Aurora"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Serviceberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cherimoya"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Japanese persimmon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Japanese persimmon", "Coffee Cake"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("American persimmon"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun pollinationRequirementFor_resolvesAvocadoSpeciesAndCultivarDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Avocado"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Persea americana", "Hass"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Alligator pear", "Lula"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Avocado", "Fuerte"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Avocado", "Pollock"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Avocado", "Booth No. 8"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun pollinationRequirementFor_resolvesAppleCultivarOverrides() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Malus domestica", "Golden Delicious"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Jonagold"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Crispin"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun pollinationRequirementFor_resolvesPomegranateDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pomegranate"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Punica granatum", "Wonderful"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pomegranate", "Russian"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun pollinationRequirementFor_resolvesWarmClimateStoneFruitAndPawpawDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Loquat"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Loquat", "Advance"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Loquat", "Champagne"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)

        assertThat(BloomForecastEngine.pollinationRequirementFor("Guava"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Sapodilla"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apricot"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apricot", "Moongold"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Nectarine"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pawpaw"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("American pawpaw", "Sunflower"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_resolvesBerryAndMelonDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cranberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Watermelon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Cantaloupe"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Honeydew"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Canary Melon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Galia Melon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Casaba Melon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Persian Melon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_resolvesBlueberrySubgroupDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Blueberry"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Rabbiteye blueberry"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Southern highbush blueberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Northern highbush blueberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Blueberry", "Climax"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Blueberry", "Sweetcrisp"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_resolvesPlumSubgroupDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Plum"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Japanese plum"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("European plum"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Hardy hybrid plum"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Plum", "Methley"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Plum", "Stanley"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Plum", "Superior"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun pollinationRequirementFor_resolvesMulberrySubgroupDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mulberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Black mulberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("White mulberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mulberry", "Illinois Everbearing"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Mulberry", "Persian"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("White mulberry", "Pakistani"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
    }

    @Test
    fun pollinationRequirementFor_resolvesFigSubgroupDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Fig"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Common fig"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Smyrna fig"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("San Pedro fig"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Fig", "Black Mission"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Fig", "Calimyrna"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("San Pedro fig", "King"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun pollinationRequirementFor_resolvesPearSubgroupDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pear"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("European pear"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Asian pear"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Pear", "Bartlett"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("European pear", "Anjou"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Asian pear", "Hosui"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Asian pear", "Chojuro"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
    }

    @Test
    fun pollinationRequirementFor_resolvesCaneberryDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Raspberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Red raspberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Black raspberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Blackberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Raspberry", "Heritage"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Raspberry", "Jewel"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Blackberry", "Marionberry"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
    }

    @Test
    fun predictMonth_usesSpeciesBaselineWindowForMangoCultivars() {
        val mangoTree = TreeEntity(
            id = "mango-1",
            orchardName = "Home",
            sectionName = "Anacardiaceae",
            nickname = null,
            species = "Mangifera indica",
            cultivar = "Honey",
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

        val januaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(mangoTree),
            yearMonth = YearMonth.of(2026, 1),
            zoneCode = "10b"
        )
        val mayWindow = BloomForecastEngine.predictMonth(
            trees = listOf(mangoTree),
            yearMonth = YearMonth.of(2026, 5),
            zoneCode = "10b"
        )

        assertThat(januaryWindow).hasSize(1)
        assertThat(januaryWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2025, 12, 1))
        assertThat(januaryWindow.single().endDate).isEqualTo(java.time.LocalDate.of(2026, 4, 30))
        assertThat(januaryWindow.single().sourceLabel).isEqualTo("species baseline")
        assertThat(mayWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesLonganPrimaryBloomWindow() {
        val longanTree = TreeEntity(
            id = "longan-1",
            orchardName = "Home",
            sectionName = "Sapindaceae",
            nickname = null,
            species = "Dragon eye",
            cultivar = "Kohala",
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
            trees = listOf(longanTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val juneWindow = BloomForecastEngine.predictMonth(
            trees = listOf(longanTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 2, 25))
        assertThat(aprilWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(juneWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesAtemoyaPrimaryBloomWindow() {
        val atemoyaTree = TreeEntity(
            id = "atemoya-1",
            orchardName = "Home",
            sectionName = "Annona row",
            nickname = null,
            species = "Annona x atemoya",
            cultivar = "Gefner",
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
            trees = listOf(atemoyaTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val augustWindow = BloomForecastEngine.predictMonth(
            trees = listOf(atemoyaTree),
            yearMonth = YearMonth.of(2026, 8),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 1))
        assertThat(aprilWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(augustWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesCaimitoPrimaryBloomWindow() {
        val caimitoTree = TreeEntity(
            id = "caimito-1",
            orchardName = "Home",
            sectionName = "Sapotes",
            nickname = null,
            species = "Chrysophyllum cainito",
            cultivar = "Haitian Star",
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

        val augustWindow = BloomForecastEngine.predictMonth(
            trees = listOf(caimitoTree),
            yearMonth = YearMonth.of(2026, 8),
            zoneCode = "10b"
        )
        val januaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(caimitoTree),
            yearMonth = YearMonth.of(2026, 1),
            zoneCode = "10b"
        )

        assertThat(augustWindow).hasSize(1)
        assertThat(augustWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 8, 1))
        assertThat(augustWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(januaryWindow).isEmpty()
    }

    @Test
    fun predictMonth_skipsAutomaticCoconutForecasts() {
        val coconutTree = TreeEntity(
            id = "coconut-1",
            orchardName = "Home",
            sectionName = "Palms",
            nickname = null,
            species = "Coconut palm",
            cultivar = "Maypan",
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
            trees = listOf(coconutTree),
            yearMonth = YearMonth.of(2026, 8),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun predictMonth_usesSoursopPrimaryBloomWindow() {
        val soursopTree = TreeEntity(
            id = "soursop-1",
            orchardName = "Home",
            sectionName = "Annonas",
            nickname = null,
            species = "Annona muricata",
            cultivar = "Whitman Fiberless",
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
            trees = listOf(soursopTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val augustWindow = BloomForecastEngine.predictMonth(
            trees = listOf(soursopTree),
            yearMonth = YearMonth.of(2026, 8),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 1))
        assertThat(aprilWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(augustWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesMamoncilloPrimaryBloomWindow() {
        val mamoncilloTree = TreeEntity(
            id = "mamoncillo-1",
            orchardName = "Home",
            sectionName = "Sapindaceae",
            nickname = null,
            species = "Melicoccus bijugatus",
            cultivar = "Montgomery",
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
            trees = listOf(mamoncilloTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val julyWindow = BloomForecastEngine.predictMonth(
            trees = listOf(mamoncilloTree),
            yearMonth = YearMonth.of(2026, 7),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 1))
        assertThat(aprilWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(julyWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesAbiuPrimaryBloomWindow() {
        val abiuTree = TreeEntity(
            id = "abiu-1",
            orchardName = "Home",
            sectionName = "Sapotaceae",
            nickname = null,
            species = "Pouteria caimito",
            cultivar = "Gray",
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

        val mayWindow = BloomForecastEngine.predictMonth(
            trees = listOf(abiuTree),
            yearMonth = YearMonth.of(2026, 5),
            zoneCode = "10b"
        )
        val septemberWindow = BloomForecastEngine.predictMonth(
            trees = listOf(abiuTree),
            yearMonth = YearMonth.of(2026, 9),
            zoneCode = "10b"
        )

        assertThat(mayWindow).hasSize(1)
        assertThat(mayWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 5, 1))
        assertThat(mayWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(septemberWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesAmbarellaPrimaryBloomWindow() {
        val ambarellaTree = TreeEntity(
            id = "ambarella-1",
            orchardName = "Home",
            sectionName = "Spondias",
            nickname = null,
            species = "Spondias dulcis",
            cultivar = "",
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
            trees = listOf(ambarellaTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val augustWindow = BloomForecastEngine.predictMonth(
            trees = listOf(ambarellaTree),
            yearMonth = YearMonth.of(2026, 8),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 1))
        assertThat(aprilWindow.single().sourceLabel).isEqualTo("species baseline")
        assertThat(augustWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesCashewPrimaryBloomWindow() {
        val cashewTree = TreeEntity(
            id = "cashew-1",
            orchardName = "Home",
            sectionName = "Anacardiaceae",
            nickname = null,
            species = "Anacardium occidentale",
            cultivar = "Gigante / Tardio",
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
            trees = listOf(cashewTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val julyWindow = BloomForecastEngine.predictMonth(
            trees = listOf(cashewTree),
            yearMonth = YearMonth.of(2026, 7),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 1))
        assertThat(aprilWindow.single().sourceLabel).isEqualTo("cultivar-adjusted")
        assertThat(julyWindow).isEmpty()
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
        assertThat(septemberWindow).hasSize(1)
        assertThat(septemberWindow.single().patternType).isEqualTo(BloomPatternType.MULTI_WAVE)
        assertThat(septemberWindow.single().endDate).isEqualTo(java.time.LocalDate.of(2026, 10, 31))
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
            species = "PiÃ±a",
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
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 4, 1))
        assertThat(aprilWindow.single().patternType).isEqualTo(BloomPatternType.MULTI_WAVE)
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
    fun predictMonth_skipsAutomaticMelonForecasts() {
        val watermelonTree = TreeEntity(
            id = "watermelon-1",
            orchardName = "Home",
            sectionName = "Annual bed",
            nickname = null,
            species = "Watermelon",
            cultivar = "Sugar Baby",
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
        val cantaloupeTree = watermelonTree.copy(
            id = "cantaloupe-1",
            species = "Cantaloupe",
            cultivar = "Ambrosia"
        )
        val honeydewTree = watermelonTree.copy(
            id = "honeydew-1",
            species = "Honeydew",
            cultivar = "Honey Brew"
        )
        val canaryTree = watermelonTree.copy(
            id = "canary-1",
            species = "Canary Melon",
            cultivar = "Juan Canary"
        )
        val galiaTree = watermelonTree.copy(
            id = "galia-1",
            species = "Galia Melon",
            cultivar = "Galia"
        )
        val casabaTree = watermelonTree.copy(
            id = "casaba-1",
            species = "Casaba Melon",
            cultivar = "Golden Beauty"
        )
        val persianTree = watermelonTree.copy(
            id = "persian-1",
            species = "Persian Melon",
            cultivar = "Caspian"
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(watermelonTree, cantaloupeTree, honeydewTree, canaryTree, galiaTree, casabaTree, persianTree),
            yearMonth = YearMonth.of(2026, 7),
            zoneCode = "8a"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun predictMonth_skipsAutomaticSugarCaneForecasts() {
        val sugarCaneTree = TreeEntity(
            id = "sugar-cane-1",
            orchardName = "Home",
            sectionName = "Canes",
            nickname = null,
            species = "Sugarcane (cultivated hybrid complex)",
            cultivar = "Yellow Gal",
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
            trees = listOf(sugarCaneTree),
            yearMonth = YearMonth.of(2026, 11),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun predictMonth_usesMameySapotePrimaryBloomWindow() {
        val mameySapoteTree = TreeEntity(
            id = "mamey-sapote-1",
            orchardName = "Home",
            sectionName = "Sapotes",
            nickname = null,
            species = "Pouteria sapota",
            cultivar = "Pantin",
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

        val januaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(mameySapoteTree),
            yearMonth = YearMonth.of(2027, 1),
            zoneCode = "10b"
        )
        val mayWindow = BloomForecastEngine.predictMonth(
            trees = listOf(mameySapoteTree),
            yearMonth = YearMonth.of(2027, 5),
            zoneCode = "10b"
        )

        assertThat(januaryWindow).hasSize(1)
        assertThat(januaryWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 6, 1))
        assertThat(mayWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesCanistelPrimaryBloomWindow() {
        val canistelTree = TreeEntity(
            id = "canistel-1",
            orchardName = "Home",
            sectionName = "Sapotes",
            nickname = null,
            species = "Eggfruit",
            cultivar = "Bruce",
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
            trees = listOf(canistelTree),
            yearMonth = YearMonth.of(2026, 2),
            zoneCode = "10b"
        )
        val julyWindow = BloomForecastEngine.predictMonth(
            trees = listOf(canistelTree),
            yearMonth = YearMonth.of(2026, 7),
            zoneCode = "10b"
        )

        assertThat(februaryWindow).hasSize(1)
        assertThat(februaryWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 1, 15))
        assertThat(julyWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesBlackSapotePrimaryBloomWindow() {
        val blackSapoteTree = TreeEntity(
            id = "black-sapote-1",
            orchardName = "Home",
            sectionName = "Sapotes",
            nickname = null,
            species = "Diospyros nigra",
            cultivar = "Merida",
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
            trees = listOf(blackSapoteTree),
            yearMonth = YearMonth.of(2026, 4),
            zoneCode = "10b"
        )
        val februaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(blackSapoteTree),
            yearMonth = YearMonth.of(2026, 2),
            zoneCode = "10b"
        )

        assertThat(aprilWindow).hasSize(1)
        assertThat(aprilWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 3, 15))
        assertThat(februaryWindow).isEmpty()
    }

    @Test
    fun predictMonth_usesGreenSapotePrimaryBloomWindow() {
        val greenSapoteTree = TreeEntity(
            id = "green-sapote-1",
            orchardName = "Home",
            sectionName = "Sapotes",
            nickname = null,
            species = "Calocarpum viride",
            cultivar = "UF/TREC selection",
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
            trees = listOf(greenSapoteTree),
            yearMonth = YearMonth.of(2026, 3),
            zoneCode = "10b"
        )
        val januaryWindow = BloomForecastEngine.predictMonth(
            trees = listOf(greenSapoteTree),
            yearMonth = YearMonth.of(2026, 1),
            zoneCode = "10b"
        )

        assertThat(marchWindow).hasSize(1)
        assertThat(marchWindow.single().startDate).isEqualTo(java.time.LocalDate.of(2026, 2, 15))
        assertThat(januaryWindow).isEmpty()
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
        assertThat(everbearing.single().speciesLabel).isEqualTo("Banana | Goldfinger")
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
        assertThat(everbearing.single().speciesLabel).isEqualTo("Acerola | Florida Sweet")
        assertThat(everbearing.single().detailLabel).isEqualTo("Repeat bloomer")
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
        assertThat(everbearing.single().speciesLabel).isEqualTo("Panama berry | Yellow-fruited form")
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

        assertThat(everbearing).hasSize(2)
        assertThat(everbearing.map { it.treeId }).containsExactly("papaya-1", "passion-fruit-1")
        assertThat(everbearing.first { it.treeId == "papaya-1" }.treeLabel).isEqualTo("Main (Kapoho Solo)")
        assertThat(everbearing.first { it.treeId == "papaya-1" }.speciesLabel).isEqualTo("Papaya | Kapoho Solo")
        assertThat(everbearing.first { it.treeId == "papaya-1" }.detailLabel).isEqualTo("Continuous / repeat-bearing")
        assertThat(everbearing.first { it.treeId == "passion-fruit-1" }.detailLabel).isEqualTo("Repeat bloomer")
    }

    @Test
    fun everbearingPlants_usesLocationAwareSeasonDetailWhenClimateProfileExists() {
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

        val everbearing = BloomForecastEngine.everbearingPlants(
            trees = listOf(papayaTree),
            locationProfilesByTreeId = mapOf(
                papayaTree.id to ForecastLocationProfile(
                    hemisphere = Hemisphere.NORTHERN,
                    usdaZoneCode = "10a",
                    climateFingerprint = LocationClimateFingerprint(
                        source = "NASA POWER",
                        fetchedAt = 1L,
                        meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                        meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                        meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
                    )
                )
            )
        )

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().detailLabel).isEqualTo("Active season Apr 1 - Nov 30")
    }

    @Test
    fun everbearingPlants_includesLemonAsRepeatBearingCitrus() {
        val lemonTree = TreeEntity(
            id = "lemon-1",
            orchardName = "Home",
            sectionName = "Citrus",
            nickname = "Patio",
            species = "Lemon",
            cultivar = "Meyer",
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

        val everbearing = BloomForecastEngine.everbearingPlants(
            trees = listOf(lemonTree),
            locationProfilesByTreeId = mapOf(
                lemonTree.id to ForecastLocationProfile(
                    hemisphere = Hemisphere.NORTHERN,
                    usdaZoneCode = "10a",
                    climateFingerprint = LocationClimateFingerprint(
                        source = "NASA POWER",
                        fetchedAt = 1L,
                        meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                        meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                        meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
                    )
                )
            )
        )

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("lemon-1")
        assertThat(everbearing.single().detailLabel).isEqualTo("Active season Apr 1 - Nov 30")
    }

    @Test
    fun everbearingPlants_includesGuavaAndSapodillaAsRepeatBearingWarmClimateTrees() {
        val guavaTree = TreeEntity(
            id = "guava-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = "Pink",
            species = "Guava",
            cultivar = "Ruby Supreme",
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
        val sapodillaTree = guavaTree.copy(
            id = "sapodilla-1",
            species = "Sapodilla",
            cultivar = "Alano",
            nickname = "Brown"
        )
        val warmProfile = ForecastLocationProfile(
            hemisphere = Hemisphere.NORTHERN,
            usdaZoneCode = "10a",
            climateFingerprint = LocationClimateFingerprint(
                source = "NASA POWER",
                fetchedAt = 1L,
                meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
            )
        )

        val everbearing = BloomForecastEngine.everbearingPlants(
            trees = listOf(guavaTree, sapodillaTree),
            locationProfilesByTreeId = mapOf(
                guavaTree.id to warmProfile,
                sapodillaTree.id to warmProfile
            )
        )

        assertThat(everbearing.map { it.treeId }).containsAtLeast("guava-1", "sapodilla-1")
        assertThat(everbearing.first { it.treeId == "guava-1" }.detailLabel).isEqualTo("Active season Apr 1 - Nov 30")
        assertThat(everbearing.first { it.treeId == "sapodilla-1" }.detailLabel).isEqualTo("Active season Apr 1 - Nov 30")
    }

    @Test
    fun everbearingPlants_returnsTrackedCoconutForSeparateDashboardListing() {
        val coconutTree = TreeEntity(
            id = "coconut-1",
            orchardName = "Home",
            sectionName = "Palms",
            nickname = "South side",
            species = "Cocos nucifera",
            cultivar = "Maypan",
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
        val mangoTree = coconutTree.copy(
            id = "mango-1",
            species = "Mango",
            cultivar = "Kent",
            nickname = null
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(coconutTree, mangoTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("coconut-1")
        assertThat(everbearing.single().treeLabel).isEqualTo("South side (Maypan)")
        assertThat(everbearing.single().speciesLabel).isEqualTo("Cocos nucifera | Maypan")
        assertThat(everbearing.single().detailLabel).isEqualTo("Continuous / repeat-bearing")
    }

    @Test
    fun everbearingPlants_skipsSugarCaneEvenWhenForecastsAreSuppressed() {
        val sugarCaneTree = TreeEntity(
            id = "sugar-cane-1",
            orchardName = "Home",
            sectionName = "Canes",
            nickname = "North row",
            species = "Sugar cane",
            cultivar = "Yellow Gal",
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
        val papayaTree = sugarCaneTree.copy(
            id = "papaya-1",
            species = "Papaya",
            cultivar = "Kapoho Solo",
            nickname = "Main"
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(sugarCaneTree, papayaTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("papaya-1")
    }

    @Test
    fun predictMonth_usesClimateBandShiftWhenLatitudeIsProvidedWithoutUsdaZone() {
        val appleTree = TreeEntity(
            id = "apple-climate-band",
            orchardName = "Home",
            sectionName = "Test",
            nickname = null,
            species = "Apple",
            cultivar = "",
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
            trees = listOf(appleTree),
            yearMonth = YearMonth.of(2026, 3),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                latitudeDeg = 18.0
            )
        )

        assertThat(windows).hasSize(1)
        assertThat(windows.single().startDate).isEqualTo(LocalDate.of(2026, 3, 8))
        assertThat(windows.single().sourceLabel).isEqualTo("climate band")
    }

    @Test
    fun predictMonth_addsElevationDelayWhenUsingClimateBandFallback() {
        val appleTree = TreeEntity(
            id = "apple-elevation",
            orchardName = "Home",
            sectionName = "Test",
            nickname = null,
            species = "Apple",
            cultivar = "",
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
            trees = listOf(appleTree),
            yearMonth = YearMonth.of(2026, 3),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                latitudeDeg = 18.0,
                elevationM = 1200.0
            )
        )

        assertThat(windows).hasSize(1)
        assertThat(windows.single().startDate).isEqualTo(LocalDate.of(2026, 3, 22))
        assertThat(windows.single().sourceLabel).isEqualTo("climate band")
    }

    @Test
    fun predictMonth_prefersHistoryLearnedWindowsWhenBloomObservationsExist() {
        val appleTree = TreeEntity(
            id = "apple-history-learned",
            orchardName = "Home",
            sectionName = "Test",
            nickname = null,
            species = "Apple",
            cultivar = "",
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
        val observations = listOf(
            PhenologyObservation(
                treeId = appleTree.id,
                dateMillis = LocalDate.of(2024, 3, 20).atStartOfDay(OrchardTime.zoneId()).toInstant().toEpochMilli(),
                eventType = EventType.BLOOM
            ),
            PhenologyObservation(
                treeId = appleTree.id,
                dateMillis = LocalDate.of(2025, 3, 24).atStartOfDay(OrchardTime.zoneId()).toInstant().toEpochMilli(),
                eventType = EventType.BLOOM
            )
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(appleTree),
            yearMonth = YearMonth.of(2026, 3),
            locationProfile = ForecastLocationProfile(hemisphere = Hemisphere.NORTHERN),
            observations = observations
        )

        assertThat(windows).hasSize(1)
        assertThat(windows.single().source).isEqualTo(ForecastSource.HISTORY_LEARNED)
        assertThat(windows.single().confidence).isEqualTo(ForecastConfidence.MEDIUM)
        assertThat(windows.single().startDate.monthValue).isEqualTo(3)
    }

    @Test
    fun predictMonth_prefersClimateFingerprintOverUsdaZoneWhenBothExist() {
        val appleTree = TreeEntity(
            id = "apple-climate-fingerprint",
            orchardName = "Home",
            sectionName = "Test",
            nickname = null,
            species = "Apple",
            cultivar = "",
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
            trees = listOf(appleTree),
            yearMonth = YearMonth.of(2026, 4),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "9a",
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(0.0, 1.0, 4.0, 8.0, 13.0, 17.0, 20.0, 19.0, 15.0, 10.0, 5.0, 1.0),
                    meanMonthlyMinTempC = listOf(-4.0, -3.0, 0.0, 3.0, 7.0, 11.0, 14.0, 13.0, 9.0, 4.0, 0.0, -3.0),
                    meanMonthlyMaxTempC = listOf(4.0, 6.0, 9.0, 14.0, 19.0, 23.0, 26.0, 25.0, 21.0, 15.0, 9.0, 5.0)
                )
            )
        )

        assertThat(windows).hasSize(1)
        assertThat(windows.single().source).isEqualTo(ForecastSource.CLIMATE_BAND)
        assertThat(windows.single().startDate).isEqualTo(LocalDate.of(2026, 4, 18))
    }

    @Test
    fun predictMonth_usesWarmSeasonClimateFingerprintForDragonFruit() {
        val dragonFruitTree = TreeEntity(
            id = "dragon-fruit-climate-fingerprint",
            orchardName = "Home",
            sectionName = "Test",
            nickname = null,
            species = "Dragon fruit",
            cultivar = "",
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
            trees = listOf(dragonFruitTree),
            yearMonth = YearMonth.of(2026, 6),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "10a",
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                    meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                    meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
                )
            )
        )

        assertThat(windows).hasSize(1)
        assertThat(windows.single().source).isEqualTo(ForecastSource.CLIMATE_BAND)
        assertThat(windows.single().confidence).isEqualTo(ForecastConfidence.MEDIUM)
        assertThat(windows.single().startDate).isEqualTo(LocalDate.of(2026, 5, 1))
        assertThat(windows.single().endDate).isEqualTo(LocalDate.of(2026, 10, 31))
    }

    @Test
    fun autoBloomTimingLabelFor_usesOrchardClimateInsteadOfCatalogDefault() {
        val label = BloomForecastEngine.autoBloomTimingLabelFor(
            speciesInput = "Dragon fruit",
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "10a",
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                    meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                    meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
                )
            )
        )

        assertThat(label).isEqualTo("This orchard - May 1 - Oct 31")
        assertThat(label).doesNotContain("Catalog default")
        assertThat(label).doesNotContain("USDA")
    }

    @Test
    fun autoBloomTimingLabelFor_usesRepeatBearingSeasonForLemonAndLime() {
        val locationProfile = ForecastLocationProfile(
            hemisphere = Hemisphere.NORTHERN,
            usdaZoneCode = "10a",
            climateFingerprint = LocationClimateFingerprint(
                source = "NASA POWER",
                fetchedAt = 1L,
                meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
            )
        )

        val lemonLabel = BloomForecastEngine.autoBloomTimingLabelFor(
            speciesInput = "Lemon",
            locationProfile = locationProfile
        )
        val limeLabel = BloomForecastEngine.autoBloomTimingLabelFor(
            speciesInput = "Lime",
            locationProfile = locationProfile
        )

        assertThat(lemonLabel).isEqualTo("This orchard - Active season Apr 1 - Nov 30")
        assertThat(limeLabel).isEqualTo("This orchard - Active season Apr 1 - Nov 30")
        assertThat(lemonLabel).doesNotContain("Catalog default")
        assertThat(limeLabel).doesNotContain("Catalog default")
    }

    @Test
    fun autoBloomTimingLabelFor_usesRepeatBearingSeasonForGuavaAndSapodilla() {
        val locationProfile = ForecastLocationProfile(
            hemisphere = Hemisphere.NORTHERN,
            usdaZoneCode = "10a",
            climateFingerprint = LocationClimateFingerprint(
                source = "NASA POWER",
                fetchedAt = 1L,
                meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
            )
        )

        val guavaLabel = BloomForecastEngine.autoBloomTimingLabelFor(
            speciesInput = "Guava",
            locationProfile = locationProfile
        )
        val sapodillaLabel = BloomForecastEngine.autoBloomTimingLabelFor(
            speciesInput = "Sapodilla",
            locationProfile = locationProfile
        )

        assertThat(guavaLabel).isEqualTo("This orchard - Active season Apr 1 - Nov 30")
        assertThat(sapodillaLabel).isEqualTo("This orchard - Active season Apr 1 - Nov 30")
    }

    @Test
    fun autoBloomTimingLabelFor_usesCoolingSeasonForLoquatInSouthernHemisphere() {
        val locationProfile = ForecastLocationProfile(
            hemisphere = Hemisphere.SOUTHERN,
            latitudeDeg = -27.5,
            climateFingerprint = LocationClimateFingerprint(
                source = "NASA POWER",
                fetchedAt = 1L,
                meanMonthlyTempC = listOf(26.0, 26.0, 24.0, 21.0, 18.0, 15.0, 14.0, 15.0, 17.0, 20.0, 23.0, 25.0),
                meanMonthlyMinTempC = listOf(20.0, 20.0, 18.0, 15.0, 12.0, 9.0, 8.0, 9.0, 11.0, 14.0, 17.0, 19.0),
                meanMonthlyMaxTempC = listOf(32.0, 32.0, 30.0, 27.0, 24.0, 21.0, 20.0, 21.0, 24.0, 28.0, 30.0, 31.0)
            )
        )

        val label = BloomForecastEngine.autoBloomTimingLabelFor(
            speciesInput = "Loquat",
            locationProfile = locationProfile
        )

        assertThat(label).isEqualTo("This orchard - May 20 - Jul 4")
    }

    @Test
    fun predictMonth_usesClimateFingerprintForTemperateWindowSpecies() {
        val appleTree = TreeEntity(
            id = "apple-climate-window",
            orchardName = "Home",
            sectionName = "Back row",
            nickname = "Anna",
            species = "Apple",
            cultivar = "",
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
            trees = listOf(appleTree),
            yearMonth = YearMonth.of(2026, 3),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                latitudeDeg = 29.7,
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(10.0, 12.0, 15.0, 18.0, 22.0, 26.0, 28.0, 28.0, 25.0, 20.0, 15.0, 11.0),
                    meanMonthlyMinTempC = listOf(5.0, 7.0, 10.0, 14.0, 18.0, 22.0, 23.0, 23.0, 20.0, 15.0, 10.0, 6.0),
                    meanMonthlyMaxTempC = listOf(15.0, 18.0, 21.0, 25.0, 29.0, 33.0, 35.0, 35.0, 32.0, 27.0, 21.0, 16.0)
                )
            )
        )

        assertThat(marchWindow).hasSize(1)
        assertThat(marchWindow.single().source).isEqualTo(ForecastSource.CLIMATE_BAND)
        assertThat(marchWindow.single().startDate).isEqualTo(LocalDate.of(2026, 3, 4))
        assertThat(marchWindow.single().endDate).isEqualTo(LocalDate.of(2026, 3, 16))
    }

    @Test
    fun predictMonth_usesTropicalRepeatSeasonForGuavaAndSapodilla() {
        val guavaTree = TreeEntity(
            id = "guava-repeat",
            orchardName = "Home",
            sectionName = "Warm side",
            nickname = null,
            species = "Guava",
            cultivar = "Thai White",
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
        val sapodillaTree = guavaTree.copy(
            id = "sapodilla-repeat",
            species = "Sapodilla",
            cultivar = "Hasya"
        )
        val warmProfile = ForecastLocationProfile(
            hemisphere = Hemisphere.NORTHERN,
            usdaZoneCode = "10a",
            climateFingerprint = LocationClimateFingerprint(
                source = "NASA POWER",
                fetchedAt = 1L,
                meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
            )
        )

        val octoberWindows = BloomForecastEngine.predictMonth(
            trees = listOf(guavaTree, sapodillaTree),
            yearMonth = YearMonth.of(2026, 10),
            locationProfile = warmProfile
        )
        val januaryWindows = BloomForecastEngine.predictMonth(
            trees = listOf(guavaTree, sapodillaTree),
            yearMonth = YearMonth.of(2026, 1),
            locationProfile = warmProfile
        )

        assertThat(octoberWindows.map { it.treeId }).containsAtLeast("guava-repeat", "sapodilla-repeat")
        assertThat(octoberWindows.first { it.treeId == "guava-repeat" }.patternType).isEqualTo(BloomPatternType.MULTI_WAVE)
        assertThat(octoberWindows.first { it.treeId == "guava-repeat" }.source).isEqualTo(ForecastSource.CLIMATE_BAND)
        assertThat(octoberWindows.first { it.treeId == "sapodilla-repeat" }.patternType).isEqualTo(BloomPatternType.MULTI_WAVE)
        assertThat(januaryWindows).isEmpty()
    }

    @Test
    fun predictMonth_clampsDragonFruitBroadWarmFingerprintToSubtropicalSeason() {
        val dragonFruitTree = TreeEntity(
            id = "dragon-fruit-broad-climate-fingerprint",
            orchardName = "Home",
            sectionName = "Test",
            nickname = null,
            species = "Dragon fruit",
            cultivar = "",
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
            trees = listOf(dragonFruitTree),
            yearMonth = YearMonth.of(2026, 4),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "10a",
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(19.0, 20.0, 21.0, 23.0, 25.0, 27.0, 28.0, 28.0, 27.0, 25.0, 22.0, 20.0),
                    meanMonthlyMinTempC = listOf(12.0, 13.0, 15.0, 17.0, 20.0, 23.0, 24.0, 24.0, 23.0, 20.0, 17.0, 14.0),
                    meanMonthlyMaxTempC = listOf(27.0, 28.0, 29.0, 31.0, 33.0, 35.0, 36.0, 36.0, 35.0, 33.0, 30.0, 28.0)
                )
            )
        )
        val juneWindow = BloomForecastEngine.predictMonth(
            trees = listOf(dragonFruitTree),
            yearMonth = YearMonth.of(2026, 6),
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "10a",
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(19.0, 20.0, 21.0, 23.0, 25.0, 27.0, 28.0, 28.0, 27.0, 25.0, 22.0, 20.0),
                    meanMonthlyMinTempC = listOf(12.0, 13.0, 15.0, 17.0, 20.0, 23.0, 24.0, 24.0, 23.0, 20.0, 17.0, 14.0),
                    meanMonthlyMaxTempC = listOf(27.0, 28.0, 29.0, 31.0, 33.0, 35.0, 36.0, 36.0, 35.0, 33.0, 30.0, 28.0)
                )
            )
        )

        assertThat(aprilWindow).isEmpty()
        assertThat(juneWindow).hasSize(1)
        assertThat(juneWindow.single().startDate).isEqualTo(LocalDate.of(2026, 5, 1))
        assertThat(juneWindow.single().endDate).isEqualTo(LocalDate.of(2026, 10, 31))
    }
}




