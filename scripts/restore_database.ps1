<#
.SYNOPSIS
    Automated PostgreSQL database restore script for Construction Management Platform.
.DESCRIPTION
    Restores a timestamped backup archive created by backup_database.ps1 using pg_restore.
.PARAMETER BackupFile
    Path to the .dump file to restore.
.PARAMETER CleanTarget
    Drops and recreates database objects prior to restoring. Defaults to false.
.EXAMPLE
    .\restore_database.ps1 -BackupFile ".\backups\cms_backup_construction_db_20261030_120000.dump" -CleanTarget
#>

param (
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,
    [switch]$CleanTarget,
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

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  Apex Infra - Automated PostgreSQL Database Restore" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

if (-not (Test-Path $BackupFile)) {
    Write-Error "Backup file does not exist: $BackupFile"
    exit 1
}

$resolvedFile = (Resolve-Path $BackupFile).Path
$fileSizeMB = [math]::Round((Get-Item $resolvedFile).Length / 1MB, 2)

Write-Host "Target Host:     $DbHost:$DbPort"
Write-Host "Target Database: $DbName"
Write-Host "User:            $DbUser"
Write-Host "Source Archive:  $resolvedFile ($fileSizeMB MB)"
Write-Host "Clean First:     $CleanTarget"
Write-Host "----------------------------------------------------"

# Check for pg_restore availability
$pgRestoreCmd = Get-Command "pg_restore" -ErrorAction SilentlyContinue
if (-not $pgRestoreCmd) {
    $standardPaths = @(
        "C:\Program Files\PostgreSQL\16\bin\pg_restore.exe",
        "C:\Program Files\PostgreSQL\15\bin\pg_restore.exe",
        "C:\Program Files\PostgreSQL\14\bin\pg_restore.exe"
    )
    foreach ($p in $standardPaths) {
        if (Test-Path $p) {
            $pgRestoreCmd = $p
            break
        }
    }
}

if (-not $pgRestoreCmd) {
    Write-Error "pg_restore binary could not be found. Please ensure PostgreSQL client tools are installed and in PATH."
    exit 1
}

$restoreArgs = @(
    "--host=$DbHost",
    "--port=$DbPort",
    "--username=$DbUser",
    "--dbname=$DbName",
    "--verbose",
    "--no-owner",
    "--no-privileges"
)

if ($CleanTarget) {
    $restoreArgs += "--clean"
    $restoreArgs += "--if-exists"
}

$restoreArgs += "`"$resolvedFile`""

Write-Host "Starting restore process..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()

try {
    $proc = Start-Process -FilePath $pgRestoreCmd -ArgumentList $restoreArgs -NoNewWindow -Wait -PassThru

    $sw.Stop()
    # pg_restore exit code 0 is total success; code 1 is warnings (non-fatal index notices, etc.)
    if ($proc.ExitCode -eq 0 -or $proc.ExitCode -eq 1) {
        Write-Host "Restore completed successfully in $($sw.Elapsed.TotalSeconds.ToString('F1')) seconds!" -ForegroundColor Green
        exit 0
    } else {
        Write-Error "pg_restore terminated with error code $($proc.ExitCode)."
        exit 1
    }
} catch {
    Write-Error "Restore process encountered an unhandled exception: $_"
    exit 1
}

