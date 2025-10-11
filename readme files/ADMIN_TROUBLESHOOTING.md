# Admin Dashboard Troubleshooting Guide

## 🚨 If the App is Still Crashing

### 1. Check Logcat for Errors
Run this command to see detailed error logs:
```bash
adb logcat | grep -E "(AdminDashboard|LoginActivity|FATAL|AndroidRuntime)"
```

### 2. Common Issues and Solutions

#### Issue: "Access denied. Admin privileges required."
**Solution**: Make sure you're logging in with exactly `admin@gmail.com` (case sensitive)

#### Issue: App crashes when opening admin dashboard
**Possible causes**:
1. **Navigation graph not found**: Check if `app/src/main/res/navigation/admin_navigation.xml` exists
2. **Fragment class not found**: Make sure all admin fragment classes are compiled
3. **Layout issues**: Check if all layout files exist and have correct IDs

**Debug steps**:
1. Check logcat for specific error messages
2. Try logging in with a regular user first to see if the main app works
3. Check if Firebase is properly configured

#### Issue: Empty dashboard (no data showing)
**Solution**: 
1. Use the "Create Test Data" menu option in the admin dashboard
2. Check your Firebase project configuration
3. Ensure Firestore is enabled in your Firebase console

#### Issue: Bottom navigation not working
**Solution**: Check if all fragment classes exist and are properly named

### 3. Manual Testing Steps

#### Test 1: Basic Admin Access
1. Open app
2. Login with `admin@gmail.com` and any password
3. Should redirect to admin dashboard automatically
4. Should see 5 tabs at bottom: Dashboard, Items, Users, Analytics, Activities

#### Test 2: Test Data Creation
1. In admin dashboard, tap menu (3 dots)
2. Select "Create Test Data"
3. Should see toast "Creating test data..."
4. Dashboard should show non-zero numbers for items

#### Test 3: Navigation
1. Tap each bottom navigation tab
2. Each should load without crashing
3. Dashboard tab should show statistics cards
4. Items tab should show search bar and filters

### 4. Firebase Configuration Check

Make sure your Firebase project has:
1. **Authentication enabled** with Email/Password provider
2. **Firestore database created** (not Realtime Database)
3. **Proper security rules** (start with test mode rules)

### 5. If All Else Fails

#### Quick Fix: Disable Admin Features Temporarily
If you need the regular app to work, you can temporarily disable admin routing by commenting out the admin check in `Login.kt`:

```kotlin
private fun navigateToDashboard() {
    val currentUser = auth.currentUser
    // Temporarily disable admin routing
    // if (currentUser?.email == "admin@gmail.com") {
    //     val intent = Intent(this, com.example.loginandregistration.admin.AdminDashboardActivity::class.java)
    //     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    //     startActivity(intent)
    // } else {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    // }
    finish()
}
```

### 6. Debug Commands

#### Check if admin activity is registered:
```bash
adb shell dumpsys package com.example.loginandregistration | grep -i admin
```

#### Clear app data and try again:
```bash
adb shell pm clear com.example.loginandregistration
```

#### Check Firebase connection:
Look for Firebase connection logs in logcat:
```bash
adb logcat | grep -i firebase
```

## 📞 Getting Help

If you're still having issues:
1. **Check the logcat output** - this will show the exact error
2. **Try with a clean Firebase project** - create a new Firebase project and connect it
3. **Test with regular user first** - make sure the base app works before testing admin features

## 🔍 Key Files to Check

If something is wrong, check these files:
- `app/src/main/java/com/example/loginandregistration/Login.kt` - Admin routing logic
- `app/src/main/java/com/example/loginandregistration/admin/AdminDashboardActivity.kt` - Main admin activity
- `app/src/main/res/navigation/admin_navigation.xml` - Navigation graph
- `app/src/main/AndroidManifest.xml` - Activity registration
- `google-services.json` - Firebase configuration

The admin dashboard should work now with proper error handling and fallbacks!