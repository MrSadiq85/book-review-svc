[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$TaskName
)

$ErrorActionPreference = "Stop"

$ArchivedRoot = "archived"
# Sanitize task name for folder creation
$SafeTaskName = $TaskName -replace '[\\/:*?"<>|]', '_'
$TaskDir = Join-Path $ArchivedRoot $SafeTaskName
$WorkingDir = "working"

# 1. Create archived/<task> subdirectory if it doesn't exist
Write-Host "Creating archive directory: $TaskDir" -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path $TaskDir | Out-Null

# 2. Check if working directory exists and has files
if (-not (Test-Path $WorkingDir) -or (Get-ChildItem -Path $WorkingDir -Force | Measure-Object).Count -eq 0) {
    Write-Host "Warning: 'working/' directory is empty or does not exist." -ForegroundColor Yellow
    exit 0
}

# 3. Move all files from working/ to archived/<task>/ (leaving working/ intact)
Write-Host "Moving files from '$WorkingDir/' to '$TaskDir/'..." -ForegroundColor Cyan
Move-Item -Path "$WorkingDir\*" -Destination $TaskDir -Force

# 4. Add files under newly created folder to git
Write-Host "Staging files in Git..." -ForegroundColor Cyan
$GitPath = "$ArchivedRoot/$SafeTaskName/*"
git add $GitPath

Write-Host "Success: Files moved to '$TaskDir/' and staged in Git (working/ directory retained)." -ForegroundColor Green