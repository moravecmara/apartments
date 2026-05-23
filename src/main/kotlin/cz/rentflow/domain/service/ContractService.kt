package cz.rentflow.domain.service

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.entity.Contract
import cz.rentflow.domain.repository.ContractRepository
import cz.rentflow.domain.repository.TenantRepository
import cz.rentflow.domain.repository.UnitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContractService(
    private val contractRepository: ContractRepository,
    private val unitRepository: UnitRepository,
    private val tenantRepository: TenantRepository
) {

    fun findAll(landlordId: Long): List<ContractResponse> =
        contractRepository.findAllByUnitPropertyLandlordId(landlordId).map { it.toResponse() }

    fun findById(id: Long, landlordId: Long): ContractResponse =
        getOwnedContract(id, landlordId).toResponse()

    @Transactional
    fun create(request: ContractRequest, landlordId: Long): ContractResponse {
        val unit = unitRepository.findByIdAndPropertyLandlordId(request.unitId, landlordId)
            ?: throw NoSuchElementException("Unit ${request.unitId} not found")
        val tenant = tenantRepository.findById(request.tenantId)
            .orElseThrow { NoSuchElementException("Tenant ${request.tenantId} not found") }
        val contract = Contract(
            unit = unit, tenant = tenant,
            startDate = request.startDate, endDate = request.endDate,
            rentAmount = request.rentAmount, utilitiesDeposit = request.utilitiesDeposit,
            inflationClauseEnabled = request.inflationClauseEnabled
        )
        return contractRepository.save(contract).toResponse()
    }

    @Transactional
    fun update(id: Long, request: ContractRequest, landlordId: Long): ContractResponse {
        val contract = getOwnedContract(id, landlordId)
        contract.startDate = request.startDate
        contract.endDate = request.endDate
        contract.rentAmount = request.rentAmount
        contract.utilitiesDeposit = request.utilitiesDeposit
        contract.inflationClauseEnabled = request.inflationClauseEnabled
        return contractRepository.save(contract).toResponse()
    }

    @Transactional
    fun delete(id: Long, landlordId: Long) {
        val contract = getOwnedContract(id, landlordId)
        contractRepository.delete(contract)
    }

    private fun getOwnedContract(id: Long, landlordId: Long) =
        contractRepository.findByIdAndUnitPropertyLandlordId(id, landlordId)
            ?: throw NoSuchElementException("Contract $id not found")
}
