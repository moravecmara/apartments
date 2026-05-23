package cz.rentflow.web.controller

import cz.rentflow.banking.PaymentMatchingService
import cz.rentflow.banking.QrCodeService
import cz.rentflow.domain.service.PaymentRequest
import cz.rentflow.domain.service.PaymentResponse
import cz.rentflow.domain.service.PaymentService
import cz.rentflow.domain.repository.LandlordRepository
import cz.rentflow.security.LandlordPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
    private val qrCodeService: QrCodeService,
    private val paymentMatchingService: PaymentMatchingService,
    private val landlordRepository: LandlordRepository
) {

    @GetMapping("/contract/{contractId}")
    fun getByContract(
        @PathVariable contractId: Long,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): List<PaymentResponse> = paymentService.findByContract(contractId, principal.id)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal): PaymentResponse =
        paymentService.findById(id, principal.id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: PaymentRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): PaymentResponse = paymentService.create(request, principal.id)

    @PostMapping("/{id}/mark-paid")
    fun markAsPaid(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal): PaymentResponse =
        paymentService.markAsPaid(id, principal.id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal) =
        paymentService.delete(id, principal.id)

    /** Generate SPD QR code PNG for a payment */
    @GetMapping("/{id}/qr", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getQrCode(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): ResponseEntity<ByteArray> {
        val payment = paymentService.findById(id, principal.id)
        val landlord = landlordRepository.findById(principal.id)
            .orElseThrow { NoSuchElementException("Landlord not found") }
        val iban = landlord.bankAccountNumber
            ?: return ResponseEntity.badRequest().build()
        val png = qrCodeService.generatePaymentQrCode(
            iban = iban,
            amount = payment.amount,
            variableSymbol = payment.variableSymbol,
            message = "Najem ${payment.dueDate.month} ${payment.dueDate.year}"
        )
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(png)
    }

    /** Manually trigger bank sync (admin/testing) */
    @PostMapping("/sync")
    fun triggerSync(
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ) = if (from != null && to != null) {
        paymentMatchingService.matchTransactionsForPeriod(from, to)
    } else {
        paymentMatchingService.matchNewTransactions()
    }
}
