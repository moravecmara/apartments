package cz.rentflow.web.controller

import cz.rentflow.inflation.InflationClauseService
import cz.rentflow.security.LandlordPrincipal
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/inflation")
class InflationController(private val inflationClauseService: InflationClauseService) {

    /** Manually trigger inflation update for a given year (for testing / admin use) */
    @PostMapping("/apply")
    fun applyInflation(
        @RequestParam year: Int,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): List<Map<String, Any>> {
        val results = inflationClauseService.applyInflationUpdates(year)
        return results.map {
            mapOf(
                "contractId" to it.contractId,
                "tenantName" to it.tenantName,
                "oldRent" to it.oldRent,
                "newRent" to it.newRent,
                "inflationRate" to it.inflationRate,
                "year" to it.year
            )
        }
    }

    /** Preview new rent for a given current rent and year (no DB changes) */
    @GetMapping("/preview")
    fun previewInflation(
        @RequestParam currentRent: java.math.BigDecimal,
        @RequestParam year: Int
    ): Map<String, Any> {
        val rate = inflationClauseService.let {
            cz.rentflow.inflation.CzsoInflationClient().getAnnualInflationRate(year)
        }
        val newRent = inflationClauseService.calculateNewRent(currentRent, rate)
        return mapOf(
            "year" to year,
            "inflationRate" to rate,
            "currentRent" to currentRent,
            "newRent" to newRent
        )
    }

    /** Download inflation amendment PDF for a specific contract */
    @GetMapping("/amendment/{contractId}/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun downloadAmendmentPdf(
        @PathVariable contractId: Long,
        @RequestParam year: Int,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): ResponseEntity<ByteArray> {
        val results = inflationClauseService.applyInflationUpdates(year)
        val result = results.find { it.contractId == contractId }
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dodatek_inflace_${year}.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(result.pdfBytes)
    }
}
