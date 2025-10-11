# Push Notification System - Quick Start Guide

## Overview
This guide helps you quickly understand and use the push notification system in the Lost and Found app.

## For Developers

### 1. Sending a Notification

#### From Admin Repository
```kotlin
// Get repository instance
val repository = AdminRepository()

// Create notification
val notification = PushNotification(
    title = "New Item Found!",
    body = "A blue backpack was found in the cafeteria",
    type = NotificationType.ITEM_MATCH,
    targetUsers = listOf("userId123"), // or listOf("ALL") for everyone
    actionUrl = "item/item456"
)

// Send notification
lifecycleScope.launch {
    val result = repository.sendPushNotification(notification)
    if (result.isSuccess) {
        // Success
    } else {
        // Handle error
    }
}
```

#### Using Auto-Triggers
```kotlin
// Item match notification
NotificationTriggers.sendItemMatchNotification(
    userId = "user123",
    itemId = "item456",
    itemName = "Blue Backpack",
    itemDescription = "Nike backpack with laptop",
    location = "Cafeteria"
)

// Request approved notification
NotificationTriggers.sendRequestApprovedNotification(
    userId = "user123",
    itemId = "item456",
    itemName = "Blue Backpack",
    pickupLocation = "Security Office",
    pickupInstructions = "Bring your student ID"
)

// Donation ready notification
NotificationTriggers.sendDonationReadyNotification(
    userId = "user123",
    itemId = "item456",
    itemName = "Blue Backpack",
    reportedDate = "2024-01-15"
)
```

### 2. Checking Notification Permission

```kotlin
// Check if permission is granted
if (NotificationPermissionHelper.hasNotificationPermission(context)) {
    // Permission granted, can send notifications
} else {
    // Request permission
    NotificationPermissionHelper.checkAndRequestPermission(activity)
}
```

### 3. Handling Permission Results

```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    
    val granted = NotificationPermissionHelper.handlePermissionResult(
        requestCode,
        permissions,
        grantResults
    )
    
    if (granted) {
        // Permission granted
    } else {
        // Permission denied
    }
}
```

### 4. Getting Notification History

```kotlin
// Get notification history as Flow
lifecycleScope.launch {
    repository.getNotificationHistory(limit = 50).collect { notifications ->
        // Update UI with notification list
    }
}
```

### 5. Getting Notification Statistics

```kotlin
lifecycleScope.launch {
    val result = repository.getNotificationStats("notificationId123")
    result.onSuccess { stats ->
        println("Total sent: ${stats.totalSent}")
        println("Delivered: ${stats.delivered}")
        println("Opened: ${stats.opened}")
        println("Open rate: ${stats.getOpenRate()}%")
    }
}
```

## For Admins

### Notification Types

1. **Item Match** (Automatic)
   - Sent when a reported item matches a user's lost item
   - High priority
   - Includes deep link to item details

2. **Request Approved** (Automatic)
   - Sent when security approves an item request
   - High priority
   - Includes pickup location and instructions

3. **Request Denied** (Automatic)
   - Sent when security denies an item request
   - High priority
   - Includes denial reason

4. **Donation Notice** (Automatic)
   - Sent when item is marked for donation
   - Default priority
   - Notifies original reporter

5. **Custom Admin** (Manual)
   - Sent by admins for custom messages
   - Default priority
   - Can target specific users or roles

6. **System Announcement** (Manual)
   - System-wide announcements
   - Default priority
   - Can target all users or specific roles

7. **Security Alert** (Automatic)
   - Security-related alerts
   - High priority
   - Sent to admins and security staff

### Targeting Options

- **Specific Users**: Target individual users by ID
- **By Role**: Target all users with a specific role (USER, ADMIN, SECURITY)
- **All Users**: Broadcast to everyone

### Scheduling Notifications

```kotlin
val notification = PushNotification(
    title = "Scheduled Maintenance",
    body = "System will be down tonight at 11 PM",
    type = NotificationType.SYSTEM_ANNOUNCEMENT,
    targetUsers = listOf("ALL"),
    scheduledFor = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
)

repository.scheduleNotification(notification)
```

## Notification Channels

The app uses 4 notification channels:

1. **Item Matches** (High Importance)
   - Sound: Yes
   - Vibration: Yes
   - Badge: Yes

2. **Request Updates** (High Importance)
   - Sound: Yes
   - Vibration: Yes
   - Badge: Yes

3. **Admin Alerts** (Default Importance)
   - Sound: Yes
   - Vibration: No
   - Badge: Yes

4. **System Announcements** (Default Importance)
   - Sound: No
   - Vibration: No
   - Badge: Yes

Users can customize these channels in their device settings.

## Deep Linking

Notifications support deep linking to specific screens:

- `item/{itemId}` - Navigate to item details
- `request/{requestId}` - Navigate to request status
- `profile` - Navigate to user profile
- `browse` - Navigate to browse items

Example:
```kotlin
val notification = PushNotification(
    title = "Item Found!",
    body = "Your lost item was found",
    type = NotificationType.ITEM_MATCH,
    targetUsers = listOf("user123"),
    actionUrl = "item/item456" // Deep link
)
```

## Permission Handling

### Android 13+ (API 33+)
- Requires POST_NOTIFICATIONS permission
- Permission is requested on first app launch
- Users can grant or deny permission

### Android 12 and below
- No permission required
- Notifications work by default

### Checking Permission Status
```kotlin
// Check if permission is granted
val hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)

// Check if notifications are enabled in settings
val areEnabled = NotificationPermissionHelper.areNotificationsEnabled(context)

// Open notification settings
NotificationPermissionHelper.openNotificationSettings(context)
```

## Troubleshooting

### Notifications Not Appearing

1. **Check Permission**:
   ```kotlin
   if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
       // Request permission
   }
   ```

2. **Check Notification Settings**:
   ```kotlin
   if (!NotificationPermissionHelper.areNotificationsEnabled(context)) {
       // Open settings
       NotificationPermissionHelper.openNotificationSettings(context)
   }
   ```

3. **Check FCM Token**:
   - Verify user has FCM token in Firestore
   - Token is automatically updated on app start

4. **Check Firestore Rules**:
   - Ensure notifications collection has proper write permissions
   - Ensure notificationHistory collection has proper write permissions

### Notifications Not Tracking Opens

1. **Check Deep Linking**:
   - Verify notification includes `notification_id` in intent extras
   - Verify `markNotificationAsOpened()` is called in MainActivity

2. **Check Firestore**:
   - Verify notificationHistory collection exists
   - Check for any Firestore errors in logs

### Permission Denied

1. **Show Rationale**:
   ```kotlin
   if (NotificationPermissionHelper.shouldShowPermissionRationale(activity)) {
       NotificationPermissionHelper.showPermissionRationaleDialog(activity,
           onPositive = { /* Request again */ },
           onNegative = { /* User declined */ }
       )
   }
   ```

2. **Permanent Denial**:
   ```kotlin
   // Show dialog to open settings
   NotificationPermissionHelper.showPermissionDeniedDialog(activity)
   ```

## Best Practices

1. **Always Check Permission Before Sending**:
   ```kotlin
   if (NotificationPermissionHelper.hasNotificationPermission(context)) {
       // Send notification
   }
   ```

2. **Use Appropriate Notification Types**:
   - Use high priority for urgent notifications (item matches, request updates)
   - Use default priority for informational notifications

3. **Include Deep Links**:
   - Always include actionUrl for better user experience
   - Use meaningful URLs that navigate to relevant screens

4. **Track Metrics**:
   - Monitor delivery rates
   - Track open rates
   - Analyze notification effectiveness

5. **Respect User Preferences**:
   - Don't spam users with too many notifications
   - Allow users to customize notification settings
   - Respect system notification settings

## Testing

### Test Notification Sending
```kotlin
// Test notification
val testNotification = PushNotification(
    title = "Test Notification",
    body = "This is a test notification",
    type = NotificationType.CUSTOM_ADMIN,
    targetUsers = listOf(FirebaseAuth.getInstance().currentUser?.uid ?: "")
)

lifecycleScope.launch {
    val result = repository.sendPushNotification(testNotification)
    if (result.isSuccess) {
        Toast.makeText(context, "Test notification sent", Toast.LENGTH_SHORT).show()
    }
}
```

### Test Permission Flow
1. Uninstall and reinstall app
2. Launch app
3. Verify permission dialog appears
4. Test grant and deny scenarios

### Test Deep Linking
1. Send notification with actionUrl
2. Tap notification
3. Verify app navigates to correct screen
4. Verify notification is marked as opened

## Support

For issues or questions:
1. Check logs for error messages
2. Verify Firestore rules and permissions
3. Check FCM configuration in Firebase Console
4. Review notification channel settings
