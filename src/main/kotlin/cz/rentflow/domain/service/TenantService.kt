package cz.rentflow.domain.service

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.entity.Tenant
import cz.rentflow.domain.repository.TenantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TenantService(private val tenantRepository: TenantRepository) {

    fun findAll(): List<TenantResponse> =
        tenantRepository.findAll().map { it.toResponse() }

    fun findById(id: Long): TenantResponse =
        tenantRepository.findById(id).orElseThrow { NoSuchElementException("Tenant $id not found") }.toResponse()

    @Transactional
    fun create(request: TenantRequest): TenantResponse {
        require(!tenantRepository.existsByEmail(request.email)) { "Email already used" }
        val tenant = Tenant(name = request.name, email = request.email, phone = request.phone, bankAccountNumber = request.bankAccountNumber)
        return tenantRepository.save(tenant).toResponse()
    }

    @Transactional
    fun update(id: Long, request: TenantRequest): TenantResponse {
        val tenant = tenantRepository.findById(id).orElseThrow { NoSuchElementException("Tenant $id not found") }
        tenant.name = request.name
        tenant.email = request.email
        tenant.phone = request.phone
        tenant.bankAccountNumber = request.bankAccountNumber
        return tenantRepository.save(tenant).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val tenant = tenantRepository.findById(id).orElseThrow { NoSuchElementException("Tenant $id not found") }
        tenantRepository.delete(tenant)
    }
}
