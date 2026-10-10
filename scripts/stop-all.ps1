# Stops services started by start-all.ps1
$LogDir = Join-Path (Split-Path -Parent $PSScriptRoot) "logs"
Get-ChildItem -Path $LogDir -Filter *.pid -ErrorAction SilentlyContinue | ForEach-Object {
    $id = Get-Content $_.FullName
    Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
    Write-Host "stopped $($_.BaseName) ($id)"
    Remove-Item $_.FullName
}
