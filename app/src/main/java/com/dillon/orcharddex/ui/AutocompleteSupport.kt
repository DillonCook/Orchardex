package com.dillon.orcharddex.ui

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.CultivarAutocompleteOption

internal fun resolveStableSpeciesSelection(
    query: String,
    options: List<String>
): String? {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.length < 2) return null
    BloomForecastEngine.resolveSpeciesAutocomplete(query)?.let { return it }
    return options.firstOrNull { normalizeAutocomplete(it) == normalizedQuery }
}

internal fun autocompleteSpeciesOptions(
    query: String,
    options: List<String>,
    limit: Int = 8
): List<String> {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return emptyList()
    return options
        .mapNotNull { option ->
            autocompleteMatchScore(normalizedQuery, normalizeAutocomplete(option))?.let { score -> option to score }
        }
        .sortedWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy { it.first.lowercase() }
        )
        .map(Pair<String, Int>::first)
        .distinctBy(::normalizeAutocomplete)
        .take(limit)
}

internal fun existingCultivarAutocompleteOptions(
    query: String,
    speciesQuery: String,
    trees: List<TreeEntity>,
    limit: Int = 8
): List<CultivarAutocompleteOption> {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) {
        val normalizedSpeciesScope = normalizedCultivarSpeciesScope(speciesQuery)
        if (normalizedSpeciesScope.isBlank()) return emptyList()
        return trees
            .filter { it.cultivar.isNotBlank() }
            .map {
                CultivarAutocompleteOption(
                    species = BloomForecastEngine.resolveSpeciesAutocomplete(it.species) ?: it.species,
                    cultivar = it.cultivar
                )
            }
            .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
            .filter { normalizedCultivarSpeciesScope(it.species) == normalizedSpeciesScope }
            .sortedWith(
                compareBy<CultivarAutocompleteOption> { it.species.lowercase() }
                    .thenBy { it.cultivar.lowercase() }
            )
            .take(limit)
    }
    return trees
        .filter { it.cultivar.isNotBlank() }
        .mapNotNull { tree ->
            val score = autocompleteMatchScore(normalizedQuery, normalizeAutocomplete(tree.cultivar))
                ?: return@mapNotNull null
            CultivarAutocompleteOption(
                species = BloomForecastEngine.resolveSpeciesAutocomplete(tree.species) ?: tree.species,
                cultivar = tree.cultivar
            ) to score
        }
        .sortedWith(
            compareByDescending<Pair<CultivarAutocompleteOption, Int>> { it.second }
                .thenByDescending {
                    speciesAutocompleteScore(speciesQuery, it.first.species)
                }
                .thenBy { it.first.species.lowercase() }
                .thenBy { it.first.cultivar.lowercase() }
        )
        .map(Pair<CultivarAutocompleteOption, Int>::first)
        .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
        .take(limit)
}

internal fun resolveExistingCultivarAutocomplete(
    query: String,
    trees: List<TreeEntity>
): CultivarAutocompleteOption? {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return null
    val matches = trees
        .filter { normalizeAutocomplete(it.cultivar) == normalizedQuery }
        .map {
            CultivarAutocompleteOption(
                species = BloomForecastEngine.resolveSpeciesAutocomplete(it.species) ?: it.species,
                cultivar = it.cultivar
            )
        }
        .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
    if (matches.isEmpty()) return null
    return matches.singleOrNull()
}

internal fun previewCultivarAliases(
    query: String,
    aliases: List<String>,
    limit: Int = 2
): List<String> {
    val normalizedQuery = normalizeAutocomplete(query)
    val distinctAliases = aliases.distinctBy(::normalizeAutocomplete)
    if (normalizedQuery.isBlank()) return distinctAliases.take(limit)
    return distinctAliases
        .sortedWith(
            compareByDescending<String> {
                autocompleteMatchScore(normalizedQuery, normalizeAutocomplete(it)) ?: 0
            }.thenBy { it.lowercase() }
        )
        .take(limit)
}

internal fun cultivarDisplayOptions(option: CultivarAutocompleteOption?): List<String> = option
    ?.let { listOf(it.cultivar) + it.aliases }
    .orEmpty()
    .filter(String::isNotBlank)
    .distinctBy(::normalizeAutocomplete)

private fun speciesAutocompleteScore(query: String, species: String): Int {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return 0
    val normalizedSpecies = normalizeAutocomplete(species)
    return when {
        normalizedSpecies == normalizedQuery -> 3
        normalizedSpecies.startsWith(normalizedQuery) -> 2
        normalizedSpecies.contains(normalizedQuery) -> 1
        else -> 0
    }
}

private fun autocompleteMatchScore(query: String, candidate: String): Int? = when {
    candidate == query -> 500
    candidate.startsWith(query) -> 400
    candidate.split(' ').any { it.startsWith(query) } -> 320
    candidate.contains(query) -> 220
    else -> null
}

private fun normalizedCultivarSpeciesScope(species: String): String =
    normalizeAutocomplete(BloomForecastEngine.resolveSpeciesAutocomplete(species) ?: species)

internal fun normalizeAutocomplete(value: String): String = value
    .trim()
    .lowercase()
    .replace("&", "and")
    .replace(Regex("[^a-z0-9]+"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()
