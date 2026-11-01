-- =============================================================================
-- Migration V13: Rename default organization to Chaitanya InfraTech
-- =============================================================================

UPDATE organizations
SET name = 'Chaitanya InfraTech'
WHERE id = 'org_apex_infra' OR slug = 'apex-infra' OR name LIKE 'Apex%';
