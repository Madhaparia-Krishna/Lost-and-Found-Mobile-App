# Lost and Found App - Systematic Testing Script

## 🎯 TESTING OVERVIEW
This script is specifically designed for your Lost and Found Android app with Firebase integration.

**App Details:**
- Package: `com.example.loginandregistration`
- Firebase Project: `lost-and-found-954f6`
- Admin Email: `admin@gmail.com`

---

## 📋 PHASE 1: FIREBASE CONNECTION & SETUP VERIFICATION

### ✅ Step 1.1: Verify Firebase Configuration
```bash
# Check if your app builds successfully
./gradlew app:assembleDebug

# Install and run the app
./gradlew app:installDebug
```

**Manual Checks:**
- [ ] App launches without crashes
- [ ] No Firebase initialization errors in logcat
- [ ] Google Services plugin is working

### ✅ Step 1.2: Test Firebase Connection
**In your app's debug mode, add this test code to MainActivity:**

```kotlin
// Add to MainActivity onCreate() for testing
private fun testFirebaseConnection() {
    Log.d("FirebaseTest", "Testing Firebase connection...")
    
    // Test Firestore connection
    FirebaseFirestore.getInstance()
        .collection("test")
        .add(mapOf(
            "message" to "Test connection",
            "timestamp" to FieldValue.serverTimestamp()
        ))
        .addOnSuccessListener { documentReference ->
            Log.d("FirebaseTest", "✅ Firestore write successful: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("FirebaseTest", "❌ Firestore write failed", e)
        }
    
    // Test Auth connection
    FirebaseAuth.getInstance().addAuthStateListener { auth ->
        Log.d("FirebaseTest", "Auth state: ${auth.currentUser?.email ?: "No user"}")
    }
}
```

**Expected Results:**
- [ ] Firestore write operation succeeds
- [ ] Auth state listener works
- [ ] No network or permission errors

---

## 📋 PHASE 2: AUTHENTICATION TESTING

### ✅ Step 2.1: Admin User Testing

**Test Admin Login:**
1. Launch app
2. Navigate to Login screen
3. Enter credentials:
   - Email: `admin@gmail.com`
   - Password: [Your admin password]

**Verification Checklist:**
- [ ] Login succeeds without errors
- [ ] User is redirected to appropriate screen
- [ ] Admin role is recognized (check logcat for role verification)
- [ ] Admin-specific UI elements are visible

### ✅ Step 2.2: Regular User Testing

**Test Regular User Registration:**
1. Navigate to Register screen
2. Create new user account
3. Complete registration process

**Test Regular User Login:**
1. Login with newly created account
2. Verify user experience

**Verification Checklist:**
- [ ] Registration completes successfully
- [ ] User can login after registration
- [ ] Regular user sees appropriate UI (no admin features)
- [ ] User profile is created in Firestore

### ✅ Step 2.3: Authentication State Management

**Test Session Persistence:**
1. Login to app
2. Close app completely
3. Reopen app

**Verification Checklist:**
- [ ] User remains logged in after app restart
- [ ] Correct user role is maintained
- [ ] No re-authentication required

---

## 📋 PHASE 3: NAVIGATION & UI TESTING

### ✅ Step 3.1: Main Navigation Flow

**Test Bottom Navigation (if applicable):**
1. Login as regular user
2. Test each navigation tab:
   - Home/Browse
   - Report Item
   - Profile

**Verification Checklist:**
- [ ] All navigation items work
- [ ] Fragments load without errors
- [ ] Back button behavior is correct
- [ ] No navigation stack issues

### ✅ Step 3.2: Admin Dashboard Navigation

**Test Admin Access:**
1. Login as admin
2. Access AdminDashboardActivity
3. Navigate through admin sections

**Verification Checklist:**
- [ ] Admin dashboard loads successfully
- [ ] All admin fragments are accessible
- [ ] Data displays correctly in dashboard
- [ ] Navigation between admin sections works

---

## 📋 PHASE 4: CORE FUNCTIONALITY TESTING

### ✅ Step 4.1: Item Reporting (Lost/Found Items)

**Test Item Creation:**
1. Login as regular user
2. Navigate to ReportFragment
3. Fill out item report form:
   - Item name
   - Description
   - Category (Lost/Found)
   - Location
   - Add photo (if applicable)
4. Submit report

**Verification Checklist:**
- [ ] Form validation works correctly
- [ ] Item saves to Firestore successfully
- [ ] Item appears in browse/home view immediately
- [ ] Image upload works (if implemented)
- [ ] User can see their own items

### ✅ Step 4.2: Item Browsing

**Test Item Display:**
1. Navigate to BrowseFragment or HomeFragment
2. View list of reported items
3. Test item filtering/searching (if implemented)

**Verification Checklist:**
- [ ] Items load from Firestore
- [ ] Item list displays correctly
- [ ] Images load properly (if applicable)
- [ ] Real-time updates work (new items appear)
- [ ] Performance is acceptable with multiple items

### ✅ Step 4.3: Data Persistence Testing

**Test Data Integrity:**
1. Add several test items
2. Close and reopen app
3. Check if items persist
4. Verify in Firebase Console

**Verification Checklist:**
- [ ] Items persist after app restart
- [ ] Data matches between app and Firebase Console
- [ ] No data corruption or loss
- [ ] Timestamps are correct

---

## 📋 PHASE 5: ADMIN FUNCTIONALITY TESTING

### ✅ Step 5.1: Admin Dashboard Features

**Test Admin Data View:**
1. Login as admin
2. Open AdminDashboardActivity
3. Check dashboard statistics and data

**Verification Checklist:**
- [ ] Dashboard loads all user data
- [ ] Statistics are accurate
- [ ] Charts/graphs display correctly (if implemented)
- [ ] Real-time data updates work

### ✅ Step 5.2: Admin Item Management

**Test Admin Item Operations:**
1. View all items as admin
2. Test item moderation features
3. Test item deletion (if implemented)

**Verification Checklist:**
- [ ] Admin can view all items from all users
- [ ] Admin can modify item status
- [ ] Admin can delete inappropriate items
- [ ] Changes reflect immediately in user views

---

## 📋 PHASE 6: PROFILE & LOGOUT TESTING

### ✅ Step 6.1: Profile Management

**Test Profile Display:**
1. Navigate to ProfileFragment
2. Check user information display
3. Test profile editing (if implemented)

**Verification Checklist:**
- [ ] Profile loads user data correctly
- [ ] User information matches Firebase Auth
- [ ] Profile updates save to Firestore
- [ ] Profile picture works (if implemented)

### ✅ Step 6.2: Logout Functionality

**Test Complete Logout:**
1. Navigate to profile or settings
2. Tap logout button
3. Verify logout behavior

**Verification Checklist:**
- [ ] Logout button is accessible
- [ ] User session clears completely
- [ ] Redirected to login screen
- [ ] Cannot navigate back to protected screens
- [ ] Re-login required to access app

---

## 🐛 DEBUGGING COMMANDS & TOOLS

### Logcat Monitoring
```bash
# Monitor app logs
adb logcat | grep "com.example.loginandregistration"

# Monitor Firebase logs specifically
adb logcat | grep -E "(Firebase|Firestore|FirebaseAuth)"

# Monitor crashes
adb logcat | grep "AndroidRuntime"
```

### Firebase Console Checks
1. **Authentication Tab**: Verify user accounts
2. **Firestore Database**: Check data structure and content
3. **Storage**: Verify image uploads (if applicable)
4. **Analytics**: Monitor app usage and crashes

### Common Debug Points
```kotlin
// Add these logs to key functions for debugging:

// In FirebaseManager or auth functions:
Log.d("AUTH_DEBUG", "User: ${FirebaseAuth.getInstance().currentUser?.email}")
Log.d("AUTH_DEBUG", "UID: ${FirebaseAuth.getInstance().currentUser?.uid}")

// In Firestore operations:
Log.d("FIRESTORE_DEBUG", "Attempting to save item: $itemData")
Log.d("FIRESTORE_DEBUG", "Query result size: ${documents.size()}")

// In navigation:
Log.d("NAV_DEBUG", "Navigating to: $fragmentName")
```

---

## ✅ FINAL TESTING CHECKLIST

### Critical Path Testing
- [ ] **User Registration** → **Login** → **Report Item** → **View Item** → **Logout**
- [ ] **Admin Login** → **Dashboard** → **View All Data** → **Manage Items** → **Logout**

### Performance Testing
- [ ] App launches in under 3 seconds
- [ ] Firestore queries complete in reasonable time
- [ ] No memory leaks during extended use
- [ ] Smooth scrolling in item lists

### Error Handling Testing
- [ ] Network disconnection handling
- [ ] Invalid login credentials
- [ ] Empty form submissions
- [ ] Image upload failures (if applicable)

### Security Testing
- [ ] Regular users cannot access admin features
- [ ] Users can only modify their own items
- [ ] Firestore rules are properly enforced
- [ ] No sensitive data in logs

---

## 🚀 NEXT STEPS AFTER TESTING

1. **Document Issues**: Create a list of any failing tests
2. **Prioritize Fixes**: Focus on authentication and data storage issues first
3. **Performance Optimization**: Address any slow operations
4. **User Experience**: Improve loading states and error messages
5. **Production Readiness**: Remove debug logs and test data

---

## 📞 QUICK REFERENCE

**Test Accounts:**
- Admin: `admin@gmail.com`
- Test User: Create during testing

**Key Files to Monitor:**
- `FirebaseManager.kt` - Firebase operations
- `MainActivity.kt` - Main app flow
- `AdminDashboardActivity.kt` - Admin features

**Firebase Console:** https://console.firebase.google.com/project/lost-and-found-954f6