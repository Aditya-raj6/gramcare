@echo off
REM Starts all six GramCare services in separate windows (Windows).
REM Requires Maven (mvn) on PATH and PostgreSQL running with the gramcare_* databases.
REM If your PostgreSQL password is not "postgres", run:  set DB_PASSWORD=yourpassword   before this script.
cd /d %~dp0
start "action-service :8085"           cmd /k "cd action-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul
start "resident-service :8081"         cmd /k "cd resident-service && mvn spring-boot:run"
start "inventory-service :8083"        cmd /k "cd inventory-service && mvn spring-boot:run"
start "sanitation-service :8084"       cmd /k "cd sanitation-service && mvn spring-boot:run"
start "appointment-camp-service :8082" cmd /k "cd appointment-camp-service && mvn spring-boot:run"
timeout /t 10 /nobreak >nul
start "web-app :8080"                  cmd /k "cd web-app && mvn spring-boot:run"
echo Services are starting. Open http://localhost:8080 once all windows show "Started ...Application".
