package cz.rentflow.web.controller

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.service.UnitService
import cz.rentflow.security.LandlordPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/properties/{propertyId}/units")
class UnitController(private val unitService: UnitService) {

    @GetMapping
    fun getAll(@PathVariable propertyId: Long): List<UnitResponse> =
        unitService.findAll(propertyId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable propertyId: Long,
        @Valid @RequestBody request: UnitRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): UnitResponse = unitService.create(propertyId, request, principal.id)

    @PutMapping("/{id}")
    fun update(
        @PathVariable propertyId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: UnitRequest,
        @AuthenticationPrincipal principal: LandlordPrincipal
    ): UnitResponse = unitService.update(id, request, principal.id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal principal: LandlordPrincipal) =
        unitService.delete(id, principal.id)
}
