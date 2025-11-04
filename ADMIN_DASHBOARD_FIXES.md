# Admin Dashboard Fixes and Enhancements

## Issues Identified

### 1. Navigation Back Button Issue
**Problem**: When clicking on "Review Items" or "Pending Items" from the dashboard, users cannot navigate back to the dashboard.

**Root Cause**: The navigation is using `findNavController().navigate()` which adds fragments to the back stack, but there's no back button handling in the fragments.

**Solution**: 
- Add proper back navigation support in ItemDetailsFragment and other detail fragments
- Ensure the ActionBar/Toolbar has a back button enabled
- Use `popBackStack()` or proper navigation actions

### 2. Users Not Showing in Admin Dashboard
**Problem**: Registered users don't appear in the admin dashboard users list.

**Root Cause**: 
- Users are being registered in Firebase Authentication but not being added to the Firestore `users` collection
- The registration process doesn't create a user document in Firestore

**Solution**:
- Update the registration process to create user documents in Firestore
- Ensure all registered users have a corresponding document in the `users` collection
- Add migration script to sync existing Firebase Auth users to Firestore

### 3. Donation System Enhancement
**Problem**: Need to add test donated items that are 1+ year old and require admin approval for donation.

**Solution**:
- Create test data generator for old unclaimed items
- Implement automatic detection of items older than 1 year
- Add donation workflow where admin can mark items for donation
- Display items in the donations list with age indicators

## Implementation Plan

### Fix 1: Navigation Back Button
1. Enable back button in AdminDashboardActivity
2. Add navigation handling in ItemDetailsFragment
3. Test navigation flow

### Fix 2: User Registration to Firestore
1. Update Register.kt to create Firestore user document
2. Create migration function to sync existing users
3. Test user registration flow

### Fix 3: Donation System with Old Items
1. Create test data generator for old items
2. Implement age-based filtering in donation queue
3. Add visual indicators for item age
4. Test donation workflow

## Files to Modify

1. `app/src/main/java/com/example/loginandregistration/Register.kt`
2. `app/src/main/java/com/example/loginandregistration/admin/AdminDashboardActivity.kt`
3. `app/src/main/java/com/example/loginandregistration/admin/fragments/ItemDetailsFragment.kt`
4. `app/src/main/java/com/example/loginandregistration/admin/repository/AdminRepository.kt`
5. `app/src/main/java/com/example/loginandregistration/admin/viewmodel/AdminDashboardViewModel.kt`
