package com.dillon.orcharddex.data.remote

import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.LocationSearchResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.IOException

interface LocationSearchService {
    suspend fun search(query: String): List<LocationSearchResult>
}

interface ClimateFingerprintService {
    suspend fun fetch(latitudeDeg: Double, longitudeDeg: Double): LocationClimateFingerprint?
}

class OpenMeteoLocationSearchService(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LocationSearchService {
    override suspend fun search(query: String): List<LocationSearchResult> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.name())
        val response = httpGet(
            "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=8&language=en&format=json"
        )
        val payload = json.decodeFromString<OpenMeteoGeocodingResponse>(response)
        return payload.results.orEmpty().map { result ->
            LocationSearchResult(
                name = result.name,
                countryCode = result.countryCode.orEmpty(),
                country = result.country.orEmpty(),
                admin1 = result.admin1.orEmpty(),
                admin2 = result.admin2.orEmpty(),
                timezoneId = result.timezone.orEmpty(),
                latitudeDeg = result.latitude,
                longitudeDeg = result.longitude,
                elevationM = result.elevation
            )
        }
    }
}

class NasaPowerClimateService : ClimateFingerprintService {
    override suspend fun fetch(latitudeDeg: Double, longitudeDeg: Double): LocationClimateFingerprint? {
        val response = httpGet(
            "https://power.larc.nasa.gov/api/temporal/climatology/point" +
                "?parameters=T2M,T2M_MIN,T2M_MAX" +
                "&community=AG" +
                "&longitude=$longitudeDeg" +
                "&latitude=$latitudeDeg" +
                "&format=JSON"
        )
        val payload = Json { ignoreUnknownKeys = true }.decodeFromString<NasaPowerClimateResponse>(response)
        val parameters = payload.properties.parameter
        val meanMonthlyTemp = parameters["T2M"]?.toMonthlyValues().orEmpty()
        val meanMonthlyMinTemp = parameters["T2M_MIN"]?.toMonthlyValues().orEmpty()
        val meanMonthlyMaxTemp = parameters["T2M_MAX"]?.toMonthlyValues().orEmpty()
        if (meanMonthlyTemp.size != 12 || meanMonthlyMinTemp.size != 12 || meanMonthlyMaxTemp.size != 12) {
            return null
        }
        return LocationClimateFingerprint(
            source = "NASA POWER",
            fetchedAt = System.currentTimeMillis(),
            meanMonthlyTempC = meanMonthlyTemp,
            meanMonthlyMinTempC = meanMonthlyMinTemp,
            meanMonthlyMaxTempC = meanMonthlyMaxTemp
        )
    }
}

private fun httpGet(url: String): String {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 15_000
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "OrcharDex/1.0")
    }
    try {
        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (responseCode !in 200..299) {
            throw IOException("HTTP $responseCode for $url${body.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()}")
        }
        return body
    } finally {
        connection.disconnect()
    }
}

private fun Map<String, Double>.toMonthlyValues(): List<Double> {
    val monthOrder = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    return monthOrder.mapNotNull { month -> this[month] }
}

@Serializable
private data class OpenMeteoGeocodingResponse(
    val results: List<OpenMeteoGeocodingResult>? = null
)

@Serializable
private data class OpenMeteoGeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val country: String? = null,
    val timezone: String? = null,
    val admin1: String? = null,
    val admin2: String? = null
)

@Serializable
private data class NasaPowerClimateResponse(
    val properties: NasaPowerProperties
)

@Serializable
private data class NasaPowerProperties(
    val parameter: Map<String, Map<String, Double>>
)
