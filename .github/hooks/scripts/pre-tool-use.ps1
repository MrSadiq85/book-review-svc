# Copilot CLI sends preToolUse payloads with toolName and toolArgs.
# This hook must emit only one JSON decision object on stdout.

$rawInput = [System.Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($rawInput)) {
    Write-Output '{"permissionDecision":"deny","permissionDecisionReason":"The preToolUse hook received no input."}'
    exit 0
}

try {
    $payload = $rawInput | ConvertFrom-Json -ErrorAction Stop
} catch {
    Write-Output '{"permissionDecision":"deny","permissionDecisionReason":"The preToolUse hook could not validate the tool request."}'
    exit 0
}

function Get-StringValues {
    param([object]$Value)

    if ($null -eq $Value) {
        return
    }

    if ($Value -is [string]) {
        Write-Output $Value
        return
    }

    if ($Value -is [System.Collections.IEnumerable] -and
        -not ($Value -is [System.Collections.IDictionary])) {
        foreach ($item in $Value) {
            Get-StringValues -Value $item
        }
        return
    }

    if ($Value.PSObject -and $Value.PSObject.Properties) {
        foreach ($property in $Value.PSObject.Properties) {
            Get-StringValues -Value $property.Value
        }
    }
}

$toolName = [string]$payload.toolName
$toolArgs = $payload.toolArgs

# Some integrations provide toolArgs as a JSON string rather than an object.
if ($toolArgs -is [string]) {
    try {
        $parsedArgs = $toolArgs | ConvertFrom-Json -ErrorAction Stop
        $toolArgs = $parsedArgs
    } catch {
        # Keep the original string; shell commands still need to be inspected.
    }
}

$valuesToInspect = @()
$valuesToInspect += Get-StringValues -Value $toolArgs
$valuesToInspect += [string]$toolName

# Match an exact .env path/name, but do not block .env.example or dotenv.
$dotEnvPattern = '(?i)(^|[^\w])\.env(?!\.)([^\w]|$)'
$isDotEnvRequest = $false
foreach ($value in $valuesToInspect) {
    if (-not [string]::IsNullOrWhiteSpace($value) -and
        $value -match $dotEnvPattern) {
        $isDotEnvRequest = $true
        break
    }
}

if ($isDotEnvRequest) {
    Write-Output '{"permissionDecision":"deny","permissionDecisionReason":"Access to .env files is strictly prohibited by repository policy."}'
    exit 0
}

# Empty output preserves the normal permission flow for non-sensitive requests.
exit 0