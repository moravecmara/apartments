package cz.rentflow.domain.service

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.entity.RentalUnit
import cz.rentflow.domain.repository.PropertyRepository
import cz.rentflow.domain.repository.UnitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UnitService(
    private val unitRepository: UnitRepository,
    private val propertyRepository: PropertyRepository
) {

    fun findAll(propertyId: Long): List<UnitResponse> =
        unitRepository.findAllByPropertyId(propertyId).map { it.toResponse() }

    fun findById(id: Long, landlordId: Long): UnitResponse =
        getOwnedUnit(id, landlordId).toResponse()

    @Transactional
    fun create(propertyId: Long, request: UnitRequest, landlordId: Long): UnitResponse {
        val property = propertyRepository.findByIdAndLandlordId(propertyId, landlordId)
            ?: throw NoSuchElementException("Property $propertyId not found")
        val unit = RentalUnit(property = property, unitNumber = request.unitNumber, status = request.status)
        return unitRepository.save(unit).toResponse()
    }

    @Transactional
    fun update(id: Long, request: UnitRequest, landlordId: Long): UnitResponse {
        val unit = getOwnedUnit(id, landlordId)
        unit.unitNumber = request.unitNumber
        unit.status = request.status
        return unitRepository.save(unit).toResponse()
    }

    @Transactional
    fun delete(id: Long, landlordId: Long) {
        val unit = getOwnedUnit(id, landlordId)
        unitRepository.delete(unit)
    }

    private fun getOwnedUnit(id: Long, landlordId: Long) =
        unitRepository.findByIdAndPropertyLandlordId(id, landlordId)
            ?: throw NoSuchElementException("Unit $id not found")
}
