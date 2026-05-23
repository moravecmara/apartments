package cz.rentflow.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "tenants")
class Tenant(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    var phone: String? = null,

    @Column(name = "bank_account_number")
    var bankAccountNumber: String? = null,

    @OneToMany(mappedBy = "tenant", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val contracts: MutableList<Contract> = mutableListOf()
)
