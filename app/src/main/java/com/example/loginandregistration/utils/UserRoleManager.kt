package com.example.loginandregistration.utils

/**
 * Utility object for managing user roles and permissions based on email addresses.
 * Provides role-checking methods for admin, security, and sensitive information access.
 */
object UserRoleManager {
    
    /**
     * Checks if the user is an admin based on their email address.
     * Admin email: admin@gmail.com
     * 
     * @param email The user's email address
     * @return true if the user is an admin, false otherwise
     */
    fun isAdmin(email: String): Boolean {
        return email.equals("admin@gmail.com", ignoreCase = true)
    }
    
    /**
     * Checks if the user has security role privileges.
     * Security users are identified by having "security" in their email domain or username.
     * Admin users also have security privileges.
     * 
     * @param email The user's email address
     * @return true if the user is a security officer or admin, false otherwise
     */
    fun isSecurity(email: String): Boolean {
        return email.contains("security", ignoreCase = true) || isAdmin(email)
    }
    
    /**
     * Checks if the user can view sensitive information (date, location, contact info).
     * Only admin and security users can view sensitive information.
     * 
     * @param email The user's email address
     * @return true if the user can view sensitive information, false otherwise
     */
    fun canViewSensitiveInfo(email: String): Boolean {
        return isAdmin(email) || isSecurity(email)
    }
}
