package cz.rentflow.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "landlords")
class Landlord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "bank_account_number")
    var bankAccountNumber: String? = null,

    @OneToMany(mappedBy = "landlord", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val properties: MutableList<Property> = mutableListOf()
)
