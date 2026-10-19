# Construction Management System (CMS)

A role-based native Android application and Spring Boot backend for construction project management, field tracking, civil engineering calculations, BOQ, inventory, labor, expenses, documents, and reports.

## Architecture & Technology Stack
- **Android App**: Native Android (Jetpack Compose + Material 3)
  - Theme: "Dark Industrial Console" — charcoal surfaces, 1px soft borders, high outdoor contrast, signature orange accent (`#F26B1D`).
  - Architecture: MVVM + Clean Architecture
- **Backend**: Kotlin + Spring Boot 3.4 (Spring Web, Spring Data JPA / Hibernate, Spring Security with method-level `@PreAuthorize`, Flyway, PostgreSQL, JJWT)
- **Database**: PostgreSQL (containerized via `docker-compose.yml`)
- **Base Package**: `com.example.contruction_management_app`

## Multi-Module Project Structure
- `app/` — Native Android client module
- `backend/` — Spring Boot 3.4 (Kotlin) backend service module
- `docs/` — Project specifications and master prompts (`MASTER_PROMPT.yaml`)
- `docker-compose.yml` — Local PostgreSQL 16 database definition

## Run Instructions

### 1. Database Setup

#### Option A: Supabase PostgreSQL (Cloud - Free Perpetual)
1. Go to your [Supabase Dashboard](https://supabase.com) -> **SQL Editor**.
2. Copy and paste the contents of `supabase_schema_and_seed.sql` and click **Run**.
3. In `backend/src/main/resources/application-dev.yml` (or via environment variables), provide your Supabase connection:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://db.<your-project-ref>.supabase.co:5432/postgres?sslmode=require
       username: postgres
       password: <your-database-password>
   ```

#### Option B: Local PostgreSQL (Docker)
Start the local PostgreSQL database using Docker Compose:
```bash
docker compose up -d
```

### 2. Spring Boot Backend
To run the Spring Boot backend service on port 8000:
```bash
./gradlew :backend:bootRun
```
Health Check:
```bash
curl http://localhost:8000/api/v1/health
```

To build and test the backend module:
```bash
./gradlew :backend:build
```

### 3. Android Client App
Open the project in Android Studio (JDK 17+ configured for Gradle JVM).
To build the debug APK:
```bash
./gradlew :app:assembleDebug
```
The resulting APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Deploying Backend to Render (Monorepo)
You can push this whole repository as a single Monorepo to GitHub and deploy the backend to Render:

1. **Push repository to GitHub**:
   ```bash
   git add .
   git commit -m "feat: initial CMS setup with Android app and Spring Boot backend"
   git push origin main
   ```
2. **On Render Dashboard**:
   - Click **New +** -> **Web Service** (or **Blueprint** using `render.yaml`).
   - Connect your GitHub repository.
   - Settings:
     - **Name**: `cms-backend`
     - **Runtime**: `Java`
     - **Build Command**: `./gradlew :backend:bootJar`
     - **Start Command**: `java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar`
     - **Health Check Path**: `/api/v1/health`
   - **Environment Variables**:
     - `DB_URL`: `jdbc:postgresql://db.<your-project-ref>.supabase.co:5432/postgres?sslmode=require`
     - `DB_USERNAME`: `postgres`
     - `DB_PASSWORD`: `<your-supabase-password>`
     - `JWT_SECRET`: *(A random 256-bit string or generate one)*
3. **Connect Android App to Hosted Backend**:
   - Once Render provides your live URL (e.g. `https://cms-backend-xxxx.onrender.com`), update `BACKEND_BASE_URL` in `app/build.gradle.kts`.
