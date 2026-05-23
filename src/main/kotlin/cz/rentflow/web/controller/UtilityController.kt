package cz.rentflow.web.controller

import cz.rentflow.security.LandlordPrincipal
import cz.rentflow.utility.*
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/units/{unitId}/utility-readings")
class UtilityReadingController(private val utilityReadingService: UtilityReadingService) {

    @GetMapping
    fun getAll(@PathVariable unitId: Long): List<UtilityReadingResponse> =
        utilityReadingService.findAll(unitId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable unitId: Long,
        @Valid @RequestBody request: UtilityReadingRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): UtilityReadingResponse = utilityReadingService.create(unitId, request, principal.id)

    @PutMapping("/{id}")
    fun update(
        @PathVariable unitId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: UtilityReadingRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): UtilityReadingResponse = utilityReadingService.update(id, request, principal.id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ) = utilityReadingService.delete(id, principal.id)
}

@RestController
@RequestMapping("/api/billing")
class UtilityBillingController(private val utilityBillingService: UtilityBillingService) {

    /**
     * Calculate utility billing for a unit and return JSON summary + PDF download link.
     * Body example:
     * {
     *   "unitId": 1,
     *   "periodFrom": "2024-01-01",
     *   "periodTo": "2024-12-31",
     *   "actualCosts": { "ELECTRICITY": 12500.00, "GAS": 8300.00, "WATER": 2400.00 }
     * }
     */
    @PostMapping("/calculate")
    fun calculate(
        @RequestBody request: UtilityBillingRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): UtilityBillingSummaryResponse {
        val result = utilityBillingService.calculate(request, principal.id)
        return result.toSummary()
    }

    /** Calculate and immediately stream the PDF. */
    @PostMapping("/calculate/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun calculateAndDownloadPdf(
        @RequestBody request: UtilityBillingRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): ResponseEntity<ByteArray> {
        val result = utilityBillingService.calculate(request, principal.id)
        val filename = "vyuctovani_${result.unitNumber}_${request.periodFrom.year}.pdf"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(result.pdfBytes)
    }
}

data class UtilityLineItemResponse(
    val utilityType: String,
    val openingReading: String?,
    val closingReading: String?,
    val consumption: String?,
    val actualCost: String,
    val depositsCollected: String,
    val balance: String,
    val balanceDescription: String
)

data class UtilityBillingSummaryResponse(
    val unitId: Long,
    val unitNumber: String,
    val address: String,
    val tenantName: String,
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val lineItems: List<UtilityLineItemResponse>,
    val totalActualCost: String,
    val totalDepositsCollected: String,
    val totalBalance: String,
    val totalBalanceDescription: String
)

fun UtilityBillingResult.toSummary() = UtilityBillingSummaryResponse(
    unitId = unitId,
    unitNumber = unitNumber,
    address = address,
    tenantName = tenantName,
    periodFrom = periodFrom,
    periodTo = periodTo,
    lineItems = lineItems.map {
        UtilityLineItemResponse(
            utilityType = it.utilityType.name,
            openingReading = it.openingReading?.toPlainString(),
            closingReading = it.closingReading?.toPlainString(),
            consumption = it.consumption?.toPlainString(),
            actualCost = "${it.actualCost} Kč",
            depositsCollected = "${it.depositsCollected} Kč",
            balance = "${it.balance} Kč",
            balanceDescription = if (it.balance >= java.math.BigDecimal.ZERO) "Nájemce doplácí" else "Pronajímatel vrací"
        )
    },
    totalActualCost = "$totalActualCost Kč",
    totalDepositsCollected = "$totalDepositsCollected Kč",
    totalBalance = "$totalBalance Kč",
    totalBalanceDescription = if (totalBalance >= java.math.BigDecimal.ZERO)
        "Nájemce doplácí ${totalBalance} Kč"
    else
        "Pronajímatel vrací ${totalBalance.abs()} Kč"
)
