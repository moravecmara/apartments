package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.Contract
import org.springframework.data.jpa.repository.JpaRepository

interface ContractRepository : JpaRepository<Contract, Long> {
    fun findAllByUnitId(unitId: Long): List<Contract>
    fun findByIdAndUnitPropertyLandlordId(id: Long, landlordId: Long): Contract?
    fun findAllByUnitPropertyLandlordId(landlordId: Long): List<Contract>
}
