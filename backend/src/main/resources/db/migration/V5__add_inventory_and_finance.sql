-- =============================================================================
-- Migration V5: Materials, Inventory, Procurement & Financial Management
-- =============================================================================

-- 1. Materials Catalog & Stock
CREATE TABLE materials (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    current_stock NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    min_stock_threshold NUMERIC(14,2) NOT NULL DEFAULT 10.00,
    unit_cost NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_material_code UNIQUE (project_id, code)
);

CREATE INDEX idx_materials_project_id ON materials(project_id);
CREATE INDEX idx_materials_category ON materials(category);

-- 2. Material Requisitions / Requests (MRN)
CREATE TABLE material_requests (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    material_id VARCHAR(50) NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
    quantity NUMERIC(14,2) NOT NULL,
    required_by_date DATE,
    requested_by VARCHAR(50) NOT NULL REFERENCES users(id),
    approved_by VARCHAR(50) REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    phase_or_task_desc VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_material_requests_project_id ON material_requests(project_id);
CREATE INDEX idx_material_requests_status ON material_requests(status);

-- 3. Stock Movements (Inward GRN, Outward Issue, Adjustment)
CREATE TABLE stock_movements (
    id VARCHAR(50) PRIMARY KEY,
    material_id VARCHAR(50) NOT NULL REFERENCES materials(id) ON DELETE CASCADE,
    movement_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(14,2) NOT NULL,
    balance_after NUMERIC(14,2) NOT NULL,
    reference_id VARCHAR(100),
    notes TEXT,
    performed_by VARCHAR(50) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movements_material_id ON stock_movements(material_id);

-- 4. Vendors Directory
CREATE TABLE vendors (
    id VARCHAR(50) PRIMARY KEY,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    gstin VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(100),
    category VARCHAR(50),
    payment_terms VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vendors_organization_id ON vendors(organization_id);

-- 5. Purchase Orders (PO)
CREATE TABLE purchase_orders (
    id VARCHAR(50) PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    vendor_id VARCHAR(50) NOT NULL REFERENCES vendors(id) ON DELETE RESTRICT,
    order_date DATE NOT NULL,
    delivery_deadline DATE,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ISSUED',
    created_by VARCHAR(50) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_purchase_orders_project_id ON purchase_orders(project_id);
CREATE INDEX idx_purchase_orders_vendor_id ON purchase_orders(vendor_id);

-- 6. Purchase Order Items
CREATE TABLE purchase_order_items (
    id VARCHAR(50) PRIMARY KEY,
    purchase_order_id VARCHAR(50) NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    material_id VARCHAR(50) NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
    ordered_quantity NUMERIC(14,2) NOT NULL,
    unit_price NUMERIC(14,2) NOT NULL,
    total_price NUMERIC(14,2) NOT NULL,
    received_quantity NUMERIC(14,2) NOT NULL DEFAULT 0.00
);

CREATE INDEX idx_po_items_po_id ON purchase_order_items(purchase_order_id);

-- 7. Goods Receipt Notes (GRN)
CREATE TABLE goods_receipt_notes (
    id VARCHAR(50) PRIMARY KEY,
    purchase_order_id VARCHAR(50) NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    material_id VARCHAR(50) NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
    challan_number VARCHAR(50) NOT NULL,
    received_quantity NUMERIC(14,2) NOT NULL,
    received_date DATE NOT NULL,
    inspected_by VARCHAR(50) NOT NULL REFERENCES users(id),
    quality_remarks VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_grn_po_id ON goods_receipt_notes(purchase_order_id);

-- 8. Project Expenses & Stepper Approval
CREATE TABLE expenses (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    expense_date DATE NOT NULL,
    receipt_url VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    submitted_by VARCHAR(50) NOT NULL REFERENCES users(id),
    verified_by VARCHAR(50) REFERENCES users(id),
    approved_by VARCHAR(50) REFERENCES users(id),
    paid_by VARCHAR(50) REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_project_id ON expenses(project_id);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_expenses_status ON expenses(status);

-- 9. Contractor Bills & Invoicing
CREATE TABLE contractor_bills (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    contractor_name VARCHAR(150) NOT NULL,
    bill_number VARCHAR(50) NOT NULL,
    bill_date DATE NOT NULL,
    gross_amount NUMERIC(14,2) NOT NULL,
    retention_pct NUMERIC(5,2) NOT NULL DEFAULT 5.00,
    retention_amount NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    tds_pct NUMERIC(5,2) NOT NULL DEFAULT 2.00,
    tds_amount NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    net_payable NUMERIC(14,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    work_description TEXT,
    certified_by VARCHAR(50) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contractor_bills_project_id ON contractor_bills(project_id);
CREATE INDEX idx_contractor_bills_status ON contractor_bills(status);

-- =============================================================================
-- Seed Demo Data for prj_commercial_tower_b
-- =============================================================================

-- Ensure Organization, Project and Project Members exist
INSERT INTO organizations (id, name, slug)
VALUES ('org_apex_infra', 'Apex Infra Ltd', 'apex-infra')
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, name, code, location, budget_inr, status, progress_pct, organization_id)
VALUES ('prj_commercial_tower_b', 'Commercial Tower B', 'CTB-02', 'Whitefield, Bangalore', 54000000.00, 'Active', 42.0, 'org_apex_infra')
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_members (id, project_id, user_id, assigned_role)
VALUES
('pm_ctb_01', 'prj_commercial_tower_b', 'usr_pm', 'Project Manager'),
('pm_ctb_02', 'prj_commercial_tower_b', 'usr_engineer', 'Civil Engineer'),
('pm_ctb_03', 'prj_commercial_tower_b', 'usr_supervisor', 'Site Supervisor')
ON CONFLICT (id) DO NOTHING;

-- Materials
INSERT INTO materials (id, project_id, code, name, category, unit, current_stock, min_stock_threshold, unit_cost)
VALUES
('mat_cement_opc', 'prj_commercial_tower_b', 'MAT-CEM-01', 'UltraTech OPC 53 Grade Cement', 'CEMENT', 'BAGS', 480.00, 100.00, 395.00),
('mat_steel_tmt', 'prj_commercial_tower_b', 'MAT-STL-01', 'Tata Tiscon 550D TMT Rebar (16mm)', 'STEEL', 'KG', 12500.00, 2000.00, 72.00),
('mat_river_sand', 'prj_commercial_tower_b', 'MAT-SND-01', 'Screened River Sand Zone II', 'AGGREGATE', 'M3', 42.00, 15.00, 1850.00),
('mat_aggregate_20mm', 'prj_commercial_tower_b', 'MAT-AGG-01', '20mm Graded Blue Metal Coarse Aggregate', 'AGGREGATE', 'M3', 8.00, 20.00, 1600.00), -- Low Stock Trigger!
('mat_clay_bricks', 'prj_commercial_tower_b', 'MAT-BRK-01', 'Class 1 Common Burnt Clay Bricks', 'MASONRY', 'NOS', 14000.00, 2500.00, 9.50),
('mat_weather_paint', 'prj_commercial_tower_b', 'MAT-PNT-01', 'Asian Paints Apex Weatherproof Exterior Emulsion', 'PAINT', 'LITRE', 180.00, 40.00, 320.00)
ON CONFLICT (id) DO NOTHING;

-- Vendors
INSERT INTO vendors (id, organization_id, name, gstin, phone, email, category, payment_terms)
VALUES
('vnd_ultratech', 'org_green_valley', 'UltraTech Cement Distribution Hub', '29AABCU1234F1Z1', '+91 98765 43210', 'sales@ultratech-hub.com', 'CEMENT_SUPPLIER', 'Net 30 Days'),
('vnd_tata_steel', 'org_green_valley', 'Tata Tiscon Steel Depot', '29AAACT9876E1Z5', '+91 98450 11223', 'orders@tiscondepot.com', 'STEEL_TRADER', 'Net 15 Days'),
('vnd_apex_quarry', 'org_green_valley', 'Apex Blue Metal & Sand Quarry', '29AABBA5566G1Z9', '+91 99001 22334', 'quarry@apexaggregates.in', 'AGGREGATE_QUARRY', 'Advance 50%'),
('vnd_apex_ultratech', 'org_apex_infra', 'UltraTech Cement Distribution Hub', '29AABCU1234F1Z1', '+91 98765 43210', 'sales@ultratech-hub.com', 'CEMENT_SUPPLIER', 'Net 30 Days'),
('vnd_apex_tata_steel', 'org_apex_infra', 'Tata Tiscon Steel Depot', '29AAACT9876E1Z5', '+91 98450 11223', 'orders@tiscondepot.com', 'STEEL_TRADER', 'Net 15 Days'),
('vnd_apex_quarry_apex', 'org_apex_infra', 'Apex Blue Metal & Sand Quarry', '29AABBA5566G1Z9', '+91 99001 22334', 'quarry@apexaggregates.in', 'AGGREGATE_QUARRY', 'Advance 50%')
ON CONFLICT (id) DO NOTHING;

-- Purchase Orders
INSERT INTO purchase_orders (id, po_number, project_id, vendor_id, order_date, delivery_deadline, total_amount, status, created_by)
VALUES
('po_001', 'PO-2026-0089', 'prj_commercial_tower_b', 'vnd_ultratech', '2026-03-01', '2026-03-05', 197500.00, 'COMPLETED', 'usr_engineer'),
('po_002', 'PO-2026-0092', 'prj_commercial_tower_b', 'vnd_apex_quarry', '2026-03-03', '2026-03-08', 64000.00, 'ISSUED', 'usr_pm')
ON CONFLICT (id) DO NOTHING;

-- Purchase Order Items
INSERT INTO purchase_order_items (id, purchase_order_id, material_id, ordered_quantity, unit_price, total_price, received_quantity)
VALUES
('poi_001', 'po_001', 'mat_cement_opc', 500.00, 395.00, 197500.00, 500.00),
('poi_002', 'po_002', 'mat_aggregate_20mm', 40.00, 1600.00, 64000.00, 0.00)
ON CONFLICT (id) DO NOTHING;

-- Goods Receipt Note (GRN)
INSERT INTO goods_receipt_notes (id, purchase_order_id, material_id, challan_number, received_quantity, received_date, inspected_by, quality_remarks)
VALUES
('grn_001', 'po_001', 'mat_cement_opc', 'CH-UT-98124', 500.00, '2026-03-04', 'usr_engineer', '53-grade batch bags checked. Zero tear/moisture damage.')
ON CONFLICT (id) DO NOTHING;

-- Stock Movement (GRN inward for the cement delivery)
INSERT INTO stock_movements (id, material_id, movement_type, quantity, balance_after, reference_id, notes, performed_by)
VALUES
('sm_001', 'mat_cement_opc', 'INWARD_GRN', 500.00, 500.00, 'grn_001', 'GRN delivery verification via PO-2026-0089', 'usr_engineer'),
('sm_002', 'mat_cement_opc', 'OUTWARD_ISSUE', 20.00, 480.00, 'mr_001', 'Dispatched to Level 1 Column C4 concrete pour', 'usr_supervisor')
ON CONFLICT (id) DO NOTHING;

-- Material Request
INSERT INTO material_requests (id, project_id, material_id, quantity, required_by_date, requested_by, approved_by, status, phase_or_task_desc)
VALUES
('mr_001', 'prj_commercial_tower_b', 'mat_cement_opc', 20.00, '2026-03-04', 'usr_supervisor', 'usr_pm', 'FULFILLED', 'Level 1 Column casting batch 2'),
('mr_002', 'prj_commercial_tower_b', 'mat_steel_tmt', 2500.00, '2026-03-07', 'usr_engineer', 'usr_pm', 'APPROVED', 'Podium Slab Main Reinforcement Bar placement')
ON CONFLICT (id) DO NOTHING;

-- Project Expenses
INSERT INTO expenses (id, project_id, category, title, amount, expense_date, status, submitted_by, verified_by, approved_by, paid_by)
VALUES
('exp_001', 'prj_commercial_tower_b', 'EQUIPMENT', '50 Ton Hydraulic Crane 3-Day Rental for Girder Erection', 45000.00, '2026-03-02', 'PAID', 'usr_supervisor', 'usr_engineer', 'usr_pm', 'usr_admin'),
('exp_002', 'prj_commercial_tower_b', 'UTILITIES', 'Temporary Site 3-Phase Electricity and Water Tanker Supply', 18500.00, '2026-03-03', 'APPROVED', 'usr_supervisor', 'usr_engineer', 'usr_pm', NULL),
('exp_003', 'prj_commercial_tower_b', 'PETTY_CASH', 'Site First Aid Kit Refill and Safety Goggles Pack (50 nos)', 4200.00, '2026-03-04', 'VERIFIED', 'usr_supervisor', 'usr_engineer', NULL, NULL),
('exp_004', 'prj_commercial_tower_b', 'SUBCONTRACTOR', 'Specialized Concrete Core Drilling & Ultrasonic Testing', 22000.00, '2026-03-04', 'SUBMITTED', 'usr_engineer', NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Contractor Bills
INSERT INTO contractor_bills (id, project_id, contractor_name, bill_number, bill_date, gross_amount, retention_pct, retention_amount, tds_pct, tds_amount, net_payable, status, work_description, certified_by)
VALUES
('bill_001', 'prj_commercial_tower_b', 'Apex Shuttering & Scaffolding Works', 'RA-BILL-03', '2026-03-02', 250000.00, 5.00, 12500.00, 2.00, 5000.00, 232500.00, 'APPROVED', 'Ground Floor Podium Shuttering staging and falsework complete', 'usr_engineer')
ON CONFLICT (id) DO NOTHING;
