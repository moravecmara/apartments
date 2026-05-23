package cz.rentflow.domain.service

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.entity.Property
import cz.rentflow.domain.repository.LandlordRepository
import cz.rentflow.domain.repository.PropertyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PropertyService(
    private val propertyRepository: PropertyRepository,
    private val landlordRepository: LandlordRepository
) {

    fun findAll(landlordId: Long): List<PropertyResponse> =
        propertyRepository.findAllByLandlordId(landlordId).map { it.toResponse() }

    fun findById(id: Long, landlordId: Long): PropertyResponse =
        getOwnedProperty(id, landlordId).toResponse()

    @Transactional
    fun create(request: PropertyRequest, landlordId: Long): PropertyResponse {
        val landlord = landlordRepository.getReferenceById(landlordId)
        val property = Property(landlord = landlord, address = request.address, description = request.description)
        return propertyRepository.save(property).toResponse()
    }

    @Transactional
    fun update(id: Long, request: PropertyRequest, landlordId: Long): PropertyResponse {
        val property = getOwnedProperty(id, landlordId)
        property.address = request.address
        property.description = request.description
        return propertyRepository.save(property).toResponse()
    }

    @Transactional
    fun delete(id: Long, landlordId: Long) {
        val property = getOwnedProperty(id, landlordId)
        propertyRepository.delete(property)
    }

    private fun getOwnedProperty(id: Long, landlordId: Long) =
        propertyRepository.findByIdAndLandlordId(id, landlordId)
            ?: throw NoSuchElementException("Property $id not found")
}
