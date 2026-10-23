# Construction Management System (CMS)

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
│       (Flyway Migrations V1-V6 + Seeded Test Data)     │
└────────────────────────────────────────────────────────┘
```

---

## Features & Implementation Highlights

### 1. Role-Based Access Control (RBAC) & Authentication
- **Roles**: All 9 enterprise roles supported with specific permission scopes.
- **Username & Password Authentication**: Dedicated credentials per operator role with auto-fill support and master demo pass (`Apex@123`).
- Secure JWT bearer authentication with token expiration, auto-header injection via `AuthInterceptor`, and method-level backend security (`@PreAuthorize`).
- Clean user session management backed by encrypted `SessionDataStore`.

#### Default Operator Role Credentials

| Role | Username | Email | Default Password | Primary Scope |
| :--- | :--- | :--- | :--- | :--- |
| **Super Admin** | `superadmin` | `superadmin@apexinfra.com` | `Admin@123` | Full enterprise console control |
| **Company Admin** | `companyadmin` | `companyadmin@apexinfra.com` | `Company@123` | Operations & organization setup |
| **Project Manager** | `pm` | `pm@apexinfra.com` | `Manager@123` | Milestones, tasks & stage approvals |
| **Civil Engineer** | `engineer` | `engineer@apexinfra.com` | `Engineer@123` | Technical calculations, BOQ & drawings |
| **Site Supervisor** | `supervisor` | `supervisor@apexinfra.com` | `Site@123` | Daily logs, labor counts & roadblocks |
| **Storekeeper** | `storekeeper` | `storekeeper@apexinfra.com` | `Store@123` | Material GRN, stock issues & ledger |
| **Accountant** | `accountant` | `accountant@apexinfra.com` | `Accounts@123` | Budgets, billing, petty cash & payouts |
| **Contractor** | `contractor` | `contractor@apexinfra.com` | `Contract@123` | Work packages, claims & labor supply |
| **Client Owner** | `client` | `client@apexinfra.com` | `Client@123` | Executive dashboard & audit reports |
| *Universal Demo* | *(any)* | *(any)* | `Apex@123` | Master password for development/demo |

### 2. Civil Engineering Calculation Engine
Native calculation tools complying with standard Indian civil engineering codes:
- **Concrete Mix Design**: Volume calculations for M15, M20, and M25 with dry-volume factor (1.54) calculating cement bags, sand volume, aggregate, and water.
- **Brickwork & Mortar Estimator**: Standard modular brick dimensions with mortar joint allowances, cement-to-sand mortar ratios, and wastage factors.
- **Steel Rebar Weight Calculator**: Unit weight derived from $D^2 / 162 \times L$ supporting rebar diameters from 8mm to 32mm.
- **Excavation & Backfill**: Trench and pit volumes with soil expansion/shrinkage adjustments.
- **Plastering & Flooring**: Wall surface areas, tile counts, and adhesive mortar estimates.

### 3. BOQ & Field Operations
- **Bill of Quantities (BOQ)**: Itemized work schedules, unit rates, consumed quantities, and budget variance indicators.
- **Daily Site Diary**: Daily labor headcount, contractor presence, weather conditions, and site narratives.
- **Safety & Quality Inspection**: Digital snag lists with pass/fail statuses, corrective notes, and incident severity tracking.

### 4. Inventory, Procurement & Financial Tracking
- **Inventory Ledger**: Real-time stock levels, Goods Receipt Notes (GRN), and material issues.
- **Low-Stock Alerts**: Automatic warnings when critical construction supplies drop below safety thresholds.
- **Petty Cash & Expense Approvals**: On-site expense submissions with receipt attachments and manager approval workflows.
- **Contractor & Client Invoicing**: Milestone payment disbursements and client payment tracking.

### 5. Drawing & Document Management
- Blueprint and architectural revision control with semantic version tags.
- Offline document cache for uninterrupted field inspection.
- System-wide immutable audit trail capturing user, action, and timestamp for all data mutations.

### 6. Offline-First Synchronization Engine
- Background sync queue (`SyncQueueItem`) storing pending mutations when network is unavailable.
- Network connectivity listener triggers automatic background synchronization upon reconnection.
- Idempotent API requests preventing duplicate ledger or inventory entries.

---

## Repository Structure

```
├── app/                                # Android Native Client
│   ├── src/main/java/.../
│   │   ├── core/                       # Network, Auth, Session & Theme tokens
│   │   ├── domain/model/               # Domain entities (BOQ, Inventory, Finance, etc.)
│   │   ├── domain/repository/          # Repository contracts
│   │   └── feature/                    # UI screens (Auth, Dashboard, Project, Tools, etc.)
│   ├── src/main/res/                   # Layouts, dark industrial drawables & styles
│   └── build.gradle.kts                # Android dependencies and BuildConfig injection
├── backend/                            # Spring Boot 3.4.2 Service
│   ├── src/main/java/.../              # Controllers, Services, Entities & Repositories
│   ├── src/main/resources/
│   │   ├── application.yml             # Base Spring profile
│   │   ├── application-dev.yml         # Development profile (reads environment variables)
│   │   └── db/migration/               # Flyway SQL migrations (V1 - V6)
│   └── build.gradle.kts                # Backend Gradle configuration
├── docs/                               # Architecture guidelines and MASTER_PROMPT.yaml
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
1. Connect the GitHub repository to [Render](https://render.com).
2. Create a new **Web Service** with the following configuration:
   - **Environment**: `Java`
   - **Build Command**: `./gradlew :backend:bootJar`
   - **Start Command**: `java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar`
   - **Health Check Path**: `/api/v1/health`
3. Add the following **Environment Variables** in Render Dashboard:
   - `DB_URL`: Supabase pooled connection string
   - `DB_USERNAME`: Supabase database user
   - `DB_PASSWORD`: Supabase database password
   - `JWT_SECRET`: Minimum 256-bit secret key

### Automated Keep-Alive
Render free-tier instances sleep after 15 minutes of inactivity. To ensure high availability and prevent cold starts:
- The [`.github/workflows/keep_alive.yml`](file:///.github/workflows/keep_alive.yml) cron job pings `/api/v1/health` every **14 minutes**.
- This simultaneously queries PostgreSQL, ensuring both Render and Supabase remain active and warm.
