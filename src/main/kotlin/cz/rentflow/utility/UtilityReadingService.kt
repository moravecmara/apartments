package cz.rentflow.utility

import cz.rentflow.domain.entity.UtilityReading
import cz.rentflow.domain.entity.UtilityType
import cz.rentflow.domain.repository.UnitRepository
import cz.rentflow.domain.repository.UtilityReadingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

data class UtilityReadingRequest(
    val readingDate: LocalDate,
    val utilityType: UtilityType,
    val value: BigDecimal
)

data class UtilityReadingResponse(
    val id: Long,
    val unitId: Long,
    val readingDate: LocalDate,
    val utilityType: UtilityType,
    val value: BigDecimal
)

fun UtilityReading.toResponse() = UtilityReadingResponse(id, unit.id, readingDate, utilityType, value)

@Service
class UtilityReadingService(
    private val utilityReadingRepository: UtilityReadingRepository,
    private val unitRepository: UnitRepository
) {

    fun findAll(unitId: Long): List<UtilityReadingResponse> =
        utilityReadingRepository.findAllByUnitId(unitId).map { it.toResponse() }

    fun findById(id: Long, landlordId: Long): UtilityReadingResponse =
        getOwned(id, landlordId).toResponse()

    @Transactional
    fun create(unitId: Long, request: UtilityReadingRequest, landlordId: Long): UtilityReadingResponse {
        val unit = unitRepository.findByIdAndPropertyLandlordId(unitId, landlordId)
            ?: throw NoSuchElementException("Unit $unitId not found")
        val reading = UtilityReading(
            unit = unit,
            readingDate = request.readingDate,
            utilityType = request.utilityType,
            value = request.value
        )
        return utilityReadingRepository.save(reading).toResponse()
    }

    @Transactional
    fun update(id: Long, request: UtilityReadingRequest, landlordId: Long): UtilityReadingResponse {
        val reading = getOwned(id, landlordId)
        reading.readingDate = request.readingDate
        reading.utilityType = request.utilityType
        reading.value = request.value
        return utilityReadingRepository.save(reading).toResponse()
    }

    @Transactional
    fun delete(id: Long, landlordId: Long) {
        utilityReadingRepository.delete(getOwned(id, landlordId))
    }

    private fun getOwned(id: Long, landlordId: Long) =
        utilityReadingRepository.findByIdAndUnitPropertyLandlordId(id, landlordId)
            ?: throw NoSuchElementException("UtilityReading $id not found")
}
