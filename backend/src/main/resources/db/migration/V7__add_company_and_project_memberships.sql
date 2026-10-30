-- =============================================================================
-- Migration V7: Company Memberships, Invitations & Enhanced Project Memberships
-- =============================================================================

-- 1. Company Memberships Table
CREATE TABLE IF NOT EXISTS company_memberships (
    id VARCHAR(50) PRIMARY KEY,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INVITED, SUSPENDED
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_org_user UNIQUE (organization_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_comp_mem_org ON company_memberships(organization_id);
CREATE INDEX IF NOT EXISTS idx_comp_mem_user ON company_memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_comp_mem_role ON company_memberships(role_id);
CREATE INDEX IF NOT EXISTS idx_comp_mem_status ON company_memberships(status);

-- 2. Invitations Table
CREATE TABLE IF NOT EXISTS invitations (
    id VARCHAR(50) PRIMARY KEY,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id),
    invited_by VARCHAR(50) NOT NULL REFERENCES users(id),
    token VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, EXPIRED, REVOKED
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_invitations_org ON invitations(organization_id);
CREATE INDEX IF NOT EXISTS idx_invitations_email ON invitations(email);
CREATE INDEX IF NOT EXISTS idx_invitations_token ON invitations(token);
CREATE INDEX IF NOT EXISTS idx_invitations_status ON invitations(status);

-- 3. Enhance Project Members Table
ALTER TABLE project_members ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE';
ALTER TABLE project_members ADD COLUMN IF NOT EXISTS company_id VARCHAR(50) REFERENCES organizations(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_project_members_company ON project_members(company_id);
CREATE INDEX IF NOT EXISTS idx_project_members_status ON project_members(status);

-- 4. Backfill Project Members company_id from parent project organization_id
UPDATE project_members pm
SET company_id = p.organization_id
FROM projects p
WHERE pm.project_id = p.id AND pm.company_id IS NULL;

-- 5. Backfill Company Memberships from existing Users
INSERT INTO company_memberships (id, organization_id, user_id, role_id, status, joined_at)
SELECT 
    'cm_' || SUBSTRING(MD5(u.id || '_' || u.organization_id), 1, 16),
    u.organization_id,
    u.id,
    u.role_id,
    'ACTIVE',
    u.created_at
FROM users u
ON CONFLICT (organization_id, user_id) DO NOTHING;
