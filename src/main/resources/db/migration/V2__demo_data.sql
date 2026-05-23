-- Demo data for RentFlow
-- Login: demo@rentflow.cz / Demo1234 (BCrypt hash)

INSERT INTO landlords (name, email, password_hash, bank_account_number) VALUES
('Jan Novák (Demo)', 'demo@rentflow.cz',
 '$2a$12$WK3lVCLXOJi5Uu3HXmZ0e.z3gHi6jVtxBX6fNjkQ8PJqFjJdS2TZy',
 'CZ6508000000192000145399');

INSERT INTO properties (landlord_id, address, description) VALUES
(1, 'Václavské náměstí 1, Praha 1', 'Historický byt v centru Prahy'),
(1, 'Brněnská 42, Brno-střed', 'Panelový dům, dobré MHD spojení');

INSERT INTO units (property_id, unit_number, status) VALUES
(1, '2B', 'OCCUPIED'),
(1, '3A', 'OCCUPIED'),
(2, '101', 'OCCUPIED'),
(2, '204', 'VACANT');

INSERT INTO tenants (name, email, phone, bank_account_number) VALUES
('Petra Horáková', 'petra.horakova@email.cz', '+420 777 123 456', 'CZ5503000000000123456789'),
('Martin Dvořák', 'martin.dvorak@email.cz', '+420 602 987 654', 'CZ2601000000001234567890'),
('Lucie Marková', 'lucie.markova@email.cz', '+420 731 456 789', NULL);

INSERT INTO contracts (unit_id, tenant_id, start_date, end_date, rent_amount, utilities_deposit, inflation_clause_enabled) VALUES
(1, 1, '2023-03-01', NULL,   18500.00, 2500.00, true),
(2, 2, '2022-09-01', NULL,   16000.00, 2000.00, false),
(3, 3, '2024-01-01', NULL,   12500.00, 1800.00, true);

-- Payments: contract 1 (Petra - byt 2B Praha)
INSERT INTO payments (contract_id, amount, due_date, received_date, type, status, variable_symbol) VALUES
(1, 18500.00, '2025-01-01', '2025-01-03', 'RENT', 'PAID',          '2025010001'),
(1, 18500.00, '2025-02-01', '2025-02-02', 'RENT', 'PAID',          '2025020001'),
(1, 18500.00, '2025-03-01', '2025-03-04', 'RENT', 'PAID',          '2025030001'),
(1, 18500.00, '2025-04-01', '2025-04-01', 'RENT', 'PAID',          '2025040001'),
(1, 18500.00, '2025-05-01', NULL,         'RENT', 'UNPAID',         '2025050001'),
(1,  2500.00, '2025-01-01', '2025-01-03', 'UTILITY', 'PAID',       '2025010011'),
(1,  2500.00, '2025-05-01', NULL,         'UTILITY', 'UNPAID',     '2025050011');

-- Payments: contract 2 (Martin - byt 3A Praha)
INSERT INTO payments (contract_id, amount, due_date, received_date, type, status, variable_symbol) VALUES
(2, 16000.00, '2025-01-01', '2025-01-05', 'RENT', 'PAID',          '2025010002'),
(2, 16000.00, '2025-02-01', '2025-02-07', 'RENT', 'PAID',          '2025020002'),
(2, 16000.00, '2025-03-01', NULL,         'RENT', 'PARTIALLY_PAID','2025030002'),
(2, 16000.00, '2025-04-01', NULL,         'RENT', 'UNPAID',         '2025040002'),
(2, 16000.00, '2025-05-01', NULL,         'RENT', 'UNPAID',         '2025050002');

-- Payments: contract 3 (Lucie - Brno)
INSERT INTO payments (contract_id, amount, due_date, received_date, type, status, variable_symbol) VALUES
(3, 12500.00, '2025-01-01', '2025-01-02', 'RENT', 'PAID',          '2025010003'),
(3, 12500.00, '2025-02-01', '2025-02-03', 'RENT', 'PAID',          '2025020003'),
(3, 12500.00, '2025-03-01', '2025-03-01', 'RENT', 'PAID',          '2025030003'),
(3, 12500.00, '2025-04-01', '2025-04-02', 'RENT', 'PAID',          '2025040003'),
(3, 12500.00, '2025-05-01', NULL,         'RENT', 'UNPAID',         '2025050003');

-- Utility readings: unit 1 (Praha 2B)
INSERT INTO utility_readings (unit_id, reading_date, utility_type, value) VALUES
(1, '2024-12-31', 'ELECTRICITY', 12345.000),
(1, '2025-05-01', 'ELECTRICITY', 13102.500),
(1, '2024-12-31', 'GAS',         8901.200),
(1, '2025-05-01', 'GAS',         9340.800),
(1, '2024-12-31', 'WATER',        412.750),
(1, '2025-05-01', 'WATER',        498.200);
