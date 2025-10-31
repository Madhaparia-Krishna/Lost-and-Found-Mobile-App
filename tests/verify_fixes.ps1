# Quick Verification Script for Critical Fixes
# Verifies all code changes from tasks 1-14 are in place

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Critical Fixes Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$passCount = 0
$failCount = 0

function Test-Fix {
    param(
        [string]$Name,
        [string]$Pattern,
        [string]$File
    )
    
    Write-Host "Checking $Name..." -NoNewline
    
    if (Select-String -Path $File -Pattern $Pattern -Quiet) {
        Write-Host " [PASS]" -ForegroundColor Green
        return $true
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        return $false
    }
}

# Fix 1: Nullable imageUrl
if (Test-Fix "Fix 1: LostFoundItem nullable imageUrl" "val imageUrl: String\? = null" "app\src\main\java\com\example\loginandregistration\LostFoundItem.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 2: @IgnoreExtraProperties
if (Test-Fix "Fix 2: @IgnoreExtraProperties annotation" "@IgnoreExtraProperties" "app\src\main\java\com\example\loginandregistration\LostFoundItem.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 3: Glide placeholder
if (Test-Fix "Fix 3: Glide placeholder handling" "placeholder" "app\src\main\java\com\example\loginandregistration\ItemsAdapter.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 4: Coroutines dependency
if (Test-Fix "Fix 4: Coroutines dependency" "kotlinx-coroutines-core" "app\build.gradle.kts") {
    $passCount++
} else {
    $failCount++
}

# Fix 5: lifecycleScope usage
if (Test-Fix "Fix 5: lifecycleScope usage in HomeFragment" "lifecycleScope\.launch" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 6: Dispatchers.IO
if (Test-Fix "Fix 6: Dispatchers.IO usage" "Dispatchers\.IO" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 7: await() extension
if (Test-Fix "Fix 7: await extension usage" "\.await\(\)" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 8: Error handling
if (Test-Fix "Fix 8: Error handling with try-catch" "try \{" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 9: FirebaseFirestoreException
if (Test-Fix "Fix 9: FirebaseFirestoreException handling" "FirebaseFirestoreException" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 10: mapNotNull
if (Test-Fix "Fix 10: mapNotNull for safe deserialization" "mapNotNull" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 11: Loading indicators
if (Test-Fix "Fix 11: Loading indicators" "showLoading|hideLoading" "app\src\main\java\com\example\loginandregistration\HomeFragment.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 12: Google One Tap
if (Test-Fix "Fix 12: Google Sign-In with One Tap" "oneTapClient" "app\src\main\java\com\example\loginandregistration\Login.kt") {
    $passCount++
} else {
    $failCount++
}

# Fix 13: Coroutines in Login
if (Test-Fix "Fix 13: Coroutines in Login activity" "lifecycleScope\.launch" "app\src\main\java\com\example\loginandregistration\Login.kt") {
    $passCount++
} else {
    $failCount++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verification Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Checks: 13"
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "[SUCCESS] All fixes are in place!" -ForegroundColor Green
    Write-Host "You can proceed with integration testing." -ForegroundColor Green
} else {
    Write-Host "[WARNING] Some fixes are missing or incomplete." -ForegroundColor Yellow
    Write-Host "Please review the failed checks above." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Run automated tests: gradlew connectedAndroidTest"
Write-Host "2. Follow FINAL_INTEGRATION_TEST_GUIDE.md for manual testing"
Write-Host "3. Monitor logcat for any errors during testing"
Write-Host ""
