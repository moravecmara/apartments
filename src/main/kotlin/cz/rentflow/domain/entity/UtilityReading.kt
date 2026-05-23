package cz.rentflow.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

enum class UtilityType { ELECTRICITY, GAS, WATER }

@Entity
@Table(name = "utility_readings")
class UtilityReading(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    var unit: RentalUnit,

    @Column(name = "reading_date", nullable = false)
    var readingDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false)
    var utilityType: UtilityType,

    @Column(nullable = false, precision = 10, scale = 3)
    var value: BigDecimal
)
