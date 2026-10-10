# Starts every HealthHarbor backend service on Windows (PowerShell).
# Build first:   .\mvnw.cmd -DskipTests package
# Usage:         .\scripts\start-all.ps1          (PostgreSQL)
#                .\scripts\start-all.ps1 h2       (embedded database, no PostgreSQL needed)
param([string]$Profile = "")

$Root = Split-Path -Parent $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$JavaOpts = @("-Xms64m", "-Xmx256m", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1")

function Start-HHService([string]$Name) {
    $jar = Join-Path $Root "$Name\target\$Name-1.0.0.jar"
    if (-not (Test-Path $jar)) { throw "$jar not found - run: .\mvnw.cmd -DskipTests package" }
    $javaArgs = $JavaOpts + @("-jar", $jar)
    if ($Profile) { $javaArgs += "--spring.profiles.active=$Profile" }
    $p = Start-Process java -ArgumentList $javaArgs -WorkingDirectory (Join-Path $Root $Name) -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $LogDir "$Name.log") -RedirectStandardError (Join-Path $LogDir "$Name.err.log") -PassThru
    Set-Content -Path (Join-Path $LogDir "$Name.pid") -Value $p.Id
    Write-Host "started $Name (pid $($p.Id))"
}

function Wait-For([string]$Url, [string]$Name) {
    for ($i = 0; $i -lt 90; $i++) {
        try { Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 $Url | Out-Null; Write-Host "  $Name is up"; return } catch { Start-Sleep 2 }
    }
    Write-Host "  $Name did not become healthy - see logs\$Name.log"
}

Start-HHService "discovery-server"
Wait-For "http://localhost:8761/actuator/health" "discovery-server"
foreach ($svc in "auth-service", "patient-service", "healthcenter-service", "appointment-service", "inventory-service",
                 "healthcamp-service", "sanitation-service", "water-service", "dashboard-service", "api-gateway") {
    Start-HHService $svc
}
Wait-For "http://localhost:8080/actuator/health" "api-gateway"
Write-Host "All services launched. Gateway: http://localhost:8080  Eureka: http://localhost:8761  Logs: $LogDir"
Write-Host "Now start the UI:  cd frontend; npm install; npm run dev   ->  http://localhost:5173"
