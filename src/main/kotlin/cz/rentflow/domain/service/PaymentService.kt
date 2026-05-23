package cz.rentflow.domain.service

import cz.rentflow.domain.entity.Payment
import cz.rentflow.domain.entity.PaymentStatus
import cz.rentflow.domain.entity.PaymentType
import cz.rentflow.domain.repository.ContractRepository
import cz.rentflow.domain.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

data class PaymentRequest(
    val contractId: Long,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val type: PaymentType = PaymentType.RENT
)

data class PaymentResponse(
    val id: Long,
    val contractId: Long,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val receivedDate: LocalDate?,
    val type: PaymentType,
    val status: PaymentStatus,
    val variableSymbol: String
)

fun Payment.toResponse() = PaymentResponse(
    id, contract.id, amount, dueDate, receivedDate, type, status, variableSymbol
)

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val contractRepository: ContractRepository
) {

    fun findByContract(contractId: Long, landlordId: Long): List<PaymentResponse> {
        // Verify landlord owns this contract
        contractRepository.findByIdAndUnitPropertyLandlordId(contractId, landlordId)
            ?: throw NoSuchElementException("Contract $contractId not found")
        return paymentRepository.findAllByContractId(contractId).map { it.toResponse() }
    }

    fun findById(id: Long, landlordId: Long): PaymentResponse =
        getOwnedPayment(id, landlordId).toResponse()

    @Transactional
    fun create(request: PaymentRequest, landlordId: Long): PaymentResponse {
        val contract = contractRepository.findByIdAndUnitPropertyLandlordId(request.contractId, landlordId)
            ?: throw NoSuchElementException("Contract ${request.contractId} not found")
        val vs = generateVariableSymbol(request.dueDate, contract.unit.id)
        val payment = Payment(
            contract = contract,
            amount = request.amount,
            dueDate = request.dueDate,
            type = request.type,
            variableSymbol = vs
        )
        return paymentRepository.save(payment).toResponse()
    }

    @Transactional
    fun markAsPaid(id: Long, landlordId: Long): PaymentResponse {
        val payment = getOwnedPayment(id, landlordId)
        payment.status = PaymentStatus.PAID
        payment.receivedDate = LocalDate.now()
        return paymentRepository.save(payment).toResponse()
    }

    @Transactional
    fun delete(id: Long, landlordId: Long) {
        val payment = getOwnedPayment(id, landlordId)
        paymentRepository.delete(payment)
    }

    private fun getOwnedPayment(id: Long, landlordId: Long) =
        paymentRepository.findByIdAndContractUnitPropertyLandlordId(id, landlordId)
            ?: throw NoSuchElementException("Payment $id not found")

    /**
     * Variable symbol format: YYYYMMuu (year + month + unit id, zero-padded to 10 digits max)
     */
    private fun generateVariableSymbol(dueDate: LocalDate, unitId: Long): String {
        val base = "${dueDate.year}${dueDate.monthValue.toString().padStart(2, '0')}${unitId.toString().padStart(4, '0')}"
        // Ensure uniqueness by checking DB and appending counter if needed
        var vs = base
        var counter = 1
        while (paymentRepository.findByVariableSymbol(vs).isPresent) {
            vs = base + counter.toString().padStart(2, '0')
            counter++
        }
        return vs
    }
}
