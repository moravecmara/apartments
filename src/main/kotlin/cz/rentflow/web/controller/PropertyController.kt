package cz.rentflow.web.controller

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.service.PropertyService
import cz.rentflow.security.LandlordPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/properties")
class PropertyController(private val propertyService: PropertyService) {

    @GetMapping
    fun getAll(@AuthenticationPrincipal principal: LandlordPrincipal): List<PropertyResponse> =
        propertyService.findAll(principal.id)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal): PropertyResponse =
        propertyService.findById(id, principal.id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: PropertyRequest, @AuthenticationPrincipal principal: LandlordPrincipal): PropertyResponse =
        propertyService.create(request, principal.id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PropertyRequest, @AuthenticationPrincipal principal: LandlordPrincipal): PropertyResponse =
        propertyService.update(id, request, principal.id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal) =
        propertyService.delete(id, principal.id)
}
