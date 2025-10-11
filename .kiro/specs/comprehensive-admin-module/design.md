# Design Document

## Overview

This design document outlines the architecture and implementation strategy for the comprehensive admin module of the Lost and Found Android application. The module extends the existing basic admin dashboard with advanced features including enhanced user management, comprehensive item lifecycle management, automated donation workflows, data export capabilities, system activity logging, and push notification functionality.

The design follows the existing MVVM (Model-View-ViewModel) architecture pattern with Repository pattern for data access, leveraging Firebase Firestore for real-time data synchronization and Firebase Cloud Messaging (FCM) for push notifications. The module is built as an extension to the existing admin package structure.

### Design Principles

1. **Consistency**: Maintain consistency with existing codebase architecture and patterns
2. **Scalability**: Design for efficient handling of large datasets with pagination and caching
3. **Real-time Updates**: Leverage Firestore listeners for live data synchronization
4. **Security**: Implement role-based access control and comprehensive audit logging
5. **User Experience**: Provide intuitive mobile-first UI with responsive feedback
6. **Maintainability**: Use modular design with clear separation of concerns

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Fragments   │  │   Adapters   │  │   Dialogs    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   ViewModel Layer                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │  AdminDashboardViewModel (Extended)              │   │
│  │  - LiveData for UI state                         │   │
│  │  - Coroutines for async operations               │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   Repository Layer                       │
│  ┌──────────────────────────────────────────────────┐   │
│  │  AdminRepository (Extended)                      │   │
│  │  - User management operations                    │   │
│  │  - Item management operations                    │   │
│  │  - Donation workflow operations                  │   │
│  │  - Activity logging operations                   │   │
│  │  - Export operations                             │   │
│  │  - Notification operations                       │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │Firestore │  │  FCM     │  │ Storage  │  │  Local  │ │
│  │          │  │          │  │          │  │  Cache  │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Language**: Kotlin
- **Architecture**: MVVM + Repository Pattern
- **Database**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **File Storage**: Firebase Storage (for exports)
- **Image Loading**: Glide
- **Charts**: MPAndroidChart
- **Async Operations**: Kotlin Coroutines + Flow
- **UI Components**: Material Design 3, AndroidX libraries

## Components and Interfaces

### 1. Enhanced Data Models

#### Extended AdminUser Model

```kotlin
data class AdminUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.USER,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = 0,
    val itemsReported: Int = 0,
    val itemsFound: Int = 0,
    val itemsClaimed: Int = 0,
    // New fields
    val blockReason: String = "",
    val blockedBy: String = "",
    val blockedAt: Long = 0,
    val deviceInfo: String = "",
    val lastActivityAt: Long = 0
)
```

#### Enhanced LostFoundItem Model

```kotlin
data class LostFoundItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    val isLost: Boolean = true,
    val userId: String = "",
    val userEmail: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    // New fields for enhanced management
    val category: String = "",
    val status: ItemStatus = ItemStatus.ACTIVE,
    val statusHistory: List<StatusChange> = emptyList(),
    val requestedBy: String = "",
    val requestedAt: Long = 0,
    val returnedAt: Long = 0,
    val donationEligibleAt: Long = 0,
    val donatedAt: Long = 0,
    val lastModifiedBy: String = "",
    val lastModifiedAt: Long = 0
)

enum class ItemStatus {
    ACTIVE,           // Lost or Found, visible to users
    REQUESTED,        // User has requested the item
    RETURNED,         // Item returned to owner
    DONATION_PENDING, // Eligible for donation (1 year old)
    DONATION_READY,   // Admin marked as ready for donation
    DONATED           // Final status - donated
}

data class StatusChange(
    val previousStatus: ItemStatus,
    val newStatus: ItemStatus,
    val changedBy: String,
    val changedAt: Long,
    val reason: String = ""
)
```

#### Donation Models

```kotlin
data class DonationItem(
    val itemId: String = "",
    val itemName: String = "",
    val category: String = "",
    val location: String = "",
    val reportedAt: Long = 0,
    val eligibleAt: Long = 0,
    val status: DonationStatus = DonationStatus.PENDING,
    val markedReadyBy: String = "",
    val markedReadyAt: Long = 0,
    val donatedAt: Long = 0,
    val donatedBy: String = "",
    val estimatedValue: Double = 0.0,
    val donationRecipient: String = ""
)

enum class DonationStatus {
    PENDING,    // Eligible but not reviewed
    READY,      // Admin marked as ready
    DONATED     // Final status
}

data class DonationStats(
    val totalDonated: Int = 0,
    val totalValue: Double = 0.0,
    val donationsByCategory: Map<String, Int> = emptyMap(),
    val donationsByMonth: Map<String, Int> = emptyMap(),
    val pendingDonations: Int = 0,
    val readyForDonation: Int = 0
)
```

#### Activity Log Models

```kotlin
data class ActivityLog(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val actorId: String = "",
    val actorEmail: String = "",
    val actorRole: UserRole = UserRole.USER,
    val actionType: ActionType,
    val targetType: TargetType,
    val targetId: String = "",
    val description: String = "",
    val previousValue: String = "",
    val newValue: String = "",
    val ipAddress: String = "",
    val deviceInfo: String = "",
    val metadata: Map<String, String> = emptyMap()
)

enum class ActionType {
    // User actions
    USER_LOGIN, USER_LOGOUT, USER_REGISTER,
    ITEM_REPORT, ITEM_REQUEST, ITEM_CLAIM,
    
    // Admin actions
    USER_BLOCK, USER_UNBLOCK, USER_ROLE_CHANGE, USER_EDIT,
    ITEM_EDIT, ITEM_STATUS_CHANGE, ITEM_DELETE,
    DONATION_MARK_READY, DONATION_COMPLETE,
    NOTIFICATION_SEND,
    
    // System events
    SYSTEM_MAINTENANCE, SYSTEM_ERROR, AUTO_DONATION_FLAG
}

enum class TargetType {
    USER, ITEM, DONATION, NOTIFICATION, SYSTEM
}
```

#### Notification Models

```kotlin
data class PushNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType,
    val targetUsers: List<String> = emptyList(), // User IDs or "ALL"
    val targetRoles: List<UserRole> = emptyList(),
    val actionUrl: String = "",
    val imageUrl: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledFor: Long = 0,
    val sentAt: Long = 0,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val deliveredCount: Int = 0,
    val openedCount: Int = 0,
    val metadata: Map<String, String> = emptyMap()
)

enum class NotificationType {
    ITEM_MATCH,           // Auto: Item matches user's lost item
    REQUEST_APPROVED,     // Auto: Request approved by security
    REQUEST_DENIED,       // Auto: Request denied
    DONATION_NOTICE,      // Auto: Item marked for donation
    CUSTOM_ADMIN,         // Manual: Admin custom message
    SYSTEM_ANNOUNCEMENT,  // Manual: System-wide announcement
    SECURITY_ALERT        // Auto: Security-related alerts
}

enum class DeliveryStatus {
    PENDING, SCHEDULED, SENDING, SENT, FAILED, PARTIALLY_SENT
}

data class NotificationHistory(
    val notificationId: String = "",
    val userId: String = "",
    val deliveredAt: Long = 0,
    val openedAt: Long = 0,
    val isOpened: Boolean = false,
    val deviceToken: String = ""
)
```

#### Export Models

```kotlin
data class ExportRequest(
    val id: String = "",
    val format: ExportFormat,
    val dataType: ExportDataType,
    val dateRange: DateRange,
    val filters: Map<String, String> = emptyMap(),
    val requestedBy: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val status: ExportStatus = ExportStatus.PENDING,
    val fileUrl: String = "",
    val completedAt: Long = 0,
    val errorMessage: String = ""
)

enum class ExportFormat {
    PDF, CSV, EXCEL
}

enum class ExportDataType {
    ITEMS, USERS, ACTIVITIES, DONATIONS, COMPREHENSIVE
}

enum class ExportStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

data class DateRange(
    val startDate: Long,
    val endDate: Long
)
```

### 2. Repository Layer Extensions

#### AdminRepository Extended Interface

```kotlin
class AdminRepository {
    // Existing methods...
    
    // Enhanced User Management
    suspend fun getUserDetails(userId: String): Result<AdminUser>
    suspend fun updateUserDetails(userId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun blockUser(userId: String, reason: String): Result<Unit>
    suspend fun unblockUser(userId: String): Result<Unit>
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit>
    fun getUserAnalytics(): Flow<UserAnalytics>
    suspend fun searchUsers(query: String): Result<List<AdminUser>>
    
    // Enhanced Item Management
    suspend fun getItemDetails(itemId: String): Result<LostFoundItem>
    suspend fun updateItemDetails(itemId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun updateItemStatus(itemId: String, newStatus: ItemStatus, reason: String): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    fun getAllItemsWithStatus(): Flow<List<LostFoundItem>>
    suspend fun searchItems(query: String, filters: Map<String, String>): Result<List<LostFoundItem>>
    
    // Donation Management
    fun getDonationQueue(): Flow<List<DonationItem>>
    suspend fun markItemForDonation(itemId: String): Result<Unit>
    suspend fun markItemReadyForDonation(itemId: String): Result<Unit>
    suspend fun markItemAsDonated(itemId: String, recipient: String, value: Double): Result<Unit>
    fun getDonationStats(dateRange: DateRange): Flow<DonationStats>
    suspend fun getDonationHistory(dateRange: DateRange): Result<List<DonationItem>>
    
    // Activity Logging
    suspend fun logActivity(log: ActivityLog): Result<Unit>
    fun getActivityLogs(limit: Int, filters: Map<String, String>): Flow<List<ActivityLog>>
    suspend fun searchActivityLogs(query: String, filters: Map<String, String>): Result<List<ActivityLog>>
    
    // Data Export
    suspend fun exportData(request: ExportRequest): Result<String>
    suspend fun generatePdfReport(request: ExportRequest): Result<String>
    suspend fun generateCsvExport(request: ExportRequest): Result<String>
    
    // Push Notifications
    suspend fun sendPushNotification(notification: PushNotification): Result<Unit>
    suspend fun scheduleNotification(notification: PushNotification): Result<Unit>
    fun getNotificationHistory(limit: Int): Flow<List<PushNotification>>
    suspend fun getNotificationStats(notificationId: String): Result<NotificationStats>
    
    // Background Jobs
    suspend fun checkAndFlagOldItems(): Result<Int>
    suspend fun cleanupOldLogs(): Result<Unit>
}

data class UserAnalytics(
    val totalUsers: Int,
    val activeUsers: Int,
    val blockedUsers: Int,
    val usersByRole: Map<UserRole, Int>,
    val newUsersThisMonth: Int,
    val averageItemsPerUser: Float,
    val topContributors: List<AdminUser>
)

data class NotificationStats(
    val totalSent: Int,
    val delivered: Int,
    val opened: Int,
    val failed: Int,
    val openRate: Float
)
```

### 3. ViewModel Layer

#### Extended AdminDashboardViewModel

```kotlin
class AdminDashboardViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    
    // Existing LiveData...
    
    // Enhanced User Management
    private val _userDetails = MutableLiveData<AdminUser>()
    val userDetails: LiveData<AdminUser> = _userDetails
    
    private val _userAnalytics = MutableLiveData<UserAnalytics>()
    val userAnalytics: LiveData<UserAnalytics> = _userAnalytics
    
    // Enhanced Item Management
    private val _itemDetails = MutableLiveData<LostFoundItem>()
    val itemDetails: LiveData<LostFoundItem> = _itemDetails
    
    private val _allItemsWithStatus = MutableLiveData<List<LostFoundItem>>()
    val allItemsWithStatus: LiveData<List<LostFoundItem>> = _allItemsWithStatus
    
    // Donation Management
    private val _donationQueue = MutableLiveData<List<DonationItem>>()
    val donationQueue: LiveData<List<DonationItem>> = _donationQueue
    
    private val _donationStats = MutableLiveData<DonationStats>()
    val donationStats: LiveData<DonationStats> = _donationStats
    
    // Activity Logs
    private val _activityLogs = MutableLiveData<List<ActivityLog>>()
    val activityLogs: LiveData<List<ActivityLog>> = _activityLogs
    
    // Notifications
    private val _notificationHistory = MutableLiveData<List<PushNotification>>()
    val notificationHistory: LiveData<List<PushNotification>> = _notificationHistory
    
    // UI State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage
    
    // User Management Functions
    fun loadUserDetails(userId: String) { /* ... */ }
    fun blockUser(userId: String, reason: String) { /* ... */ }
    fun unblockUser(userId: String) { /* ... */ }
    fun updateUserRole(userId: String, role: UserRole) { /* ... */ }
    fun updateUserDetails(userId: String, updates: Map<String, Any>) { /* ... */ }
    fun loadUserAnalytics() { /* ... */ }
    fun searchUsers(query: String) { /* ... */ }
    
    // Item Management Functions
    fun loadItemDetails(itemId: String) { /* ... */ }
    fun updateItemDetails(itemId: String, updates: Map<String, Any>) { /* ... */ }
    fun updateItemStatus(itemId: String, newStatus: ItemStatus, reason: String) { /* ... */ }
    fun deleteItem(itemId: String) { /* ... */ }
    fun loadAllItemsWithStatus() { /* ... */ }
    fun searchItems(query: String, filters: Map<String, String>) { /* ... */ }
    
    // Donation Functions
    fun loadDonationQueue() { /* ... */ }
    fun markItemReadyForDonation(itemId: String) { /* ... */ }
    fun markItemAsDonated(itemId: String, recipient: String, value: Double) { /* ... */ }
    fun loadDonationStats(dateRange: DateRange) { /* ... */ }
    
    // Activity Log Functions
    fun loadActivityLogs(filters: Map<String, String>) { /* ... */ }
    fun searchActivityLogs(query: String, filters: Map<String, String>) { /* ... */ }
    
    // Notification Functions
    fun sendNotification(notification: PushNotification) { /* ... */ }
    fun loadNotificationHistory() { /* ... */ }
    
    // Export Functions
    fun exportData(request: ExportRequest) { /* ... */ }
}
```

### 4. UI Components

#### New Fragments

1. **AdminDonationsFragment**: Manage donation queue and history
2. **AdminActivityLogFragment**: View and filter activity logs
3. **AdminNotificationsFragment**: Send and manage notifications
4. **AdminExportFragment**: Configure and generate exports
5. **UserDetailsFragment**: Detailed user information and management
6. **ItemDetailsFragment**: Enhanced item details with full history

#### Enhanced Existing Fragments

1. **AdminUsersFragment**: Add search, filtering, and bulk operations
2. **AdminItemsFragment**: Add status filters and advanced search
3. **AdminAnalyticsFragment**: Add donation analytics and export options

#### New Dialogs

1. **BlockUserDialog**: Block user with reason input
2. **EditUserDialog**: Edit user details
3. **EditItemDialog**: Edit item details
4. **DonationConfirmDialog**: Confirm donation actions
5. **NotificationComposerDialog**: Compose custom notifications
6. **ExportConfigDialog**: Configure export parameters

#### New Adapters

1. **DonationQueueAdapter**: Display donation queue items
2. **ActivityLogAdapter**: Display activity logs with filtering
3. **NotificationHistoryAdapter**: Display notification history

### 5. Navigation Structure

```
Bottom Navigation:
├── Dashboard (existing)
├── Items (enhanced)
├── Users (enhanced)
├── Donations (new)
├── Analytics (enhanced)
├── Activity Log (new)
└── Notifications (new)

Fragment Navigation Flow:
Dashboard → Item Details → Edit Item
Dashboard → User Details → Edit User / Block User
Items → Item Details → Status History
Users → User Details → Activity History
Donations → Item Details → Mark Ready / Mark Donated
Activity Log → Detail View → Related Entity
Notifications → Compose → Send / Schedule
```

## Data Models

### Firestore Collections Structure

```
firestore/
├── users/
│   └── {userId}/
│       ├── uid: String
│       ├── email: String
│       ├── displayName: String
│       ├── role: String
│       ├── isBlocked: Boolean
│       ├── blockReason: String
│       ├── blockedBy: String
│       ├── blockedAt: Long
│       ├── createdAt: Long
│       ├── lastLoginAt: Long
│       ├── lastActivityAt: Long
│       ├── itemsReported: Int
│       ├── itemsFound: Int
│       └── itemsClaimed: Int
│
├── items/
│   └── {itemId}/
│       ├── id: String
│       ├── name: String
│       ├── description: String
│       ├── category: String
│       ├── location: String
│       ├── isLost: Boolean
│       ├── status: String
│       ├── userId: String
│       ├── userEmail: String
│       ├── timestamp: Timestamp
│       ├── requestedBy: String
│       ├── requestedAt: Long
│       ├── returnedAt: Long
│       ├── donationEligibleAt: Long
│       ├── donatedAt: Long
│       ├── lastModifiedBy: String
│       ├── lastModifiedAt: Long
│       └── statusHistory: Array<StatusChange>
│
├── donations/
│   └── {donationId}/
│       ├── itemId: String
│       ├── itemName: String
│       ├── category: String
│       ├── reportedAt: Long
│       ├── eligibleAt: Long
│       ├── status: String
│       ├── markedReadyBy: String
│       ├── markedReadyAt: Long
│       ├── donatedAt: Long
│       ├── donatedBy: String
│       ├── estimatedValue: Double
│       └── donationRecipient: String
│
├── activityLogs/
│   └── {logId}/
│       ├── timestamp: Long
│       ├── actorId: String
│       ├── actorEmail: String
│       ├── actorRole: String
│       ├── actionType: String
│       ├── targetType: String
│       ├── targetId: String
│       ├── description: String
│       ├── previousValue: String
│       ├── newValue: String
│       ├── ipAddress: String
│       ├── deviceInfo: String
│       └── metadata: Map<String, String>
│
├── notifications/
│   └── {notificationId}/
│       ├── title: String
│       ├── body: String
│       ├── type: String
│       ├── targetUsers: Array<String>
│       ├── targetRoles: Array<String>
│       ├── actionUrl: String
│       ├── createdBy: String
│       ├── createdAt: Long
│       ├── scheduledFor: Long
│       ├── sentAt: Long
│       ├── deliveryStatus: String
│       ├── deliveredCount: Int
│       ├── openedCount: Int
│       └── metadata: Map<String, String>
│
└── notificationHistory/
    └── {historyId}/
        ├── notificationId: String
        ├── userId: String
        ├── deliveredAt: Long
        ├── openedAt: Long
        ├── isOpened: Boolean
        └── deviceToken: String
```

### Firestore Indexes Required

```
Collection: items
- status ASC, timestamp DESC
- donationEligibleAt ASC, status ASC
- category ASC, status ASC

Collection: activityLogs
- timestamp DESC, actionType ASC
- actorId ASC, timestamp DESC
- targetType ASC, timestamp DESC

Collection: donations
- status ASC, eligibleAt DESC
- category ASC, status ASC

Collection: notifications
- createdAt DESC, deliveryStatus ASC
- type ASC, createdAt DESC
```

## Error Handling

### Error Handling Strategy

1. **Repository Layer**: Catch all exceptions and return Result<T> or emit error states in Flow
2. **ViewModel Layer**: Handle Result failures and update error LiveData
3. **UI Layer**: Observe error LiveData and display user-friendly messages

### Error Types and Handling

```kotlin
sealed class AdminError {
    data class NetworkError(val message: String) : AdminError()
    data class AuthenticationError(val message: String) : AdminError()
    data class PermissionError(val message: String) : AdminError()
    data class ValidationError(val field: String, val message: String) : AdminError()
    data class NotFoundError(val entity: String) : AdminError()
    data class FirestoreError(val message: String) : AdminError()
    data class ExportError(val message: String) : AdminError()
    data class NotificationError(val message: String) : AdminError()
    data class UnknownError(val message: String) : AdminError()
}

// Error handling in Repository
suspend fun blockUser(userId: String, reason: String): Result<Unit> {
    return try {
        if (!isAdminUser()) {
            return Result.failure(Exception("Permission denied"))
        }
        if (reason.isBlank()) {
            return Result.failure(Exception("Block reason is required"))
        }
        // Perform operation
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(mapOf(
                "isBlocked" to true,
                "blockReason" to reason,
                "blockedBy" to auth.currentUser?.uid,
                "blockedAt" to System.currentTimeMillis()
            ))
            .await()
        
        logActivity(/* ... */)
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Result.failure(Exception("Database error: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("Failed to block user: ${e.message}"))
    }
}
```

### Retry Logic

```kotlin
suspend fun <T> retryOperation(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    operation: suspend () -> T
): Result<T> {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return Result.success(operation())
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) {
                return Result.failure(e)
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return Result.failure(Exception("Max retries exceeded"))
}
```

## Testing Strategy

### Unit Testing

1. **Repository Tests**: Mock Firestore and test all repository methods
2. **ViewModel Tests**: Test LiveData updates and coroutine flows
3. **Model Tests**: Test data validation and transformations
4. **Utility Tests**: Test helper functions and extensions

### Integration Testing

1. **Firebase Integration**: Test Firestore operations with emulator
2. **FCM Integration**: Test notification delivery
3. **Export Generation**: Test PDF and CSV generation

### UI Testing

1. **Fragment Tests**: Test fragment lifecycle and UI interactions
2. **Navigation Tests**: Test navigation flows
3. **Adapter Tests**: Test RecyclerView adapters

### Test Coverage Goals

- Repository Layer: 80%+
- ViewModel Layer: 75%+
- UI Layer: 60%+

## Security Considerations

### Authentication and Authorization

1. **Admin Verification**: Check admin status on every sensitive operation
2. **Session Management**: Implement session timeout and re-authentication
3. **Role-Based Access**: Enforce role-based permissions at repository level

```kotlin
private fun requireAdminAccess() {
    if (!isAdminUser()) {
        throw SecurityException("Admin access required")
    }
}

suspend fun performSensitiveOperation() {
    requireAdminAccess()
    // Perform operation
}
```

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is admin
    function isAdmin() {
      return request.auth != null && 
             request.auth.token.email == 'admin@gmail.com';
    }
    
    // Users collection - admin only for modifications
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if isAdmin();
    }
    
    // Items collection - admin has full access
    match /items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if isAdmin() || 
                               resource.data.userId == request.auth.uid;
    }
    
    // Activity logs - admin read only
    match /activityLogs/{logId} {
      allow read: if isAdmin();
      allow write: if false; // Only server-side writes
    }
    
    // Donations - admin only
    match /donations/{donationId} {
      allow read, write: if isAdmin();
    }
    
    // Notifications - admin only
    match /notifications/{notificationId} {
      allow read, write: if isAdmin();
    }
    
    // Notification history - users can read their own
    match /notificationHistory/{historyId} {
      allow read: if request.auth != null && 
                     resource.data.userId == request.auth.uid;
      allow write: if isAdmin();
    }
  }
}
```

### Data Validation

1. **Input Sanitization**: Sanitize all user inputs before storage
2. **Field Validation**: Validate data types and constraints
3. **SQL Injection Prevention**: Use parameterized queries (Firestore handles this)
4. **XSS Prevention**: Escape HTML in user-generated content

### Sensitive Data Protection

1. **PII Handling**: Encrypt sensitive user data
2. **Export Security**: Secure export file storage and access
3. **Log Sanitization**: Remove sensitive data from logs
4. **Token Management**: Secure FCM token storage

## Performance Optimization

### Pagination Strategy

```kotlin
class PaginationHelper<T> {
    private var lastDocument: DocumentSnapshot? = null
    private val pageSize = 50
    
    suspend fun loadNextPage(
        query: Query,
        transform: (DocumentSnapshot) -> T
    ): Result<List<T>> {
        return try {
            val queryWithPagination = if (lastDocument != null) {
                query.startAfter(lastDocument!!).limit(pageSize.toLong())
            } else {
                query.limit(pageSize.toLong())
            }
            
            val snapshot = queryWithPagination.get().await()
            if (snapshot.documents.isNotEmpty()) {
                lastDocument = snapshot.documents.last()
            }
            
            val items = snapshot.documents.map { transform(it) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun reset() {
        lastDocument = null
    }
}
```

### Caching Strategy

```kotlin
class CacheManager {
    private val cache = mutableMapOf<String, CacheEntry<*>>()
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutes
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    )
    
    fun <T> get(key: String): T? {
        val entry = cache[key] as? CacheEntry<T> ?: return null
        return if (System.currentTimeMillis() - entry.timestamp < cacheTimeout) {
            entry.data
        } else {
            cache.remove(key)
            null
        }
    }
    
    fun <T> put(key: String, data: T) {
        cache[key] = CacheEntry(data, System.currentTimeMillis())
    }
    
    fun invalidate(key: String) {
        cache.remove(key)
    }
    
    fun clear() {
        cache.clear()
    }
}
```

### Firestore Query Optimization

1. **Use Indexes**: Create composite indexes for complex queries
2. **Limit Results**: Always use `.limit()` for large collections
3. **Selective Fields**: Use `.select()` to fetch only needed fields
4. **Batch Operations**: Use batch writes for multiple updates

```kotlin
// Optimized query with selective fields
suspend fun getUserSummaries(): Result<List<UserSummary>> {
    return try {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .select("uid", "email", "displayName", "role", "isBlocked")
            .limit(100)
            .get()
            .await()
        
        val summaries = snapshot.documents.map { doc ->
            UserSummary(
                uid = doc.getString("uid") ?: "",
                email = doc.getString("email") ?: "",
                displayName = doc.getString("displayName") ?: "",
                role = UserRole.valueOf(doc.getString("role") ?: "USER"),
                isBlocked = doc.getBoolean("isBlocked") ?: false
            )
        }
        Result.success(summaries)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Background Processing

```kotlin
class BackgroundJobManager(private val repository: AdminRepository) {
    
    fun scheduleOldItemCheck() {
        // Run daily at midnight
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "check_old_items",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<OldItemCheckWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
                    .build()
            )
    }
    
    class OldItemCheckWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {
        
        override suspend fun doWork(): Result {
            return try {
                val repository = AdminRepository()
                val flaggedCount = repository.checkAndFlagOldItems().getOrThrow()
                Log.d("BackgroundJob", "Flagged $flaggedCount items for donation")
                Result.success()
            } catch (e: Exception) {
                Log.e("BackgroundJob", "Failed to check old items", e)
                Result.retry()
            }
        }
    }
}
```

## Push Notification Implementation

### FCM Setup

1. **Add FCM Dependency**: Already included in Firebase BOM
2. **Service Implementation**: Create FirebaseMessagingService
3. **Token Management**: Store and update FCM tokens

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update token in Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("fcmToken", token)
        }
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Handle notification
        val notification = message.notification
        val data = message.data
        
        if (notification != null) {
            showNotification(
                title = notification.title ?: "",
                body = notification.body ?: "",
                actionUrl = data["actionUrl"] ?: ""
            )
        }
    }
    
    private fun showNotification(title: String, body: String, actionUrl: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("actionUrl", actionUrl)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
```

### Notification Channel Setup

```kotlin
fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channels = listOf(
            NotificationChannel(
                "item_matches",
                "Item Matches",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for matching lost/found items"
            },
            NotificationChannel(
                "request_updates",
                "Request Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Updates on item requests"
            },
            NotificationChannel(
                "admin_alerts",
                "Admin Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important admin notifications"
            },
            NotificationChannel(
                "system_announcements",
                "System Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General system announcements"
            }
        )
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }
}
```

### Server-Side Notification Sending

```kotlin
suspend fun sendPushNotification(notification: PushNotification): Result<Unit> {
    return try {
        // Get FCM tokens for target users
        val tokens = getTargetUserTokens(notification)
        
        if (tokens.isEmpty()) {
            return Result.failure(Exception("No valid FCM tokens found"))
        }
        
        // Prepare FCM message
        val message = mapOf(
            "notification" to mapOf(
                "title" to notification.title,
                "body" to notification.body
            ),
            "data" to mapOf(
                "type" to notification.type.name,
                "actionUrl" to notification.actionUrl,
                "notificationId" to notification.id
            ),
            "tokens" to tokens
        )
        
        // Send via Cloud Function or Admin SDK
        // Note: Actual sending requires backend implementation
        // This is a placeholder for the client-side preparation
        
        // Update notification status
        firestore.collection("notifications")
            .document(notification.id)
            .update(mapOf(
                "sentAt" to System.currentTimeMillis(),
                "deliveryStatus" to DeliveryStatus.SENT.name,
                "deliveredCount" to tokens.size
            ))
            .await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private suspend fun getTargetUserTokens(notification: PushNotification): List<String> {
    val tokens = mutableListOf<String>()
    
    if (notification.targetUsers.contains("ALL")) {
        // Get all user tokens
        val snapshot = firestore.collection(USERS_COLLECTION)
            .whereNotEqualTo("fcmToken", null)
            .get()
            .await()
        
        tokens.addAll(snapshot.documents.mapNotNull { it.getString("fcmToken") })
    } else {
        // Get specific user tokens
        for (userId in notification.targetUsers) {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            doc.getString("fcmToken")?.let { tokens.add(it) }
        }
    }
    
    // Filter by roles if specified
    if (notification.targetRoles.isNotEmpty()) {
        val roleSnapshot = firestore.collection(USERS_COLLECTION)
            .whereIn("role", notification.targetRoles.map { it.name })
            .whereNotEqualTo("fcmToken", null)
            .get()
            .await()
        
        tokens.addAll(roleSnapshot.documents.mapNotNull { it.getString("fcmToken") })
    }
    
    return tokens.distinct()
}
```

## Export Implementation

### PDF Generation

```kotlin
// Add dependency: implementation 'com.itextpdf:itext7-core:7.2.5'

class PdfExportGenerator(private val context: Context) {
    
    suspend fun generateItemsReport(
        items: List<LostFoundItem>,
        dateRange: DateRange
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "items_report_${System.currentTimeMillis()}.pdf"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                
                val writer = PdfWriter(file)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)
                
                // Add title
                document.add(
                    Paragraph("Lost & Found Items Report")
                        .setFontSize(20f)
                        .setBold()
                )
                
                // Add date range
                document.add(
                    Paragraph("Period: ${formatDate(dateRange.startDate)} - ${formatDate(dateRange.endDate)}")
                        .setFontSize(12f)
                )
                
                // Add summary statistics
                document.add(Paragraph("\nSummary:").setBold())
                document.add(Paragraph("Total Items: ${items.size}"))
                document.add(Paragraph("Lost Items: ${items.count { it.isLost }}"))
                document.add(Paragraph("Found Items: ${items.count { !it.isLost }}"))
                
                // Add items table
                document.add(Paragraph("\nItems Details:").setBold())
                val table = Table(5)
                table.addHeaderCell("Name")
                table.addHeaderCell("Category")
                table.addHeaderCell("Status")
                table.addHeaderCell("Location")
                table.addHeaderCell("Date")
                
                items.forEach { item ->
                    table.addCell(item.name)
                    table.addCell(item.category)
                    table.addCell(if (item.isLost) "Lost" else "Found")
                    table.addCell(item.location)
                    table.addCell(formatDate(item.timestamp.toDate().time))
                }
                
                document.add(table)
                document.close()
                
                Result.success(file.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

### CSV Generation

```kotlin
class CsvExportGenerator {
    
    suspend fun generateItemsCsv(items: List<LostFoundItem>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "items_export_${System.currentTimeMillis()}.csv"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                
                file.bufferedWriter().use { writer ->
                    // Write header
                    writer.write("ID,Name,Category,Status,Location,Reporter,Date,Description\n")
                    
                    // Write data
                    items.forEach { item ->
                        writer.write(
                            "${item.id}," +
                            "\"${escapeCsv(item.name)}\"," +
                            "\"${escapeCsv(item.category)}\"," +
                            "\"${if (item.isLost) "Lost" else "Found"}\"," +
                            "\"${escapeCsv(item.location)}\"," +
                            "\"${escapeCsv(item.userEmail)}\"," +
                            "\"${formatDate(item.timestamp.toDate().time)}\"," +
                            "\"${escapeCsv(item.description)}\"\n"
                        )
                    }
                }
                
                Result.success(file.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
```

## Donation Workflow Implementation

### Auto-Flagging Old Items

```kotlin
suspend fun checkAndFlagOldItems(): Result<Int> {
    return try {
        val oneYearAgo = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)
        
        // Query items older than 1 year that are still active
        val snapshot = firestore.collection(ITEMS_COLLECTION)
            .whereEqualTo("status", ItemStatus.ACTIVE.name)
            .whereLessThan("timestamp", Timestamp(Date(oneYearAgo)))
            .get()
            .await()
        
        val batch = firestore.batch()
        var count = 0
        
        snapshot.documents.forEach { doc ->
            val itemRef = firestore.collection(ITEMS_COLLECTION).document(doc.id)
            batch.update(itemRef, mapOf(
                "status" to ItemStatus.DONATION_PENDING.name,
                "donationEligibleAt" to System.currentTimeMillis()
            ))
            
            // Create donation record
            val donationRef = firestore.collection("donations").document()
            batch.set(donationRef, DonationItem(
                itemId = doc.id,
                itemName = doc.getString("name") ?: "",
                category = doc.getString("category") ?: "",
                reportedAt = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0,
                eligibleAt = System.currentTimeMillis(),
                status = DonationStatus.PENDING
            ))
            
            count++
        }
        
        if (count > 0) {
            batch.commit().await()
            
            // Log activity
            logActivity(ActivityLog(
                actorId = "SYSTEM",
                actorEmail = "system@lostandfound.com",
                actorRole = UserRole.ADMIN,
                actionType = ActionType.AUTO_DONATION_FLAG,
                targetType = TargetType.ITEM,
                description = "Automatically flagged $count items for donation (1 year old)"
            ))
        }
        
        Result.success(count)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Required Android Permissions

### AndroidManifest.xml

```xml
<manifest>
    <!-- Existing permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- New permissions for admin module -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                     android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
                     android:maxSdkVersion="32" />
    
    <application>
        <!-- FCM Service -->
        <service
            android:name=".services.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        
        <!-- Background Worker -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup" />
        </provider>
    </application>
</manifest>
```

## Additional Dependencies

### build.gradle.kts additions

```kotlin
dependencies {
    // Existing dependencies...
    
    // PDF Generation
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // CSV/Excel Export
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    
    // Background Jobs
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // FCM (already in Firebase BOM)
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Coroutines (already included)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

## Implementation Phases

### Phase 1: Enhanced User Management (Week 1-2)
- Extend AdminUser model
- Implement block/unblock with reasons
- Add user details view
- Implement user editing
- Add user analytics

### Phase 2: Enhanced Item Management (Week 2-3)
- Extend LostFoundItem model with status
- Implement status history tracking
- Add comprehensive item details view
- Implement item editing
- Add advanced search and filters

### Phase 3: Donation Management (Week 3-4)
- Create donation models and collection
- Implement auto-flagging background job
- Create donation queue UI
- Implement donation workflow
- Add donation analytics

### Phase 4: Activity Logging (Week 4-5)
- Create activity log models
- Implement comprehensive logging
- Create activity log UI with filters
- Add search functionality
- Implement log archiving

### Phase 5: Push Notifications (Week 5-6)
- Set up FCM service
- Implement notification models
- Create notification composer UI
- Implement auto-notifications
- Add notification history and analytics

### Phase 6: Data Export (Week 6-7)
- Implement PDF generation
- Implement CSV export
- Create export configuration UI
- Add export history
- Implement file sharing

### Phase 7: Testing and Polish (Week 7-8)
- Unit testing
- Integration testing
- UI testing
- Performance optimization
- Bug fixes and polish

## Success Metrics

1. **Performance**: All list views load within 2 seconds
2. **Reliability**: 99% success rate for critical operations
3. **Usability**: Admin can complete common tasks in < 3 clicks
4. **Notification Delivery**: 95%+ delivery rate for push notifications
5. **Export Success**: 100% success rate for exports under 10,000 records
6. **Background Jobs**: 100% success rate for auto-flagging old items
