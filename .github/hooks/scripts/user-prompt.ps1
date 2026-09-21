
# 1. Read the JSON payload streamed from GitHub Copilot CLI via stdin
$inputJson = [System.Console]::In.ReadToEnd()

# 2. Parse the JSON string into a PowerShell object
$payload = $inputJson | ConvertFrom-Json
$userPrompt = $payload.prompt

# 3. Define log directory and find an existing file inside it
$logDir = "./log"

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

# Look for any existing file in the ./log directory
$existingLogFile = Get-ChildItem -Path $logDir -File | Select-Object -First 1

if ($null -eq $existingLogFile) {
    # If no file exists yet, create a default one (e.g., session-log.txt)
    $timestamp = Get-Date -Format "MMddyyyy_HHmmss"
    $logFile = Join-Path $logDir "log_${timestamp}.txt"
    Add-Content -Path $logFile -Value "Session Started"
} else {
    # Use the name/path of the already available file under the /log directory
    $logFile = $existingLogFile.FullName
}

# 4. Append timestamp and the user prompt to the discovered log file
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$logEntry = "[$timestamp] $userPrompt"
Add-Content -Path $logFile -Value $logEntry