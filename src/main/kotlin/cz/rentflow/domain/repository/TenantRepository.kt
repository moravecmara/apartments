package cz.rentflow.domain.repository

import cz.rentflow.domain.entity.Tenant
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface TenantRepository : JpaRepository<Tenant, Long> {
    fun findByEmail(email: String): Optional<Tenant>
    fun existsByEmail(email: String): Boolean
}
