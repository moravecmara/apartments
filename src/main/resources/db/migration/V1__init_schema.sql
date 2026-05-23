CREATE TABLE landlords (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    bank_account_number VARCHAR(50)
);

CREATE TABLE properties (
    id          BIGSERIAL PRIMARY KEY,
    landlord_id BIGINT NOT NULL REFERENCES landlords(id) ON DELETE CASCADE,
    address     VARCHAR(500) NOT NULL,
    description TEXT
);

CREATE TABLE units (
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    unit_number VARCHAR(50) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'VACANT'
);

CREATE TABLE tenants (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    phone               VARCHAR(50),
    bank_account_number VARCHAR(50)
);

CREATE TABLE contracts (
    id                       BIGSERIAL PRIMARY KEY,
    unit_id                  BIGINT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    tenant_id                BIGINT NOT NULL REFERENCES tenants(id),
    start_date               DATE NOT NULL,
    end_date                 DATE,
    rent_amount              NUMERIC(10,2) NOT NULL,
    utilities_deposit        NUMERIC(10,2) NOT NULL DEFAULT 0,
    inflation_clause_enabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    amount          NUMERIC(10,2) NOT NULL,
    due_date        DATE NOT NULL,
    received_date   DATE,
    type            VARCHAR(20) NOT NULL DEFAULT 'RENT',
    status          VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    variable_symbol VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE utility_readings (
    id           BIGSERIAL PRIMARY KEY,
    unit_id      BIGINT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    reading_date DATE NOT NULL,
    utility_type VARCHAR(20) NOT NULL,
    value        NUMERIC(10,3) NOT NULL
);
