package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.Property
import org.springframework.data.jpa.repository.JpaRepository

interface PropertyRepository : JpaRepository<Property, Long> {
    fun findAllByLandlordId(landlordId: Long): List<Property>
    fun findByIdAndLandlordId(id: Long, landlordId: Long): Property?
}
