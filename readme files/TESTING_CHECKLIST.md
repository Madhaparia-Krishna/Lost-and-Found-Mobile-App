# Quick Testing Checklist

**Use this checklist during manual testing sessions**

## Pre-Testing Setup
- [ ] Install debug APK on test device
- [ ] Enable Developer Options
- [ ] Enable "Show GPU view updates" for performance monitoring
- [ ] Connect device to Android Studio for logcat monitoring
- [ ] Clear app data before starting fresh tests

---

## Critical Path Testing (30 minutes)

### User Flow (15 min)
- [ ] Launch app → Splash screen displays
- [ ] Register new user → Success
- [ ] Login → Redirected to main screen
- [ ] Report lost item → Item created
- [ ] Browse items → Items display
- [ ] View item details → Details load
- [ ] Logout → Returns to login

### Admin Flow (15 min)
- [ ] Login as admin → Admin dashboard loads
- [ ] User Management → List displays
- [ ] View user details → Details load
- [ ] Edit user → Update successful
- [ ] Ban user → User blocked
- [ ] Item Management → All items display
- [ ] Edit item → Update successful
- [ ] Activity Log → Loads without crash ✅ (Task 1 fix)
- [ ] Logout → Returns to login

---

## Security Testing (15 minutes)

### Blocked User Test
- [ ] Block a test user via admin panel
- [ ] Attempt login with blocked user
- [ ] Verify: "Your account has been blocked" message
- [ ] Verify: User is signed out

### Firestore Rules Test
- [ ] User A creates item
- [ ] User B cannot access User A's item
- [ ] Admin can access all items
- [ ] Activity logs only accessible by admin

---

## Performance Testing (10 minutes)

### Scroll Performance
- [ ] Scroll through items list (20+ items)
- [ ] Monitor frame drops (should be < 10)
- [ ] Scroll through user list in admin panel
- [ ] Scroll through activity log

### Main Thread Check
- [ ] Enable StrictMode (if in debug build)
- [ ] Perform various operations
- [ ] Check logcat for violations
- [ ] Verify no NetworkOnMainThreadException

---

## UI/UX Testing (10 minutes)

### Splash Screen
- [ ] Force stop app
- [ ] Launch app
- [ ] Verify splash screen displays
- [ ] Verify smooth transition

### App Icon
- [ ] Check home screen icon
- [ ] Check app drawer icon
- [ ] Verify not default Android icon

---

## Error Check (10 minutes)

### Logcat Errors
- [ ] Clear logcat
- [ ] Perform complete flow
- [ ] Filter for ERROR level
- [ ] Verify no critical errors:
  - ❌ Resources$NotFoundException
  - ❌ CustomClassMapper errors
  - ❌ PERMISSION_DENIED
  - ❌ Failed to get service from broker

### Logcat Warnings
- [ ] Filter for WARN level
- [ ] Verify no critical warnings:
  - ❌ Resource failed to call release
  - ❌ commitText on inactive InputConnection
  - ❌ Main thread violations

---

## Quick Pass/Fail Criteria

### PASS Criteria ✅
- No crashes during critical flows
- Blocked users cannot login
- Admin operations work correctly
- Performance < 10 frames dropped
- Splash screen displays
- App icon displays
- No critical errors in logcat

### FAIL Criteria ❌
- Any crash during testing
- Blocked users can login
- Admin operations fail
- Severe performance issues
- Missing splash screen or icon
- Critical errors in logcat

---

## Test Results Summary

**Date:** _______________  
**Tester:** _______________  
**Device:** _______________  
**Android Version:** _______________

**Overall Status:** [ ] PASS  [ ] FAIL  [ ] NEEDS REVIEW

**Critical Issues Found:** _______________

**Notes:**
_______________________________________
_______________________________________
_______________________________________

---

## Quick Commands

### Install APK
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Clear App Data
```bash
adb shell pm clear com.example.loginandregistration
```

### Monitor Logcat
```bash
adb logcat | findstr "loginandregistration"
```

### Check Frame Drops
```bash
adb shell dumpsys gfxinfo com.example.loginandregistration
```
