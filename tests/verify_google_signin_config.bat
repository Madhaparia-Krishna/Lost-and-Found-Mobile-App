@echo off
echo ========================================
echo Google Sign-In Configuration Verification
echo ========================================
echo.

echo [1/5] Checking Package Name Consistency...
echo.
echo --- build.gradle.kts ---
findstr "applicationId" app\build.gradle.kts
echo.
echo --- AndroidManifest.xml ---
findstr "package=" app\src\main\AndroidManifest.xml
echo.
echo --- google-services.json ---
findstr "package_name" app\google-services.json
echo.
echo Expected: All should show "com.example.loginandregistration"
echo.

echo [2/5] Checking google-services.json exists...
if exist "app\google-services.json" (
    echo ✓ google-services.json found
) else (
    echo ✗ google-services.json NOT FOUND!
    echo Please download from Firebase Console
)
echo.

echo [3/5] Checking SHA-1 Fingerprint Configuration...
echo.
echo Run this command to get your SHA-1 fingerprint:
echo gradlew signingReport
echo.
echo Then verify it matches the fingerprint in Firebase Console:
echo https://console.firebase.google.com/project/lost-and-found-954f6/settings/general
echo.

echo [4/5] Checking Firebase Dependencies...
echo.
findstr "firebase" app\build.gradle.kts
echo.

echo [5/5] Checking Google Play Services Dependencies...
echo.
findstr "play-services-auth" app\build.gradle.kts
echo.

echo ========================================
echo Configuration Check Complete
echo ========================================
echo.
echo Next Steps:
echo 1. Verify all package names match
echo 2. Run: gradlew signingReport
echo 3. Add SHA-1 to Firebase Console if not already added
echo 4. Download latest google-services.json if needed
echo 5. Run: gradlew clean build
echo 6. Install and test on device
echo.
pause
