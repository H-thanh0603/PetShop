@echo off

echo ========================================
echo   PetShop - Starting Tomcat 10 Server
echo ========================================
echo.

:: Cấu hình đường dẫn
if defined PETSHOP_TOMCAT_HOME (
    set "CATALINA_HOME=%PETSHOP_TOMCAT_HOME%"
) else (
    set "CATALINA_HOME=E:\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
)
set "PROJECT_ROOT=d:\Petshop2\PetShop"
set "WAR_FILE=%PROJECT_ROOT%\build\libs\PetShop.war"
if defined PETSHOP_BASE_URL (
    set "PETSHOP_URL=%PETSHOP_BASE_URL%"
) else (
    set "PETSHOP_URL=http://localhost:8080/PetShop/home"
)

:: Kiểm tra đường dẫn Tomcat
if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo [ERROR] Khong tim thay Tomcat tai: %CATALINA_HOME%
    pause
    exit /b
)

:: Build dự án bằng Gradle
echo Step 1: Building project with Gradle...
cd /d "%PROJECT_ROOT%"
call gradlew.bat clean war
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build Gradle that bai!
    pause
    exit /b
)

:: Dọn dẹp và Deploy
echo.
echo Step 2: Cleaning old deployment...
if exist "%CATALINA_HOME%\webapps\PetShop.war" del /f /q "%CATALINA_HOME%\webapps\PetShop.war"
if exist "%CATALINA_HOME%\webapps\PetShop" rd /s /q "%CATALINA_HOME%\webapps\PetShop"

echo Step 3: Deploying new WAR file...
copy "%WAR_FILE%" "%CATALINA_HOME%\webapps\"

:: Tắt Tomcat cũ nếu đang chạy
echo Step 4: Starting Tomcat 10...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

:: Khởi động Tomcat trong cửa sổ riêng (giữ nguyên biến môi trường hiện tại)
start "Tomcat PetShop" /D "%CATALINA_HOME%\bin" cmd /c "startup.bat && pause"

echo.
echo ========================================
echo   Server dang duoc khoi dong!
echo   Vui long doi khoang 15 giay...
echo   URL: %PETSHOP_URL%
echo ========================================
timeout /t 15 /nobreak >nul

:: Mo trinh duyet
start %PETSHOP_URL%
