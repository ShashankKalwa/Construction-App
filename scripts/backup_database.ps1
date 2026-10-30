<#
.SYNOPSIS
    Automated PostgreSQL database backup script for Construction Management Platform.
.DESCRIPTION
    Creates timestamped database dumps, verifies backup integrity, prunes older backups,
    and supports custom retention policies.
.PARAMETER BackupDir
    Destination directory where backups are stored. Defaults to ./backups.
.PARAMETER RetentionDays
    Number of days to retain backups before automated pruning. Defaults to 30.
.EXAMPLE
    .\backup_database.ps1 -BackupDir "C:\backups\cms" -RetentionDays 14
#>

param (
    [string]$BackupDir = ".\backups",
    [int]$RetentionDays = 30,
    [string]$DbHost = $env:DB_HOST,
    [string]$DbPort = $env:DB_PORT,
    [string]$DbName = $env:DB_NAME,
    [string]$DbUser = $env:DB_USERNAME
)

# Fallback defaults for local development
if (-not $DbHost) { $DbHost = "localhost" }
if (-not $DbPort) { $DbPort = "5432" }
if (-not $DbName) { $DbName = "construction_db" }
if (-not $DbUser) { $DbUser = "postgres" }

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$targetDir = Resolve-Path -Path $BackupDir -ErrorAction SilentlyContinue
if (-not $targetDir) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
    $targetDir = Resolve-Path -Path $BackupDir
}

$backupFile = Join-Path $targetDir "cms_backup_${DbName}_${timestamp}.dump"
$logFile = Join-Path $targetDir "backup_${timestamp}.log"

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  Apex Infra - Automated PostgreSQL Database Backup" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Host:        $DbHost:$DbPort"
Write-Host "Database:    $DbName"
Write-Host "User:        $DbUser"
Write-Host "Target:      $backupFile"
Write-Host "Retention:   $RetentionDays days"
Write-Host "----------------------------------------------------"

# Check for pg_dump availability
$pgDumpCmd = Get-Command "pg_dump" -ErrorAction SilentlyContinue
if (-not $pgDumpCmd) {
    Write-Warning "pg_dump not found in system PATH. Attempting standard PostgreSQL paths..."
    $standardPaths = @(
        "C:\Program Files\PostgreSQL\16\bin\pg_dump.exe",
        "C:\Program Files\PostgreSQL\15\bin\pg_dump.exe",
        "C:\Program Files\PostgreSQL\14\bin\pg_dump.exe"
    )
    foreach ($p in $standardPaths) {
        if (Test-Path $p) {
            $pgDumpCmd = $p
            break
        }
    }
}

if (-not $pgDumpCmd) {
    Write-Error "pg_dump binary could not be found. Please ensure PostgreSQL client tools are installed and in PATH."
    exit 1
}

$sw = [System.Diagnostics.Stopwatch]::StartNew()

try {
    Write-Host "Starting pg_dump execution..." -ForegroundColor Yellow
    
    # Execute pg_dump in custom directory/compressed archive format (-Fc)
    $env:PGHOST = $DbHost
    $env:PGPORT = $DbPort
    $env:PGDATABASE = $DbName
    $env:PGUSER = $DbUser

    & $pgDumpCmd -Fc -v -f "$backupFile" 2>&1 | Tee-Object -FilePath $logFile

    if ($LASTEXITCODE -eq 0 -and (Test-Path $backupFile)) {
        $sw.Stop()
        $fileSizeMB = [math]::Round((Get-Item $backupFile).Length / 1MB, 2)
        Write-Host "Backup completed successfully!" -ForegroundColor Green
        Write-Host "Backup size: $fileSizeMB MB in $($sw.Elapsed.TotalSeconds.ToString('F1')) seconds" -ForegroundColor Green

        # Automated retention pruning
        Write-Host "Checking for backups older than $RetentionDays days..." -ForegroundColor Gray
        $cutoffDate = (Get-Date).AddDays(-$RetentionDays)
        $oldBackups = Get-ChildItem -Path $targetDir -Filter "cms_backup_*.dump" | Where-Object { $_.LastWriteTime -lt $cutoffDate }
        
        foreach ($old in $oldBackups) {
            Write-Host "Pruning expired backup: $($old.Name)" -ForegroundColor DarkGray
            Remove-Item $old.FullName -Force
        }
        
        exit 0
    } else {
        $sw.Stop()
        Write-Error "pg_dump failed with exit code $LASTEXITCODE. Check log: $logFile"
        exit 1
    }
} catch {
    Write-Error "Backup process encountered an unhandled exception: $_"
    exit 1
}

