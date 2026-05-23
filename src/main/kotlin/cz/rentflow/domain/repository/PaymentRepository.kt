package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findAllByContractId(contractId: Long): List<Payment>
    fun findByVariableSymbol(variableSymbol: String): Optional<Payment>
    fun findByIdAndContractUnitPropertyLandlordId(id: Long, landlordId: Long): Payment?
}
