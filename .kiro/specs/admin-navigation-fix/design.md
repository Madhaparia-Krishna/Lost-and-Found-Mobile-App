# Design Document

## Overview

This design document outlines the fixes needed to resolve critical navigation crashes, data model mismatches, and performance issues in the admin module. The primary issue is that the AdminDashboardActivity attempts to set up ActionBar navigation without having a Toolbar configured as the support action bar.

### Root Causes

1. **Navigation Crash**: `setupActionBarWithNavController()` is called but no Toolbar is set via `setSupportActionBar()`
2. **Data Model Mismatches**: Firestore field names don't match Kotlin property names
3. **Missing Enum Values**: UserRole enum doesn't include "Security" role that exists in database
4. **Main Thread Blocking**: Heavy operations running on main thread causing frame drops

## Architecture

### Solution Approach

We'll fix these issues with minimal code changes:

1. Remove ActionBar-dependent navigation setup or add proper Toolbar configuration
2. Add @field:JvmField annotations or custom property names to match Firestore fields
3. Add missing enum values with proper fallback handling
4. Move heavy operations to background threads

## Components and Interfaces

### 1. Navigation Fix Options

#### Option A: Remove ActionBar Navigation (Recommended)

Remove the `setupActionBarWithNavController()` call since bottom navigation is already working.

```kotlin
// Remove this line:
// setupActionBarWithNavController(navController, appBarConfiguration)

// Keep only:
navView.setupWithNavController(navController)
```

#### Option B: Add Toolbar Configuration

Add a Toolbar to the layout and configure it:

```xml
<!-- In activity_admin_dashboard.xml -->
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="?attr/colorPrimary"
    android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar" />
```

```kotlin
// In onCreate():
val toolbar = findViewById<Toolbar>(R.id.toolbar)
setSupportActionBar(toolbar)
setupActionBarWithNavController(navController, appBarConfiguration)
```

### 2. Data Model Fixes

#### LostFoundItem Model

```kotlin
data class LostFoundItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    
    // Fix field name mismatch
    @get:PropertyName("lost_found")
    @set:PropertyName("lost_found")
    var isLost: Boolean = true,
    
    val userId: String = "",
    val userEmail: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val category: String = "",
    
    // Add proper status field
    @get:PropertyName("status")
    @set:PropertyName("status")
    var itemStatus: String = "ACTIVE",
    
    // ... other fields
)
```

#### ActivityItem Model

```kotlin
data class ActivityItem(
    val id: String = "",
    val timestamp: Long = 0,
    val actorEmail: String = "",
    val actionType: String = "",
    val description: String = "",
    
    // Fix field name mismatch
    @get:PropertyName("new_found")
    @set:PropertyName("new_found")
    var isNewFound: Boolean = false,
    
    // ... other fields
)
```

#### UserRole Enum

```kotlin
enum class UserRole {
    USER,
    ADMIN,
    SECURITY,  // Add missing role
    MODERATOR;
    
    companion object {
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.w("UserRole", "Unknown role: $value, defaulting to USER")
                USER  // Default fallback
            }
        }
    }
}
```

### 3. Error Handling Improvements

#### Repository Error Handling

```kotlin
suspend fun getUserAnalytics(): Result<UserAnalytics> {
    return try {
        // ... existing code
    } catch (e: FirebaseFirestoreException) {
        Log.e(TAG, "Firestore error in getUserAnalytics", e)
        Result.failure(e)
    } catch (e: RuntimeException) {
        // Handle deserialization errors
        Log.e(TAG, "Deserialization error in getUserAnalytics", e)
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error in getUserAnalytics", e)
        Result.failure(e)
    }
}
```

#### Safe Deserialization

```kotlin
private fun <T> DocumentSnapshot.toObjectSafe(clazz: Class<T>): T? {
    return try {
        toObject(clazz)
    } catch (e: RuntimeException) {
        Log.e(TAG, "Failed to deserialize document ${this.id}", e)
        null
    }
}
```

### 4. Performance Optimizations

#### Move Heavy Operations Off Main Thread

```kotlin
// In AdminDashboardActivity
private fun testFirebaseConnection() {
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            val result = adminRepository.checkFirebaseConnection()
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    Toast.makeText(this@AdminDashboardActivity, "Connected", Toast.LENGTH_SHORT).show()
                    initializeAdminUser()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
        }
    }
}
```

## Data Models

### Updated Firestore Mappings

```
Firestore Field Name → Kotlin Property Name
--------------------------------
lost_found → isLost (with @PropertyName)
status → itemStatus (with @PropertyName)
new_found → isNewFound (with @PropertyName)
role: "Security" → UserRole.SECURITY
```

## Error Handling

### Error Recovery Strategy

1. **Deserialization Errors**: Log and skip problematic documents
2. **Navigation Errors**: Catch and display toast messages
3. **Enum Parsing Errors**: Use default values with logging
4. **Main Thread Warnings**: Move operations to coroutines

## Testing Strategy

### Manual Testing Checklist

1. Open admin dashboard - should not crash
2. Navigate between all tabs - should work smoothly
3. Press back button - should navigate properly
4. View users with "Security" role - should display correctly
5. View items with various statuses - should display correctly
6. Check logcat for frame skip warnings - should be minimal

### Regression Testing

1. Verify existing functionality still works
2. Check that data is still saved correctly
3. Ensure notifications still work
4. Verify exports still generate properly

## Security Considerations

No security changes needed for these fixes.

## Performance Optimization

### Main Thread Optimization

1. Move Firebase operations to coroutines
2. Use `Dispatchers.IO` for database operations
3. Use `Dispatchers.Main` only for UI updates
4. Implement proper lifecycle-aware coroutines

### Memory Optimization

1. Properly unsubscribe from Firestore listeners
2. Use weak references where appropriate
3. Clear caches when memory is low
