# 🧪 Complete Working App Test Guide

## 📱 **Testing Regular User Functionality**

### Test 1: User Registration & Login
1. **Open the app**
2. **Register a new user**:
   - Email: `testuser@gmail.com`
   - Password: `password123`
3. **Login with the new user**
4. **Should see**: Main dashboard with 4 tabs (Home, Browse, Report, Profile)

### Test 2: Report an Item (Regular User)
1. **Login as regular user** (not admin)
2. **Go to "Report" tab**
3. **Fill out the form**:
   - Item Name: "Test iPhone"
   - Description: "Black iPhone 13"
   - Location: "Library"
   - Contact Info: "testuser@gmail.com"
   - Select "Lost" or "Found"
4. **Tap "Submit"**
5. **Should see**: "Item reported successfully" message

### Test 3: Browse Items (Regular User)
1. **Go to "Browse" tab**
2. **Should see**: List of reported items (including the one you just added)

### Test 4: User Profile & Logout
1. **Go to "Profile" tab**
2. **Should see**: Your email and name
3. **Tap "Logout"**
4. **Should redirect**: Back to login screen

## 👑 **Testing Admin Functionality**

### Test 5: Admin Login
1. **Login with**: `admin@gmail.com` (any password)
2. **Should redirect**: Automatically to admin dashboard
3. **Should see**: 6 tabs (Dashboard, Items, Users, Analytics, Activities, Profile)

### Test 6: Admin Dashboard
1. **Dashboard tab should show**:
   - Statistics cards (may show 0 initially)
   - Recent activity section
2. **All cards should be clickable**

### Test 7: Create Test Data
1. **In admin dashboard, go to "Profile" tab**
2. **Tap "Create Test Data"**
3. **Should see**: "Creating test data..." message
4. **Go back to Dashboard tab**
5. **Should see**: Updated numbers (3 total items, 2 lost, 1 found)

### Test 8: Admin Items Management
1. **Go to "Items" tab**
2. **Should see**: Search bar and filter chips
3. **Should see**: List of items (including test data)
4. **Tap on an item**: Should show details dialog
5. **Try "Edit Status"**: Should show status change dialog

### Test 9: Admin Users Management
1. **Go to "Users" tab**
2. **Should see**: List of users (including test users)
3. **Try user actions**: Block/unblock, change roles

### Test 10: Admin Profile & Logout
1. **Go to "Profile" tab**
2. **Should see**: Admin profile information
3. **Should see**: Admin action buttons
4. **Tap "Logout"**: Should return to login screen

## 🔥 **Firebase Connection Test**

### Verify Firebase Setup:
1. **Check Firebase Console**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project: `lost-and-found-954f6`
   - Check Authentication → Users (should show logged-in users)
   - Check Firestore → Data (should show collections after test data)

2. **Check App Logs**:
   ```bash
   adb logcat | grep -E "(Firebase|Admin|LostFound)"
   ```
   Should see:
   - "Firebase initialized successfully"
   - "Firebase connection successful"
   - "Admin user initialized successfully"

## 🚨 **If Something Doesn't Work**

### Issue: App crashes on startup
**Solution**: Check logcat for specific error messages

### Issue: Admin dashboard doesn't load
**Solutions**:
1. Make sure you're using exactly `admin@gmail.com`
2. Check if all admin fragment classes exist
3. Verify navigation graph is correct

### Issue: Can't create/view items
**Solutions**:
1. Check internet connection
2. Verify Firebase project is active
3. Check Firestore rules in Firebase Console

### Issue: "Permission denied" errors
**Solutions**:
1. Apply the Firestore rules from `firestore.rules`
2. Make sure user is authenticated
3. Check Firebase Console → Firestore → Rules

## 📊 **Expected Results**

### After Complete Testing:
- ✅ Regular users can register, login, report items, browse items, logout
- ✅ Admin can login, see dashboard, create test data, manage items/users
- ✅ Firebase Console shows users in Authentication
- ✅ Firebase Console shows items/users/activities in Firestore
- ✅ All navigation tabs work without crashes
- ✅ Both user types have proper logout functionality

## 🎯 **Success Criteria**

Your app is fully working when:
1. **Regular users** can complete the full flow: register → login → report item → browse → logout
2. **Admin user** can complete: login → dashboard → create test data → manage items/users → logout
3. **No crashes** during normal usage
4. **Firebase data** appears in console after operations
5. **All navigation tabs** work properly

## 🔧 **Quick Fixes for Common Issues**

### If admin dashboard is empty:
1. Go to Profile tab → "Create Test Data"
2. Refresh the dashboard
3. Check Firebase Console for data

### If regular user can't report items:
1. Check internet connection
2. Verify user is logged in
3. Check Firebase Console → Authentication

### If navigation doesn't work:
1. Check if all fragment classes exist
2. Verify layout files have correct IDs
3. Check navigation graph configuration

Your Lost & Found app should now be fully functional for both regular users and administrators! 🎉