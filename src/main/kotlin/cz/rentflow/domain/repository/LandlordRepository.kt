package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.Landlord
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface LandlordRepository : JpaRepository<Landlord, Long> {
    fun findByEmail(email: String): Optional<Landlord>
    fun existsByEmail(email: String): Boolean
}
