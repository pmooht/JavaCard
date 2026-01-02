@echo off
echo ===================================
echo  BUILD GYM CARD APPLICATION v3.0
echo ===================================
echo  With JavaCard Integration
echo ===================================
echo.

REM Clean old build
echo [1/5] Cleaning old build...
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist

REM Create directories
echo [2/5] Creating directories...
mkdir build\classes
mkdir dist

REM Compile CardManager and utilities
echo [3/5] Compiling CardManager and utilities...
javac -d build\classes -sourcepath src -encoding UTF-8 src\gymcard\CardManager\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] CardManager compilation failed!
    pause
    exit /b 1
)

REM Compile client application
echo [4/5] Compiling client application...
javac -cp "lib\jcalendar-1.4.jar;build\classes" -d build\classes -sourcepath src -encoding UTF-8 src\gymcard\client\*.java src\gymcard\client\ui\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation successful!
) else (
    echo [ERROR] Client compilation failed!
    pause
    exit /b 1
)

REM Create JAR file
echo [5/5] Creating JAR file...
cd build\classes
jar cfm ..\..\dist\GymCardApp.jar ..\..\manifest.mf .
cd ..\..

if exist dist\GymCardApp.jar (
    echo [SUCCESS] JAR file created: dist\GymCardApp.jar
) else (
    echo [WARNING] JAR creation failed, but compiled classes are available
)

echo.
echo ===================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ===================================
echo.
echo To run the application:
echo   run.bat
echo.
echo To test card connection:
echo   java -cp "build\classes;lib\jcalendar-1.4.jar" gymcard.client.TestCardConnection
echo.
pause
