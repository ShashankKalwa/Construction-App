-- =============================================================================
-- Migration V11: Enhance Procurement, Finance Stepper & Cost Dashboard
-- =============================================================================

-- 1. Add idempotency and payment audit columns to expenses
ALTER TABLE expenses
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(100),
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_expenses_idempotency ON expenses(idempotency_key);

-- 2. Add idempotency and payment audit columns to contractor_bills
ALTER TABLE contractor_bills
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_contractor_bills_idempotency ON contractor_bills(idempotency_key);

-- 3. Add idempotency column to purchase_orders
ALTER TABLE purchase_orders
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_purchase_orders_idempotency ON purchase_orders(idempotency_key);

-- 4. Seed Vendors for org_apex_1 (if not present)
INSERT INTO vendors (id, organization_id, name, gstin, phone, email, category, payment_terms)
VALUES
('vnd_apex_cement', 'org_apex_1', 'UltraTech Cement Distribution Hub', '29AABCU1234F1Z1', '+91 98765 43210', 'sales@ultratech-hub.com', 'CEMENT_SUPPLIER', 'Net 30 Days'),
('vnd_apex_steel', 'org_apex_1', 'Reliable Steels & TMT Depot', '29AAACT9876E1Z5', '+91 98450 11223', 'orders@reliablesteels.com', 'STEEL_TRADER', 'Net 15 Days'),
('vnd_apex_quarry', 'org_apex_1', 'Apex Blue Metal & Sand Quarry', '29AABBA5566G1Z9', '+91 99001 22334', 'quarry@apexaggregates.in', 'AGGREGATE_QUARRY', 'Advance 50%')
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Demo Purchase Orders for prj_commercial_tower_b
INSERT INTO purchase_orders (id, po_number, project_id, vendor_id, order_date, delivery_deadline, total_amount, status, created_by)
VALUES
('po_demo_01', 'PO-2026-0101', 'prj_commercial_tower_b', 'vnd_apex_cement', '2026-03-01', '2026-03-05', 190000.00, 'COMPLETED', 'usr_pm'),
('po_demo_02', 'PO-2026-0102', 'prj_commercial_tower_b', 'vnd_apex_steel', '2026-03-10', '2026-03-25', 650000.00, 'ISSUED', 'usr_pm')
ON CONFLICT (id) DO NOTHING;

-- 6. Seed Purchase Order Items
INSERT INTO purchase_order_items (id, purchase_order_id, material_id, ordered_quantity, unit_price, total_price, received_quantity)
VALUES
('poi_demo_01', 'po_demo_01', 'mat_cem_opc', 500.00, 380.00, 190000.00, 500.00),
('poi_demo_02', 'po_demo_02', 'mat_steel_16', 10.00, 65000.00, 650000.00, 0.00)
ON CONFLICT (id) DO NOTHING;

-- 7. Seed Goods Receipt Note for po_demo_01
INSERT INTO goods_receipt_notes (id, purchase_order_id, material_id, challan_number, received_quantity, received_date, inspected_by, quality_remarks)
VALUES
('grn_demo_01', 'po_demo_01', 'mat_cem_opc', 'CH-2026-881', 500.00, '2026-03-04', 'usr_pm', 'OPC 53 Bags verified intact, zero moisture damage')
ON CONFLICT (id) DO NOTHING;

-- 8. Seed Demo Expenses for prj_commercial_tower_b
INSERT INTO expenses (id, project_id, category, title, amount, expense_date, status, submitted_by, verified_by, approved_by, paid_by, payment_reference)
VALUES
('exp_demo_01', 'prj_commercial_tower_b', 'EQUIPMENT', 'Hydraulic Piling Rig 5-day rental for basement', 120000.00, '2026-03-02', 'PAID', 'usr_supervisor_amit', 'usr_engineer_priya', 'usr_pm', 'usr_admin', 'TXN-PAY-001'),
('exp_demo_02', 'prj_commercial_tower_b', 'UTILITIES', 'Diesel fuel 800 Litres for generators and pumps', 72000.00, '2026-03-05', 'APPROVED', 'usr_supervisor_amit', 'usr_engineer_priya', 'usr_pm', NULL, NULL),
('exp_demo_03', 'prj_commercial_tower_b', 'SAFETY', 'Safety harness, helmets and perimeter net supply', 35000.00, '2026-03-12', 'VERIFIED', 'usr_supervisor_amit', 'usr_engineer_priya', NULL, NULL, NULL),
('exp_demo_04', 'prj_commercial_tower_b', 'PETTY_CASH', 'Site office survey station laser calibration', 12500.00, '2026-03-15', 'SUBMITTED', 'usr_supervisor_amit', NULL, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- 9. Seed Demo Contractor Bills for prj_commercial_tower_b
INSERT INTO contractor_bills (id, project_id, contractor_name, bill_number, bill_date, gross_amount, retention_pct, retention_amount, tds_pct, tds_amount, net_payable, status, work_description, certified_by)
VALUES
('bill_demo_01', 'prj_commercial_tower_b', 'Apex Earthmovers & Shoring Co.', 'RA-BILL-01', '2026-03-05', 400000.00, 5.00, 20000.00, 2.00, 8000.00, 372000.00, 'APPROVED', 'Complete excavation and shoring for Tower B Basement', 'usr_pm'),
('bill_demo_02', 'prj_commercial_tower_b', 'Southern Structural Fabrication', 'RA-BILL-02', '2026-03-18', 650000.00, 5.00, 32500.00, 2.00, 13000.00, 604500.00, 'PENDING', 'Footing reinforcement cutting, bending and fabrication', 'usr_pm')
ON CONFLICT (id) DO NOTHING;

