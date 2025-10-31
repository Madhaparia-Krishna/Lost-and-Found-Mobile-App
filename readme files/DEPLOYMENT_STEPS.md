# Deployment Steps - Crash Fixes

## Prerequisites
- Android Studio installed
- Firebase project configured
- Admin credentials (admin@gmail.com)
- Device/emulator for testing

---

## Step 1: Deploy Firestore Rules (5 minutes)

### Option A: Firebase Console (Recommended)
1. Open https://console.firebase.google.com
2. Select your project
3. Click "Firestore Database" in left menu
4. Click "Rules" tab
5. Copy entire content from `firestore.rules` file
6. Paste into the editor
7. Click "Publish"
8. Wait for "Rules published successfully" message

### Option B: Firebase CLI
```bash
# Install Firebase CLI if not installed
npm install -g firebase-tools

# Login
firebase login

# Deploy rules
firebase deploy --only firestore:rules
```

**Verification**:
- Check "Last published" timestamp in Firebase Console
- Should show current date/time

---

## Step 2: Build the Application (5 minutes)

```bash
# Navigate to project directory
cd path/to/your/project

# Clean previous builds
gradlew clean

# Build debug APK
gradlew assembleDebug

# Or build and install directly
gradlew installDebug
```

**Expected Output**:
```
BUILD SUCCESSFUL in 2m 15s
```

**If Build Fails**:
- Check for syntax errors: `gradlew build --stacktrace`
- Sync Gradle: Android Studio > File > Sync Project with Gradle Files
- Invalidate caches: Android Studio > File > Invalidate Caches / Restart

---

## Step 3: Install on Device (2 minutes)

### Option A: Via Gradle
```bash
gradlew installDebug
```

### Option B: Via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option C: Via Android Studio
1. Click "Run" button (green triangle)
2. Select device/emulator
3. Wait for installation

**Verification**:
```bash
adb shell pm list packages | findstr loginandregistration
```

---

## Step 4: Test Navigation (5 minutes)

### Test 1: Item Details Navigation
1. Launch app
2. Login as admin (admin@gmail.com)
3. Navigate to "Items" tab
4. Click on any item
5. **Expected**: Item details screen opens (no crash)
6. Press back button
7. **Expected**: Returns to items list

### Test 2: User Details Navigation
1. Navigate to "Users" tab
2. Click on any user
3. **Expected**: User details screen opens (no crash)
4. Press back button
5. **Expected**: Returns to users list

### Test 3: Activity Log Navigation
1. Navigate to "Activity Log" tab
2. Click on any log entry
3. Click "View Related" button (if available)
4. **Expected**: Navigates to related item/user (no crash)

**If Navigation Crashes**:
- Check logcat: `adb logcat | findstr "FATAL"`
- Verify navigation graph includes detail fragments
- Rebuild and reinstall

---

## Step 5: Verify Permissions (3 minutes)

### Monitor Logcat
```bash
# Open new terminal
adb logcat | findstr "PERMISSION_DENIED"
```

### Test All Tabs
1. Dashboard tab - Check stats load
2. Items tab - Check items list loads
3. Users tab - Check users list loads
4. Donations tab - Check donations load
5. Notifications tab - Check notifications load
6. Activity Log tab - Check logs load

**Expected**: No PERMISSION_DENIED errors in logcat

**If Permission Errors Occur**:
1. Verify Firestore rules are deployed (Step 1)
2. Check admin user document exists in Firestore:
   - Firebase Console > Firestore > users collection
   - Find document with your UID
   - Verify "role" field = "ADMIN" or "admin"
3. If document missing, proceed to Step 6

---

## Step 6: Run Data Migration (5 minutes)

### Option A: Add Temporary Button (Recommended)
1. Open `AdminDashboardFragment.kt`
2. Add this code in `onViewCreated()`:

```kotlin
// Temporary migration button - remove after use
view.findViewById<Button>(R.id.btnMigrate)?.setOnClickListener {
    lifecycleScope.launch {
        val result = DataMigrationHelper.runAllMigrations()
        result.onSuccess { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }.onFailure { error ->
            Toast.makeText(requireContext(), "Migration failed: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }
}
```

3. Add button to layout (or use existing button temporarily)
4. Rebuild and install
5. Click the migration button
6. Wait for success message

### Option B: Manual Fix in Firebase Console
1. Open Firebase Console > Firestore
2. Go to "users" collection
3. For each user document:
   - Change "role" from "user" to "USER"
   - Change "role" from "admin" to "ADMIN"
   - If "createdAt" is a number, delete and re-add as timestamp

### Verification
```bash
# Check for deserialization warnings
adb logcat | findstr "deserialize"
```

**Expected**: No warnings after migration

---

## Step 7: Final Verification (5 minutes)

### Run Verification Script
```bash
verify_crash_fixes.bat
```

### Manual Checks
- [ ] App launches without crash
- [ ] Login works
- [ ] All tabs load data
- [ ] Item details navigation works
- [ ] User details navigation works
- [ ] Back navigation works
- [ ] No crashes in logcat
- [ ] No permission errors in logcat
- [ ] No deserialization warnings in logcat

### Performance Check
- [ ] Navigation is smooth (no lag)
- [ ] Data loads quickly
- [ ] No memory leaks (check Android Profiler)

---

## Step 8: Monitor (24 hours)

### Set Up Monitoring
```bash
# Continuous logcat monitoring
adb logcat | findstr "FATAL\|PERMISSION_DENIED\|deserialize" > crash_monitor.log
```

### Check Periodically
- Every 2 hours for first 8 hours
- Every 8 hours for next 16 hours
- Review crash_monitor.log for any issues

### Firebase Console Monitoring
1. Firebase Console > Crashlytics (if enabled)
2. Check for new crash reports
3. Monitor user engagement metrics

---

## Rollback Plan (If Needed)

### If Critical Issues Occur:

1. **Revert Firestore Rules**:
   - Firebase Console > Firestore > Rules
   - Click "History" tab
   - Select previous version
   - Click "Restore"

2. **Revert Code**:
   ```bash
   git log --oneline -10
   git revert <commit-hash>
   gradlew assembleDebug
   gradlew installDebug
   ```

3. **Notify Users**:
   - If app is in production
   - Send notification about temporary issues
   - Provide ETA for fix

---

## Success Criteria

✅ **All checks must pass**:
- [ ] Firestore rules deployed successfully
- [ ] App builds without errors
- [ ] App installs on device
- [ ] Navigation works without crashes
- [ ] No permission errors
- [ ] No deserialization warnings
- [ ] All tabs load data
- [ ] Back navigation works correctly
- [ ] No new crashes in 24 hours

---

## Troubleshooting

### Build Fails
```bash
# Clean and rebuild
gradlew clean
gradlew assembleDebug --stacktrace
```

### Navigation Crashes
- Verify `admin_navigation.xml` includes detail fragments
- Check imports in fragment files
- Ensure `findNavController()` is used correctly

### Permission Errors
- Redeploy Firestore rules
- Check admin user document exists
- Verify role field is "ADMIN" or "admin"

### Deserialization Warnings
- Run data migration (Step 6)
- Manually fix data in Firebase Console
- Check UserRole enum matches database values

---

## Post-Deployment

### Clean Up (After 48 hours)
1. Remove temporary migration button
2. Remove migration logging
3. Update Firestore rules to only accept uppercase (optional)
4. Document any remaining issues

### Documentation
1. Update README with new navigation structure
2. Document data migration process
2. Add troubleshooting guide for team

---

## Contact

If you encounter issues during deployment:
1. Check logcat for specific errors
2. Review `CRASH_FIXES_SUMMARY.md`
3. Consult `CRASH_FIXES_IMPLEMENTATION.md` for details
4. Check `QUICK_FIX_REFERENCE.md` for quick solutions

---

**Estimated Total Time**: 30-40 minutes
**Recommended Time**: Off-peak hours or staging environment first
