package cz.rentflow.domain.dto

import cz.rentflow.domain.entity.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate

// --- Auth ---
data class RegisterRequest(
    @field:NotBlank val name: String,
    @field:Email val email: String,
    @field:Size(min = 8) val password: String,
    val bankAccountNumber: String? = null
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String
)

data class AuthResponse(val token: String, val email: String, val name: String)

// --- Property ---
data class PropertyRequest(
    @field:NotBlank val address: String,
    val description: String? = null
)

data class PropertyResponse(val id: Long, val address: String, val description: String?)

fun Property.toResponse() = PropertyResponse(id, address, description)

// --- Unit ---
data class UnitRequest(
    @field:NotBlank val unitNumber: String,
    val status: UnitStatus = UnitStatus.VACANT
)

data class UnitResponse(val id: Long, val propertyId: Long, val unitNumber: String, val status: UnitStatus)

fun RentalUnit.toResponse() = UnitResponse(id, property.id, unitNumber, status)

// --- Tenant ---
data class TenantRequest(
    @field:NotBlank val name: String,
    @field:Email val email: String,
    val phone: String? = null,
    val bankAccountNumber: String? = null
)

data class TenantResponse(val id: Long, val name: String, val email: String, val phone: String?, val bankAccountNumber: String?)

fun Tenant.toResponse() = TenantResponse(id, name, email, phone, bankAccountNumber)

// --- Contract ---
data class ContractRequest(
    @field:Positive val unitId: Long,
    @field:Positive val tenantId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    @field:DecimalMin("0.01") val rentAmount: BigDecimal,
    val utilitiesDeposit: BigDecimal = BigDecimal.ZERO,
    val inflationClauseEnabled: Boolean = false
)

data class ContractResponse(
    val id: Long, val unitId: Long, val tenantId: Long,
    val startDate: LocalDate, val endDate: LocalDate?,
    val rentAmount: BigDecimal, val utilitiesDeposit: BigDecimal,
    val inflationClauseEnabled: Boolean
)

fun Contract.toResponse() = ContractResponse(id, unit.id, tenant.id, startDate, endDate, rentAmount, utilitiesDeposit, inflationClauseEnabled)
