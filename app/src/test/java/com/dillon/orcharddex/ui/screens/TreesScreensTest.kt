package com.dillon.orcharddex.ui.screens

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeOriginType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.ui.viewmodel.TreeFormState
import com.dillon.orcharddex.ui.existingCultivarAutocompleteOptions
import com.dillon.orcharddex.ui.previewCultivarAliases
import com.dillon.orcharddex.ui.resolveStableSpeciesSelection
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TreesScreensTest {
    @Test
    fun resolveStableSpeciesSelection_doesNotLockOntoPartialSpeciesText() {
        val resolved = resolveStableSpeciesSelection(
            query = "ma",
            options = listOf("Mango", "Guava")
        )

        assertThat(resolved).isNull()
    }

    @Test
    fun resolveStableSpeciesSelection_canonicalizesExactCatalogAliases() {
        val resolved = resolveStableSpeciesSelection(
            query = "Mangifera indica",
            options = listOf("Mango", "Guava")
        )

        assertThat(resolved).isEqualTo("Mango")
    }

    @Test
    fun resolveStableSpeciesSelection_matchesExactOrchardSpecies() {
        val resolved = resolveStableSpeciesSelection(
            query = "Backyard hybrid",
            options = listOf("Backyard Hybrid", "Guava")
        )

        assertThat(resolved).isEqualTo("Backyard Hybrid")
    }

    @Test
    fun existingCultivarAutocompleteOptions_blankQuery_scopesToExactSpecies() {
        val cultivars = existingCultivarAutocompleteOptions(
            query = "",
            speciesQuery = "Mango",
            trees = listOf(
                testTree(id = "1", species = "Mango", cultivar = "Carrie"),
                testTree(id = "2", species = "Guava", cultivar = "Ruby Supreme"),
                testTree(id = "3", species = "Mango", cultivar = "Pickering")
            )
        )

        assertThat(cultivars.map { it.cultivar }).containsExactly("Carrie", "Pickering")
    }

    @Test
    fun existingCultivarAutocompleteOptions_blankQuery_matchesLegacySpeciesAliasRows() {
        val cultivars = existingCultivarAutocompleteOptions(
            query = "",
            speciesQuery = "Mango",
            trees = listOf(
                testTree(id = "1", species = "Mangifera indica", cultivar = "Carrie"),
                testTree(id = "2", species = "Guava", cultivar = "Ruby Supreme")
            )
        )

        assertThat(cultivars.map { it.cultivar }).containsExactly("Carrie")
    }

    @Test
    fun previewCultivarAliases_prioritizesAliasesMatchingQuery() {
        val preview = previewCultivarAliases(
            query = "honey",
            aliases = listOf("Champagne", "Champagne mango", "Honey", "Honey mango")
        )

        assertThat(preview).containsExactly("Honey", "Honey mango").inOrder()
    }

    @Test
    fun previewCultivarAliases_keepsOriginalOrderWhenQueryBlank() {
        val preview = previewCultivarAliases(
            query = "",
            aliases = listOf("Champagne", "Champagne mango", "Honey", "Honey mango")
        )

        assertThat(preview).containsExactly("Champagne", "Champagne mango").inOrder()
    }

    @Test
    fun shouldAutoExpandAdvancedFields_opensForPropagationDrafts() {
        val shouldExpand = shouldAutoExpandAdvancedFields(
            TreeFormState(
                species = "Mango",
                cultivar = "Pickering",
                parentTreeId = "parent-1",
                originType = TreeOriginType.PROPAGATED
            )
        )

        assertThat(shouldExpand).isTrue()
    }

    private fun testTree(
        id: String,
        species: String,
        cultivar: String
    ) = TreeEntity(
        id = id,
        orchardName = "Test Orchard",
        sectionName = "",
        nickname = null,
        species = species,
        cultivar = cultivar,
        rootstock = null,
        source = null,
        purchaseDate = null,
        plantedDate = 0L,
        plantType = PlantType.IN_GROUND,
        containerSize = null,
        sunExposure = null,
        frostSensitivity = FrostSensitivityLevel.MEDIUM,
        frostSensitivityNote = null,
        irrigationNote = null,
        status = TreeStatus.ACTIVE,
        notes = "",
        tags = "",
        bloomTimingMode = BloomTimingMode.AUTO,
        createdAt = 0L,
        updatedAt = 0L
    )
}
