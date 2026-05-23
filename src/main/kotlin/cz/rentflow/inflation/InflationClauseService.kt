package cz.rentflow.inflation

import cz.rentflow.domain.entity.Contract
import cz.rentflow.domain.repository.ContractRepository
import cz.rentflow.email.EmailService
import cz.rentflow.pdf.PdfService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class InflationUpdateResult(
    val contractId: Long,
    val landlordEmail: String,
    val tenantName: String,
    val oldRent: BigDecimal,
    val newRent: BigDecimal,
    val inflationRate: BigDecimal,
    val year: Int,
    val pdfBytes: ByteArray
)

@Service
class InflationClauseService(
    private val contractRepository: ContractRepository,
    private val czsoClient: CzsoInflationClient,
    private val pdfService: PdfService,
    private val emailService: EmailService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Scheduled every January 1st at 09:00.
     * Checks all active contracts with inflation clause enabled,
     * recalculates rent, generates PDF amendment, and notifies landlords.
     */
    @Scheduled(cron = "0 0 9 1 1 *")
    fun processInflationClauses() {
        val year = LocalDate.now().year - 1 // use previous year's inflation
        log.info("Processing inflation clauses for year {}", year)

        val results = applyInflationUpdates(year)
        log.info("Inflation update complete: {} contracts updated", results.size)
    }

    @Transactional
    fun applyInflationUpdates(year: Int): List<InflationUpdateResult> {
        val inflationRate = czsoClient.getAnnualInflationRate(year)
        log.info("Annual inflation rate for {}: {}%", year, inflationRate)

        val contracts = contractRepository.findAll().filter { it.inflationClauseEnabled && isActive(it) }
        log.info("Found {} active contracts with inflation clause", contracts.size)

        val results = mutableListOf<InflationUpdateResult>()

        for (contract in contracts) {
            val oldRent = contract.rentAmount
            val newRent = calculateNewRent(oldRent, inflationRate)
            contract.rentAmount = newRent
            contractRepository.save(contract)

            val landlord = contract.unit.property.landlord
            val pdfBytes = pdfService.generateInflationAmendment(contract, oldRent, newRent, inflationRate, year)

            emailService.sendInflationAmendment(
                to = landlord.email,
                landlordName = landlord.name,
                tenantName = contract.tenant.name,
                oldRent = oldRent,
                newRent = newRent,
                inflationRate = inflationRate,
                year = year,
                pdfBytes = pdfBytes
            )

            results.add(
                InflationUpdateResult(
                    contractId = contract.id,
                    landlordEmail = landlord.email,
                    tenantName = contract.tenant.name,
                    oldRent = oldRent,
                    newRent = newRent,
                    inflationRate = inflationRate,
                    year = year,
                    pdfBytes = pdfBytes
                )
            )
            log.info("Contract {}: rent updated {} → {} CZK (+{}%)", contract.id, oldRent, newRent, inflationRate)
        }
        return results
    }

    fun calculateNewRent(currentRent: BigDecimal, inflationRate: BigDecimal): BigDecimal {
        val multiplier = BigDecimal.ONE + inflationRate.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
        return (currentRent * multiplier).setScale(2, RoundingMode.HALF_UP)
    }

    private fun isActive(contract: Contract): Boolean {
        val today = LocalDate.now()
        return contract.endDate == null || contract.endDate!!.isAfter(today)
    }
}
