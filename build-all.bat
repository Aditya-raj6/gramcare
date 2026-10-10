@echo off
REM Builds every GramCare service in turn and stops at the first failure,
REM so compile errors show up in one window before you start anything.
REM   build-all.bat            build and run the tests (needs no PostgreSQL; tests use in-memory H2)
REM   build-all.bat skiptests  build only (faster)
cd /d %~dp0
set MVN_ARGS=clean package
if /i "%1"=="skiptests" set MVN_ARGS=clean package -DskipTests

for %%S in (action-service resident-service inventory-service sanitation-service appointment-camp-service web-app) do (
  echo.
  echo ===== Building %%S =====
  pushd %%S
  call mvn -q %MVN_ARGS%
  if errorlevel 1 (
    popd
    echo.
    echo ***** BUILD FAILED in %%S - see the errors above. *****
    exit /b 1
  )
  popd
)
echo.
echo All six services built successfully. Run start-all.bat to launch them.
