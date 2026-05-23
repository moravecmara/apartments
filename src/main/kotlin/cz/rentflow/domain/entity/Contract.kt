package cz.rentflow.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "contracts")
class Contract(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    var unit: RentalUnit,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    var tenant: Tenant,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Column(name = "rent_amount", nullable = false, precision = 10, scale = 2)
    var rentAmount: BigDecimal,

    @Column(name = "utilities_deposit", precision = 10, scale = 2)
    var utilitiesDeposit: BigDecimal = BigDecimal.ZERO,

    @Column(name = "inflation_clause_enabled", nullable = false)
    var inflationClauseEnabled: Boolean = false,

    @OneToMany(mappedBy = "contract", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payments: MutableList<Payment> = mutableListOf()
)
