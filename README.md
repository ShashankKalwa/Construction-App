# Chaitanya InfraTech — Construction Management System (CMS)

A production-grade, role-based native Android application and Spring Boot backend designed for construction contractors, site engineers, project managers, and clients. It replaces fragmented paper notebooks, manual Excel sheets, and unstructured messaging groups with a unified, high-reliability system.

---

## Architecture & Technology Stack

| Layer | Technologies & Tools |
| :--- | :--- |
| **Android Client** | Native Android (Kotlin, XML Views with **ViewBinding**, Material 3) |
| **Design Language** | "Dark Industrial Console" — `#0F0F10` charcoal canvas, 1px soft borders, high outdoor daylight contrast, signature `#F26B1D` safety orange accent |
| **Mobile Architecture**| MVVM + Clean Architecture, Kotlin Coroutines, StateFlow, Retrofit 2, OkHttp3, Room/SQLite Offline Sync Queue, DataStore |
| **Backend Service** | Java 17 / Spring Boot 3.4 (Spring Web, Spring Data JPA / Hibernate, Spring Security, JJWT) |
| **Database & Migration**| PostgreSQL 16 (Hosted on **Supabase** & containerized via `docker-compose.yml`), Flyway database migrations |
| **Hosting & CI/CD** | **Render** Web Service (Auto-deploy from GitHub), GitHub Actions Keep-Alive Workflow |

---

## System Architecture Diagram

```
┌────────────────────────────────────────────────────────┐
│                   Android Native App                   │
│      (Kotlin + ViewBinding + Dark Industrial UI)       │
│                                                        │
│  [Activities / Fragments] ──► [ViewModels (StateFlow)] │
│                                      │                 │
│                                      ▼                 │
│   [Offline Sync Engine]    [NetworkClient (Retrofit)]  │
│   (SQLite / Room Queue)              │                 │
└──────────────────────────────────────┼─────────────────┘
                                       │ HTTPS (JWT Auth)
                                       ▼
┌────────────────────────────────────────────────────────┐
│               Spring Boot 3.4.2 Backend                │
│                 (Deployed on Render)                   │
│                                                        │
│  [REST Controllers] ──► [Services] ──► [Repositories]  │
│                                              │         │
└──────────────────────────────────────────────┼─────────┘
                                               │ JDBC (SSL)
                                               ▼
┌────────────────────────────────────────────────────────┐
│                  Supabase PostgreSQL                   │
│       (Flyway Migrations V1-V12 + Seeded Test Data)    │
└────────────────────────────────────────────────────────┘
```

---

## Features & Implementation Highlights

### 1. Role-Based Access Control (RBAC) & Authentication
- **Roles**: All 9 enterprise roles supported with specific permission scopes.
- **Enterprise Authentication**: User credentials authenticate against BCrypt-hashed database records with strict session management.
- Secure JWT bearer authentication with token expiration, auto-header injection via `AuthInterceptor`, and method-level backend security (`@PreAuthorize`).
- Clean user session management backed by encrypted `SessionDataStore`.

#### Operator Role Scopes

| Role | Primary Scope | Access Scope |
| :--- | :--- | :--- |
| **Super Admin** | Full enterprise console control | Cross-company platform administration |
| **Company Admin** | Operations & organization setup | Company profile, employees, invitations, all company projects |
| **Project Manager** | Milestones, tasks & stage approvals | Project delivery, team coordination, approvals, budget |
| **Civil Engineer** | Technical calculations, BOQ & drawings | Quantities, BOQ items, calculations, inspections, drawings |
| **Site Supervisor** | Daily logs, labor counts & roadblocks | Daily site diary, labor attendance, material requests |
| **Storekeeper** | Material GRN, stock issues & ledger | Stock ledger, goods receipts, material issues, low-stock alerts |
| **Accountant** | Budgets, billing, petty cash & payouts | Expenses, contractor bills, payments, budget variance |
| **Contractor** | Work packages, claims & labor supply | Assigned work packages, progress submissions |
| **Client Owner** | Executive dashboard & audit reports | Approved progress, milestone oversight, invoice tracking |

### 2. Civil Engineering Calculation Engine
Native calculation tools complying with standard Indian civil engineering codes:
- **Concrete Mix Design**: Volume calculations for M15, M20, and M25 with dry-volume factor (1.54) calculating cement bags, sand volume, aggregate, and water.
- **Brickwork & Mortar Estimator**: Standard modular brick dimensions with mortar joint allowances, cement-to-sand mortar ratios, and wastage factors.
- **Steel Rebar Weight Calculator**: Unit weight derived from $D^2 / 162 \times L$ supporting rebar diameters from 8mm to 32mm.
- **Excavation & Backfill**: Trench and pit volumes with soil expansion/shrinkage adjustments.
- **Plastering & Flooring**: Wall surface areas, tile counts, and adhesive mortar estimates.

### 3. Project Workspace, Milestones & Task Coordination
- **Phased Milestones**: Structural milestones with date constraints, % completion, and stage gating.
- **Task Management**: Assignment by role/worker, priority weighting, status lifecycles (`PENDING`, `IN_PROGRESS`, `BLOCKED`, `COMPLETED`), and photo attachments.
- **Team Management**: Project-level memberships, multi-role assignment, and invitation lifecycles.

### 4. Site Operations, Field Verification & Inspections
- **Daily Site Diary**: Labor headcount, contractor rosters, weather conditions, work progress summaries, and site narratives.
- **Site Photos & Progress Verification**: Digital photo logs with geotagging metadata and verification review flows.
- **Quality & Safety Inspections**: Digital snag lists with pass/fail statuses, corrective action notes, and defect reinspection loops.

### 5. Materials Management & Stock Inventory Ledger
- **Master Material Catalog**: Construction materials categorized with standard units (bags, MT, brass, sq.ft).
- **Immutable Stock Ledger**: Real-time warehouse balance tracking, stock issues, site transfers, and physical stock returns.
- **Stock Threshold Alerts**: Real-time warnings when inventory levels drop below defined minimum safety buffers.

### 6. Finance, Procurement, RA Bills & Budget Dashboard
- **Purchase Orders (PO) & GRN**: Vendor commitments, line-item quantity tracking, and physical delivery Goods Receipt Notes (GRN).
- **Contractor RA Billing**: Running Account bills with automated deductions for 5% retention money and 2% TDS.
- **Expense Dual-Control Workflow**: Multi-tier approvals (`SUBMITTED -> VERIFIED -> APPROVED -> PAID`) preventing self-approval by submitters.
- **Live Cost Dashboard**: Real-time variance tracking comparing Approved Budget, Actual Cost, Committed Cost, and category-level expenditure.

### 7. Collaboration, Notifications & Immutable Audit Trail
- **In-App Notification Engine**: Real-time alerts for stock warnings, approval decisions, task reviews, payment disbursements, and document releases.
- **Company Portfolio vs Project Telemetry**: Executive-level aggregation across all company projects alongside project-specific drill-downs with strict multi-tenant boundary isolation.
- **Immutable Audit Trail**: Append-only compliance log capturing actor ID, operation, entity type, project ID, details, and IP address for all mutations.

### 8. Hardened Offline-First Sync & Conflict Resolution
- **Zero-Silent-Overwrite Guarantee**: HTTP 409 conflict detection halts auto-sync when local edits clash with newer server data.
- **3-Way Resolution Console**: Dark Industrial UI dialog enabling field engineers to choose **Discard Local**, **Merge**, or **Force Overwrite**.
- **Idempotency Keys**: UUID-based idempotency keys on all network transactions preventing duplicate financial disbursements or inventory adjustments.

---

## Repository Structure

```
├── app/                                # Android Native Client (Zero Compose, Pure Kotlin + XML ViewBinding)
│   ├── src/main/java/.../
│   │   ├── core/                       # Network, Auth, Session & Theme tokens
│   │   ├── domain/model/               # Domain entities (Project, Task, Material, Finance, Sync, Audit)
│   │   ├── domain/repository/          # Repository contracts & offline cache implementations
│   │   └── feature/                    # UI screens (Auth, Dashboard, Project, SiteOps, Inventory, Finance, Sync)
│   ├── src/main/res/                   # Layouts, dark industrial drawables, styles & themes
│   └── build.gradle.kts                # Android dependencies and BuildConfig injection
├── backend/                            # Spring Boot 3.4.2 Service (Pure Java 17 + Spring Security)
│   ├── src/main/java/.../              # REST Controllers, Services, JPA Entities & Repositories
│   ├── src/main/resources/
│   │   ├── application.yml             # Base Spring profile
│   │   ├── application-dev.yml         # Development profile (reads environment variables)
│   │   └── db/migration/               # Flyway SQL migrations (V1 - V12)
│   └── build.gradle.kts                # Backend Gradle configuration
├── docs/                               # Architecture guidelines, AUDIT.md, and BACKUP_AND_RESTORE.md
├── scripts/                            # PowerShell automation scripts (backup_database.ps1, restore_database.ps1)
├── .github/workflows/
│   └── keep_alive.yml                  # Automated cron pinging Render & Supabase
├── docker-compose.yml                  # Local PostgreSQL 16 container definition
└── local.properties                    # Local private configurations (IGNORED by Git)
```

---

## Security & Credential Isolation

This project adheres to a **Zero-Leakage Policy**: no passwords, secret keys, or environment-specific URLs are committed to version control.

### 1. Android App Backend URL Isolation
The live backend URL is resolved dynamically at compile time from your local environment:
- Stored privately in [`local.properties`](file:///local.properties):
  ```properties
  cms.backend.url=https://construction-app-eepz.onrender.com/api/v1/
  ```
- `local.properties` is strictly ignored by [`.gitignore`](file:///.gitignore).
- Gradle automatically injects this property into `BuildConfig.BACKEND_BASE_URL` when compiling debug or release APKs.
- If not configured locally, Gradle falls back safely to Android emulator localhost (`http://10.0.2.2:8080/api/v1/`).

### 2. Backend Database Credentials
- Database credentials (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) are provided via **Environment Variables** in Render's encrypted settings or `.env` files.
- [`backend/src/main/resources/application-dev.yml`](file:///backend/src/main/resources/application-dev.yml) uses parameter substitution with safe localhost defaults for developers working with local Docker containers:
  ```yaml
  spring:
    datasource:
      url: ${DB_URL:jdbc:postgresql://localhost:5432/cms_db}
      username: ${DB_USERNAME:postgres}
      password: ${DB_PASSWORD:postgres}
  ```

---

## Setup & Running the Project

### Prerequisites
- **Java**: JDK 17 (Configured as Gradle JVM)
- **Android Studio**: Ladybug / Meerkat or newer
- **Android SDK**: `compileSdk = 37`, `minSdk = 24`, `targetSdk = 37`

---

### Step 1: Configure Android Local Properties
Create or open [`local.properties`](file:///local.properties) in the root directory:
```properties
sdk.dir=C\:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
cms.backend.url=https://construction-app-eepz.onrender.com/api/v1/
```
*(Replace `cms.backend.url` with `http://10.0.2.2:8080/api/v1/` if testing against a local backend on an Android emulator).*

---

### Step 2: Running the Spring Boot Backend

#### Option A: Run Locally with Docker Database
1. Start local PostgreSQL:
   ```bash
   docker compose up -d
   ```
2. Run backend service:
   ```bash
   ./gradlew :backend:bootRun
   ```
3. Verify backend health:
   ```bash
   curl http://localhost:8080/api/v1/health
   ```

#### Option B: Run with Cloud Database (Supabase)
Pass the connection parameters via environment variables:
```bash
# PowerShell
$env:DB_URL="jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres.<your-project-ref>"
$env:DB_PASSWORD="<your-db-password>"
$env:JWT_SECRET="<your-256-bit-jwt-secret>"
./gradlew :backend:bootRun
```

---

### Step 3: Building & Running the Android App

#### Build Debug APK via CLI
```bash
./gradlew :app:assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

#### Run in Android Studio
1. Open the project in Android Studio.
2. Select **app** in the run configuration drop-down.
3. Select an emulator or connected physical Android device.
4. Click **Run** ▶️.

---

## Testing & Verification

Run the comprehensive test suites across both modules:

```bash
# Run Android client unit tests
./gradlew :app:test

# Run Spring Boot backend tests
./gradlew :backend:test

# Run full project validation
./gradlew check
```

---

## Cloud Deployment & Reliability

### Render Web Service Deployment
- **Live Production URL**: `https://construction-app-eepz.onrender.com`
- **Health Check Endpoint**: `https://construction-app-eepz.onrender.com/api/v1/health`
- **Actuator Health Endpoint**: `https://construction-app-eepz.onrender.com/actuator/health`

#### Deployment Configuration
1. Connect the GitHub repository to [Render](https://render.com).
2. Create a new **Web Service** using the root `Dockerfile`:
   - **Environment**: `Docker` (Automatically detected via multi-stage [`Dockerfile`](file:///Dockerfile))
   - **Base Image**: Eclipse Temurin 17 (JDK for build stage, minimal JRE 17 for runtime image)
   - **Health Check Path**: `/api/v1/health`
   - **Exposed Port**: Auto-bound to Render's dynamic `$PORT`
3. Set the following **Environment Variables** in the Render Dashboard:
   - `DB_URL`: Supabase pooled connection string (`jdbc:postgresql://<host>:5432/postgres?sslmode=require`)
   - `DB_USERNAME`: Supabase database username
   - `DB_PASSWORD`: Supabase database password
   - `JWT_SECRET`: Minimum 256-bit secret key for secure JWT signing

### Automated Keep-Alive
Render free-tier instances sleep after 15 minutes of inactivity. To ensure high availability and prevent cold starts:
- The [`.github/workflows/keep_alive.yml`](file:///.github/workflows/keep_alive.yml) cron job pings `/api/v1/health` every **14 minutes**.
- This simultaneously queries PostgreSQL, ensuring both Render and Supabase remain active and warm.
