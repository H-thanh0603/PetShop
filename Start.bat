@echo off
setlocal

echo ========================================
echo   PetShop - Starting Tomcat 10 Server
echo ========================================
echo.

:: 1. Cấu hình đường dẫn
set "CATALINA_HOME=E:\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
set "PROJECT_ROOT=d:\PetShop"
set "WAR_FILE=%PROJECT_ROOT%\target\PetShop.war"

:: 2. Kiểm tra đường dẫn Tomcat
if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo [ERROR] Khong tim thay Tomcat tai: %CATALINA_HOME%
    echo Vui long kiem tra lai duong dan trong file bat nay.
    pause
    exit /b
)

:: 3. Build dự án bằng Maven
echo Step 1: Building project with Maven...
cd /d "%PROJECT_ROOT%"
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build Maven that bai!
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
