package cz.rentflow.web.ui

import cz.rentflow.domain.repository.*
import cz.rentflow.domain.entity.PaymentStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

@Controller
class UiController(
    private val landlordRepository: LandlordRepository,
    private val propertyRepository: PropertyRepository,
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository
) {

    @GetMapping("/login")
    fun login() = "login"

    @GetMapping("/", "/dashboard")
    fun dashboard(auth: Authentication, model: Model): String {
        val landlord = landlordRepository.findByEmail(auth.name).orElseThrow()
        val properties = propertyRepository.findAllByLandlordId(landlord.id)
        val units = properties.flatMap { it.units }
        val contracts = contractRepository.findAllByUnitPropertyLandlordId(landlord.id)
        val allPayments = paymentRepository.findAll().filter {
            it.contract.unit.property.landlord.id == landlord.id
        }
        val unpaidPayments = allPayments.filter { it.status == PaymentStatus.UNPAID }
        val overduePayments = unpaidPayments.filter { it.dueDate.isBefore(LocalDate.now()) }
        val monthlyRentSum = contracts
            .filter { it.endDate == null || it.endDate!!.isAfter(LocalDate.now()) }
            .sumOf { it.rentAmount }

        model.addAttribute("landlordName", landlord.name)
        model.addAttribute("propertyCount", properties.size)
        model.addAttribute("unitCount", units.size)
        model.addAttribute("occupiedCount", units.count { u ->
            u.contracts.any { c -> c.endDate == null || c.endDate!!.isAfter(LocalDate.now()) }
        })
        model.addAttribute("contractCount", contracts.size)
        model.addAttribute("unpaidCount", unpaidPayments.size)
        model.addAttribute("overdueCount", overduePayments.size)
        model.addAttribute("monthlyRent", monthlyRentSum)
        model.addAttribute("recentPayments", allPayments.sortedByDescending { it.dueDate }.take(5))
        model.addAttribute("overduePayments", overduePayments.sortedBy { it.dueDate }.take(5))
        return "dashboard"
    }

    @GetMapping("/properties")
    fun properties(auth: Authentication, model: Model): String {
        val landlord = landlordRepository.findByEmail(auth.name).orElseThrow()
        model.addAttribute("properties", propertyRepository.findAllByLandlordId(landlord.id))
        return "properties/list"
    }

    @GetMapping("/tenants")
    fun tenants(model: Model): String {
        model.addAttribute("tenants", tenantRepository.findAll())
        return "tenants/list"
    }

    @GetMapping("/payments")
    fun payments(auth: Authentication, model: Model): String {
        val landlord = landlordRepository.findByEmail(auth.name).orElseThrow()
        val payments = paymentRepository.findAll()
            .filter { it.contract.unit.property.landlord.id == landlord.id }
            .sortedByDescending { it.dueDate }
        model.addAttribute("payments", payments)
        return "payments/list"
    }
}
