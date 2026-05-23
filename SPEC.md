# Specifikace projektu: PropTech Micro-SaaS "RentFlow" (v1.0)

## 1. Architektura a Kontext Aplikace
* **Cílová skupina:** Malí pronajímatelé v ČR (vlastnící 1–10 bytových jednotek).
* **Architektura:** Monolitická webová aplikace s čistou architekturou (Separation of Concerns).
* **Backend:** Kotlin / Java (REST API, zabezpečení přes JWT/OAuth2).
* **Frontend:** Responzivní web (Server-Side Rendering s Thymeleaf/HTMX nebo jednoduché SPA).
* **Databáze:** PostgreSQL.

---

## 2. Datový Model (Core Entity)
* **Landlord (Majitel):** `id`, `name`, `email`, `password_hash`, `bank_account_number`
* **Property (Nemovitost):** `id`, `landlord_id`, `address`, `description`
* **Unit (Bytová jednotka):** `id`, `property_id`, `unit_number`, `status` (Vacant / Occupied)
* **Tenant (Nájemce):** `id`, `name`, `email`, `phone`, `bank_account_number`
* **Contract (Smlouva):** `id`, `unit_id`, `tenant_id`, `start_date`, `end_date`, `rent_amount`, `utilities_deposit`, `inflation_clause_enabled` (boolean)
* **Payment (Platba):** `id`, `contract_id`, `amount`, `due_date`, `received_date`, `type` (Rent / Utility), `status` (Unpaid / Paid / Partially_Paid), `variable_symbol`
* **UtilityReading (Stav energií):** `id`, `unit_id`, `reading_date`, `utility_type` (Electricity / Gas / Water), `value`

---

## 3. Funkční Celky k Implementaci (Iterace pro vývoj)

### Fáze 1: CRUD a Správa Nemovitostí (Základ)
* **Zadání:** Vytvořit bezpečný autentizační systém (Registrace/Přihlášení majitele). Implementovat kompletní CRUD operace pro Nemovitosti, Jednotky, Nájemce a Smlouvy.
* **Lokální specifikum:** Automatické přiřazení unikátního **Variabilního symbolu** (např. formát `RRRRMMUU` - rok, měsíc, číslo jednotky) pro platby.

### Fáze 2: Automatické párování plateb (Bankovní API)
* **Zadání:** Vytvořit asynchronní službu (Cron job) pro denní stahování bankovních výpisů přes otevřené API (např. Fio banka / Air Bank).
* **Logika párování:** Matchování příchozích plateb podle `variable_symbol` a `amount`. Pokud platba chybí 3 dny po `due_date`, vygenerovat upozornění s platebním **QR kódem** (formát `SPD - Short Payment Descriptor`).

### Fáze 3: Inflační doložky a legislativa (ČSÚ integrace)
* **Zadání:** Vytvořit modul, který v lednu zkontroluje smlouvy se zapnutým `inflation_clause_enabled`.
* **Logika:** Stažení oficiální míry inflace z webu Českého statistického úřadu (ČSÚ). Automatický přepočet `rent_amount`, generování PDF dodatku a odeslání majiteli ke schválení.

### Fáze 4: Vyúčtování energií na jeden klik
* **Zadání:** Kalkulační engine pro roční zúčtování záloh na základě počátečního a konečného stavu elektroměru/plynoměru (`UtilityReading`). Porovnání se zaplacenými zálohami a export do legálně čistého PDF "Vyúčtování služeb a energií".

---

## 4. Příklady promptů pro GitHub Copilot CLI

### Inicializace datového modelu (Kotlin/JPA):
```bash
gh copilot suggest "Create JPA entities for RentFlow application in Kotlin: Landlord, Property, Unit, Tenant, Contract, Payment, and UtilityReading with appropriate OneToMany and ManyToOne relationships based on standard database design."
```
