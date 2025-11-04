package com.example.loginandregistration.admin.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AdminUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.USER,
    val isBlocked: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp? = null,
    val itemsReported: Int = 0,
    val itemsFound: Int = 0,
    val itemsClaimed: Int = 0
)

/**
 * User role enum for role-based access and notifications
 * Requirements: 6.2, 3.1, 3.2, 3.3, 3.4, 3.5
 * 
 * Firestore Deserialization Notes:
 * - Firestore automatically deserializes enum values by matching the string in the database
 *   to the enum constant name (case-sensitive)
 * - This enum includes SECURITY and STUDENT constants to support all user roles
 * - The fromString() method provides case-insensitive parsing with fallback to USER
 * - For automatic Firestore deserialization to work, database values should be stored
 *   in uppercase (SECURITY, STUDENT, etc.) which is done automatically via role.name
 * - If legacy data exists with mixed case (e.g., "Security", "Student"), it should be
 *   migrated to uppercase, or manual parsing with fromString() should be used
 */
enum class UserRole {
    USER, STUDENT, MODERATOR, SECURITY, ADMIN;
    
    fun getDisplayName(): String {
        return when (this) {
            USER -> "User"
            STUDENT -> "Student"
            MODERATOR -> "Moderator"
            SECURITY -> "Security"
            ADMIN -> "Admin"
        }
    }
    
    companion object {
        /**
         * Safely parse role from string with case-insensitive matching and fallback to USER
         * Prevents crashes from unknown role values in database
         * Handles "Security", "Student", "SECURITY", "STUDENT", etc.
         * Requirements: 3.1, 3.2, 3.3, 3.4
         */
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("UserRole", "Unknown role: $value, defaulting to USER")
                USER
            }
        }
    }
}