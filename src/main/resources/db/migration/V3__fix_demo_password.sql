-- Fix demo account password hash (correct BCrypt hash for "Demo1234")
UPDATE landlords
SET password_hash = '$2b$12$U8TfjBxU5oHsLmIlAZfDcehwpwjjr92UoJbaGpJvXjsw/H/sEEKxW'
WHERE email = 'demo@rentflow.cz';
