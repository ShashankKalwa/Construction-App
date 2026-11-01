-- =============================================================================
-- Migration V14: Update User Emails to Chaitanya InfraTech Domain
-- =============================================================================

UPDATE users
SET email = REPLACE(email, '@apexinfra.com', '@chaitanyainfratech.com')
WHERE email LIKE '%@apexinfra.com';
