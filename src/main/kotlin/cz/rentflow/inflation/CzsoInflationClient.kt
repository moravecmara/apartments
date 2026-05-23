package cz.rentflow.inflation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

/**
 * Client for the Czech Statistical Office (ČSÚ) public API.
 * Uses the official JSON API: https://api.czso.cz/
 *
 * Inflation dataset: "0100197" = Consumer Price Index (CPI), year-on-year change.
 */
@Component
class CzsoInflationClient {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    // ČSÚ API endpoint for annual average inflation (CPI, year-on-year %)
    private val apiUrl = "https://api.czso.cz/csw/v1/dcat/datasetDetail/0100197"
    // Direct data endpoint for inflation series
    private val dataUrl = "https://api.czso.cz/czso/v1/datova-sada/0100197/data?rok={year}"

    /**
     * Fetch the official annual inflation rate for the given year.
     * Returns e.g. 2.0 for 2.0% inflation.
     * Falls back to hardcoded recent values if API is unreachable.
     */
    fun getAnnualInflationRate(year: Int): BigDecimal {
        return runCatching { fetchFromApi(year) }
            .getOrElse {
                log.warn("ČSÚ API unavailable, using fallback for year {}: {}", year, it.message)
                getFallbackRate(year)
            }
    }

    private fun fetchFromApi(year: Int): BigDecimal {
        // ČSÚ data API - annual CPI data for all categories
        val url = "https://api.czso.cz/czso/v1/datova-sada/0100197/data?rok=$year&hodnotaKod=PRUMERNE"
        val response = restClient.get()
            .uri(url)
            .header("Accept", "application/json")
            .retrieve()
            .body(CzsoDataResponse::class.java)

        val rate = response?.data
            ?.firstOrNull { it.rok == year.toString() && it.hodnota != null }
            ?.hodnota
            ?.let { BigDecimal(it.toString()) }

        return rate ?: throw IllegalStateException("No inflation data for year $year in ČSÚ response")
    }

    /** Fallback rates from official ČSÚ publications */
    private fun getFallbackRate(year: Int): BigDecimal = when (year) {
        2024 -> BigDecimal("2.4")
        2023 -> BigDecimal("10.7")
        2022 -> BigDecimal("15.1")
        2021 -> BigDecimal("3.8")
        2020 -> BigDecimal("3.2")
        else -> BigDecimal("2.0") // ECB target as safe default
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CzsoDataResponse(val data: List<CzsoDataRow>? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CzsoDataRow(
    val rok: String? = null,
    val hodnota: Any? = null
)
