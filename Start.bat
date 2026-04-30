@echo off
setlocal

echo ========================================
echo   PetShop - Starting Tomcat 10 Server
echo ========================================
echo.

if defined PETSHOP_DB_PASSWORD (
    echo [INFO] Using PETSHOP_DB_PASSWORD from environment.
) else (
    echo [INFO] Enter MySQL password for the PetShop app:
    set /p PETSHOP_DB_PASSWORD=
)
set "PETSHOP_DB_PASSWORD=%PETSHOP_DB_PASSWORD%"

:: 1. Cấu hình đường dẫn
set "CATALINA_HOME=E:\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
set "PROJECT_ROOT=d:\Petshop2\PetShop"
set "WAR_FILE=%PROJECT_ROOT%\build\libs\PetShop.war"

if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo [ERROR] Khong tim thay Tomcat tai: %CATALINA_HOME%
    echo [HINT] Hay set PETSHOP_TOMCAT_HOME truoc khi chay script.
    pause
    exit /b 1
)

:: 3. Build dự án bằng Gradle
echo Step 1: Building project with Gradle...
cd /d "%PROJECT_ROOT%"
call gradlew.bat clean war
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build Gradle that bai!
    pause
    exit /b
)

echo.
echo Step 2: Cleaning old deployment...
if exist "%CATALINA_HOME%\bin\shutdown.bat" (
    call "%CATALINA_HOME%\bin\shutdown.bat" >nul 2>&1
    timeout /t 3 /nobreak >nul
)
if exist "%CATALINA_HOME%\webapps\PetShop.war" del /f /q "%CATALINA_HOME%\webapps\PetShop.war"
if exist "%CATALINA_HOME%\webapps\PetShop" rd /s /q "%CATALINA_HOME%\webapps\PetShop"

echo Step 3: Deploying new WAR file...
copy /y "%WAR_FILE%" "%CATALINA_HOME%\webapps\"

echo Step 4: Starting Tomcat 10...
start "Tomcat PetShop" /D "%CATALINA_HOME%\bin" cmd /c "startup.bat && pause"

echo.
echo ========================================
echo   Server dang duoc khoi dong!
echo   Vui long doi khoang 15 giay...
echo   URL: %PETSHOP_URL%
echo ========================================
timeout /t 15 /nobreak >nul

if /I "%PETSHOP_OPEN_BROWSER%"=="true" (
    start "" "%PETSHOP_URL%"
)

endlocal
