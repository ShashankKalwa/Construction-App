-- =============================================================================
-- Migration V10: Materials, Inventory Requisitions, Issuance, Ledger & Low-Stock Alerts
-- =============================================================================

-- 1. Enhance material_requests with rejection and issuance tracking
ALTER TABLE material_requests
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT,
    ADD COLUMN IF NOT EXISTS issued_by VARCHAR(50) REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS issued_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_material_requests_req_by ON material_requests(requested_by);
CREATE INDEX IF NOT EXISTS idx_material_requests_created_at ON material_requests(created_at DESC);

-- 2. Enhance stock_movements with idempotency key
ALTER TABLE stock_movements
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_stock_movements_created_at ON stock_movements(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_movements_type ON stock_movements(movement_type);

-- 3. Enhance materials with low-stock index
CREATE INDEX IF NOT EXISTS idx_materials_stock_threshold ON materials(project_id, current_stock, min_stock_threshold);

-- 4. Seed Real-world Demo Materials for prj_commercial_tower_b if not present
INSERT INTO materials (id, project_id, code, name, category, unit, current_stock, min_stock_threshold, unit_cost, created_at, updated_at)
VALUES
    ('mat_cem_opc_ctb', 'prj_commercial_tower_b', 'MAT-CEM-OPC', 'UltraTech OPC 53 Grade Cement', 'CEMENT', 'Bags', 450.00, 100.00, 385.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mat_steel_16_ctb', 'prj_commercial_tower_b', 'MAT-STL-16', '16mm Fe550D TMT Reinforcement Steel', 'STEEL', 'MT', 14.50, 5.00, 65000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mat_steel_12_ctb', 'prj_commercial_tower_b', 'MAT-STL-12', '12mm Fe550D TMT Reinforcement Steel', 'STEEL', 'MT', 3.20, 6.00, 66000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Triggers LOW STOCK ALERT
    ('mat_sand_m_ctb', 'prj_commercial_tower_b', 'MAT-SAND-M', 'Manufactured Concrete M-Sand', 'AGGREGATE', 'Ton', 240.00, 50.00, 1250.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mat_agg_20_ctb', 'prj_commercial_tower_b', 'MAT-AGG-20', '20mm Blue Metal Coarse Aggregate', 'AGGREGATE', 'Ton', 310.00, 80.00, 1100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mat_brk_red_ctb', 'prj_commercial_tower_b', 'MAT-BRK-RED', 'Class-1 Wire Cut Clay Bricks', 'MASONRY', 'Pcs', 15000.00, 3000.00, 9.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Material Requests for prj_commercial_tower_b
INSERT INTO material_requests (id, project_id, material_id, quantity, required_by_date, requested_by, approved_by, status, phase_or_task_desc, created_at, updated_at)
VALUES
    ('mr_pending_1', 'prj_commercial_tower_b', 'mat_steel_12_ctb', 5.00, CURRENT_DATE + INTERVAL '2 days', 'usr_supervisor_05', NULL, 'PENDING', 'Level 1 Slab Top Mesh Rebar Fabrication', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mr_approved_1', 'prj_commercial_tower_b', 'mat_cem_opc_ctb', 50.00, CURRENT_DATE + INTERVAL '1 day', 'usr_supervisor_05', 'usr_pm_rahul', 'APPROVED', 'Ground Floor Column C1-C8 M35 RCC Pouring', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    ('mr_issued_1', 'prj_commercial_tower_b', 'mat_steel_16_ctb', 4.00, CURRENT_DATE, 'usr_supervisor_05', 'usr_pm_rahul', 'FULFILLED', 'Ground Floor Columns Starter Cages', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

