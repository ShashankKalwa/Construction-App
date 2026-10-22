-- =====================================================================
-- CMS PostgreSQL Initial Schema & Seed Data (Idempotent for Supabase & Flyway)
-- =====================================================================

-- 1. Organizations
CREATE TABLE IF NOT EXISTS organizations (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Permissions
CREATE TABLE IF NOT EXISTS permissions (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL
);

-- 3. Roles
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 4. Role Permissions Mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(100) NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 5. Users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_users_org ON users(organization_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role_id);

-- 6. Projects
CREATE TABLE IF NOT EXISTS projects (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    budget_inr NUMERIC(14, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'Active',
    progress_pct DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_projects_org ON projects(organization_id);

-- 7. Project Members
CREATE TABLE IF NOT EXISTS project_members (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_role VARCHAR(50),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_project_members_prj ON project_members(project_id);
CREATE INDEX IF NOT EXISTS idx_project_members_usr ON project_members(user_id);

-- =====================================================================
-- SEED DATA
-- =====================================================================

-- Seed standard permissions
INSERT INTO permissions (id, name, module) VALUES
('projects:view', 'View Projects', 'projects'),
('projects:create', 'Create Projects', 'projects'),
('projects:edit', 'Edit Projects', 'projects'),
('tasks:view', 'View Tasks', 'tasks'),
('tasks:manage', 'Manage Tasks', 'tasks'),
('tasks:update', 'Update Task Progress', 'tasks'),
('calculators:concrete', 'Use Concrete Calculator', 'calculators'),
('calculators:steel', 'Use Steel Calculator', 'calculators'),
('measurements:take', 'Take Measurements', 'measurements'),
('boq:view', 'View BOQ', 'boq'),
('boq:edit', 'Edit BOQ', 'boq'),
('boq:approve', 'Approve BOQ', 'boq'),
('inventory:view', 'View Inventory', 'inventory'),
('inventory:request', 'Request Materials', 'inventory'),
('inventory:issue', 'Issue Stock', 'inventory'),
('finance:view', 'View Financials', 'finance'),
('expenses:submit', 'Submit Expenses', 'finance'),
('expenses:approve', 'Approve Expenses', 'finance'),
('reports:daily_site', 'Write Daily Site Report', 'reports'),
('audit:view', 'View Audit Logs', 'audit')
ON CONFLICT (id) DO NOTHING;

-- Seed 9 CMS Roles
INSERT INTO roles (id, name, description) VALUES
('super_admin', 'Super Admin', 'Full cross-organization control'),
('company_admin', 'Company Admin', 'Organization-wide administrative control'),
('project_manager', 'Project Manager', 'Project delivery, milestones, approvals, budget'),
('civil_engineer', 'Civil Engineer', 'Calculations, takeoff, BOQ, drawings'),
('site_supervisor', 'Site Supervisor', 'Daily execution, reports, labor, requests'),
('storekeeper', 'Storekeeper', 'Inventory, goods received and issued, stock balances'),
('accountant', 'Accountant', 'Expenses, contractor bills, payments, variance'),
('contractor', 'Contractor', 'Assigned package execution and billing'),
('client_owner', 'Client/Owner', 'Authorized milestone and budget oversight')
ON CONFLICT (id) DO NOTHING;

-- Seed Role -> Permission Mappings
-- Super Admin: All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'super_admin', id FROM permissions
ON CONFLICT DO NOTHING;

-- Company Admin: All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'company_admin', id FROM permissions
ON CONFLICT DO NOTHING;

-- Project Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
('project_manager', 'projects:view'),
('project_manager', 'projects:create'),
('project_manager', 'projects:edit'),
('project_manager', 'tasks:view'),
('project_manager', 'tasks:manage'),
('project_manager', 'tasks:update'),
('project_manager', 'calculators:concrete'),
('project_manager', 'calculators:steel'),
('project_manager', 'measurements:take'),
('project_manager', 'boq:view'),
('project_manager', 'boq:edit'),
('project_manager', 'boq:approve'),
('project_manager', 'inventory:view'),
('project_manager', 'inventory:request'),
('project_manager', 'finance:view'),
('project_manager', 'expenses:submit'),
('project_manager', 'expenses:approve'),
('project_manager', 'reports:daily_site'),
('project_manager', 'audit:view')
ON CONFLICT DO NOTHING;

-- Civil Engineer
INSERT INTO role_permissions (role_id, permission_id) VALUES
('civil_engineer', 'projects:view'),
('civil_engineer', 'tasks:view'),
('civil_engineer', 'tasks:update'),
('civil_engineer', 'calculators:concrete'),
('civil_engineer', 'calculators:steel'),
('civil_engineer', 'measurements:take'),
('civil_engineer', 'boq:view'),
('civil_engineer', 'boq:edit'),
('civil_engineer', 'reports:daily_site')
ON CONFLICT DO NOTHING;

-- Site Supervisor
INSERT INTO role_permissions (role_id, permission_id) VALUES
('site_supervisor', 'projects:view'),
('site_supervisor', 'tasks:view'),
('site_supervisor', 'tasks:update'),
('site_supervisor', 'measurements:take'),
('site_supervisor', 'inventory:view'),
('site_supervisor', 'inventory:request'),
('site_supervisor', 'expenses:submit'),
('site_supervisor', 'reports:daily_site')
ON CONFLICT DO NOTHING;

-- Storekeeper
INSERT INTO role_permissions (role_id, permission_id) VALUES
('storekeeper', 'projects:view'),
('storekeeper', 'inventory:view'),
('storekeeper', 'inventory:request'),
('storekeeper', 'inventory:issue')
ON CONFLICT DO NOTHING;

-- Accountant
INSERT INTO role_permissions (role_id, permission_id) VALUES
('accountant', 'projects:view'),
('accountant', 'finance:view'),
('accountant', 'expenses:submit'),
('accountant', 'expenses:approve'),
('accountant', 'boq:view')
ON CONFLICT DO NOTHING;

-- Contractor
INSERT INTO role_permissions (role_id, permission_id) VALUES
('contractor', 'projects:view'),
('contractor', 'tasks:view'),
('contractor', 'tasks:update')
ON CONFLICT DO NOTHING;

-- Client / Owner
INSERT INTO role_permissions (role_id, permission_id) VALUES
('client_owner', 'projects:view'),
('client_owner', 'boq:view'),
('client_owner', 'finance:view')
ON CONFLICT DO NOTHING;

-- Seed Default Organization
INSERT INTO organizations (id, name, slug) VALUES
('org_green_valley', 'Green Valley Constructions Pvt Ltd', 'green-valley')
ON CONFLICT (id) DO NOTHING;

-- Seed Default Demo Users (Password: password123, BCrypt hashed)
-- BCrypt: $2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea
INSERT INTO users (id, email, full_name, hashed_password, organization_id, role_id) VALUES
('usr_admin', 'admin@greenvalley.com', 'Rajesh Sharma', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_green_valley', 'company_admin'),
('usr_pm', 'pm@greenvalley.com', 'Vikram Patel', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_green_valley', 'project_manager'),
('usr_engineer', 'engineer@greenvalley.com', 'Ananya Roy', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_green_valley', 'civil_engineer'),
('usr_supervisor', 'supervisor@greenvalley.com', 'Suresh Kumar', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_green_valley', 'site_supervisor')
ON CONFLICT (id) DO NOTHING;

-- Seed Sample Project: Green Valley Residency
INSERT INTO projects (id, name, code, location, budget_inr, status, progress_pct, organization_id) VALUES
('prj_green_valley_residency', 'Green Valley Residency', 'GVR-01', 'Sector 42, Gurgaon', 25000000.00, 'Active', 35.0, 'org_green_valley')
ON CONFLICT (id) DO NOTHING;

-- Assign Demo Users to Green Valley Residency Project
INSERT INTO project_members (id, project_id, user_id, assigned_role) VALUES
('pm_gvr_01', 'prj_green_valley_residency', 'usr_pm', 'Project Manager'),
('pm_gvr_02', 'prj_green_valley_residency', 'usr_engineer', 'Civil Engineer'),
('pm_gvr_03', 'prj_green_valley_residency', 'usr_supervisor', 'Site Supervisor')
ON CONFLICT (id) DO NOTHING;
