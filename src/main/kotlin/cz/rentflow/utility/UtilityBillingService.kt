package cz.rentflow.utility

import cz.rentflow.domain.entity.PaymentStatus
import cz.rentflow.domain.entity.PaymentType
import cz.rentflow.domain.entity.UtilityType
import cz.rentflow.domain.repository.PaymentRepository
import cz.rentflow.domain.repository.UnitRepository
import cz.rentflow.domain.repository.UtilityReadingRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Billing request for one unit for a given period.
 * The landlord provides actual costs (from energy supplier invoices) per utility type.
 */
data class UtilityBillingRequest(
    val unitId: Long,
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    /** Actual costs from energy supplier invoices, keyed by utility type */
    val actualCosts: Map<UtilityType, BigDecimal> = emptyMap(),
    /** Unit price per kWh/m³ for consumption-based calculation (alternative to actualCosts) */
    val unitPrices: Map<UtilityType, BigDecimal> = emptyMap()
)

data class UtilityLineItem(
    val utilityType: UtilityType,
    val openingReading: BigDecimal?,
    val closingReading: BigDecimal?,
    val consumption: BigDecimal?,
    val unitPrice: BigDecimal?,
    val actualCost: BigDecimal,
    val depositsCollected: BigDecimal,
    /** Positive = tenant owes landlord, Negative = landlord returns to tenant */
    val balance: BigDecimal
)

data class UtilityBillingResult(
    val unitId: Long,
    val unitNumber: String,
    val address: String,
    val tenantName: String,
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val lineItems: List<UtilityLineItem>,
    val totalActualCost: BigDecimal,
    val totalDepositsCollected: BigDecimal,
    val totalBalance: BigDecimal,
    val pdfBytes: ByteArray
)

@Service
class UtilityBillingService(
    private val unitRepository: UnitRepository,
    private val utilityReadingRepository: UtilityReadingRepository,
    private val paymentRepository: PaymentRepository,
    private val utilityBillingPdfService: UtilityBillingPdfService
) {

    fun calculate(request: UtilityBillingRequest, landlordId: Long): UtilityBillingResult {
        val unit = unitRepository.findByIdAndPropertyLandlordId(request.unitId, landlordId)
            ?: throw NoSuchElementException("Unit ${request.unitId} not found")

        val activeContract = unit.contracts
            .filter { it.startDate <= request.periodTo && (it.endDate == null || it.endDate!! >= request.periodFrom) }
            .maxByOrNull { it.startDate }

        val tenantName = activeContract?.tenant?.name ?: "—"

        val lineItems = UtilityType.entries.mapNotNull { utilityType ->
            buildLineItem(request, utilityType)
        }

        val totalActualCost = lineItems.sumOf { it.actualCost }
        val totalDeposits = lineItems.sumOf { it.depositsCollected }
        val totalBalance = lineItems.sumOf { it.balance }

        val pdfBytes = utilityBillingPdfService.generate(
            unit = unit,
            tenantName = tenantName,
            periodFrom = request.periodFrom,
            periodTo = request.periodTo,
            lineItems = lineItems,
            totalActualCost = totalActualCost,
            totalDeposits = totalDeposits,
            totalBalance = totalBalance
        )

        return UtilityBillingResult(
            unitId = unit.id,
            unitNumber = unit.unitNumber,
            address = unit.property.address,
            tenantName = tenantName,
            periodFrom = request.periodFrom,
            periodTo = request.periodTo,
            lineItems = lineItems,
            totalActualCost = totalActualCost,
            totalDepositsCollected = totalDeposits,
            totalBalance = totalBalance,
            pdfBytes = pdfBytes
        )
    }

    private fun buildLineItem(request: UtilityBillingRequest, utilityType: UtilityType): UtilityLineItem? {
        val readings = utilityReadingRepository.findAllByUnitIdAndUtilityTypeAndReadingDateBetween(
            request.unitId, utilityType, request.periodFrom, request.periodTo
        ).sortedBy { it.readingDate }

        val deposits = paymentRepository.findByUnitIdAndTypeAndStatusAndPeriod(
            request.unitId, PaymentType.UTILITY, PaymentStatus.PAID,
            request.periodFrom, request.periodTo
        ).sumOf { it.amount }

        val openingReading = readings.firstOrNull()?.value
        val closingReading = readings.lastOrNull()?.value
        val consumption = if (openingReading != null && closingReading != null && readings.size >= 2)
            (closingReading - openingReading).setScale(3, RoundingMode.HALF_UP) else null

        val unitPrice = request.unitPrices[utilityType]
        val actualCost = when {
            request.actualCosts.containsKey(utilityType) -> request.actualCosts[utilityType]!!
            consumption != null && unitPrice != null -> (consumption * unitPrice).setScale(2, RoundingMode.HALF_UP)
            else -> return null // no data for this utility type — skip
        }

        return UtilityLineItem(
            utilityType = utilityType,
            openingReading = openingReading,
            closingReading = closingReading,
            consumption = consumption,
            unitPrice = unitPrice,
            actualCost = actualCost,
            depositsCollected = deposits,
            balance = (actualCost - deposits).setScale(2, RoundingMode.HALF_UP)
        )
    }
}
