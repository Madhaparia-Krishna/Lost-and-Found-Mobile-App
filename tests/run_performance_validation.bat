@echo off
REM Performance Validation Script for Lost and Found App
REM This script automates some performance testing tasks

echo ========================================
echo Performance Validation Script
echo ========================================
echo.

REM Check if ADB is available
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: ADB not found in PATH
    echo Please ensure Android SDK platform-tools is in your PATH
    pause
    exit /b 1
)

echo [1/6] Checking connected devices...
adb devices
echo.

set /p CONTINUE="Press Enter to continue with testing or Ctrl+C to cancel..."
echo.

echo [2/6] Clearing app data for fresh test...
adb shell pm clear com.example.loginandregistration
if %ERRORLEVEL% EQU 0 (
    echo ✓ App data cleared successfully
) else (
    echo ✗ Failed to clear app data - app may not be installed
)
echo.

echo [3/6] Launching the app...
adb shell am start -n com.example.loginandregistration/.MainActivity
if %ERRORLEVEL% EQU 0 (
    echo ✓ App launched successfully
) else (
    echo ✗ Failed to launch app
)
echo.

echo [4/6] Monitoring for frame drops (30 seconds)...
echo Press Ctrl+C to stop monitoring early
echo.
echo Watching for "Skipped X frames" messages...
echo.

REM Create a temporary file for logcat output
set LOGFILE=performance_test_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.log
set LOGFILE=%LOGFILE: =0%

timeout /t 5 /nobreak >nul
echo Starting logcat monitoring...
echo Logcat output will be saved to: %LOGFILE%
echo.

REM Monitor logcat for 30 seconds
start /b cmd /c "adb logcat -s Choreographer:I HomeFragment:D StrictMode:W > %LOGFILE%"
set LOGCAT_PID=%ERRORLEVEL%

echo Monitoring... (30 seconds)
timeout /t 30 /nobreak

REM Stop logcat
taskkill /F /FI "WINDOWTITLE eq Administrator:  C:\WINDOWS\system32\cmd.exe - adb  logcat -s Choreographer:I HomeFragment:D StrictMode:W" >nul 2>nul

echo.
echo [5/6] Analyzing results...
echo.

REM Check for frame drops in the log
findstr /C:"Skipped" %LOGFILE% >nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠ Frame drops detected:
    findstr /C:"Skipped" %LOGFILE%
    echo.
) else (
    echo ✓ No frame drops detected in the monitoring period
)

REM Check for StrictMode violations
findstr /C:"StrictMode" %LOGFILE% >nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠ StrictMode violations detected:
    findstr /C:"StrictMode" %LOGFILE%
    echo.
) else (
    echo ✓ No StrictMode violations detected
)

REM Check for errors
findstr /C:"Error" /C:"Exception" %LOGFILE% >nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠ Errors detected:
    findstr /C:"Error" /C:"Exception" %LOGFILE%
    echo.
) else (
    echo ✓ No errors detected
)

echo.
echo [6/6] Test Summary
echo ========================================
echo Full log saved to: %LOGFILE%
echo.
echo Manual Testing Required:
echo 1. Open Android Studio Profiler to monitor main thread
echo 2. Scroll through HomeFragment and observe performance
echo 3. Check for UI responsiveness during data loading
echo 4. Verify loading indicators display correctly
echo.
echo For detailed testing instructions, see:
echo PERFORMANCE_TESTING_SCRIPT.md
echo.
echo ========================================
echo Performance validation monitoring complete!
echo ========================================
echo.

pause
