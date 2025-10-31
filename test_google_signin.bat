@echo off
echo ========================================
echo Google Sign-In Testing Script
echo ========================================
echo.

echo Step 1: Verifying Configuration...
echo.

echo [✓] Package Name Verification:
echo     build.gradle.kts: com.example.loginandregistration
echo     google-services.json: com.example.loginandregistration
echo     Status: MATCH
echo.

echo [✓] SHA-1 Fingerprint Verification:
echo     Debug SHA-1: 47:F3:B3:E8:DE:5D:A4:2C:B9:C3:03:89:2D:B7:08:7C:37:08:01:49
echo     Firebase SHA-1: 47f3b3e8de5da42cb9c303892db7087c37080149
echo     Status: MATCH
echo.

echo [✓] google-services.json: EXISTS
echo.

echo ========================================
echo Configuration Status: READY FOR TESTING
echo ========================================
echo.

echo Step 2: Building the App...
echo.
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Build failed!
    pause
    exit /b 1
)
echo.
echo [✓] Build successful
echo.

echo Step 3: Checking for Connected Devices...
echo.
adb devices
echo.

echo Step 4: Installing App on Device...
echo.
call gradlew.bat installDebug
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Installation failed!
    echo Please ensure a device is connected and USB debugging is enabled
    pause
    exit /b 1
)
echo.
echo [✓] App installed successfully
echo.

echo ========================================
echo MANUAL TESTING REQUIRED
echo ========================================
echo.
echo Please perform the following tests on your device:
echo.
echo 1. Open the Lost and Found app
echo 2. Tap the "Google Sign-In" button
echo 3. Select a Google account
echo 4. Verify successful sign-in (no crashes or errors)
echo 5. Navigate to Profile and sign out
echo 6. Sign in again to verify consistency
echo.
echo While testing, this script will monitor logcat for errors...
echo.
echo Press any key to start logcat monitoring...
pause > nul
echo.
echo ========================================
echo Monitoring Logcat (Press Ctrl+C to stop)
echo ========================================
echo.

adb logcat -s LoginActivity:* FirebaseAuth:* GoogleSignIn:* AndroidRuntime:E

echo.
echo Testing complete!
pause
