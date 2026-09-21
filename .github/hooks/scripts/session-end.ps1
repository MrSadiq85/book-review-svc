$sourceDir = "./log"
$destDir = "./copilot-arch-log"

if (-not (Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir | Out-Null
}

if (Test-Path $sourceDir) {
    Get-ChildItem -Path $sourceDir -File | ForEach-Object {
        $destPath = Join-Path $destDir $_.Name
        Move-Item -Path $_.FullName -Destination $destPath -Force

        # Add file into git tracking
        git add $destPath
    }
}