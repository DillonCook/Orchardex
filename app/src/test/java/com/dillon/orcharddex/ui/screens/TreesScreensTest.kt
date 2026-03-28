package com.dillon.orcharddex.ui.screens

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TreesScreensTest {
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
}
