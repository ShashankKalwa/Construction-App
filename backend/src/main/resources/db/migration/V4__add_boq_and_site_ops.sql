-- =====================================================================
-- V4__add_boq_and_site_ops.sql: BOQ, Measurements & Site Operations
-- =====================================================================

-- 1. Unit Rate Master Schedule
CREATE TABLE IF NOT EXISTS unit_rates (
    id VARCHAR(50) PRIMARY KEY,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    item_code VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(64) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    standard_rate NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bill of Quantities (BOQ) Master
CREATE TABLE IF NOT EXISTS boqs (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, APPROVED, REJECTED
    total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    created_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    approved_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. BOQ Line Items
CREATE TABLE IF NOT EXISTS boq_items (
    id VARCHAR(50) PRIMARY KEY,
    boq_id VARCHAR(50) NOT NULL REFERENCES boqs(id) ON DELETE CASCADE,
    item_code VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(64) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    estimated_quantity NUMERIC(12, 3) NOT NULL,
    unit_rate NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(14, 2) NOT NULL,
    calculation_id VARCHAR(50) REFERENCES calculations(id) ON DELETE SET NULL,
    order_index INT NOT NULL DEFAULT 0
);

-- 4. Measurement Book (Field Quantities)
CREATE TABLE IF NOT EXISTS measurements (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    boq_item_id VARCHAR(50) REFERENCES boq_items(id) ON DELETE SET NULL,
    item_description TEXT NOT NULL,
    number_of_units NUMERIC(10, 2) NOT NULL DEFAULT 1.0,
    length_m NUMERIC(10, 3),
    width_m NUMERIC(10, 3),
    depth_m NUMERIC(10, 3),
    deduction_m3 NUMERIC(10, 3) NOT NULL DEFAULT 0.0,
    net_quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    location_details VARCHAR(255),
    measured_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    measured_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Daily Site Reports (DSR)
CREATE TABLE IF NOT EXISTS daily_site_reports (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    report_date DATE NOT NULL,
    weather VARCHAR(32) NOT NULL DEFAULT 'SUNNY', -- SUNNY, CLOUDY, RAINY, STORMY
    work_summary TEXT NOT NULL,
    planned_tomorrow TEXT,
    created_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Labor Attendance Logs (Trade-wise)
CREATE TABLE IF NOT EXISTS labor_attendance (
    id VARCHAR(50) PRIMARY KEY,
    report_id VARCHAR(50) NOT NULL REFERENCES daily_site_reports(id) ON DELETE CASCADE,
    trade VARCHAR(32) NOT NULL, -- MASON, BARBENDER, CARPENTER, HELPER, ELECTRICIAN, PLUMBER
    headcount INT NOT NULL,
    hours_worked NUMERIC(4, 1) NOT NULL DEFAULT 8.0,
    contractor_name VARCHAR(128)
);

-- 7. Site Issues & Blockers
CREATE TABLE IF NOT EXISTS site_issues (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    report_id VARCHAR(50) REFERENCES daily_site_reports(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',     -- OPEN, IN_PROGRESS, RESOLVED
    assigned_to VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    created_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_boqs_project ON boqs(project_id);
CREATE INDEX IF NOT EXISTS idx_boq_items_boq ON boq_items(boq_id);
CREATE INDEX IF NOT EXISTS idx_measurements_project ON measurements(project_id);
CREATE INDEX IF NOT EXISTS idx_dsr_project_date ON daily_site_reports(project_id, report_date DESC);
CREATE INDEX IF NOT EXISTS idx_labor_attendance_report ON labor_attendance(report_id);
CREATE INDEX IF NOT EXISTS idx_site_issues_project ON site_issues(project_id);

-- =====================================================================
-- Seed Demonstration Data for Green Valley Residency
-- =====================================================================

-- Seed Unit Rates
INSERT INTO unit_rates (id, organization_id, item_code, description, category, unit, standard_rate) VALUES
('ur_01', 'org_green_valley', 'UR-CONC-M25', 'M25 Reinforced Concrete in superstructure including formwork & curing', 'CONCRETE', 'm³', 6500.00),
('ur_02', 'org_green_valley', 'UR-STEEL-TMT', 'High Yield Strength Deformed Fe500D TMT Rebar cutting, bending and placing', 'STEEL', 'kg', 78.00),
('ur_03', 'org_green_valley', 'UR-BRICK-9IN', '9-inch External Brickwork in 1:6 cement-sand mortar', 'MASONRY', 'm³', 4800.00),
('ur_04', 'org_green_valley', 'UR-EXCAV-SOIL', 'Earthwork excavation in foundation pits and trenches in all soils', 'EARTHWORK', 'm³', 280.00),
('ur_05', 'org_green_valley', 'UR-PLAST-12MM', '12mm Internal Cement Plastering in 1:4 mortar finished smooth', 'PLASTER', 'm²', 220.00),
('ur_06', 'org_green_valley', 'UR-PAINT-EXT', 'Exterior Weatherproof Acrylic Emulsion paint 2 coats over 1 coat primer', 'FINISHES', 'm²', 180.00)
ON CONFLICT (id) DO NOTHING;

-- Seed Approved BOQ (v1)
INSERT INTO boqs (id, project_id, title, version, status, total_amount, created_by, approved_by, created_at, updated_at) VALUES
('boq_gvr_v1', 'prj_green_valley_residency', 'Green Valley Residency Main Substructure & Superstructure BOQ', 1, 'APPROVED', 541198.80, 'usr_engineer', 'usr_pm', '2026-03-25 11:00:00+00', '2026-03-26 15:30:00+00')
ON CONFLICT (id) DO NOTHING;

-- Seed BOQ Line Items (Linking to Phase 3 Calculations)
INSERT INTO boq_items (id, boq_id, item_code, description, category, unit, estimated_quantity, unit_rate, total_amount, calculation_id, order_index) VALUES
('boq_item_01', 'boq_gvr_v1', 'UR-CONC-M25', 'Tower A Ground Floor Slab M25 Concrete Casting', 'CONCRETE', 'm³', 50.40, 6500.00, 327600.00, 'calc_gvr_01', 1),
('boq_item_02', 'boq_gvr_v1', 'UR-STEEL-TMT', 'Tower A Main Column 20mm High Strength Rebar', 'STEEL', 'kg', 1464.60, 78.00, 114238.80, 'calc_gvr_02', 2),
('boq_item_03', 'boq_gvr_v1', 'UR-BRICK-9IN', 'Perimeter 9-inch Brick Masonry Wall (Block A)', 'MASONRY', 'm³', 20.70, 4800.00, 99360.00, 'calc_gvr_03', 3)
ON CONFLICT (id) DO NOTHING;

-- Seed Measurement Record
INSERT INTO measurements (id, project_id, boq_item_id, item_description, number_of_units, length_m, width_m, depth_m, deduction_m3, net_quantity, unit, location_details, measured_by, measured_at) VALUES
('meas_gvr_01', 'prj_green_valley_residency', 'boq_item_01', 'Ground Floor Slab Pour (Grid A1 to A4)', 1.0, 24.0, 14.0, 0.15, 0.0, 50.40, 'm³', 'Tower A, Level 0 Slab', 'usr_engineer', '2026-03-28 16:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- Seed Daily Site Report (DSR)
INSERT INTO daily_site_reports (id, project_id, report_date, weather, work_summary, planned_tomorrow, created_by, created_at) VALUES
('dsr_gvr_01', 'prj_green_valley_residency', '2026-04-10', 'SUNNY', 'Completed reinforcement tying and shuttering inspection for Tower A Level 1 slab. Formwork oiling finished.', 'Ready-mix concrete pour of 50 m³ starting at 07:00 AM with 2 boom pumpers.', 'usr_supervisor', '2026-04-10 18:30:00+00')
ON CONFLICT (id) DO NOTHING;

-- Seed Labor Attendance
INSERT INTO labor_attendance (id, report_id, trade, headcount, hours_worked, contractor_name) VALUES
('la_01', 'dsr_gvr_01', 'CARPENTER', 8, 8.0, 'Apex Formwork Contracting'),
('la_02', 'dsr_gvr_01', 'BARBENDER', 12, 8.5, 'Shree Steel Contractors'),
('la_03', 'dsr_gvr_01', 'HELPER', 16, 8.0, 'General Site Labor Force')
ON CONFLICT (id) DO NOTHING;

-- Seed Site Issue
INSERT INTO site_issues (id, project_id, report_id, title, description, severity, status, assigned_to, created_by, created_at) VALUES
('issue_gvr_01', 'prj_green_valley_residency', 'dsr_gvr_01', 'Water accumulation near Grid B3 tower crane pad', 'Heavy runoff water pool forming near crane outrigger foundation. Requires submersible dewatering pump.', 'MEDIUM', 'OPEN', 'usr_supervisor', 'usr_engineer', '2026-04-10 11:20:00+00')
ON CONFLICT (id) DO NOTHING;
