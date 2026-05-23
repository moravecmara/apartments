package cz.rentflow.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "properties")
class Property(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    var landlord: Landlord,

    @Column(nullable = false)
    var address: String,

    var description: String? = null,

    @OneToMany(mappedBy = "property", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val units: MutableList<RentalUnit> = mutableListOf()
)
