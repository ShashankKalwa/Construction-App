-- =============================================================================
-- Migration V12: Reporting, Collaboration, In-App Notifications & Audit Trail
-- =============================================================================

-- 1. Performance Indexes for Rapid Real-Time Querying
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_proj_action ON audit_logs(project_id, action, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_org_created ON audit_logs(organization_id, created_at DESC);

-- 2. Seed Realistic Audit Trail Events for Project: prj_commercial_tower_b
INSERT INTO audit_logs (id, organization_id, project_id, user_id, user_name, action, entity_type, entity_id, details, ip_address, created_at)
VALUES
('aud_001', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'STATUS_CHANGE', 'PROJECT', 'prj_commercial_tower_b', 'Project status transitioned from PLANNING to ACTIVE following structural drawing sign-off.', '192.168.1.101', '2026-03-01 09:00:00+00'),
('aud_002', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'CREATE', 'MILESTONE', 'ms_foundation_raft', 'Created milestone Foundation & Raft Slab with target date 2026-04-15.', '192.168.1.101', '2026-03-01 10:30:00+00'),
('aud_003', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_eng_priya', 'Priya Sharma', 'APPROVE', 'TASK', 'task_excavation_01', 'Approved task Basement Bulk Excavation review. 100% volume verified against site measurement book.', '192.168.1.105', '2026-03-03 14:15:00+00'),
('aud_004', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_storekeeper_06', 'Ramesh Yadav', 'ISSUE_STOCK', 'STOCK', 'mat_cem_opc_ctb', 'Issued 200 bags of OPC 53 Cement to Site Supervisor for PCC blinding layer.', '192.168.1.112', '2026-03-04 11:20:00+00'),
('aud_005', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'APPROVE', 'EXPENSE', 'exp_001', 'Approved site expense of ₹1,20,000 for hydraulic rig rental following engineer verification.', '192.168.1.101', '2026-03-05 16:45:00+00'),
('aud_006', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_accountant_07', 'Sunita Roy', 'PAY_BILL', 'CONTRACTOR_BILL', 'bill_001', 'Disbursed payment of ₹3,72,000 to Apex Earthmovers (Gross: ₹4,00,000, Retention: ₹20,000, TDS: ₹8,000). Ref: RTGS-88129.', '192.168.1.120', '2026-03-06 17:00:00+00'),
('aud_007', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'CREATE', 'PURCHASE_ORDER', 'po_001', 'Issued PO-2026-0101 to UltraTech Cement Hub for 500 bags @ ₹380/bag (Total: ₹1,90,000).', '192.168.1.101', '2026-03-08 09:30:00+00'),
('aud_008', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_storekeeper_06', 'Ramesh Yadav', 'RECEIVE_STOCK', 'STOCK', 'po_001', 'Recorded GRN inward receipt for 500 bags OPC Cement via Challan DC-77821. Stock updated.', '192.168.1.112', '2026-03-10 14:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- 3. Seed Realistic In-App Notifications for Project Roles
INSERT INTO notifications (id, organization_id, user_id, project_id, title, message, type, is_read, entity_id, action_url, created_at)
VALUES
('notif_001', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Contractor RA Bill Awaiting Approval', 'Apex Earthmovers submitted RA Bill #01 (₹4,00,000) for basement shoring. Verified by Priya Sharma.', 'APPROVAL_REQUIRED', FALSE, 'bill_001', '/finance/bills/bill_001', '2026-03-05 10:00:00+00'),
('notif_002', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Low Stock Alert: 12mm TMT Rebar', 'Current stock is 3.50 MT, falling below safety threshold of 6.00 MT. Immediate PO requisition recommended.', 'STOCK_ALERT', FALSE, 'mat_steel_12', '/inventory/materials/mat_steel_12', '2026-03-06 08:30:00+00'),
('notif_003', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Site Expense Verified', 'Site supervisor submitted fuel expense ₹72,000 verified by engineer. Ready for PM approval.', 'APPROVAL_REQUIRED', TRUE, 'exp_002', '/finance/expenses/exp_002', '2026-03-05 15:30:00+00'),
('notif_004', 'org_apex_infra', 'usr_eng_priya', 'prj_commercial_tower_b', 'Task Progress Review Submitted', 'Supervisor Shashank reported 100% completion on Basement Retaining Wall Shuttering.', 'TASK_REVIEW', FALSE, 'task_shuttering_02', '/tasks/task_shuttering_02', '2026-03-07 16:00:00+00'),
('notif_005', 'org_apex_infra', 'usr_eng_priya', 'prj_commercial_tower_b', 'Structural Drawing Revision Uploaded', 'Architect uploaded Revision v2.1 for Raft Foundation Beam Reinforcement.', 'DOCUMENT_UPDATED', FALSE, 'doc_dwg_raft_01', '/documents/doc_dwg_raft_01', '2026-03-08 11:45:00+00'),
('notif_006', 'org_apex_infra', 'usr_supervisor_05', 'prj_commercial_tower_b', 'Material Requisition Approved', 'MRN-2026-0042 for 150 bags OPC Cement has been approved by PM Rahul Verma.', 'APPROVAL_DECISION', FALSE, 'mrn_0042', '/materials/requests/mrn_0042', '2026-03-09 09:15:00+00'),
('notif_007', 'org_apex_infra', 'usr_accountant_07', 'prj_commercial_tower_b', 'Payment Disbursement Authorized', 'RA Bill #01 approved by PM. Ready for disbursement of Net Payable ₹3,72,000.', 'PAYMENT_DISBURSED', FALSE, 'bill_001', '/finance/bills/bill_001', '2026-03-05 16:50:00+00'),
('notif_008', 'org_apex_infra', 'usr_company_admin_02', 'prj_commercial_tower_b', 'Monthly Budget Variance Notice', 'Commercial Tower B reported Actual Cost ₹54.2 L against Committed Cost ₹1.82 Cr. Budget utilization at 63.8%.', 'REPORT_READY', FALSE, 'prj_commercial_tower_b', '/dashboard/cost-dashboard', '2026-03-10 18:00:00+00')
ON CONFLICT (id) DO NOTHING;

