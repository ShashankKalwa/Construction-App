# Construction Management System (CMS) — Repository Audit Report
**Date:** October 2026  
**Auditor:** Antigravity Senior Engineering Agent  
**Repository:** [https://github.com/ShashankKalwa/Construction-App](https://github.com/ShashankKalwa/Construction-App)  
**Target:** Medium-sized construction company (20–100 staff, multiple simultaneous projects)

---

## 1. Executive Summary

This audit assesses the current state of the Construction Management System (CMS) repository against the new multi-company, multi-project workflow specifications defined in `docs/MASTER_PROMPT.yaml`.

The codebase contains a functional baseline Android app (Kotlin, XML Views with ViewBinding, Material 3) and Spring Boot 3.4.2 backend (Java 17, Spring Data JPA, PostgreSQL, Flyway V1–V6). Core computational engines (Concrete, Steel, Brickwork, Excavation) and domain schemas (Phases, BOQ, Inventory, Finance, Documents, Notifications, Audit Logs) have been implemented and seeded.

**Primary Gap Identified:**  
Role-based authentication exists, but all roles currently route to the identical dashboard (`HomeDashboardFragment`) through a fixed 4-tab `ViewPager2` (`MainShellFragment`). There is no dynamic role-based routing or role-tailored console. Access control on the backend is enforced through disparate, ad-hoc service checks rather than a unified `PermissionService` with method-level `@projectAccess` security. Furthermore, projects lack full company ownership scoping and project membership granularity, and credentials are exposed in the repository documentation.

---

## 2. Module & Feature Status Matrix

| Module / Feature | Backend API | Database (Flyway) | Android UI | Status | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Authentication & Session** | Working | V1 Seeded | Working | **Working** | JWT auth working; needs `/api/v1/me` and JWT claims enrichment. |
| **Universal Demo Password Guard** | Partial | N/A | Working | **Security Risk** | Demo & master passwords work in all environments without dev profile check. |
| **Company Membership** | Missing | Missing | Missing | **Missing** | Users have a single `organization_id` foreign key; no multi-membership model. |
| **Project Membership** | Partial | V1 `project_members` | Partial | **Partial** | Basic table exists without status (Active/Inactive), company scoping, or granular roles. |
| **Invitations & Employee Directory**| Missing | Missing | Missing | **Missing** | No invitation entity, invitation flow, or employee management UI. |
| **Role-Aware Home Routing** | N/A | N/A | Missing | **Missing** | All 9 roles land on `HomeDashboardFragment`. No dedicated role consoles. |
| **Workspace / Project Switcher** | Partial | N/A | UI-only | **Partial** | Popup menu exists on home screen; lacks bottom sheet switcher & multi-company support. |
| **Permission-Aware Quick Create FAB**| N/A | N/A | Partial | **Partial** | Bottom sheet shows all options regardless of user permissions. |
| **Civil Engineering Calculators** | Working | V3 `calculations` | Working | **Working** | Calculation engine (Concrete, Steel, Brickwork, Excavation) verified with unit tests. |
| **BOQ & Field Measurements** | Working | V4 `boqs`, `measurements` | Working | **Working** | BOQ master, items, unit rates, and measurement books exist. |
| **Site Operations & Daily Diary** | Working | V4 `daily_site_reports` | Working | **Working** | DSR, labor attendance logs, and site issues are operational. |
| **Materials & Inventory** | Working | V5 `materials`, `stock_mvts` | Working | **Working** | Materials ledger, low stock thresholds, and stock movements in place. |
| **Procurement & POs** | Working | V5 `purchase_orders` | Working | **Working** | Purchase orders, items, vendors, and GRNs implemented. |
| **Finance & Expenses** | Working | V5 `expenses`, `bills` | Working | **Working** | Stepper status exists, but missing self-approval restriction and permission validation. |
| **Drawings & Versioned Documents**| Working | V6 `documents` | Working | **Working** | Document revision tracking and category indexing operational. |
| **Notifications & Audit Trail** | Working | V6 `notifications`, `audit` | Working | **Working** | Notifications and immutable audit logging implemented. |
| **Offline Sync Engine** | N/A | N/A | Partial | **Partial** | `SyncQueueRepository` uses in-memory `CopyOnWriteArrayList`; needs SQLite/Room persistence. |

---

## 3. Deep-Dive Architecture Inspection

### 3.1 Android Client Architecture (`:app`)
- **Package Organization**: Clean architecture layers:
  - `core/`: Network (`NetworkClient`, `AuthInterceptor`, `CmsApiService`), Auth (`SessionDataStore`), UI tokens.
  - `domain/`: Models (`User`, `Role`, `Project`, `BOQ`, `Material`, `Expense`, etc.) and Repository interfaces.
  - `feature/`: Organized by feature domain (`auth`, `dashboard`, `project`, `task`, `tools`, `sync`, `notifications`).
- **Navigation Flow**:
  - `nav_graph.xml`: `loginFragment` navigates directly to `mainShellFragment` via `action_login_to_mainShell`.
  - `MainShellFragment`: Utilizes an internal `ShellPagerAdapter` with 4 fixed tabs:
    - Index 0: `HomeDashboardFragment`
    - Index 1: `ProjectListFragment`
    - Index 2: `TaskListFragment`
    - Index 3: `ToolsFragment`
  - Regardless of whether the authenticated user is a `COMPANY_ADMIN`, `PROJECT_MANAGER`, `CIVIL_ENGINEER`, `SITE_SUPERVISOR`, `STOREKEEPER`, or `ACCOUNTANT`, the user is presented with the identical `HomeDashboardFragment`.
- **Session Management**:
  - `SessionDataStore.kt` persists auth token, user ID, email, name, role ID, organization ID, and active project ID using Jetpack Preferences DataStore.
  - `AuthInterceptor.kt` injects the bearer token on all outbound requests.
- **Design Tokens**:
  - Tokens strictly align with the "Dark Industrial Console" theme:
    - Canvas: `#0F0F10`, Surface: `#171718`, Card: `#1F1F20`, Elevated: `#262627`, Border: `#2E2E30`.
    - Signature Accent: Safety Orange `#F26B1D` (with `#26F26B1D` tint and `#D95A10` pressed state).
    - Typography: 11sp caption, 14sp body, 16sp subheading, 18sp title, 24/28sp display.
    - Radii: 20dp cards, 16dp tiles, 14dp chips, 999dp pills.
    - Zero Jetpack Compose dependencies; 100% Kotlin + XML ViewBinding + Material 3.

### 3.2 Backend Service Architecture (`:backend`)
- **Framework & Dependencies**: Java 17, Spring Boot 3.4.2, Spring Security 6, Spring Data JPA / Hibernate, Flyway 10, JJWT 0.12.6, Lombok.
- **Security & Authorization**:
  - `JwtTokenProvider.java`: Generates signed JWTs storing only `userId` in the subject claim. Expiration: 7 days.
  - `UserPrincipal.java`: Implements Spring Security `UserDetails`, exposing `ROLE_<ROLE_ID>` and granular permission authorities from the user's role.
  - `SecurityConfig.java`: Enables `@EnableMethodSecurity(prePostEnabled = true)`. Authenticates `/api/v1/auth/login`, `/api/v1/health`, `/actuator/**`, and `/api/v1/calculators/**` as public.
- **Permission Enforcement Gaps**:
  - Only 5 controllers have `@PreAuthorize` annotations: `ProjectController`, `TaskController`, `SiteOperationsController`, `MeasurementController`, `BoqController`.
  - Missing `@PreAuthorize` in: `FinanceController`, `InventoryController`, `ProcurementController`, `DocumentController`.
  - Lack of Method Security Expression Bean: No `@projectAccess.can(#projectId, 'PERM')` bean exists.
  - Access control is currently written as procedural helper methods (`validateProjectAccess`) duplicated across services (`ProjectService`, `FinanceService`, `TaskService`).
  - Self-Approval Flaw: In `FinanceService.updateExpenseStatus`, there is no check preventing the submitter (`submittedBy`) from approving their own expense (`setApprovedBy`).
- **Endpoint Structure**:
  - Current me endpoint: `/api/v1/auth/me` returning basic `UserDto`.
  - Missing required `/api/v1/me` returning company memberships, project memberships, and aggregated effective permissions.

### 3.3 Database Schema (Flyway V1–V6)
- **V1 (`V1__initial_schema.sql`)**: `organizations`, `permissions`, `roles`, `role_permissions`, `users`, `projects`, `project_members`.
- **V2 (`V2__add_phases_and_tasks.sql`)**: `phases`, `tasks`.
- **V3 (`V3__add_calculations.sql`)**: `calculations`.
- **V4 (`V4__add_boq_and_site_ops.sql`)**: `unit_rates`, `boqs`, `boq_items`, `measurements`, `daily_site_reports`, `labor_attendance`, `site_issues`.
- **V5 (`V5__add_inventory_and_finance.sql`)**: `materials`, `material_requests`, `stock_movements`, `vendors`, `purchase_orders`, `purchase_order_items`, `goods_receipt_notes`, `expenses`, `contractor_bills`.
- **V6 (`V6__add_documents_notifications_audit.sql`)**: `documents`, `document_versions`, `notifications`, `audit_logs`.
- **Existing Rules & Invariants**:
  - Existing migrations V1–V6 must remain immutable. All schema evolutions must be added in `V7+`.
  - `projects` table has `organization_id` foreign key.
  - `project_members` has `(project_id, user_id)` unique constraint and `assigned_role` text column, but lacks `company_id`, membership `status` (ACTIVE, INVITED, SUSPENDED), and timestamps.

---

## 4. Security Findings & Immediate Remediation

### 4.1 Plaintext Credentials in Documentation (Critical)
- **Finding**: `README.md` lines 60–74 publish a table containing default usernames, emails, and passwords for all 9 operator roles, as well as the universal demo password `Apex@123`.
- **Remediation**:
  1. Remove all credential tables, emails, and default passwords from `README.md`.
  2. Replace with environment variable guidance and standard role description overviews.

### 4.2 Unconditional Master / Demo Password Bypass (Critical)
- **Finding**: In `backend/.../service/AuthService.java` (lines 44–46 and 109–123):
  ```java
  boolean matches = passwordEncoder.matches(request.password(), user.getHashedPassword())
      || "Apex@123".equals(request.password())
      || "password123".equals(request.password())
      || isRolePassword(user.getRole().getId(), request.password());
  ```
  The universal password `Apex@123`, `password123`, and role-based passwords (`Admin@123`, `Manager@123`, `Engineer@123`, etc.) are checked unconditionally in all environments. If deployed to production, any attacker can authenticate as any user.
- **Remediation**:
  1. Inject Spring `Environment` or `@Value("${spring.profiles.active:dev}")`.
  2. Restrict master password bypass exclusively to the `dev` profile.
  3. Add a Spring `@PostConstruct` startup guard that throws an `IllegalStateException` if master demo passwords or dev bypasses are active in any non-dev environment.

### 4.3 Supabase Database Accounts Requiring Password Rotation
The following seeded demo accounts in the live Supabase PostgreSQL database have known passwords (`password123` or role passwords) and must have their passwords rotated or replaced with secure randomly generated credentials for any production deployment:
1. `usr_super_admin_01` (`superadmin@apexinfra.com`)
2. `usr_company_admin_02` (`companyadmin@apexinfra.com`)
3. `usr_pm_rahul` (`rahul.verma@apexinfra.com`)
4. `usr_eng_priya` (`priya.sharma@apexinfra.com`)
5. `usr_pm_03` (`pm@apexinfra.com`)
6. `usr_engineer_04` (`engineer@apexinfra.com`)
7. `usr_supervisor_05` (`supervisor@apexinfra.com`)
8. `usr_storekeeper_06` (`storekeeper@apexinfra.com`)
9. `usr_accountant_07` (`accountant@apexinfra.com`)
10. `usr_contractor_08` (`contractor@apexinfra.com`)
11. `usr_client_09` (`client@apexinfra.com`)
12. Legacy V1 seeds: `usr_admin`, `usr_pm`, `usr_engineer`, `usr_supervisor` at `greenvalley.com`.

---

## 5. Proposed New Database Migrations

### Migration `V7__add_company_and_project_memberships.sql`
To support the enterprise workflow model without breaking existing code:
1. **`company_memberships` table**:
   - `id VARCHAR(50) PRIMARY KEY`
   - `organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE`
   - `user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE`
   - `role_id VARCHAR(50) NOT NULL REFERENCES roles(id)`
   - `status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'` (`ACTIVE`, `INVITED`, `SUSPENDED`)
   - `joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP`
   - `CONSTRAINT uk_org_user UNIQUE (organization_id, user_id)`
2. **`invitations` table**:
   - `id VARCHAR(50) PRIMARY KEY`
   - `organization_id VARCHAR(50) NOT NULL REFERENCES organizations(id) ON DELETE CASCADE`
   - `email VARCHAR(255) NOT NULL`
   - `role_id VARCHAR(50) NOT NULL REFERENCES roles(id)`
   - `invited_by VARCHAR(50) NOT NULL REFERENCES users(id)`
   - `status VARCHAR(30) NOT NULL DEFAULT 'PENDING'` (`PENDING`, `ACCEPTED`, `EXPIRED`, `REVOKED`)
   - `token VARCHAR(100) UNIQUE NOT NULL`
   - `expires_at TIMESTAMP WITH TIME ZONE NOT NULL`
   - `created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP`
3. **Enhance `project_members`**:
   - Add `status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'`.
   - Add `company_id VARCHAR(50) REFERENCES organizations(id)`.
   - Add index on `(project_id, user_id, status)`.
4. **Data Migration / Backfill**:
   - Idempotently populate `company_memberships` from existing `users` table so current seeded users automatically have active company memberships.
   - Update `project_members.company_id` from parent `projects.organization_id`.

---

## 6. Verification Status

The current codebase compiles and passes test verification:
- `./gradlew :app:testDebugUnitTest :backend:test`: **All tests PASSED (Build successful)**.
- Android APK assemble: Verified working (`app-debug.apk`).
- Backend bootJar & Render deployment: Live and healthy (`https://construction-app-eepz.onrender.com/api/v1/health`).
