package cz.rentflow.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

enum class PaymentType { RENT, UTILITY }
enum class PaymentStatus { UNPAID, PAID, PARTIALLY_PAID }

@Entity
@Table(name = "payments")
class Payment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    var contract: Contract,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,

    @Column(name = "due_date", nullable = false)
    var dueDate: LocalDate,

    @Column(name = "received_date")
    var receivedDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: PaymentType = PaymentType.RENT,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.UNPAID,

    @Column(name = "variable_symbol", nullable = false, unique = true, length = 10)
    var variableSymbol: String
)
