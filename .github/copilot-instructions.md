# RentFlow – GitHub Copilot Instructions

## Project Overview
PropTech Micro-SaaS for small landlords in Czech Republic (1–10 rental units).
Manages properties, tenants, contracts, payments, and utility billing.

## Tech Stack
- **Language:** Kotlin (JVM 17+)
- **Framework:** Spring Boot 3.x
- **ORM:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL
- **Security:** Spring Security with JWT (stateless REST API)
- **Build:** Gradle (Kotlin DSL)
- **Frontend:** Thymeleaf + HTMX (SSR) or Vue.js SPA
- **PDF generation:** iText or Apache PDFBox
- **QR code:** ZXing library (SPD format for Czech payments)

## Architecture
- Clean architecture: Controller → Service → Repository
- Use `@Transactional` on service methods
- DTOs for API input/output (never expose JPA entities directly)
- Validation with Jakarta Bean Validation (`@Valid`, `@NotBlank`, etc.)
- Exception handling via `@ControllerAdvice`

## Coding Conventions
- Kotlin idiomatic code: data classes, extension functions, null safety
- Repository layer: Spring Data JPA interfaces
- Service layer: business logic only, no HTTP concerns
- REST: standard HTTP status codes, JSON responses
- Use `snake_case` for database columns, `camelCase` for Kotlin properties
- Tests: JUnit 5 + MockK for unit tests, Testcontainers for integration tests

## Domain Model
- `Landlord` → owns many `Property`
- `Property` → has many `Unit`
- `Unit` → has many `Contract`, `UtilityReading`
- `Contract` → belongs to `Unit` + `Tenant`, has many `Payment`
- `Payment` → has `variable_symbol`, `status` (Unpaid/Paid/Partially_Paid), `type` (Rent/Utility)

## Czech-specific Requirements
- Variable symbol format: `RRRRMMUU` (year + month + unit number)
- QR payment: SPD (Short Payment Descriptor) format
- Inflation index: fetched from ČSÚ (Czech Statistical Office) API
- Bank integration: Fio banka API or Air Bank API

## Security
- JWT-based authentication (stateless)
- Each landlord sees only their own data (row-level security via service layer)
- Passwords hashed with BCrypt

## What to Avoid
- No Lombok (use Kotlin data classes instead)
- No field injection (`@Autowired` on fields) — use constructor injection
- No raw SQL unless absolutely necessary — prefer JPQL or Spring Data methods
