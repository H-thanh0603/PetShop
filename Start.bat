@echo off
setlocal

echo ========================================
echo   PetShop - Starting Tomcat 10 Server
echo ========================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"

if defined PETSHOP_TOMCAT_HOME (
    set "CATALINA_HOME=%PETSHOP_TOMCAT_HOME%"
) else (
    set "CATALINA_HOME=C:\apache-tomcat-10.1.55"
)

set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
set "WAR_FILE=%PROJECT_ROOT%\build\libs\ROOT.war"

if defined PETSHOP_BASE_URL (
    set "PETSHOP_URL=%PETSHOP_BASE_URL%"
) else (    set "PETSHOP_URL=http://localhost:8080/home"
)

if not defined PETSHOP_OPEN_BROWSER (
    set "PETSHOP_OPEN_BROWSER=true"
)

if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo [ERROR] Khong tim thay Tomcat tai: %CATALINA_HOME%
    echo [HINT] Hay set PETSHOP_TOMCAT_HOME truoc khi chay script.
    pause
    exit /b 1
)

echo Step 1: Building project with Gradle...
cd /d "%PROJECT_ROOT%"
if /I "%PETSHOP_SKIP_BUILD%"=="true" (
    echo [INFO] Bo qua buoc build vi PETSHOP_SKIP_BUILD=true
) else (
    call gradlew.bat clean war
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build Gradle that bai!
        pause
        exit /b 1
    )
)

if not exist "%WAR_FILE%" (
    echo [ERROR] Khong tim thay file WAR tai %WAR_FILE%
    pause
    exit /b 1
)

echo.
echo Step 2: Cleaning old deployment...
if exist "%CATALINA_HOME%\bin\shutdown.bat" (
    set "CATALINA_HOME=%CATALINA_HOME%"
    call "%CATALINA_HOME%\bin\shutdown.bat" >nul 2>&1
    timeout /t 3 /nobreak >nul
)
if exist "%CATALINA_HOME%\webapps\ROOT.war" del /f /q "%CATALINA_HOME%\webapps\ROOT.war"
if exist "%CATALINA_HOME%\webapps\ROOT" rd /s /q "%CATALINA_HOME%\webapps\ROOT"

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
