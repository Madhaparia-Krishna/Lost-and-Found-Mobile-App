# StrictMode Testing Guide

## Overview
StrictMode has been successfully enabled for debug builds in the LostFoundApplication class. This will help detect main thread violations during development.

## What Was Implemented

### 1. StrictMode Configuration
- **Location**: `app/src/main/java/com/example/loginandregistration/LostFoundApplication.kt`
- **Enabled for**: Debug builds only (using `BuildConfig.DEBUG` check)
- **Requirement**: 3.6

### 2. ThreadPolicy Configuration
The following violations are detected:
- ✅ Disk reads on main thread
- ✅ Disk writes on main thread
- ✅ Network operations on main thread
- ✅ Unbuffered I/O (Android P+)

### 3. VmPolicy Configuration
The following violations are detected:
- ✅ Memory leaks
- ✅ Leaked closable objects (unclosed files, cursors)
- ✅ Content URI without permission (Android O+)
- ✅ Untagged sockets (Android O+)
- ✅ Unsafe intent launch (Android S+)

### 4. Penalty
- All violations are logged to logcat with `penaltyLog()`
- No crashes - violations are reported for debugging only

## How to Test

### Step 1: Build and Run in Debug Mode
```bash
# Clean and rebuild the project
gradlew clean assembleDebug

# Install on device/emulator
gradlew installDebug
```

### Step 2: Monitor Logcat for StrictMode Violations
```bash
# Filter for StrictMode violations
adb logcat | findstr "StrictMode"

# Or filter for the Application tag
adb logcat | findstr "LostFoundApplication"
```

### Step 3: Look for Violation Messages
When StrictMode detects a violation, you'll see messages like:
```
D/StrictMode: StrictMode policy violation; ~duration=123 ms: android.os.strictmode.DiskReadViolation
D/StrictMode: StrictMode policy violation; ~duration=456 ms: android.os.strictmode.NetworkViolation
```

### Step 4: Test Common Scenarios
1. **Launch the app** - Check for violations during initialization
2. **Sign in** - Check for violations during authentication
3. **Navigate to HomeFragment** - Check for violations during data loading
4. **Browse items** - Check for violations during scrolling
5. **Create new item** - Check for violations during data submission

## Expected Results

### ✅ No Violations Expected
Since tasks 1-13 have already been completed, the following should NOT trigger violations:
- Firestore operations (moved to background threads with coroutines)
- Image loading (handled by Glide on background threads)
- Data deserialization (handled properly with null safety)

### ⚠️ Potential Violations to Watch For
If you see violations, they might be from:
- Third-party libraries (Firebase, Glide, etc.)
- System operations during app initialization
- Any remaining code that hasn't been refactored

## How to Fix Violations

If StrictMode detects violations in your code:

1. **Identify the violation** - Check the stack trace in logcat
2. **Move to background thread** - Use coroutines:
   ```kotlin
   lifecycleScope.launch(Dispatchers.IO) {
       // Blocking operation here
       withContext(Dispatchers.Main) {
           // Update UI here
       }
   }
   ```
3. **Verify the fix** - Run the app again and check logcat

## Disabling StrictMode

StrictMode is only enabled in debug builds. For release builds:
- StrictMode is automatically disabled
- No performance impact in production
- No need to manually disable

## Additional Notes

- StrictMode helps catch performance issues during development
- All previous tasks (1-13) have already addressed main thread blocking
- This task adds an additional safety net for future development
- Violations are logged, not crashed, so the app remains functional

## Verification Checklist

- [x] StrictMode enabled in LostFoundApplication.onCreate()
- [x] Only enabled for debug builds (BuildConfig.DEBUG check)
- [x] ThreadPolicy configured to detect disk and network operations
- [x] VmPolicy configured to detect memory leaks
- [x] Penalty set to log violations
- [x] Application class registered in AndroidManifest.xml
- [x] Code compiles without errors
- [ ] Run app in debug mode and check logcat (manual testing required)
- [ ] Verify no violations or fix any detected violations (manual testing required)

## Next Steps

To complete this task:
1. Run the app on a physical device or emulator
2. Monitor logcat for StrictMode messages
3. Test all major app flows (sign in, browse, create item, etc.)
4. If violations are found, investigate and fix them
5. Document any violations found and their fixes
