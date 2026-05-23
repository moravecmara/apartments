package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.RentalUnit
import org.springframework.data.jpa.repository.JpaRepository

interface UnitRepository : JpaRepository<RentalUnit, Long> {
    fun findAllByPropertyId(propertyId: Long): List<RentalUnit>
    fun findByIdAndPropertyLandlordId(id: Long, landlordId: Long): RentalUnit?
}
