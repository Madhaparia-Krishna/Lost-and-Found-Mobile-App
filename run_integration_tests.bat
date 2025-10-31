@echo off
REM Integration Testing Script for Lost and Found App
REM This script helps run comprehensive integration tests

echo ========================================
echo Lost and Found - Integration Testing
echo ========================================
echo.

:MENU
echo Please select a test option:
echo.
echo 1. Run Automated Integration Tests
echo 2. Monitor Logcat for Errors
echo 3. Check for NullPointerException
echo 4. Check for PERMISSION_DENIED Errors
echo 5. Monitor Frame Drops
echo 6. Clear Logcat
echo 7. Build and Install App
echo 8. Clear App Data
echo 9. Run All Tests (Automated + Manual Checks)
echo 0. Exit
echo.

set /p choice="Enter your choice (0-9): "

if "%choice%"=="1" goto RUN_TESTS
if "%choice%"=="2" goto MONITOR_LOGCAT
if "%choice%"=="3" goto CHECK_NPE
if "%choice%"=="4" goto CHECK_PERMISSION
if "%choice%"=="5" goto CHECK_FRAMES
if "%choice%"=="6" goto CLEAR_LOGCAT
if "%choice%"=="7" goto BUILD_INSTALL
if "%choice%"=="8" goto CLEAR_DATA
if "%choice%"=="9" goto RUN_ALL
if "%choice%"=="0" goto END

echo Invalid choice. Please try again.
echo.
goto MENU

:RUN_TESTS
echo.
echo Running automated integration tests...
echo.
call gradlew connectedAndroidTest
echo.
echo Tests completed. Check the results above.
echo.
pause
goto MENU

:MONITOR_LOGCAT
echo.
echo Monitoring logcat for errors (Press Ctrl+C to stop)...
echo.
adb logcat *:E
goto MENU

:CHECK_NPE
echo.
echo Checking for NullPointerException errors...
echo.
adb logcat -d | findstr /i "NullPointerException"
if errorlevel 1 (
    echo [PASS] No NullPointerException found!
) else (
    echo [FAIL] NullPointerException detected!
)
echo.
pause
goto MENU

:CHECK_PERMISSION
echo.
echo Checking for PERMISSION_DENIED errors...
echo.
adb logcat -d | findstr /i "PERMISSION_DENIED"
if errorlevel 1 (
    echo [PASS] No PERMISSION_DENIED errors found!
) else (
    echo [FAIL] PERMISSION_DENIED errors detected!
)
echo.
pause
goto MENU

:CHECK_FRAMES
echo.
echo Checking for frame drops...
echo.
adb logcat -d | findstr /i "Skipped.*frames"
echo.
echo Review the frame drops above. Less than 10 frames is acceptable.
echo.
pause
goto MENU

:CLEAR_LOGCAT
echo.
echo Clearing logcat...
adb logcat -c
echo Logcat cleared.
echo.
pause
goto MENU

:BUILD_INSTALL
echo.
echo Building and installing app...
echo.
call gradlew installDebug
echo.
echo App installed. You can now run manual tests.
echo.
pause
goto MENU

:CLEAR_DATA
echo.
echo Clearing app data...
echo.
adb shell pm clear com.example.loginandregistration
echo App data cleared.
echo.
pause
goto MENU

:RUN_ALL
echo.
echo ========================================
echo Running Complete Test Suite
echo ========================================
echo.

echo Step 1: Clearing logcat...
adb logcat -c
echo.

echo Step 2: Building and installing app...
call gradlew installDebug
echo.

echo Step 3: Running automated tests...
call gradlew connectedAndroidTest
echo.

echo Step 4: Checking for NullPointerException...
adb logcat -d | findstr /i "NullPointerException"
if errorlevel 1 (
    echo [PASS] No NullPointerException found!
) else (
    echo [FAIL] NullPointerException detected!
)
echo.

echo Step 5: Checking for PERMISSION_DENIED errors...
adb logcat -d | findstr /i "PERMISSION_DENIED"
if errorlevel 1 (
    echo [PASS] No PERMISSION_DENIED errors found!
) else (
    echo [FAIL] PERMISSION_DENIED errors detected!
)
echo.

echo Step 6: Checking for frame drops...
adb logcat -d | findstr /i "Skipped.*frames"
echo.

echo Step 7: Checking for Google Sign-In errors...
adb logcat -d | findstr /i "SecurityException GoogleSignIn broker"
if errorlevel 1 (
    echo [PASS] No Google Sign-In errors found!
) else (
    echo [FAIL] Google Sign-In errors detected!
)
echo.

echo ========================================
echo Test Suite Complete
echo ========================================
echo.
echo Please review the results above and perform manual tests:
echo 1. Launch the app
echo 2. Sign in with Google
echo 3. Navigate through all screens
echo 4. Create items with and without images
echo 5. Test with poor network conditions
echo.
echo Refer to FINAL_INTEGRATION_TEST_GUIDE.md for detailed manual testing steps.
echo.
pause
goto MENU

:END
echo.
echo Exiting...
exit /b 0
