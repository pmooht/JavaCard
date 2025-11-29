@echo off
REM Run Gym Card Desktop Application (Mock Mode - No Physical Card Needed)

echo ================================================
echo   GYM CARD MANAGEMENT SYSTEM - DEMO MODE
echo ================================================
echo.
echo Che do: Mock Card (The ao - Khong can the vat ly)
echo.

REM Check if compiled
if not exist build\classes\gymcard\client\GymCardApp.class (
    echo Chua compile! Dang compile...
    call build_simple.bat
    if errorlevel 1 (
        echo.
        echo Loi compile. Vui long kiem tra lai.
        pause
        exit /b 1
    )
)

REM Run application
echo Dang khoi dong ung dung...
echo.
java -cp build\classes gymcard.client.GymCardApp

if errorlevel 1 (
    echo.
    echo Loi khi chay ung dung!
    echo Vui long kiem tra Java da duoc cai dat dung.
    pause
)
