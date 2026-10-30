# Disaster Recovery & Database Backup Runbook

## 1. Overview & Strategy

This document describes the automated backup and restore architecture for the Construction Management Platform (Apex Infra). The strategy complies with enterprise standards for civil engineering projects, ensuring zero data loss across multi-tenant organizations, project records, procurement ledgers, and compliance audit logs.

### Key Metrics:
- **Recovery Point Objective (RPO)**: < 1 hour (via automated hourly/daily backups).
- **Recovery Time Objective (RTO)**: < 15 minutes (via automated `pg_restore` automation scripts).
- **Retention Policy**: Standard 30-day rolling window for on-premise/local backups, with weekly long-term snapshots retained for 1 year.

---

## 2. Automated Backup Execution

### PowerShell Script: `scripts/backup_database.ps1`
The backup script performs a transactional, compressed snapshot of the PostgreSQL database using `pg_dump -Fc`:

```powershell
# Run with default parameters (localhost, construction_db, ./backups, 30 days retention)
.\scripts\backup_database.ps1

# Run with custom parameters and remote host
.\scripts\backup_database.ps1 -BackupDir "D:\DatabaseBackups" -RetentionDays 60 -DbHost "192.168.1.50" -DbName "construction_prod"
```

### Automation via Windows Task Scheduler
To schedule daily automated backups at 02:00 AM:
```powershell
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File C:\Users\shashank\AndroidStudioProjects\contructionmanagementapp\scripts\backup_database.ps1 -BackupDir C:\Backups\ApexInfra"
$trigger = New-ScheduledTaskTrigger -Daily -At "02:00AM"
Register-ScheduledTask -TaskName "ApexInfra_Daily_DB_Backup" -Action $action -Trigger $trigger -Description "Daily PostgreSQL backup for Construction Management App"
```

---

## 3. Database Restoration

### PowerShell Script: `scripts/restore_database.ps1`
Restores a previously generated `.dump` archive:

```powershell
# Restore without dropping existing schema
.\scripts\restore_database.ps1 -BackupFile ".\backups\cms_backup_construction_db_20261030_120000.dump"

# Clean target database objects before restoring (full recovery)
.\scripts\restore_database.ps1 -BackupFile ".\backups\cms_backup_construction_db_20261030_120000.dump" -CleanTarget
```

---

## 4. Disaster Recovery Drill Verification Checklist

| Step | Action | Expected Result | Verified |
|---|---|---|---|
| 1 | Execute `backup_database.ps1` | Creates timestamped `.dump` in `./backups/` | [x] |
| 2 | Verify archive size & format | Valid PostgreSQL custom archive (`pg_restore -l <file>`) | [x] |
| 3 | Create fresh sandbox database | `createdb -U postgres construction_dr_test` | [x] |
| 4 | Run `restore_database.ps1` | All tables, foreign keys, indexes, and migrations intact | [x] |
| 5 | Verify Flyway schema history | `SELECT version, description, success FROM flyway_schema_history;` | [x] |
| 6 | Execute health checks | `GET /api/v1/health` returns `200 OK` | [x] |

---

## 5. Security & Isolation Considerations

- **Multi-Tenant Integrity**: All database backups maintain tenant partitioning via `organization_id` foreign keys and Row Level Security constraints.
- **Credential Safety**: No database passwords are hardcoded in scripts or repo files. Passwords are provided via `PGPASSWORD` environment variable or standard `.pgpass` files.
- **Audit Immutability**: The `audit_logs` table is preserved in full fidelity during backup and restore, satisfying statutory civil engineering audit requirements.

