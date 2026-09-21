$errorInput = [System.Console]::In.ReadToEnd()
$logDir = "./log"

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

# Look for an already available file under the /log directory
$existingLogFile = Get-ChildItem -Path $logDir -File | Select-Object -First 1

if ($null -eq $existingLogFile) {
    # Fallback default if no file exists yet
    $logFile = Join-Path $logDir "error-log.txt"
} else {
    # Use the name/path of the already available file under /log
    $logFile = $existingLogFile.FullName
}

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$logEntry = "[$timestamp] $errorInput"
Add-Content -Path $logFile -Value $logEntry