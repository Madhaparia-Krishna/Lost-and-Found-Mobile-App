# Firestore Rules - Changes Summary

## What Changed

Your Firestore rules have been updated to work seamlessly with your Android app while maintaining backward compatibility with your existing data.

## Key Changes

### 1. Enhanced `isAdmin()` Function
**Before**: Only checked email (`admin@gmail.com`)
**After**: Checks BOTH email AND role field

```javascript
// Now supports:
// - Email: admin@gmail.com
// - Role: 'ADMIN', 'Admin', or 'admin'
```

**Why**: Your app stores roles in the database, so rules need to check the role field. Email check is kept as fallback.

### 2. Added `isSecurityOrAdmin()` Function
**New function** that checks for Security or Admin roles:

```javascript
// Supports all these formats:
// - 'SECURITY', 'Security', 'security'
// - 'ADMIN', 'Admin', 'admin'
```

**Why**: Your app has Security role users who need elevated permissions.

### 3. Updated Items Collection Rules
**Changes**:
- Security/Admin can create items for anyone (not just themselves)
- Users can update their own items but NOT the status field
- Security/Admin can update anything including status
- Security/Admin can delete any item

**Why**: Matches your app's requirement where Security staff can manage all items.

### 4. Updated Users Collection Rules
**Changes**:
- Users can only read their own profile (privacy)
- Security/Admin can read any profile
- Maintains protection on role and isBlocked fields

**Why**: Better privacy - regular users shouldn't see all user profiles unless needed for items.

### 5. Updated Activities Collection Rules
**Changes**:
- Security/Admin can read and create activities (not just Admin)

**Why**: Security staff need to log their actions too.

## Role Format Support

All role checks now support **three formats** for maximum compatibility:

| Role | Supported Formats |
|------|------------------|
| Admin | `ADMIN`, `Admin`, `admin` |
| Security | `SECURITY`, `Security`, `security` |
| Moderator | `MODERATOR`, `Moderator`, `moderator` |
| User | `USER`, `User`, `user` |

## Collections Covered

✅ **items** - Lost and found items
✅ **users** - User profiles
✅ **activities** - Legacy audit log
✅ **activityLogs** - Enhanced audit log
✅ **donations** - Donation tracking
✅ **notifications** - Push notifications
✅ **notificationHistory** - Notification delivery tracking
✅ **admin** - Admin-specific data

## Testing the Rules

### Test 1: Admin Access (Email-based)
```
User: admin@gmail.com
Expected: Full access to all collections
```

### Test 2: Admin Access (Role-based)
```
User: Any email with role = 'ADMIN'
Expected: Full access to all collections
```

### Test 3: Security Access
```
User: Any email with role = 'SECURITY'
Expected: Can manage items, view users, log activities
```

### Test 4: Regular User
```
User: Any email with role = 'USER'
Expected: Can create/edit own items, view own profile
```

## Deployment

### Option 1: Firebase Console (Recommended)
1. Go to Firebase Console
2. Select your project
3. Navigate to Firestore Database > Rules
4. Copy content from `firestore.rules`
5. Click "Publish"

### Option 2: Firebase CLI
```bash
firebase deploy --only firestore:rules
```

## Verification

After deployment, check:

```bash
# Monitor for permission errors
adb logcat | findstr "PERMISSION_DENIED"
```

Expected result: **No PERMISSION_DENIED errors**

## Backward Compatibility

✅ **Email-based admin** (`admin@gmail.com`) still works
✅ **Existing data** with any role format works
✅ **No breaking changes** to existing functionality
✅ **Gradual migration** supported

## Security Notes

### What's Protected
- ✅ Role field (only admins can change)
- ✅ isBlocked field (only admins can change)
- ✅ Audit logs (immutable)
- ✅ Admin collections (admin-only)

### What's Allowed
- ✅ Users can create their own items
- ✅ Users can update their own items (except status)
- ✅ Security can manage all items
- ✅ Admins have full control

## Common Issues & Solutions

### Issue: Still getting PERMISSION_DENIED
**Solution**: 
1. Verify rules are deployed (check timestamp in Firebase Console)
2. Check user document exists in Firestore
3. Verify role field is set correctly
4. Run data migration: `DataMigrationHelper.runAllMigrations()`

### Issue: Admin can't access data
**Solution**:
1. Check if admin@gmail.com is logged in, OR
2. Check if user document has role = 'ADMIN'
3. Ensure user document exists in Firestore

### Issue: Security role not working
**Solution**:
1. Verify role is spelled correctly: 'SECURITY' (all caps)
2. Check user document exists
3. Redeploy rules if recently changed

## Next Steps

1. ✅ Rules updated in code
2. ⏳ Deploy rules to Firebase Console
3. ⏳ Test with admin user
4. ⏳ Test with security user
5. ⏳ Test with regular user
6. ⏳ Monitor for 24 hours

## Rollback

If issues occur, previous rules are backed up in Firebase Console:
1. Go to Firestore Database > Rules
2. Click "History" tab
3. Select previous version
4. Click "Restore"
