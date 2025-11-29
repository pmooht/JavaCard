@echo off
echo ===================================
echo  BUILD GYM CARD APPLICATION v2.0
echo ===================================
echo.

REM Clean old build
echo [1/4] Cleaning old build...
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist

REM Create directories
echo [2/4] Creating directories...
mkdir build\classes
mkdir dist

REM Compile
echo [3/4] Compiling Java files...
javac -d build\classes -sourcepath src -encoding UTF-8 src\gymcard\client\*.java src\gymcard\client\ui\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation successful!
) else (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

REM Create manifest
echo [4/4] Creating manifest...
echo Main-Class: gymcard.client.GymCardApp > build\manifest.txt

echo.
echo ===================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ===================================
echo.
echo To run the application:
echo   run.bat
echo.
pause
