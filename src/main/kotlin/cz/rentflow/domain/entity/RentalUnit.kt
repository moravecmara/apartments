package cz.rentflow.domain.entity

import jakarta.persistence.*

enum class UnitStatus { VACANT, OCCUPIED }

@Entity
@Table(name = "units")
class RentalUnit(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    var property: Property,

    @Column(name = "unit_number", nullable = false)
    var unitNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UnitStatus = UnitStatus.VACANT,

    @OneToMany(mappedBy = "unit", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val contracts: MutableList<Contract> = mutableListOf(),

    @OneToMany(mappedBy = "unit", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val utilityReadings: MutableList<UtilityReading> = mutableListOf()
)
