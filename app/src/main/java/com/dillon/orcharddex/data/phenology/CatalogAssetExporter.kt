package com.dillon.orcharddex.data.phenology

import java.io.File

object CatalogAssetExporter {
    @JvmStatic
    fun main(args: Array<String>) {
        val assetDir = File(args.firstOrNull() ?: "app/src/main/assets").apply { mkdirs() }

        val phenologyJson = PhenologyCatalogAssets.serializePhenologyCatalog(
            speciesProfiles = BloomForecastEngine.catalogSpeciesProfilesForExport(),
            cultivarProfiles = BloomForecastEngine.catalogCultivarProfilesForExport(),
            catalogOnlyCultivars = BloomForecastEngine.catalogOnlyCultivarsForExport()
        )
        val pollinationJson = PhenologyCatalogAssets.serializePollinationCatalog()

        File(assetDir, "phenology_profiles.json").writeText(phenologyJson)
        File(assetDir, "pollination_profiles.json").writeText(pollinationJson)

        println("Exported ${BloomForecastEngine.catalogSpeciesProfilesForExport().size} species profiles")
        println("Exported ${BloomForecastEngine.catalogCultivarProfilesForExport().size} cultivar profiles")
        println("Exported ${BloomForecastEngine.catalogOnlyCultivarsForExport().size} catalog-only cultivars")
    }
}
