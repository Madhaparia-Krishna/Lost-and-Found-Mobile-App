package com.example.loginandregistration.admin.models

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
    val itemsClaimed: Int = 0
)

/**
 * User role enum for role-based access and notifications
 * Requirements: 6.2
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
         * Safely parse role from string with fallback to USER
         * Prevents crashes from unknown role values in database
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