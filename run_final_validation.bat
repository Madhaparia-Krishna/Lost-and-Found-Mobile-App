@echo off
REM Final Testing and Validation Script for Lost and Found App
REM This script performs automated checks before manual testing

echo ========================================
echo Lost and Found App - Final Validation
echo ========================================
echo.

echo [1/6] Cleaning previous builds...
call gradlew clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed
    exit /b 1
)
echo ✓ Clean completed
echo.

echo [2/6] Building debug APK...
call gradlew assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Debug build failed
    exit /b 1
)
echo ✓ Debug build successful
echo.

echo [3/6] Running lint checks...
call gradlew lint
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Lint found issues - check app/build/reports/lint-results.html
) else (
    echo ✓ Lint checks passed
)
echo.

echo [4/6] Running unit tests...
call gradlew testDebugUnitTest
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Some unit tests failed - check app/build/reports/tests/
) else (
    echo ✓ Unit tests passed
)
echo.

echo [5/6] Checking for common issues in logcat format...
echo Checking for critical error patterns in source code...
findstr /S /I /C:"Resources$NotFoundException" app\src\main\java\*.kt
if %ERRORLEVEL% EQU 0 (
    echo WARNING: Found potential Resources$NotFoundException references
)
findstr /S /I /C:"PERMISSION_DENIED" app\src\main\java\*.kt
if %ERRORLEVEL% EQU 0 (
    echo WARNING: Found potential PERMISSION_DENIED references
)
echo ✓ Source code scan completed
echo.

echo [6/6] Generating test reports...
echo Test reports available at:
echo - Lint: app\build\reports\lint-results.html
echo - Unit Tests: app\build\reports\tests\testDebugUnitTest\index.html
echo.

echo ========================================
echo Automated Validation Complete!
echo ========================================
echo.
echo Next Steps:
echo 1. Review the FINAL_TESTING_VALIDATION.md document
echo 2. Install the debug APK on a test device
echo 3. Perform manual testing following the checklist
echo 4. Update the validation document with results
echo.
echo Debug APK location: app\build\outputs\apk\debug\app-debug.apk
echo.

pause
