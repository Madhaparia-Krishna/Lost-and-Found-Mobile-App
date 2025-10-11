# Activity Logging System - Quick Start Guide

## Overview
The activity logging system automatically tracks all admin and user actions in the Lost and Found application, providing a complete audit trail.

## Key Features
- ✅ Automatic logging of all admin operations
- ✅ User authentication event tracking (login/logout/register)
- ✅ Real-time activity monitoring
- ✅ Advanced search and filtering
- ✅ Automatic monthly archiving of old logs
- ✅ Comprehensive audit trail for compliance

## Quick Usage

### 1. Automatic Logging (Already Integrated)
Most admin operations automatically log activities. No additional code needed for:
- User blocking/unblocking
- User role changes
- User detail edits
- Item status changes
- Item edits and deletions
- Donation workflow actions

### 2. Manual Activity Logging
For custom actions, use the repository method:

```kotlin
val repository = AdminRepository()

// Create activity log
val log = ActivityLog(
    actorId = currentUser.uid,
    actorEmail = currentUser.email ?: "",
    actorRole = UserRole.ADMIN,
    actionType = ActionType.ITEM_EDIT,
    targetType = TargetType.ITEM,
    targetId = itemId,
    description = "Custom action description",
    previousValue = "old value",
    newValue = "new value",
    deviceInfo = android.os.Build.MODEL
)

// Log the activity
lifecycleScope.launch {
    repository.logActivity(log)
}
```

### 3. Retrieving Activity Logs

#### Real-time Logs with Filters
```kotlin
val repository = AdminRepository()

// Get logs with filters
repository.getActivityLogs(
    limit = 50,
    filters = mapOf(
        "actionType" to "USER_BLOCK",
        "actorEmail" to "admin@gmail.com",
        "startDate" to startTimestamp.toString(),
        "endDate" to endTimestamp.toString()
    )
).collect { logs ->
    // Update UI with logs
    activityAdapter.submitList(logs)
}
```

#### Search Logs
```kotlin
lifecycleScope.launch {
    val result = repository.searchActivityLogs(
        query = "blocked",
        filters = mapOf(
            "actionType" to "USER_BLOCK",
            "startDate" to startDate.toString()
        )
    )
    
    if (result.isSuccess) {
        val logs = result.getOrNull() ?: emptyList()
        // Display search results
    }
}
```

#### Get Logs by User
```kotlin
lifecycleScope.launch {
    val result = repository.getActivityLogsByUser(
        userEmail = "user@example.com",
        limit = 100
    )
    
    if (result.isSuccess) {
        val userLogs = result.getOrNull() ?: emptyList()
        // Display user's activity history
    }
}
```

#### Get Logs by Action Type
```kotlin
lifecycleScope.launch {
    val result = repository.getActivityLogsByActionType(
        actionType = ActionType.ITEM_DELETE,
        limit = 50
    )
    
    if (result.isSuccess) {
        val deleteLogs = result.getOrNull() ?: emptyList()
        // Display deletion history
    }
}
```

### 4. User Authentication Logging

#### Log User Login
```kotlin
// In Login activity after successful authentication
lifecycleScope.launch {
    val repository = AdminRepository()
    repository.logUserLogin(
        userId = currentUser.uid,
        userEmail = currentUser.email ?: "",
        userRole = if (isAdmin) UserRole.ADMIN else UserRole.USER
    )
}
```

#### Log User Logout
```kotlin
// In logout handler
lifecycleScope.launch {
    val repository = AdminRepository()
    repository.logUserLogout(
        userId = currentUser.uid,
        userEmail = currentUser.email ?: "",
        userRole = currentUserRole
    )
}
```

#### Log User Registration
```kotlin
// In Register activity after successful registration
lifecycleScope.launch {
    val repository = AdminRepository()
    repository.logUserRegistration(
        userId = newUser.uid,
        userEmail = newUser.email ?: ""
    )
}
```

### 5. Activity Log Archiving

#### Schedule Monthly Archiving
Add to your Application class or AdminDashboardActivity onCreate:

```kotlin
class LostFoundApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Schedule monthly archiving
        ActivityLogArchiveScheduler.scheduleMonthlyArchiving(this)
    }
}
```

#### Manual Archive Trigger
```kotlin
// Trigger immediate archiving (for testing or manual execution)
ActivityLogArchiveScheduler.triggerImmediateArchiving(context)
```

#### Check Archivable Logs Count
```kotlin
lifecycleScope.launch {
    val repository = AdminRepository()
    val result = repository.getArchivableLogsCount()
    
    if (result.isSuccess) {
        val count = result.getOrNull() ?: 0
        Toast.makeText(context, "$count logs ready for archiving", Toast.LENGTH_SHORT).show()
    }
}
```

#### Execute Archiving
```kotlin
lifecycleScope.launch {
    val repository = AdminRepository()
    val result = repository.archiveOldLogs()
    
    if (result.isSuccess) {
        val archivedCount = result.getOrNull() ?: 0
        Toast.makeText(context, "Archived $archivedCount logs", Toast.LENGTH_SHORT).show()
    }
}
```

## Action Types Reference

### User Actions
- `USER_LOGIN` - User logged in
- `USER_LOGOUT` - User logged out
- `USER_REGISTER` - New user registered
- `ITEM_REPORT` - User reported an item
- `ITEM_REQUEST` - User requested an item
- `ITEM_CLAIM` - User claimed an item

### Admin Actions
- `USER_BLOCK` - Admin blocked a user
- `USER_UNBLOCK` - Admin unblocked a user
- `USER_ROLE_CHANGE` - Admin changed user role
- `USER_EDIT` - Admin edited user details
- `ITEM_EDIT` - Admin edited item details
- `ITEM_STATUS_CHANGE` - Admin changed item status
- `ITEM_DELETE` - Admin deleted an item
- `DONATION_MARK_READY` - Admin marked item ready for donation
- `DONATION_COMPLETE` - Admin completed donation
- `NOTIFICATION_SEND` - Admin sent notification
- `DATA_EXPORT` - Admin exported data

### System Events
- `SYSTEM_MAINTENANCE` - System maintenance event
- `SYSTEM_ERROR` - System error occurred
- `AUTO_DONATION_FLAG` - System auto-flagged item for donation
- `LOG_ARCHIVE` - System archived old logs

## Target Types Reference
- `USER` - Action targets a user
- `ITEM` - Action targets an item
- `DONATION` - Action targets a donation
- `NOTIFICATION` - Action targets a notification
- `SYSTEM` - Action targets the system

## Filter Options

### Available Filters
- `actionType` - Filter by action type (e.g., "USER_BLOCK")
- `targetType` - Filter by target type (e.g., "USER")
- `actorId` - Filter by actor user ID
- `actorEmail` - Filter by actor email
- `targetId` - Filter by target entity ID
- `startDate` - Filter by start date (timestamp in milliseconds)
- `endDate` - Filter by end date (timestamp in milliseconds)

### Example Filter Combinations
```kotlin
// Get all user blocks in the last 7 days
val filters = mapOf(
    "actionType" to "USER_BLOCK",
    "startDate" to (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000).toString()
)

// Get all actions by specific admin
val filters = mapOf(
    "actorEmail" to "admin@gmail.com"
)

// Get all item deletions
val filters = mapOf(
    "actionType" to "ITEM_DELETE",
    "targetType" to "ITEM"
)
```

## Best Practices

### 1. Always Include Context
Provide meaningful descriptions and metadata:
```kotlin
ActivityLog(
    description = "User blocked for violating community guidelines",
    metadata = mapOf(
        "reason" to "spam",
        "reportCount" to "5",
        "violationType" to "repeated_offense"
    )
)
```

### 2. Track Previous and New Values
For change operations, always include before/after states:
```kotlin
ActivityLog(
    previousValue = "USER",
    newValue = "MODERATOR",
    description = "User role upgraded to moderator"
)
```

### 3. Handle Errors Gracefully
Logging should never block main operations:
```kotlin
try {
    repository.logActivity(log)
} catch (e: Exception) {
    Log.e(TAG, "Failed to log activity", e)
    // Continue with main operation
}
```

### 4. Use Appropriate Action Types
Choose the most specific action type available for better filtering and reporting.

### 5. Regular Archiving
Schedule monthly archiving to keep the main collection performant:
```kotlin
ActivityLogArchiveScheduler.scheduleMonthlyArchiving(context)
```

## Troubleshooting

### Logs Not Appearing
1. Check admin access: `repository.isAdminUser()`
2. Verify Firestore connection
3. Check Firestore security rules
4. Look for errors in Logcat

### Archiving Not Running
1. Check WorkManager status: `ActivityLogArchiveScheduler.getArchivingWorkStatus(context)`
2. Verify network connectivity constraint
3. Check battery level (requires battery not low)
4. Review WorkManager logs

### Search Not Finding Results
1. Verify query string format
2. Check filter values (must match exactly)
3. Ensure logs exist in the date range
4. Try without filters first

## Security Notes

- All activity log methods require admin access
- Logs are immutable once created
- Archive operations are admin-only
- Sensitive data should not be stored in logs
- IP addresses and device info are optional

## Performance Tips

- Use pagination (default 50 items)
- Apply filters to reduce result set
- Use real-time listeners for dashboard
- Use one-time queries for reports
- Archive old logs regularly

## Support

For issues or questions:
1. Check Logcat for error messages
2. Verify Firestore security rules
3. Review implementation in AdminRepository.kt
4. Check WorkManager status for archiving issues
