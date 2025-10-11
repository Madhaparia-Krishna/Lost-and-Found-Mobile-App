# Security Features Quick Start Guide

## Overview
This guide shows how to use the security features implemented in Task 16.

---

## 1. Admin Access Verification

### Using SecurityHelper

```kotlin
import com.example.loginandregistration.admin.utils.SecurityHelper

// Check if user is admin
if (SecurityHelper.isAdminUser()) {
    // User is admin
}

// Require admin access (throws SecurityException if not admin)
try {
    SecurityHelper.requireAdminAccess()
    // Proceed with admin operation
} catch (e: SecurityException) {
    // Handle unauthorized access
    Toast.makeText(context, "Admin access required", Toast.LENGTH_SHORT).show()
}

// Check session status
if (SecurityHelper.isSessionExpired()) {
    // Session expired, redirect to login
}

// Update activity timestamp (call on user interactions)
SecurityHelper.updateLastActivity()

// Get remaining session time
val remainingTime = SecurityHelper.getRemainingSessionTime()
val minutes = remainingTime / (60 * 1000)
```

### Session Management in Activity

```kotlin
class AdminDashboardActivity : AppCompatActivity() {
    
    override fun onResume() {
        super.onResume()
        
        // Check session on resume
        if (SecurityHelper.isSessionExpired()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            // Redirect to login
            finish()
        }
    }
    
    override fun onUserInteraction() {
        super.onUserInteraction()
        // Update last activity on any user interaction
        SecurityHelper.updateLastActivity()
    }
}
```

---

## 2. Permission Management

### Using PermissionManager

```kotlin
import com.example.loginandregistration.admin.utils.PermissionManager

class AdminNotificationsFragment : Fragment() {
    
    private lateinit var permissionManager: PermissionManager
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        permissionManager = PermissionManager(requireContext())
        
        // Check notification permission
        if (!permissionManager.hasNotificationPermission()) {
            requestNotificationPermission()
        }
    }
    
    private fun requestNotificationPermission() {
        permissionManager.requestNotificationPermission(
            requireActivity(),
            onGranted = {
                // Permission granted, proceed with notification
                sendNotification()
            },
            onDenied = {
                // Permission denied, show message
                Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        permissionManager.handlePermissionResult(
            requestCode,
            permissions,
            grantResults,
            onNotificationGranted = {
                sendNotification()
            },
            onNotificationDenied = {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
```

### Storage Permission for Exports

```kotlin
class AdminExportFragment : Fragment() {
    
    private lateinit var permissionManager: PermissionManager
    
    private fun exportData() {
        permissionManager = PermissionManager(requireContext())
        
        if (!permissionManager.hasStoragePermission()) {
            permissionManager.requestStoragePermission(
                requireActivity(),
                onGranted = {
                    performExport()
                },
                onDenied = {
                    Toast.makeText(context, "Storage permission required for export", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            performExport()
        }
    }
}
```

---

## 3. Data Validation

### Using DataValidator

```kotlin
import com.example.loginandregistration.admin.utils.DataValidator

// Validate and sanitize user input
fun blockUser(userId: String, reason: String) {
    // Validate user ID
    val userIdValidation = DataValidator.validateUserId(userId)
    if (!userIdValidation.isValid) {
        showError(userIdValidation.getErrorMessage())
        return
    }
    
    // Validate block reason
    val reasonValidation = DataValidator.validateBlockReason(reason)
    if (!reasonValidation.isValid) {
        showError(reasonValidation.getErrorMessage())
        return
    }
    
    // Sanitize input
    val sanitizedReason = DataValidator.sanitizeString(reason)
    
    // Proceed with blocking
    viewModel.blockUser(userId, sanitizedReason)
}
```

### Validating Form Input in Dialogs

```kotlin
class EditUserDialog : DialogFragment() {
    
    private fun validateAndSave() {
        val displayName = binding.displayNameInput.text.toString()
        val email = binding.emailInput.text.toString()
        
        // Validate display name
        val nameValidation = DataValidator.validateDisplayName(displayName)
        if (!nameValidation.isValid) {
            binding.displayNameInput.error = nameValidation.getErrorMessage()
            return
        }
        
        // Validate email
        val emailValidation = DataValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            binding.emailInput.error = emailValidation.getErrorMessage()
            return
        }
        
        // Sanitize inputs
        val sanitizedName = DataValidator.sanitizeString(displayName)
        val sanitizedEmail = DataValidator.sanitizeEmail(email)
        
        // Save updates
        val updates = mapOf(
            "displayName" to sanitizedName,
            "email" to sanitizedEmail
        )
        
        viewModel.updateUserDetails(userId, updates)
    }
}
```

### Composite Validation

```kotlin
// Validate multiple fields at once
fun validateItemUpdate(updates: Map<String, Any>): Boolean {
    val validation = DataValidator.validateItemUpdate(updates)
    
    if (!validation.isValid) {
        // Show all errors
        showError(validation.getErrorMessage())
        return false
    }
    
    return true
}

// Validate donation data
fun markAsDonated(itemId: String, recipient: String, value: Double) {
    val validation = DataValidator.validateDonationData(recipient, value)
    
    if (!validation.isValid) {
        showError(validation.getErrorMessage())
        return
    }
    
    val sanitizedRecipient = DataValidator.sanitizeString(recipient)
    viewModel.markItemAsDonated(itemId, sanitizedRecipient, value)
}
```

---

## 4. Firestore Security Rules

### Testing Security Rules

The security rules are automatically enforced by Firestore. To test:

1. **Test as regular user:**
```kotlin
// Try to access admin-only collection
firestore.collection("activityLogs")
    .get()
    .addOnFailureListener { e ->
        // Should fail with permission denied
        Log.e(TAG, "Expected permission denied: ${e.message}")
    }
```

2. **Test as admin:**
```kotlin
// Should succeed
firestore.collection("activityLogs")
    .get()
    .addOnSuccessListener { snapshot ->
        Log.d(TAG, "Admin access granted: ${snapshot.size()} logs")
    }
```

3. **Test item status restrictions:**
```kotlin
// Regular users shouldn't see donated items
firestore.collection("items")
    .whereEqualTo("status", "DONATED")
    .get()
    .addOnSuccessListener { snapshot ->
        // Should be empty for regular users
        Log.d(TAG, "Donated items visible: ${snapshot.size()}")
    }
```

---

## 5. Error Handling

### Handling Security Exceptions

```kotlin
viewModel.blockUser(userId, reason)
    .observe(viewLifecycleOwner) { result ->
        result.onSuccess {
            Toast.makeText(context, "User blocked successfully", Toast.LENGTH_SHORT).show()
        }.onFailure { error ->
            when (error) {
                is SecurityException -> {
                    // Session expired or not admin
                    Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
                    // Redirect to login
                }
                is IllegalArgumentException -> {
                    // Validation error
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Other errors
                    Toast.makeText(context, "Operation failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
```

---

## 6. Best Practices

### 1. Always Update Activity Timestamp
```kotlin
override fun onUserInteraction() {
    super.onUserInteraction()
    SecurityHelper.updateLastActivity()
}
```

### 2. Check Permissions Before Operations
```kotlin
private fun sendNotification() {
    if (!permissionManager.hasNotificationPermission()) {
        requestNotificationPermission()
        return
    }
    // Proceed with notification
}
```

### 3. Validate All User Input
```kotlin
private fun saveData(input: String) {
    val validation = DataValidator.validateItemName(input)
    if (!validation.isValid) {
        showError(validation.getErrorMessage())
        return
    }
    
    val sanitized = DataValidator.sanitizeString(input)
    // Use sanitized input
}
```

### 4. Handle Session Expiration Gracefully
```kotlin
private fun checkSession() {
    if (SecurityHelper.isSessionExpired()) {
        showSessionExpiredDialog()
        return
    }
    // Proceed with operation
}
```

### 5. Use Composite Validation for Complex Forms
```kotlin
private fun validateForm(): Boolean {
    val updates = mapOf(
        "name" to nameInput.text.toString(),
        "description" to descInput.text.toString(),
        "location" to locationInput.text.toString()
    )
    
    val validation = DataValidator.validateItemUpdate(updates)
    if (!validation.isValid) {
        showError(validation.getErrorMessage())
        return false
    }
    
    return true
}
```

---

## 7. Common Scenarios

### Scenario 1: Admin Login
```kotlin
fun onAdminLogin() {
    // Verify admin role
    lifecycleScope.launch {
        SecurityHelper.verifyAdminRole().onSuccess { isAdmin ->
            if (isAdmin) {
                SecurityHelper.updateLastActivity()
                navigateToAdminDashboard()
            } else {
                showError("Not authorized as admin")
            }
        }
    }
}
```

### Scenario 2: Sending Notification
```kotlin
fun sendNotification(title: String, body: String) {
    // Validate notification data
    val validation = DataValidator.validateNotificationData(title, body)
    if (!validation.isValid) {
        showError(validation.getErrorMessage())
        return
    }
    
    // Check permission
    if (!permissionManager.hasNotificationPermission()) {
        requestNotificationPermission()
        return
    }
    
    // Sanitize and send
    val sanitizedTitle = DataValidator.sanitizeString(title)
    val sanitizedBody = DataValidator.sanitizeString(body)
    
    viewModel.sendNotification(sanitizedTitle, sanitizedBody)
}
```

### Scenario 3: Exporting Data
```kotlin
fun exportData() {
    // Check admin access
    try {
        SecurityHelper.requireAdminAccess()
    } catch (e: SecurityException) {
        showError("Admin access required")
        return
    }
    
    // Check storage permission
    if (!permissionManager.hasStoragePermission()) {
        permissionManager.requestStoragePermission(
            requireActivity(),
            onGranted = { performExport() },
            onDenied = { showError("Storage permission required") }
        )
        return
    }
    
    performExport()
}
```

---

## 8. Testing Checklist

- [ ] Test admin access verification with non-admin user
- [ ] Test session timeout after 30 minutes
- [ ] Test notification permission on Android 13+
- [ ] Test storage permission on different Android versions
- [ ] Test input validation with special characters
- [ ] Test Firestore security rules
- [ ] Test permission denial scenarios
- [ ] Test session expiration handling
- [ ] Test validation error messages
- [ ] Test sanitization of user input

---

## Support

For issues or questions about security features:
1. Check the implementation summary: `SECURITY_IMPLEMENTATION_SUMMARY.md`
2. Review the source code in `admin/utils/` directory
3. Check Firestore security rules in `firestore.rules`
