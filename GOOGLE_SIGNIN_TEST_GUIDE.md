# Google Sign-In Testing Guide

## Prerequisites
- Physical device or emulator with Google Play Services installed
- Google account available for testing
- App built and installed on device

## Test Execution Steps

### 1. Build and Install the App
```bash
# Build the app
gradlew assembleDebug

# Install on connected device
gradlew installDebug
```

### 2. Launch the App
- Open the Lost and Found app from the device
- You should see the Login screen

### 3. Test Google Sign-In Flow

#### Step 3.1: Initiate Google Sign-In
- Tap the "Google Sign-In" button
- **Expected**: Google One Tap sign-in dialog appears showing available Google accounts

#### Step 3.2: Select Account
- Select a Google account from the list
- **Expected**: Account selection completes without errors

#### Step 3.3: Verify Authentication Success
- **Expected**: 
  - No SecurityException thrown
  - No "Failed to get service from broker" error in logcat
  - User is successfully authenticated
  - App navigates to appropriate screen based on user role:
    - Regular users → MainActivity (Dashboard)
    - Admin users → AdminDashboardActivity
    - Security users → SecurityMainActivity

### 4. Check Logcat for Errors

Run this command to monitor logcat during sign-in:
```bash
adb logcat -s LoginActivity:* FirebaseAuth:* GoogleSignIn:*
```

**Look for these SUCCESS indicators:**
- `signInWithCredential:success`
- `User document created successfully` (for new users)
- No PERMISSION_DENIED errors
- No SecurityException errors

**Look for these FAILURE indicators (should NOT appear):**
- `Failed to get service from broker`
- `SecurityException`
- `API: Credentials.API is not available`
- `PERMISSION_DENIED`

### 5. Test Sign-Out and Re-Sign-In

#### Step 5.1: Sign Out
- Navigate to Profile section
- Tap "Sign Out" button
- **Expected**: User is signed out and returned to Login screen

#### Step 5.2: Sign In Again
- Tap "Google Sign-In" button again
- **Expected**: 
  - Google One Tap appears again
  - Sign-in completes successfully
  - User is navigated to appropriate screen
  - Consistent behavior with first sign-in

### 6. Test Edge Cases

#### Test 6.1: Cancel Sign-In
- Tap "Google Sign-In" button
- Dismiss/cancel the Google One Tap dialog
- **Expected**: 
  - No crash
  - User remains on Login screen
  - Can retry sign-in

#### Test 6.2: Network Issues
- Enable airplane mode
- Tap "Google Sign-In" button
- **Expected**: 
  - Appropriate error message shown
  - No crash
  - App remains functional

#### Test 6.3: Blocked User (if applicable)
- Sign in with a user account that has been blocked by admin
- **Expected**:
  - User is signed out immediately
  - Toast message: "Your account has been blocked. Please contact support."
  - User cannot access the app

## Verification Checklist

Use this checklist to verify all requirements:

- [ ] **Requirement 4.5**: Google Sign-In completes without SecurityException
- [ ] **Requirement 4.6**: No "Failed to get service from broker" errors in logcat
- [ ] Package name matches in build.gradle, AndroidManifest.xml, and Firebase Console
- [ ] SHA-1 fingerprint is correctly configured in Firebase
- [ ] google-services.json is up to date
- [ ] Sign-in flow works on first attempt
- [ ] Sign-out and re-sign-in works consistently
- [ ] New users get Firestore document created
- [ ] Existing users navigate to correct screen based on role
- [ ] No crashes during sign-in process
- [ ] Error handling works for cancelled sign-in
- [ ] App remains responsive during authentication

## Configuration Verification

### Verify Package Name Consistency
```bash
# Check build.gradle
grep "applicationId" app/build.gradle.kts

# Check AndroidManifest.xml
grep "package=" app/src/main/AndroidManifest.xml

# Check google-services.json
grep "package_name" app/google-services.json
```

**Expected**: All three should show `com.example.loginandregistration`

### Verify SHA-1 Fingerprint
```bash
# Generate SHA-1 fingerprint
gradlew signingReport
```

**Expected**: SHA-1 from output should match the one in Firebase Console

### Verify Firebase Configuration
- Open Firebase Console: https://console.firebase.google.com/
- Navigate to Project Settings → Your apps
- Verify:
  - Package name: `com.example.loginandregistration`
  - SHA-1 fingerprint is added
  - google-services.json is downloaded and up to date

## Common Issues and Solutions

### Issue: "API: Credentials.API is not available"
**Solution**: 
- Verify SHA-1 fingerprint is added to Firebase Console
- Download latest google-services.json
- Rebuild the app

### Issue: SecurityException during sign-in
**Solution**:
- Verify package name matches in all locations
- Verify SHA-1 fingerprint is correct
- Ensure google-services.json is up to date

### Issue: "Failed to get service from broker"
**Solution**:
- Update Google Play Services on device
- Verify google-services.json is in app/ directory
- Clean and rebuild project

### Issue: Sign-in dialog doesn't appear
**Solution**:
- Check internet connection
- Verify Google Play Services is installed and updated
- Check logcat for specific error messages

## Test Results Template

```
Test Date: _______________
Tester: _______________
Device: _______________
Android Version: _______________

Test Results:
[ ] Google Sign-In initiated successfully
[ ] Account selection dialog appeared
[ ] Authentication completed without errors
[ ] No SecurityException thrown
[ ] No "Failed to get service from broker" error
[ ] User navigated to correct screen
[ ] Sign-out successful
[ ] Re-sign-in successful
[ ] Logcat shows no errors
[ ] All edge cases handled properly

Notes:
_________________________________
_________________________________
_________________________________
```

## Automated Logcat Monitoring

Run this script to automatically monitor for errors during testing:

```bash
# Monitor for errors
adb logcat | grep -E "(LoginActivity|FirebaseAuth|GoogleSignIn|SecurityException|PERMISSION_DENIED|Failed to get service)"
```

## Success Criteria

The test is considered PASSED when:
1. ✅ Google Sign-In completes without any exceptions
2. ✅ No SecurityException appears in logcat
3. ✅ No "Failed to get service from broker" error
4. ✅ User is successfully authenticated and navigated to appropriate screen
5. ✅ Sign-out and re-sign-in works consistently
6. ✅ All edge cases are handled gracefully without crashes

## Next Steps After Testing

If all tests pass:
- Mark task 10 as complete
- Proceed to task 11 (Refactor other Fragments/Activities)

If tests fail:
- Document specific failures
- Review Firebase configuration
- Verify SHA-1 fingerprints
- Check google-services.json is up to date
- Review logcat for specific error messages
