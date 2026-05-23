package cz.rentflow.domain.service

import cz.rentflow.domain.dto.*
import cz.rentflow.domain.entity.Landlord
import cz.rentflow.domain.repository.LandlordRepository
import cz.rentflow.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val landlordRepository: LandlordRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        require(!landlordRepository.existsByEmail(request.email)) {
            "Email ${request.email} is already registered"
        }
        val landlord = Landlord(
            name = request.name,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            bankAccountNumber = request.bankAccountNumber
        )
        landlordRepository.save(landlord)
        val token = jwtTokenProvider.generateToken(landlord.email, landlord.id)
        return AuthResponse(token, landlord.email, landlord.name)
    }

    fun login(request: LoginRequest): AuthResponse {
        val landlord = landlordRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Invalid email or password") }
        require(passwordEncoder.matches(request.password, landlord.passwordHash)) {
            "Invalid email or password"
        }
        val token = jwtTokenProvider.generateToken(landlord.email, landlord.id)
        return AuthResponse(token, landlord.email, landlord.name)
    }
}
