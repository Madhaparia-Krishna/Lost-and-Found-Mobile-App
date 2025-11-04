@echo off
echo ========================================
echo Navigation Error Fix Script
echo ========================================
echo.

echo Step 1: Clearing app data...
adb shell pm clear com.example.loginandregistration
if %errorlevel% neq 0 (
    echo ERROR: Failed to clear app data. Is device connected?
    echo Run: adb devices
    pause
    exit /b 1
)
echo ✓ App data cleared
echo.

echo Step 2: Uninstalling old version...
adb uninstall com.example.loginandregistration
echo ✓ Old version uninstalled
echo.

echo Step 3: Installing fresh build...
adb install app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo ERROR: Failed to install APK
    echo Make sure the APK exists at: app\build\outputs\apk\debug\app-debug.apk
    pause
    exit /b 1
)
echo ✓ Fresh build installed
echo.

echo ========================================
echo Fix Complete!
echo ========================================
echo.
echo Now try opening the app on your device.
echo The navigation error should be gone.
echo.
pause
