-- =============================================================================
-- Migration V6: Document Versioning, In-App Notifications & Audit Trail
-- =============================================================================

-- 1. Documents Directory & Metadata
CREATE TABLE documents (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    latest_version VARCHAR(20) NOT NULL DEFAULT 'v1.0',
    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    created_by VARCHAR(50) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_project_id ON documents(project_id);
CREATE INDEX idx_documents_organization_id ON documents(organization_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_status ON documents(status);

-- 2. Document Revision History & Files
CREATE TABLE document_versions (
    id VARCHAR(50) PRIMARY KEY,
    document_id VARCHAR(50) NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_number VARCHAR(20) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    mime_type VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
    file_url VARCHAR(500) NOT NULL,
    change_notes TEXT,
    uploaded_by VARCHAR(50) NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);

-- 3. In-App Notifications
CREATE TABLE notifications (
    id VARCHAR(50) PRIMARY KEY,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id VARCHAR(50) REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    entity_id VARCHAR(100),
    action_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 4. Audit Activity Logs
CREATE TABLE audit_logs (
    id VARCHAR(50) PRIMARY KEY,
    organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    project_id VARCHAR(50) REFERENCES projects(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id),
    user_name VARCHAR(150) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_project_id ON audit_logs(project_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- =============================================================================
-- Seed Demo Data for Project: prj_commercial_tower_b
-- =============================================================================

-- Ensure Organization exists
INSERT INTO organizations (id, name, slug)
VALUES ('org_apex_infra', 'Apex Infra Ltd', 'apex-infra')
ON CONFLICT (id) DO NOTHING;

-- Ensure Project exists
INSERT INTO projects (id, name, code, location, budget_inr, status, progress_pct, organization_id)
VALUES ('prj_commercial_tower_b', 'Commercial Tower B', 'CTB-02', 'Whitefield, Bangalore', 54000000.00, 'Active', 42.0, 'org_apex_infra')
ON CONFLICT (id) DO NOTHING;

-- Seed Apex Infra Demo Users
INSERT INTO users (id, email, full_name, hashed_password, organization_id, role_id)
VALUES
('usr_pm_rahul', 'rahul.verma@apexinfra.com', 'Rahul Verma', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'project_manager'),
('usr_eng_priya', 'priya.sharma@apexinfra.com', 'Priya Sharma', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'civil_engineer'),
('usr_super_admin_01', 'superadmin@apexinfra.com', 'Vikram Malhotra', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'super_admin'),
('usr_company_admin_02', 'companyadmin@apexinfra.com', 'Rajesh Sharma', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'company_admin'),
('usr_pm_03', 'pm@apexinfra.com', 'Priya Nair', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'project_manager'),
('usr_engineer_04', 'engineer@apexinfra.com', 'Anand Verma', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'civil_engineer'),
('usr_supervisor_05', 'supervisor@apexinfra.com', 'Shashank Kumar', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'site_supervisor'),
('usr_storekeeper_06', 'storekeeper@apexinfra.com', 'Ramesh Yadav', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'storekeeper'),
('usr_accountant_07', 'accountant@apexinfra.com', 'Sunita Roy', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'accountant'),
('usr_contractor_08', 'contractor@apexinfra.com', 'Harpreet Singh', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'contractor'),
('usr_client_09', 'client@apexinfra.com', 'Deepak Singhal', '$2a$10$fDFzjDgA4/FMQV7YtuOB4.4zlmmMJbnIAHPac0IYEkmkKo11WvRea', 'org_apex_infra', 'client_owner')
ON CONFLICT (id) DO NOTHING;

-- Seed Project Members for Commercial Tower B
INSERT INTO project_members (id, project_id, user_id, assigned_role)
VALUES
('pm_ctb_04', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Project Manager'),
('pm_ctb_05', 'prj_commercial_tower_b', 'usr_eng_priya', 'Civil Engineer'),
('pm_ctb_06', 'prj_commercial_tower_b', 'usr_pm_03', 'Project Manager'),
('pm_ctb_07', 'prj_commercial_tower_b', 'usr_engineer_04', 'Civil Engineer'),
('pm_ctb_08', 'prj_commercial_tower_b', 'usr_supervisor_05', 'Site Supervisor')
ON CONFLICT (id) DO NOTHING;

-- Seed Documents
INSERT INTO documents (id, project_id, organization_id, title, document_type, category, latest_version, status, created_by, created_at, updated_at)
VALUES
('doc_arch_floor_plan', 'prj_commercial_tower_b', 'org_apex_infra', 'Architectural Layout - Typical Floor Plan (L4-L18)', 'DRAWING_ARCHITECTURAL', 'Drawings', 'v2.1', 'APPROVED', 'usr_pm_rahul', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('doc_struct_foundation', 'prj_commercial_tower_b', 'org_apex_infra', 'Structural Detailing - Raft & Pile Foundation', 'DRAWING_STRUCTURAL', 'Structural', 'v1.2', 'APPROVED', 'usr_eng_priya', CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
('doc_rera_sanction', 'prj_commercial_tower_b', 'org_apex_infra', 'RERA Municipal Commencement Certificate', 'PERMIT', 'Compliance', 'v1.0', 'APPROVED', 'usr_pm_rahul', CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '60 days')
ON CONFLICT (id) DO NOTHING;

-- Seed Document Versions
INSERT INTO document_versions (id, document_id, version_number, file_name, file_size_bytes, mime_type, file_url, change_notes, uploaded_by, uploaded_at)
VALUES
('doc_v_arch_1_0', 'doc_arch_floor_plan', 'v1.0', 'commercial_tower_b_arch_v1.0.pdf', 15520000, 'application/pdf', 'https://storage.cms.com/docs/arch_v1_0.pdf', 'Initial approved schematic architectural layout.', 'usr_pm_rahul', CURRENT_TIMESTAMP - INTERVAL '30 days'),
('doc_v_arch_2_0', 'doc_arch_floor_plan', 'v2.0', 'commercial_tower_b_arch_v2.0.pdf', 16980000, 'application/pdf', 'https://storage.cms.com/docs/arch_v2_0.pdf', 'Updated stairwell clearances per revised fire safety code.', 'usr_eng_priya', CURRENT_TIMESTAMP - INTERVAL '14 days'),
('doc_v_arch_2_1', 'doc_arch_floor_plan', 'v2.1', 'commercial_tower_b_arch_v2.1.pdf', 17300000, 'application/pdf', 'https://storage.cms.com/docs/arch_v2_1.pdf', 'Minor alignment with facade curtain-wall mullion spacing.', 'usr_eng_priya', CURRENT_TIMESTAMP - INTERVAL '2 days'),

('doc_v_struct_1_0', 'doc_struct_foundation', 'v1.0', 'tower_b_foundation_layout_v1.0.pdf', 23480000, 'application/pdf', 'https://storage.cms.com/docs/struct_v1_0.pdf', 'Initial foundation layout with 1200mm dia bored piles.', 'usr_eng_priya', CURRENT_TIMESTAMP - INTERVAL '45 days'),
('doc_v_struct_1_2', 'doc_struct_foundation', 'v1.2', 'tower_b_foundation_layout_v1.2.pdf', 25160000, 'application/pdf', 'https://storage.cms.com/docs/struct_v1_2.pdf', 'Upgraded bottom mesh reinforcement to 32mm Fe500D bars.', 'usr_eng_priya', CURRENT_TIMESTAMP - INTERVAL '10 days'),

('doc_v_rera_1_0', 'doc_rera_sanction', 'v1.0', 'rera_commencement_certificate_signed.pdf', 4710000, 'application/pdf', 'https://storage.cms.com/docs/rera_sanction.pdf', 'Officially stamped and sealed municipal commencement permit.', 'usr_pm_rahul', CURRENT_TIMESTAMP - INTERVAL '60 days')
ON CONFLICT (id) DO NOTHING;

-- Seed In-App Notifications
INSERT INTO notifications (id, organization_id, user_id, project_id, title, message, type, is_read, entity_id, action_url, created_at)
VALUES
('notif_001', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Low Stock Warning: OPC 53 Cement', 'On-hand balance (85.00 Bags) is below the minimum reorder threshold (100.00 Bags).', 'LOW_STOCK', FALSE, 'mat_cement_opc53', '/inventory', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('notif_002', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Approval Required: BOQ Rev 2', 'BOQ Revision 2 totaling Rs 1,48,50,000 submitted by Priya Sharma for verification.', 'APPROVAL_REQUIRED', FALSE, 'boq_tower_b_rev2', '/boq/boq_tower_b_rev2', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('notif_003', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Contractor Bill Submitted: Royal Infra RA #4', 'RA Bill #4 (Gross Rs 4,85,000) submitted for work done on 6th floor slab casting.', 'BILL_SUBMITTED', TRUE, 'bill_royal_infra_001', '/finance/bills', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('notif_004', 'org_apex_infra', 'usr_pm_rahul', 'prj_commercial_tower_b', 'Drawing Revision Uploaded: Arch Plan v2.1', 'Priya Sharma uploaded revised typical floor plan v2.1 with facade alignment notes.', 'DOCUMENT_UPLOADED', TRUE, 'doc_arch_floor_plan', '/documents/doc_arch_floor_plan', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- Seed Audit Trail Activity Logs
INSERT INTO audit_logs (id, organization_id, project_id, user_id, user_name, action, entity_type, entity_id, details, ip_address, created_at)
VALUES
('audit_001', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'ISSUE_STOCK', 'STOCK', 'mat_cement_opc53', 'Issued 65.00 Bags of OPC 53 Cement to Task 4th Floor Slab Casting. Balance reduced to 85.00 Bags.', '192.168.1.42', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('audit_002', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'APPROVE', 'CONTRACTOR_BILL', 'bill_royal_infra_001', 'Approved Contractor RA Bill #4. Gross: Rs 4,85,000. Retention (5%): Rs 24,250. TDS (2%): Rs 9,700. Net Payable: Rs 4,51,050.', '192.168.1.42', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('audit_003', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_eng_priya', 'Priya Sharma', 'CREATE', 'DOCUMENT', 'doc_arch_floor_plan', 'Uploaded new revision v2.1 for Typical Floor Plan with facade alignment adjustments.', '192.168.1.88', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('audit_004', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_eng_priya', 'Priya Sharma', 'CREATE', 'BOQ', 'boq_tower_b_rev2', 'Created BOQ Revision 2 with revised superstructure reinforcement rates.', '192.168.1.88', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('audit_005', 'org_apex_infra', 'prj_commercial_tower_b', 'usr_pm_rahul', 'Rahul Verma', 'APPROVE', 'EXPENSE', 'exp_diesel_gen_001', 'Approved site diesel expenditure of Rs 14,500 for heavy crane operation.', '192.168.1.42', CURRENT_TIMESTAMP - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;
