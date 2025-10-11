# 🔥 Firebase Setup Guide for Lost & Found Admin Dashboard

## 1. **Firebase Console Setup**

### Step 1: Create/Configure Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your existing project: `lost-and-found-954f6`
3. Or create a new project if needed

### Step 2: Enable Authentication
1. In Firebase Console, go to **Authentication**
2. Click **"Get started"**
3. Go to **"Sign-in method"** tab
4. Enable **"Email/Password"** provider
5. Click **"Save"**

### Step 3: Create Firestore Database
1. In Firebase Console, go to **Firestore Database**
2. Click **"Create database"**
3. **Choose "Start in test mode"** (we'll add proper rules later)
4. Select a location (choose closest to your users)
5. Click **"Done"**

### Step 4: Apply Security Rules
1. In Firestore Console, go to **"Rules"** tab
2. Replace the default rules with the content from `firestore.rules` file
3. Click **"Publish"**

## 2. **Firestore Security Rules**

Copy and paste these rules in your Firebase Console → Firestore → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if user is admin
    function isAdmin() {
      return isAuthenticated() && request.auth.token.email == 'admin@gmail.com';
    }
    
    // Helper function to check if user owns the resource
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Helper function to check if user is moderator or admin
    function isModerator() {
      return isAuthenticated() && 
             (request.auth.token.email == 'admin@gmail.com' ||
              exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
              get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'MODERATOR']);
    }
    
    // Items collection rules
    match /items/{itemId} {
      // Anyone can read items (for browsing lost & found)
      allow read: if isAuthenticated();
      
      // Users can create items (report lost/found items)
      allow create: if isAuthenticated() && 
                   request.auth.uid == resource.data.userId;
      
      // Users can update their own items, admins/moderators can update any
      allow update: if isAuthenticated() && 
                   (request.auth.uid == resource.data.userId || isModerator());
      
      // Only admins can delete items
      allow delete: if isAdmin();
    }
    
    // Users collection rules
    match /users/{userId} {
      // Users can read their own profile, admins can read all
      allow read: if isAuthenticated() && 
                 (isOwner(userId) || isAdmin());
      
      // Users can create/update their own profile
      allow create, update: if isAuthenticated() && 
                           (isOwner(userId) || isAdmin());
      
      // Only admins can delete users
      allow delete: if isAdmin();
    }
    
    // Activities collection rules (admin audit log)
    match /activities/{activityId} {
      // Only admins and moderators can read activities
      allow read: if isModerator();
      
      // Only system (admin operations) can create activities
      allow create: if isAdmin();
      
      // No updates or deletes allowed on activities (audit trail)
      allow update, delete: if false;
    }
    
    // Admin-specific collections
    match /admin/{document=**} {
      // Only admins can access admin collections
      allow read, write: if isAdmin();
    }
    
    // Default deny rule for any other collections
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## 3. **Database Collections Structure**

Your Firestore database will have these collections:

### `items/` Collection
```javascript
{
  id: "item_id",
  name: "iPhone 13 Pro",
  description: "Black iPhone with cracked screen",
  location: "Library - 2nd Floor",
  contactInfo: "john@example.com",
  isLost: true, // true = lost, false = found
  userId: "user_uid",
  userEmail: "john@example.com",
  imageUrl: "https://...",
  timestamp: Timestamp
}
```

### `users/` Collection
```javascript
{
  uid: "user_uid",
  email: "user@example.com",
  displayName: "John Doe",
  photoUrl: "https://...",
  role: "USER", // USER, MODERATOR, ADMIN
  isBlocked: false,
  createdAt: 1234567890,
  lastLoginAt: 1234567890,
  itemsReported: 5,
  itemsFound: 2,
  itemsClaimed: 1
}
```

### `activities/` Collection
```javascript
{
  id: "activity_id",
  userId: "user_uid",
  userName: "John Doe",
  userEmail: "john@example.com",
  action: "ITEM_REPORTED", // ITEM_REPORTED, ITEM_FOUND, USER_BLOCKED, etc.
  itemId: "item_id",
  itemName: "iPhone 13 Pro",
  description: "Item reported as lost",
  timestamp: 1234567890,
  isNew: true
}
```

## 4. **Testing Firebase Connection**

### Method 1: Check Logs
Run your app and check logcat for Firebase connection logs:
```bash
adb logcat | grep -E "(Firebase|Firestore)"
```

You should see:
- "Firebase initialized successfully"
- "Firestore configured with persistence enabled"
- "Firestore connection test successful"

### Method 2: Test in Admin Dashboard
1. Login as `admin@gmail.com`
2. Go to admin dashboard
3. Use "Create Test Data" menu option
4. Check if data appears in Firebase Console

## 5. **Common Issues & Solutions**

### Issue: "Permission denied" errors
**Solution**: Make sure your Firestore rules are properly applied and published

### Issue: "Network error" or connection timeouts
**Solutions**:
1. Check internet connection
2. Verify `google-services.json` is in the correct location (`app/` folder)
3. Make sure Firebase project ID matches your configuration

### Issue: Admin can't access data
**Solution**: Ensure the user is logged in with exactly `admin@gmail.com`

### Issue: Empty collections
**Solution**: Use the "Create Test Data" feature in the admin dashboard menu

## 6. **Firebase Console Verification**

After setup, verify in Firebase Console:

1. **Authentication → Users**: Should show your admin user after login
2. **Firestore → Data**: Should show collections after creating test data
3. **Firestore → Rules**: Should show your custom rules
4. **Firestore → Usage**: Should show read/write activity

## 7. **Security Best Practices**

1. **Never use test mode rules in production**
2. **Always validate user permissions server-side**
3. **Use Firebase Admin SDK for sensitive operations**
4. **Monitor usage and set up billing alerts**
5. **Regularly review security rules**

## 8. **Backup & Recovery**

1. **Export data regularly**: Use Firebase CLI or console export
2. **Version control rules**: Keep `firestore.rules` in your repository
3. **Monitor quotas**: Set up alerts for read/write limits

Your Firebase setup is now complete and secure! 🎉