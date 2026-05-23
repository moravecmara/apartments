package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.Payment
import cz.rentflow.domain.entity.PaymentStatus
import cz.rentflow.domain.entity.PaymentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findAllByContractId(contractId: Long): List<Payment>
    fun findByVariableSymbol(variableSymbol: String): Optional<Payment>
    fun findByIdAndContractUnitPropertyLandlordId(id: Long, landlordId: Long): Payment?

    @Query("""
        SELECT p FROM Payment p
        WHERE p.contract.unit.id = :unitId
          AND p.type = :type
          AND p.status = :status
          AND p.dueDate BETWEEN :from AND :to
    """)
    fun findByUnitIdAndTypeAndStatusAndPeriod(
        unitId: Long,
        type: PaymentType,
        status: PaymentStatus,
        from: LocalDate,
        to: LocalDate
    ): List<Payment>
}
