package cz.rentflow.web.controller

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.service.TenantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants")
class TenantController(private val tenantService: TenantService) {

    @GetMapping
    fun getAll(): List<TenantResponse> = tenantService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): TenantResponse = tenantService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: TenantRequest): TenantResponse = tenantService.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: TenantRequest): TenantResponse =
        tenantService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = tenantService.delete(id)
}
