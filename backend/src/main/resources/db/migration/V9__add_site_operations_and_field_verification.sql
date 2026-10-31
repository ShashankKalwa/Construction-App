-- V9__add_site_operations_and_field_verification.sql
-- Construction Management System (CMS) - Phase 3

-- 1. Enhance Daily Site Reports with review/approval lifecycle and company scoping
ALTER TABLE daily_site_reports
    ADD COLUMN IF NOT EXISTS company_id VARCHAR(50) REFERENCES organizations(id),
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- DRAFT, SUBMITTED, APPROVED, CORRECTIONS_REQUIRED
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(50) REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS approval_notes TEXT;

CREATE INDEX IF NOT EXISTS idx_dsr_company_project ON daily_site_reports(company_id, project_id);

-- 2. Work Progress Tracking (Separate Planned, Reported, and Verified Quantities)
CREATE TABLE IF NOT EXISTS site_work_progress (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    company_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    task_id VARCHAR(50) REFERENCES tasks(id) ON DELETE SET NULL,
    report_id VARCHAR(50) REFERENCES daily_site_reports(id) ON DELETE CASCADE,
    item_name VARCHAR(200) NOT NULL,
    unit VARCHAR(30) NOT NULL, -- m3, sq.m, Rmt, MT, bags, units
    planned_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    reported_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    verified_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED', -- REPORTED, VERIFIED, REJECTED
    reported_by VARCHAR(50) NOT NULL REFERENCES users(id),
    verified_by VARCHAR(50) REFERENCES users(id),
    verification_notes TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_work_progress_project ON site_work_progress(project_id);

-- 3. Digital Site Inspections with Checklists
CREATE TABLE IF NOT EXISTS site_inspections (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    company_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    inspection_type VARCHAR(50) NOT NULL, -- PRE_POUR_CONCRETE, REBAR, QUALITY, SAFETY, MASONRY, MEP
    status VARCHAR(30) NOT NULL DEFAULT 'PASSED', -- PASSED, FAILED, CONDITIONAL
    checklist_data JSONB NOT NULL DEFAULT '[]', -- Array of { "item": "Formwork stability", "passed": true, "remarks": "" }
    inspector_id VARCHAR(50) NOT NULL REFERENCES users(id),
    inspection_date DATE NOT NULL,
    location VARCHAR(200),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inspections_project ON site_inspections(project_id);

-- 4. Defects & Snag List (with Re-inspection Closure Workflow)
CREATE TABLE IF NOT EXISTS site_defects (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    company_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    inspection_id VARCHAR(50) REFERENCES site_inspections(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(200),
    severity VARCHAR(30) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    assigned_to VARCHAR(50) REFERENCES users(id),
    deadline DATE,
    created_by VARCHAR(50) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    re_inspected_by VARCHAR(50) REFERENCES users(id),
    re_inspected_at TIMESTAMP WITH TIME ZONE,
    re_inspection_notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_defects_project ON site_defects(project_id, status);

-- 5. Photo Evidence Linked to Tasks, Reports, and Defects
CREATE TABLE IF NOT EXISTS site_photos (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    company_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    report_id VARCHAR(50) REFERENCES daily_site_reports(id) ON DELETE SET NULL,
    task_id VARCHAR(50) REFERENCES tasks(id) ON DELETE SET NULL,
    defect_id VARCHAR(50) REFERENCES site_defects(id) ON DELETE SET NULL,
    photo_url VARCHAR(500) NOT NULL,
    caption VARCHAR(255),
    location_tag VARCHAR(150),
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(50) NOT NULL REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_photos_project ON site_photos(project_id);

-- 6. Backfill company_id on existing daily_site_reports
UPDATE daily_site_reports d
SET company_id = p.organization_id
FROM projects p
WHERE d.project_id = p.id AND d.company_id IS NULL;

-- 7. Seed Initial Field Inspections and Work Progress
INSERT INTO site_inspections (id, project_id, company_id, title, inspection_type, status, checklist_data, inspector_id, inspection_date, location, notes)
VALUES
('insp_01', 'prj_green_valley_residency', 'org_apex_infra', 'Tower A Foundation Raft Rebar Inspection', 'REBAR', 'PASSED',
 '[{"item": "Main bottom reinforcement spacing (150mm c/c)", "passed": true, "remarks": "Compliant with structural drawing S-04"}, {"item": "Cover block placement (50mm)", "passed": true, "remarks": "Adequate concrete cover blocks secured"}, {"item": "Column starter bars and stirrup binding", "passed": true, "remarks": "Properly tied with 18G binding wire"}]'::jsonb,
 'usr_eng_priya', CURRENT_DATE - INTERVAL '3 days', 'Tower A - Basement 2 Raft', 'Pre-pour clearance granted for M30 ready mix concrete.'),
('insp_02', 'prj_commercial_tower_b', 'org_apex_infra', 'Tower B 3rd Floor Formwork & Shuttering', 'PRE_POUR_CONCRETE', 'CONDITIONAL',
 '[{"item": "Props verticality and cross bracing", "passed": true, "remarks": "Heavy duty cup-lock props in place"}, {"item": "Slab shuttering level tolerance (+/- 3mm)", "passed": true, "remarks": "Checked with dumpy level"}, {"item": "Formwork joints sealed against slurry leakage", "passed": false, "remarks": "Gaps observed near column C4 beam junction"}]'::jsonb,
 'usr_eng_priya', CURRENT_DATE - INTERVAL '1 day', 'Tower B - Floor 3 Slab', 'Seal beam-column joints with foam tape before final pour approval.')
ON CONFLICT (id) DO NOTHING;

INSERT INTO site_defects (id, project_id, company_id, inspection_id, title, description, location, severity, status, assigned_to, deadline, created_by)
VALUES
('def_01', 'prj_commercial_tower_b', 'org_apex_infra', 'insp_02', 'Slurry Leakage Gap at Column C4 Beam Junction', 'Gaps between plywood shuttering allow cement slurry loss during vibration.', 'Floor 3, Grid C4-B1', 'MEDIUM', 'OPEN', 'usr_contractor_08', CURRENT_DATE + INTERVAL '2 days', 'usr_eng_priya')
ON CONFLICT (id) DO NOTHING;

INSERT INTO site_work_progress (id, project_id, company_id, task_id, report_id, item_name, unit, planned_quantity, reported_quantity, verified_quantity, status, reported_by, verified_by, verification_notes, recorded_at, verified_at)
VALUES
('wp_01', 'prj_green_valley_residency', 'org_apex_infra', NULL, NULL, 'Raft Concrete M30 Pouring', 'm3', 250.00, 240.00, 240.00, 'VERIFIED', 'usr_supervisor_05', 'usr_eng_priya', 'Batch slips verified against delivery weighbridge.', CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE - INTERVAL '1 day'),
('wp_02', 'prj_commercial_tower_b', 'org_apex_infra', NULL, NULL, 'AAC Block Masonry 200mm', 'sq.m', 450.00, 180.00, 0.00, 'REPORTED', 'usr_supervisor_05', NULL, 'Pending field measurement by civil engineer.', CURRENT_DATE, NULL)
ON CONFLICT (id) DO NOTHING;
