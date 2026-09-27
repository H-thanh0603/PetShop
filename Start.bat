@echo off
setlocal
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
if not defined PETSHOP_URL set "PETSHOP_URL=http://localhost:8080/home"
set "WAR_FILE=%PROJECT_ROOT%\build\libs\petshop-boot.war"

echo Step 1: Building bootWar...
cd /d "%PROJECT_ROOT%"
call gradlew.bat bootWar -x test
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build that bai!
    pause
    exit /b 1
)

echo Step 2: Starting PetShop ^(java -jar, khong can Tomcat ngoai^)...
start "PetShop" cmd /c "java -jar %WAR_FILE%"

echo.
echo   URL: %PETSHOP_URL%
timeout /t 20 /nobreak >nul
if /I "%PETSHOP_OPEN_BROWSER%"=="true" start "" "%PETSHOP_URL%"
endlocal
