@echo off
REM Quick Verification Script for Critical Fixes
REM Verifies all code changes from tasks 1-14 are in place

echo ========================================
echo Critical Fixes Verification
echo ========================================
echo.

set PASS_COUNT=0
set FAIL_COUNT=0

echo Checking Fix 1: LostFoundItem nullable imageUrl...
findstr /C:"val imageUrl: String? = null" "app\src\main\java\com\example\loginandregistration\LostFoundItem.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] imageUrl is nullable
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] imageUrl is not nullable
    set /a FAIL_COUNT+=1
)

echo Checking Fix 2: @IgnoreExtraProperties annotation...
findstr /C:"@IgnoreExtraProperties" "app\src\main\java\com\example\loginandregistration\LostFoundItem.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] @IgnoreExtraProperties annotation present
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] @IgnoreExtraProperties annotation missing
    set /a FAIL_COUNT+=1
)

echo Checking Fix 3: Glide placeholder handling...
findstr /C:"placeholder" "app\src\main\java\com\example\loginandregistration\ItemsAdapter.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Glide placeholder configured
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Glide placeholder not configured
    set /a FAIL_COUNT+=1
)

echo Checking Fix 4: Coroutines dependency...
findstr /C:"kotlinx-coroutines-core" "app\build.gradle.kts" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Coroutines dependency present
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Coroutines dependency missing
    set /a FAIL_COUNT+=1
)

echo Checking Fix 5: lifecycleScope usage in HomeFragment...
findstr /C:"lifecycleScope.launch" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] lifecycleScope used in HomeFragment
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] lifecycleScope not used in HomeFragment
    set /a FAIL_COUNT+=1
)

echo Checking Fix 6: Dispatchers.IO usage...
findstr /C:"Dispatchers.IO" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Dispatchers.IO used for background operations
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Dispatchers.IO not used
    set /a FAIL_COUNT+=1
)

echo Checking Fix 7: await extension usage...
findstr /C:".await" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] await extension used
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] await extension not used
    set /a FAIL_COUNT+=1
)

echo Checking Fix 8: Error handling with try-catch...
findstr /C:"try {" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Error handling implemented
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Error handling not implemented
    set /a FAIL_COUNT+=1
)

echo Checking Fix 9: FirebaseFirestoreException handling...
findstr /C:"FirebaseFirestoreException" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] FirebaseFirestoreException handled
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] FirebaseFirestoreException not handled
    set /a FAIL_COUNT+=1
)

echo Checking Fix 10: mapNotNull for safe deserialization...
findstr /C:"mapNotNull" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] mapNotNull used for safe deserialization
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] mapNotNull not used
    set /a FAIL_COUNT+=1
)

echo Checking Fix 11: Loading indicators...
findstr /C:"showLoading\|hideLoading" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Loading indicators implemented
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Loading indicators not implemented
    set /a FAIL_COUNT+=1
)

echo Checking Fix 12: Google Sign-In with One Tap...
findstr /C:"oneTapClient" "app\src\main\java\com\example\loginandregistration\Login.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Google One Tap Sign-In implemented
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Google One Tap Sign-In not implemented
    set /a FAIL_COUNT+=1
)

echo Checking Fix 13: Coroutines in Login activity...
findstr /C:"lifecycleScope.launch" "app\src\main\java\com\example\loginandregistration\Login.kt" >nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Coroutines used in Login activity
    set /a PASS_COUNT+=1
) else (
    echo [FAIL] Coroutines not used in Login activity
    set /a FAIL_COUNT+=1
)

echo.
echo ========================================
echo Verification Summary
echo ========================================
echo Total Checks: 13
echo Passed: %PASS_COUNT%
echo Failed: %FAIL_COUNT%
echo.

if %FAIL_COUNT%==0 (
    echo [SUCCESS] All fixes are in place!
    echo You can proceed with integration testing.
) else (
    echo [WARNING] Some fixes are missing or incomplete.
    echo Please review the failed checks above.
)

echo.
echo Next Steps:
echo 1. Run 'run_integration_tests.bat' to execute automated tests
echo 2. Follow 'FINAL_INTEGRATION_TEST_GUIDE.md' for manual testing
echo 3. Monitor logcat for any errors during testing
echo.

pause
