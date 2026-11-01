# Construction Management System (CMS) — Repository Audit Report
**Date:** November 2026  
**Auditor:** Antigravity Senior Engineering Agent  
**Repository:** [https://github.com/ShashankKalwa/Construction-App](https://github.com/ShashankKalwa/Construction-App)  
**Organization:** Chaitanya InfraTech  
**Target:** Medium-sized construction company (20–100 staff, multiple simultaneous projects)

---

## 1. Executive Summary

This audit assesses the Construction Management System (CMS) repository against the multi-company, multi-project workflow specifications defined in `docs/MASTER_PROMPT.yaml`.

The codebase is built on an Android client (Kotlin, XML Views with ViewBinding, Material 3 Dark Industrial Console theme) and a Spring Boot 3.4.2 backend (Java 17, Spring Data JPA, PostgreSQL / Supabase, Flyway migrations V1–V13).

### Key Accomplishments & State of Foundations:
1. **Immediate Owner-Requested Fixes Completed**:
   - Application and organization successfully rebranded to **"Chaitanya InfraTech"** across all user-facing strings, navigation labels, login screen, DTOs, tests, and a dedicated Flyway migration (`V13__rename_organization_to_chaitanya_infratech.sql`).
   - Removed role selection dropdown and role credentials cheatsheet from the login screen (`fragment_login.xml`, `LoginFragment.kt`, `bottom_sheet_role_credentials.xml`, `item_role_credential.xml`).
   - Removed demo credentials registry (`dummyCredentialsRegistry`, `dummyUsersRegistry`), hardcoded credentials (`Admin@123`, `Apex@123`, `password123`), and unauthenticated bypasses from both the Android client (`UserRepositoryImpl.kt`, `AuthViewModel.kt`) and the backend (`AuthService.java`). Authentication is strictly validated against BCrypt password hashes in the database.
   - Removed the debug Component Gallery (`ComponentGalleryFragment.kt`, `fragment_component_gallery.xml`, navigation action, and button) from release navigation.
2. **Access Foundation (Phase 1)**:
   - Multi-membership schema (`company_memberships`, `invitations`, enhanced `project_members` with status and company scoping) deployed in Flyway V7.
   - Unified security service (`ProjectSecurityService.java`) providing method-level `@projectAccess.can(#projectId, 'PERM')` checks across controllers (`ProjectController`, `TaskController`, `FinanceController`, `InvitationController`, `MilestoneController`).
   - Unified `/api/v1/me` endpoint (`MeController.java`) returning authenticated user profile, company memberships, project memberships, and aggregated role permissions.
   - Android client dynamically routes authenticated operators to role-specific console dashboards (`RoleHomeRouterFragment.kt` -> `CompanyAdminHomeFragment`, `SiteSupervisorHomeFragment`, `StorekeeperHomeFragment`, `AccountantHomeFragment`, `CivilEngineerHomeFragment`, `HomeDashboardFragment`).
   - Dynamic workspace/project switcher (`WorkspaceSwitcherBottomSheetDialog.kt`) and permission-filtered Quick Create action sheet (`QuickCreateBottomSheetDialog.kt`) active in the Android client shell.

---

## 2. Module & Feature Status Matrix

| Module / Feature | Backend API | Database (Flyway) | Android UI | Status | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Authentication & Session** | Working | V1 Seeded | Working | **Working** | Strict BCrypt password verification via `/api/v1/auth/login`. Session managed in DataStore. |
| **Security & Bypass Guards** | Hardened | N/A | Hardened | **Hardened** | Hardcoded passwords (`Admin@123`, `Apex@123`, `password123`) removed from production code; dev bypasses eliminated. |
| **Identity & Me Endpoint** | Working | V7 | Working | **Working** | `GET /api/v1/me` returns user, company memberships, project memberships, and permissions. |
| **Company Membership** | Working | V7 | Working | **Working** | `company_memberships` table, `CompanyMembership` entity, repository, and DTOs operational. |
| **Project Membership** | Working | V1, V7 | Working | **Working** | Scoped by company and active status; enforced via `@projectAccess.can(#projectId, ...)`. |
| **Invitations & Staff Onboarding** | Working | V7 | Working | **Working** | `InvitationController`, `InvitationService`, and admin invitation action operational. |
| **Role-Aware Home Routing** | N/A | N/A | Working | **Working** | `RoleHomeRouterFragment` dynamically dispatches to specialized role home fragments. |
| **Workspace / Project Switcher** | Working | N/A | Working | **Working** | Bottom sheet dialog switches active project context across all role dashboards. |
| **Permission-Aware Quick Create FAB**| N/A | N/A | Working | **Working** | Bottom sheet filters action tiles according to effective user permissions and roles. |
| **Civil Engineering Calculators** | Working | V3 | Working | **Working** | Concrete, Steel, Brickwork, Excavation calculators fully operational with unit tests. |
| **BOQ & Field Measurements** | Working | V4 | Working | **Working** | BOQ items, unit rates, variance calculations, and measurement books. |
| **Site Operations & Daily Diary** | Working | V4, V9 | Working | **Working** | Daily site reports, labor attendance, site issues, and field verification logs. |
| **Materials & Inventory** | Working | V5, V10 | Working | **Working** | Material catalogue, stock ledger, inventory transactions, and low-stock alerts. |
| **Procurement & POs** | Working | V5, V11 | Working | **Working** | Vendors, purchase orders, purchase requisition items, and Goods Receipt Notes (GRN). |
| **Finance & Expenses** | Working | V5, V11 | Working | **Working** | Budgets, contractor bills, expenses, approval states, and cost dashboard summaries. |
| **Drawings & Versioned Documents**| Working | V6 | Working | **Working** | Document revision tracking and category indexing operational. |
| **Notifications & Audit Trail** | Working | V6, V12 | Working | **Working** | Notification center and immutable audit logging implemented across operations. |
| **Offline Sync Engine** | N/A | N/A | Partial | **Partial** | `SyncQueueRepository` operational; next step: Room/SQLite persistence hardening. |

---

## 3. Deep-Dive Architecture Inspection

### 3.1 Android Client Architecture (`:app`)
- **Package Organization**:
  - `core/`: Network (`NetworkClient`, `AuthInterceptor`, `CmsApiService`), Auth (`SessionDataStore`), Navigation (`AppRoutes`), UI tokens (`tokens/`).
  - `domain/`: Models (`User`, `Role`, `Project`, `BOQ`, `Material`, `Expense`, etc.) and Repository interfaces.
  - `data/`: Network implementations of repositories (`UserRepositoryImpl`, `ProjectRepositoryImpl`, `BoqRepositoryImpl`, etc.).
  - `feature/`: Domain-oriented UI features (`auth`, `dashboard`, `project`, `task`, `finance`, `inventory`, `siteops`, `tools`, `notifications`).
- **Navigation Flow**:
  - Clean authentication gate in `LoginFragment.kt` -> `action_login_to_mainShell`.
  - Single Activity (`MainActivity`) hosting `MainShellFragment`.
  - Main shell features a 4-tab `ViewPager2` driven by `BottomNavigationView` with a center orange Quick Create FAB.
  - Index 0 delegates to `RoleHomeRouterFragment`, which dynamically instantiates the role console based on the authenticated operator:
    - `CompanyAdminHomeFragment`: Company KPIs, staff invitation, active site count, portfolio overview.
    - `SiteSupervisorHomeFragment`: Site diary, labor attendance, issues, and quick DSR submission.
    - `StorekeeperHomeFragment`: Warehouse inventory, pending GRNs, stock movements, low-stock warnings.
    - `AccountantHomeFragment`: Pending expense approvals, contractor bills, committed vs. actual spend.
    - `CivilEngineerHomeFragment`: Structural calculations, BOQ status, measurement books, site inspections.
    - `HomeDashboardFragment`: General project manager portfolio and milestone overview.
- **Design Tokens**:
  - Strictly adheres to the **"Dark Industrial Console"** design specification:
    - Surfaces: Background `#0F0F10`, Surface `#171718`, Card `#1F1F20`, Elevated `#262627`, Border `#2E2E30`.
    - Accent: Safety Orange `#F26B1D` (with `#26F26B1D` tint and `#D95A10` pressed state).
    - Typography: 11sp caption, 14sp body, 16sp subheading, 18sp title, 24/28sp display.
    - Zero Jetpack Compose; 100% Kotlin + XML ViewBinding + Material 3.

### 3.2 Backend Service Architecture (`:backend`)
- **Framework & Dependencies**: Java 17, Spring Boot 3.4.2, Spring Security 6, Spring Data JPA / Hibernate, Flyway 10, JJWT 0.12.6, Lombok.
- **Security & Authorization**:
  - `JwtTokenProvider.java`: Generates signed HS256 JWTs storing `userId` in the subject claim. Expiration: 7 days.
  - `UserPrincipal.java`: Implements `UserDetails`, providing user ID, email, organization ID, and granted authorities.
  - `ProjectSecurityService.java`: Evaluates `@projectAccess.can(#projectId, 'PERM')` and `@projectAccess.isCompanyAdmin()`. Ensures that users can only access resources belonging to their organization and where they hold active project membership or administrative privileges.
  - Method-level security annotations (`@PreAuthorize`) protect all key mutation and query endpoints across `ProjectController`, `TaskController`, `FinanceController`, `InvitationController`, `MilestoneController`, `SiteOperationsController`, `BoqController`, and `MeasurementController`.
  - `AuthService.java`: Verifies passwords strictly through BCrypt hashes; all demo and master password bypasses have been removed.

### 3.3 Database Schema (Flyway V1–V13)
- **V1 (`V1__initial_schema.sql`)**: Base organizations, roles, permissions, users, projects, project_members.
- **V2 (`V2__add_phases_and_tasks.sql`)**: Project phases and task lifecycle.
- **V3 (`V3__add_calculations.sql`)**: Saved civil calculations and inputs.
- **V4 (`V4__add_boq_and_site_ops.sql`)**: BOQ, items, measurement books, daily reports, labor attendance, issues.
- **V5 (`V5__add_inventory_and_finance.sql`)**: Materials, inventory, stock movements, POs, expenses, bills.
- **V6 (`V6__add_documents_notifications_audit.sql`)**: Versioned documents, notifications, audit trail.
- **V7 (`V7__add_company_and_project_memberships.sql`)**: Multi-membership foundation: `company_memberships`, `invitations`, enhanced `project_members`.
- **V8 (`V8__add_project_lifecycle_milestones_and_task_review.sql`)**: Milestones, task review cycle.
- **V9 (`V9__add_site_operations_and_field_verification.sql`)**: Verified progress, inspections, checklists.
- **V10 (`V10__enhance_materials_and_inventory.sql`)**: Enhanced materials, stock thresholds, inventory locations.
- **V11 (`V11__enhance_procurement_and_cost_dashboard.sql`)**: Procurement workflows and cost analytics.
- **V12 (`V12__enhance_reporting_notifications_and_audit.sql`)**: Enhanced notifications, audit triggers, reporting views.
- **V13 (`V13__rename_organization_to_chaitanya_infratech.sql`)**: Updates organization name to "Chaitanya InfraTech".

---

## 4. Security Findings & Remediation Status

### 4.1 Plaintext Credentials in Documentation (Resolved)
- **Status:** **REMEDIATED**.
- `README.md` and repo documentation have been scrubbed of credentials tables, plaintext passwords, and default bypass keys. All configurations now reference environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`).

### 4.2 Unconditional Demo Password Bypass (Resolved)
- **Status:** **REMEDIATED**.
- In `AuthService.java`, the unconditional bypasses (`Apex@123`, `password123`, and `isRolePassword`) have been deleted. Passwords are now verified exclusively via `passwordEncoder.matches(request.password(), user.getHashedPassword())`. Configuration keys (`cms.auth.allow-demo-passwords`) have been removed from `application.yml` and `application-dev.yml`.

### 4.3 Client-Side Credential Leaks & Cheatsheets (Resolved)
- **Status:** **REMEDIATED**.
- The role credentials bottom sheet dialog and demo credentials registries (`dummyCredentialsRegistry`, `dummyUsersRegistry`) were deleted from the Android codebase. The login screen contains only Username/Email, Password, Forgot Password prompt, and Sign In button with autofill hints.

---

## 5. Next Steps: Roadmap Execution Plan

With **0b. Immediate Owner-Requested Fixes** and **Phase 1 (Access Foundation)** fully implemented and verified:

### Phase 2: Project Workspace & Lifecycle
- **Scope**:
  - Full Project detail workspace tabs (Overview, Team, Tasks, Daily Reports, BOQ, Documents, Materials, Expenses, Inspections).
  - Project creation workflow with validation (budget, dates, project type, client details) and initial membership assignment in a single transaction.
  - Work breakdown structure (phases, work packages, task dependencies).
  - Task review lifecycle: `Planned -> Assigned -> In Progress -> Submitted for Review -> Approved/Completed` (with corrections cycle on rejection).
  - Enforce permission gates per tab according to the user's project role.
- **Verification Plan**:
  - Unit tests for project lifecycle state transitions and task review workflow.
  - Integration tests verifying cross-project data isolation.
  - Android UI verification of project tabs and task submission/review dialogs.
