package com.dillon.orcharddex.data.phenology

data class UsdaZoneDefinition(
    val code: String,
    val label: String,
    val index: Int
)

object UsdaZoneCatalog {
    val zones: List<UsdaZoneDefinition> = buildList {
        var index = 0
        for (zoneNumber in 1..13) {
            val minA = -60 + (zoneNumber - 1) * 10
            add(
                UsdaZoneDefinition(
                    code = "${zoneNumber}a",
                    label = "${zoneNumber}a ($minA to ${minA + 5}°F)",
                    index = index++
                )
            )
            val minB = minA + 5
            add(
                UsdaZoneDefinition(
                    code = "${zoneNumber}b",
                    label = "${zoneNumber}b ($minB to ${minB + 5}°F)",
                    index = index++
                )
            )
        }
    }

    private val zonesByCode = zones.associateBy { it.code }

    fun resolve(code: String?): UsdaZoneDefinition = zonesByCode[code.orEmpty().trim().lowercase()] ?: default()

    fun default(): UsdaZoneDefinition = zonesByCode.getValue("7a")
}
