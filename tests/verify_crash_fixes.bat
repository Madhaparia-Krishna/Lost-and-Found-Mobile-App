@echo off
echo ========================================
echo Crash Fixes Verification Script
echo ========================================
echo.

echo Checking modified files...
echo.

echo [1/5] Checking navigation graph...
if exist "app\src\main\res\navigation\admin_navigation.xml" (
    findstr /C:"itemDetailsFragment" "app\src\main\res\navigation\admin_navigation.xml" >nul
    if %errorlevel% equ 0 (
        echo [OK] ItemDetailsFragment added to navigation graph
    ) else (
        echo [FAIL] ItemDetailsFragment NOT found in navigation graph
    )
) else (
    echo [FAIL] Navigation graph file not found
)
echo.

echo [2/5] Checking AdminItemsFragment navigation fix...
if exist "app\src\main\java\com\example\loginandregistration\admin\fragments\AdminItemsFragment.kt" (
    findstr /C:"findNavController" "app\src\main\java\com\example\loginandregistration\admin\fragments\AdminItemsFragment.kt" >nul
    if %errorlevel% equ 0 (
        echo [OK] AdminItemsFragment uses Navigation component
    ) else (
        echo [FAIL] AdminItemsFragment still uses manual fragment transactions
    )
) else (
    echo [FAIL] AdminItemsFragment file not found
)
echo.

echo [3/5] Checking AdminUsersFragment navigation fix...
if exist "app\src\main\java\com\example\loginandregistration\admin\fragments\AdminUsersFragment.kt" (
    findstr /C:"findNavController" "app\src\main\java\com\example\loginandregistration\admin\fragments\AdminUsersFragment.kt" >nul
    if %errorlevel% equ 0 (
        echo [OK] AdminUsersFragment uses Navigation component
    ) else (
        echo [FAIL] AdminUsersFragment still uses manual fragment transactions
    )
) else (
    echo [FAIL] AdminUsersFragment file not found
)
echo.

echo [4/5] Checking DataMigrationHelper...
if exist "app\src\main\java\com\example\loginandregistration\admin\utils\DataMigrationHelper.kt" (
    echo [OK] DataMigrationHelper created
) else (
    echo [FAIL] DataMigrationHelper not found
)
echo.

echo [5/5] Checking Firestore rules update...
if exist "firestore.rules" (
    findstr /C:"'admin'" "firestore.rules" >nul
    if %errorlevel% equ 0 (
        echo [OK] Firestore rules support lowercase roles
    ) else (
        echo [FAIL] Firestore rules not updated
    )
) else (
    echo [FAIL] Firestore rules file not found
)
echo.

echo ========================================
echo Verification Complete
echo ========================================
echo.
echo Next Steps:
echo 1. Build the project: gradlew assembleDebug
echo 2. Deploy Firestore rules to Firebase Console
echo 3. Test navigation by clicking on items/users
echo 4. Check logcat for permission errors
echo.
pause
