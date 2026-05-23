package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.UtilityReading
import cz.rentflow.domain.entity.UtilityType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface UtilityReadingRepository : JpaRepository<UtilityReading, Long> {
    fun findAllByUnitId(unitId: Long): List<UtilityReading>
    fun findAllByUnitIdAndUtilityType(unitId: Long, utilityType: UtilityType): List<UtilityReading>
    fun findAllByUnitIdAndReadingDateBetween(unitId: Long, from: LocalDate, to: LocalDate): List<UtilityReading>
    fun findByIdAndUnitPropertyLandlordId(id: Long, landlordId: Long): UtilityReading?
    fun findAllByUnitIdAndUtilityTypeAndReadingDateBetween(
        unitId: Long, utilityType: UtilityType, from: LocalDate, to: LocalDate
    ): List<UtilityReading>
}
