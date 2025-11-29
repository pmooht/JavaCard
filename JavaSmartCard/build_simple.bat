@echo off
REM Simple build script - No JC_HOME required

echo ================================================
echo   BUILD GYM CARD CLIENT (Mock Mode)
echo ================================================
echo.

REM Check Java
echo [1/3] Kiểm tra Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo LỖI: Không tìm thấy Java. Vui lòng cài JDK 8+
    pause
    exit /b 1
)
echo OK: Đã tìm thấy Java.
echo.

REM Create directories
echo [2/3] Tạo thư mục...
if not exist build\classes mkdir build\classes
if not exist dist mkdir dist
echo OK: Thư mục đã sẵn sàng.
echo.

REM Compile
echo [3/3] Biên dịch (Java 8 compatible)...
javac -source 8 -target 8 -d build\classes -sourcepath src src\gymcard\client\*.java src\gymcard\client\ui\*.java
if errorlevel 1 (
    echo.
    echo LỖI: Biên dịch thất bại!
    pause
    exit /b 1
)
echo OK: Biên dịch thành công (tương thích Java 8+)!
echo.

echo ================================================
echo   BUILD HOÀN TẤT!
echo ================================================
echo.
echo Để chạy ứng dụng:
echo   run.bat
echo.
echo hoặc:
echo   java -cp build\classes gymcard.client.GymCardApp
echo.
pause
