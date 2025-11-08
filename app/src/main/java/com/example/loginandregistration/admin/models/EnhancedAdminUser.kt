package com.example.loginandregistration.admin.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Enhanced AdminUser model with additional fields for comprehensive user management
 * Requirements: 1.1, 1.3, 1.4
 */
@IgnoreExtraProperties
data class EnhancedAdminUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.STUDENT,
    val isBlocked: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp? = null,
    val itemsReported: Int = 0,
    val itemsFound: Int = 0,
    val itemsClaimed: Int = 0,
    // New fields for enhanced management
    val blockReason: String = "",
    val blockedBy: String = "",
    val blockedAt: Timestamp? = null,
    val deviceInfo: String = "",
    val lastActivityAt: Timestamp? = null,
    val fcmToken: String = ""
) {
    /**
     * Validates if the user data is complete and valid
     */
    fun isValid(): Boolean {
        return uid.isNotBlank() && email.isNotBlank()
    }

    /**
     * Checks if the user account is active (not blocked)
     */
    fun isActive(): Boolean {
        return !isBlocked
    }

    /**
     * Converts to map for Firestore updates
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "role" to role.name,
            "isBlocked" to isBlocked,
            "createdAt" to createdAt,
            "lastLoginAt" to lastLoginAt,
            "itemsReported" to itemsReported,
            "itemsFound" to itemsFound,
            "itemsClaimed" to itemsClaimed,
            "blockReason" to blockReason,
            "blockedBy" to blockedBy,
            "blockedAt" to blockedAt,
            "deviceInfo" to deviceInfo,
            "lastActivityAt" to lastActivityAt,
            "fcmToken" to fcmToken
        )
    }
}

/**
 * User analytics data model
 * Requirements: 1.7
 */
data class UserAnalytics(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val blockedUsers: Int = 0,
    val usersByRole: Map<UserRole, Int> = emptyMap(),
    val newUsersThisMonth: Int = 0,
    val averageItemsPerUser: Float = 0f,
    val topContributors: List<EnhancedAdminUser> = emptyList()
)
