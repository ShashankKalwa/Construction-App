-- =============================================================================
-- Migration V8: Project Lifecycle, Milestones & Task Review Lifecycle
-- =============================================================================

-- 1. Enhance Projects Table with Lifecycle & Metadata Fields
ALTER TABLE projects ADD COLUMN IF NOT EXISTS project_type VARCHAR(100) DEFAULT 'Commercial';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS client_name VARCHAR(255) DEFAULT 'Apex Developments';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS start_date DATE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS end_date DATE;

-- 2. Enhance Phases Table with Company Id
ALTER TABLE phases ADD COLUMN IF NOT EXISTS company_id VARCHAR(50) REFERENCES organizations(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_phases_company ON phases(company_id);

-- 3. Enhance Tasks Table with Company Id & Review Workflow Lifecycle
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS company_id VARCHAR(50) REFERENCES organizations(id) ON DELETE CASCADE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS review_status VARCHAR(50) DEFAULT 'NONE'; -- NONE, PENDING_REVIEW, APPROVED, CORRECTIONS_REQUIRED
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS review_notes TEXT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_tasks_company ON tasks(company_id);
CREATE INDEX IF NOT EXISTS idx_tasks_review_status ON tasks(review_status);

-- 4. Create Milestones Table
CREATE TABLE IF NOT EXISTS milestones (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    company_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    due_date DATE,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, REACHED, DELAYED
    target_completion_pct DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_milestones_project ON milestones(project_id);
CREATE INDEX IF NOT EXISTS idx_milestones_company ON milestones(company_id);
CREATE INDEX IF NOT EXISTS idx_milestones_status ON milestones(status);

-- 5. Backfill company_id on phases and tasks from parent project organization_id
UPDATE phases ph
SET company_id = p.organization_id
FROM projects p
WHERE ph.project_id = p.id AND ph.company_id IS NULL;

UPDATE tasks t
SET company_id = p.organization_id
FROM projects p
WHERE t.project_id = p.id AND t.company_id IS NULL;

-- 6. Backfill dates on seeded projects
UPDATE projects SET start_date = '2026-01-01', end_date = '2026-12-31' WHERE id = 'prj_green_valley_residency' AND start_date IS NULL;
UPDATE projects SET start_date = '2026-02-15', end_date = '2027-06-30' WHERE id = 'prj_commercial_tower_b' AND start_date IS NULL;

-- 7. Seed Initial Milestones for Active Projects
INSERT INTO milestones (id, project_id, company_id, title, due_date, status, target_completion_pct) VALUES
('mls_gvr_01', 'prj_green_valley_residency', 'org_apex_infra', 'Foundation & Raft Concrete Completion', '2026-03-15', 'REACHED', 25.0),
('mls_gvr_02', 'prj_green_valley_residency', 'org_apex_infra', 'Tower A & B RCC Superstructure Top-out', '2026-07-30', 'PENDING', 65.0),
('mls_gvr_03', 'prj_green_valley_residency', 'org_apex_infra', 'External Facade & AAC Masonry Completion', '2026-10-15', 'PENDING', 85.0),
('mls_gvr_04', 'prj_green_valley_residency', 'org_apex_infra', 'Final Handover & Occupancy Certificate', '2026-12-31', 'PENDING', 100.0)
ON CONFLICT (id) DO NOTHING;
