package cz.rentflow.web.controller

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.service.ContractService
import cz.rentflow.security.LandlordPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contracts")
class ContractController(private val contractService: ContractService) {

    @GetMapping
    fun getAll(@AuthenticationPrincipal principal: LandlordPrincipal): List<ContractResponse> =
        contractService.findAll(principal.id)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal): ContractResponse =
        contractService.findById(id, principal.id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: ContractRequest, @AuthenticationPrincipal principal: LandlordPrincipal): ContractResponse =
        contractService.create(request, principal.id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: ContractRequest, @AuthenticationPrincipal principal: LandlordPrincipal): ContractResponse =
        contractService.update(id, request, principal.id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal) =
        contractService.delete(id, principal.id)
}
