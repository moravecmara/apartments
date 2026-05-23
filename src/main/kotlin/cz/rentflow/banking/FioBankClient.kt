package cz.rentflow.banking

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Client for Fio Banka Open API.
 * Docs: https://www.fio.cz/docs/cz/API_Bankovnictvi.pdf
 */
@Component
class FioBankClient(
    @Value("\${app.fio.api-token:}") private val apiToken: String
) {
    private val restClient = RestClient.create()
    private val baseUrl = "https://www.fio.cz/ib_api/rest"
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Download transactions for a given date range. */
    fun getTransactions(from: LocalDate, to: LocalDate): List<BankTransaction> {
        if (apiToken.isBlank()) return emptyList()
        val url = "$baseUrl/periods/$apiToken/${from.format(dateFormat)}/${to.format(dateFormat)}/transactions.json"
        return runCatching {
            val response = restClient.get().uri(url).retrieve().body(FioResponse::class.java)
            response?.accountStatement?.transactionList?.transaction?.mapNotNull { it.toBankTransaction() } ?: emptyList()
        }.getOrElse {
            emptyList()
        }
    }

    /** Download all new transactions since last download. */
    fun getNewTransactions(): List<BankTransaction> {
        if (apiToken.isBlank()) return emptyList()
        val url = "$baseUrl/last/$apiToken/transactions.json"
        return runCatching {
            val response = restClient.get().uri(url).retrieve().body(FioResponse::class.java)
            response?.accountStatement?.transactionList?.transaction?.mapNotNull { it.toBankTransaction() } ?: emptyList()
        }.getOrElse { emptyList() }
    }
}

data class BankTransaction(
    val id: String,
    val amount: BigDecimal,
    val date: LocalDate,
    val variableSymbol: String?,
    val senderAccount: String?,
    val message: String?
)

// --- Fio API response model ---
@JsonIgnoreProperties(ignoreUnknown = true)
data class FioResponse(val accountStatement: FioAccountStatement? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FioAccountStatement(val transactionList: FioTransactionList? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FioTransactionList(val transaction: List<FioTransaction>? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FioTransaction(
    @JsonProperty("column22") val id: FioValue<Long>? = null,
    @JsonProperty("column1") val date: FioValue<String>? = null,
    @JsonProperty("column0") val amount: FioValue<Double>? = null,
    @JsonProperty("column5") val variableSymbol: FioValue<String>? = null,
    @JsonProperty("column2") val senderAccount: FioValue<String>? = null,
    @JsonProperty("column16") val message: FioValue<String>? = null
) {
    fun toBankTransaction(): BankTransaction? {
        val txId = id?.value?.toString() ?: return null
        val txAmount = amount?.value?.let { BigDecimal(it.toString()) } ?: return null
        val txDate = date?.value?.let {
            runCatching { LocalDate.parse(it.substring(0, 10)) }.getOrNull()
        } ?: return null
        return BankTransaction(
            id = txId,
            amount = txAmount,
            date = txDate,
            variableSymbol = variableSymbol?.value,
            senderAccount = senderAccount?.value,
            message = message?.value
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FioValue<T>(@JsonProperty("value") val value: T? = null)
