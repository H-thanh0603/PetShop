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

:: 2. Kiểm tra đường dẫn Tomcat
if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo [ERROR] Khong tim thay Tomcat tai: %CATALINA_HOME%
    echo Vui long kiem tra lai duong dan trong file bat nay.
    pause
    exit /b
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

:: 4. Dọn dẹp và Deploy
echo.
echo Step 2: Cleaning old deployment...
if exist "%CATALINA_HOME%\webapps\PetShop.war" del /f /q "%CATALINA_HOME%\webapps\PetShop.war"
if exist "%CATALINA_HOME%\webapps\PetShop" rd /s /q "%CATALINA_HOME%\webapps\PetShop"

echo Step 3: Deploying new WAR file...
copy "%WAR_FILE%" "%CATALINA_HOME%\webapps\"

:: 5. Khởi động Tomcat
echo Step 4: Starting Tomcat 10...
cd /d "%CATALINA_HOME%\bin"
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul
start startup.bat

echo.
echo ========================================
echo   Server dang duoc khoi dong!
echo   Vui long doi khoang 10-15 giay...
echo   URL: http://localhost:8080/PetShop/home
echo ========================================
timeout /t 10 /nobreak >nul

:: Mo trinh duyet
start http://localhost:8080/PetShop/home

endlocal
