-- =====================================================================
-- V2__add_phases_and_tasks.sql: Construction Phases and Tasks
-- =====================================================================

-- 1. Phases Table
CREATE TABLE IF NOT EXISTS phases (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) DEFAULT 'Active',
    progress_pct DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_phases_project ON phases(project_id);
CREATE INDEX IF NOT EXISTS idx_phases_order ON phases(project_id, order_index);

-- 2. Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(50) PRIMARY KEY,
    phase_id VARCHAR(50) NOT NULL REFERENCES phases(id) ON DELETE CASCADE,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assignee_id VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'PENDING',
    start_date DATE,
    due_date DATE,
    progress_pct DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tasks_phase ON tasks(phase_id);
CREATE INDEX IF NOT EXISTS idx_tasks_project ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);

-- =====================================================================
-- SEED DATA for Green Valley Residency (prj_green_valley_residency)
-- =====================================================================

-- Seed 4 Construction Phases
INSERT INTO phases (id, project_id, name, order_index, start_date, end_date, status, progress_pct) VALUES
('phs_gvr_01', 'prj_green_valley_residency', 'Phase 1: Substructure & Foundation', 1, '2026-01-10', '2026-03-15', 'Completed', 100.0),
('phs_gvr_02', 'prj_green_valley_residency', 'Phase 2: RCC Superstructure (Tower A & B)', 2, '2026-03-16', '2026-07-30', 'Active', 60.0),
('phs_gvr_03', 'prj_green_valley_residency', 'Phase 3: Brickwork & Masonry', 3, '2026-08-01', '2026-10-15', 'Planned', 0.0),
('phs_gvr_04', 'prj_green_valley_residency', 'Phase 4: Finishing, MEP & Painting', 4, '2026-10-16', '2026-12-31', 'Planned', 0.0)
ON CONFLICT (id) DO NOTHING;

-- Seed Construction Tasks for Phase 1 (Completed)
INSERT INTO tasks (id, phase_id, project_id, title, description, assignee_id, priority, status, start_date, due_date, progress_pct) VALUES
('tsk_gvr_101', 'phs_gvr_01', 'prj_green_valley_residency', 'Site soil excavation & raft pit levelling', 'Excavation to depth of 3.2m as per structural drawing S-01', 'usr_supervisor', 'HIGH', 'COMPLETED', '2026-01-10', '2026-01-25', 100.0),
('tsk_gvr_102', 'phs_gvr_01', 'prj_green_valley_residency', 'PCC 1:4:8 under footings & water proofing', '75mm PCC bed and bitumen waterproofing membrane application', 'usr_supervisor', 'MEDIUM', 'COMPLETED', '2026-01-26', '2026-02-10', 100.0),
('tsk_gvr_103', 'phs_gvr_01', 'prj_green_valley_residency', 'Raft foundation rebar binding & M25 concrete pour', 'Fe500D rebar placement and 240 m3 ready mix concrete pour', 'usr_engineer', 'HIGH', 'COMPLETED', '2026-02-11', '2026-03-15', 100.0)
ON CONFLICT (id) DO NOTHING;

-- Seed Construction Tasks for Phase 2 (Active)
INSERT INTO tasks (id, phase_id, project_id, title, description, assignee_id, priority, status, start_date, due_date, progress_pct) VALUES
('tsk_gvr_201', 'phs_gvr_02', 'prj_green_valley_residency', 'Tower A Ground Floor Columns & 1st Floor Slab', 'Formwork, conduit placement and M25 casting for 1st floor', 'usr_engineer', 'HIGH', 'COMPLETED', '2026-03-16', '2026-04-20', 100.0),
('tsk_gvr_202', 'phs_gvr_02', 'prj_green_valley_residency', 'Tower A 2nd Floor Column Shuttering & Casting', '8 columns (Grid A1-A8) formwork alignment and pour', 'usr_supervisor', 'HIGH', 'IN_PROGRESS', '2026-04-21', '2026-05-15', 75.0),
('tsk_gvr_203', 'phs_gvr_02', 'prj_green_valley_residency', 'Tower B 2nd Floor Beam Rebar Inspection', 'Structural consultant pre-pour inspection for main girders', 'usr_engineer', 'MEDIUM', 'PENDING', '2026-05-10', '2026-05-18', 0.0),
('tsk_gvr_204', 'phs_gvr_02', 'prj_green_valley_residency', 'Procure Fe500D 16mm & 20mm TMT Rebars', 'Vendor dispatch delayed from Jindal Steel plant', 'usr_pm', 'HIGH', 'BLOCKED', '2026-05-01', '2026-05-12', 30.0)
ON CONFLICT (id) DO NOTHING;

-- Seed Construction Tasks for Phase 3 (Planned)
INSERT INTO tasks (id, phase_id, project_id, title, description, assignee_id, priority, status, start_date, due_date, progress_pct) VALUES
('tsk_gvr_301', 'phs_gvr_03', 'prj_green_valley_residency', 'Ground Floor 200mm AAC Blockwork', 'External perimeter walls and party walls with block adhesive', 'usr_supervisor', 'MEDIUM', 'PENDING', '2026-08-01', '2026-08-25', 0.0)
ON CONFLICT (id) DO NOTHING;
