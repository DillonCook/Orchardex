package com.dillon.orcharddex.data.phenology

enum class OrchardRegion(val code: String, val label: String) {
    NOT_SET("", "Not set"),
    SOUTH_FLORIDA("south_florida", "South Florida"),
    HAWAII("hawaii", "Hawaii"),
    CALIFORNIA("california", "California")
}

object OrchardRegionCatalog {
    val regions: List<OrchardRegion> = OrchardRegion.entries.toList()

    fun supportedLabels(): List<String> = regions.map(OrchardRegion::label)

    fun codeFromLabel(label: String): String = regions
        .firstOrNull { it.label == label }
        ?.code
        ?: OrchardRegion.NOT_SET.code

    fun labelForCode(code: String?): String = resolve(code).label

    fun resolve(code: String?): OrchardRegion = regions
        .firstOrNull { it.code == code.orEmpty().trim().lowercase() }
        ?: OrchardRegion.NOT_SET
}
