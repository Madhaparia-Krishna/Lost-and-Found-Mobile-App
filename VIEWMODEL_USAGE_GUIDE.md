# AdminDashboardViewModel Usage Guide

## Overview

The `AdminDashboardViewModel` has been extended with comprehensive functionality for managing users, items, donations, activity logs, notifications, and data exports. This guide shows how to use the new features in your UI components.

---

## 1. User Management

### Observing User Data

```kotlin
// In your Fragment or Activity
viewModel.userDetails.observe(viewLifecycleOwner) { user ->
    // Update UI with user details
    binding.userEmail.text = user.email
    binding.userRole.text = user.role.name
    binding.blockStatus.text = if (user.isBlocked) "Blocked" else "Active"
}

viewModel.userAnalytics.observe(viewLifecycleOwner) { analytics ->
    // Display analytics
    binding.totalUsers.text = analytics.totalUsers.toString()
    binding.activeUsers.text = analytics.activeUsers.toString()
}

viewModel.filteredUsers.observe(viewLifecycleOwner) { users ->
    // Update RecyclerView adapter
    userAdapter.submitList(users)
}
```

### User Management Actions

```kotlin
// Load user details
viewModel.loadUserDetails(userId)

// Block a user
viewModel.blockUser(userId, "Violation of terms")

// Unblock a user
viewModel.unblockUser(userId)

// Update user role
viewModel.updateUserRoleEnhanced(userId, UserRole.MODERATOR)

// Update user details
val updates = mapOf(
    "displayName" to "New Name",
    "photoUrl" to "https://example.com/photo.jpg"
)
viewModel.updateUserDetailsEnhanced(userId, updates)

// Search users
viewModel.searchUsersEnhanced("john@example.com")

// Load analytics
viewModel.loadUserAnalytics()
```

---

## 2. Item Management

### Observing Item Data

```kotlin
viewModel.itemDetails.observe(viewLifecycleOwner) { item ->
    // Display item details
    binding.itemName.text = item.name
    binding.itemStatus.text = item.status.name
    binding.statusHistory.adapter = StatusHistoryAdapter(item.statusHistory)
}

viewModel.allItemsWithStatus.observe(viewLifecycleOwner) { items ->
    // Update items list
    itemAdapter.submitList(items)
}
```

### Item Management Actions

```kotlin
// Load item details
viewModel.loadItemDetails(itemId)

// Update item details
val updates = mapOf(
    "name" to "Updated Name",
    "description" to "Updated description",
    "location" to "New Location"
)
viewModel.updateItemDetailsEnhanced(itemId, updates)

// Update item status
viewModel.updateItemStatusEnhanced(itemId, ItemStatus.RETURNED, "Item claimed by owner")

// Delete item
viewModel.deleteItem(itemId)

// Search items with filters
val filters = mapOf(
    "category" to "Electronics",
    "status" to "ACTIVE"
)
viewModel.searchItemsEnhanced("iPhone", filters)

// Load all items with status
viewModel.loadAllItemsWithStatus()
```

---

## 3. Donation Management

### Observing Donation Data

```kotlin
viewModel.donationQueue.observe(viewLifecycleOwner) { donations ->
    // Update donation queue
    donationAdapter.submitList(donations)
}

viewModel.donationStats.observe(viewLifecycleOwner) { stats ->
    // Display statistics
    binding.totalDonated.text = stats.totalDonated.toString()
    binding.totalValue.text = "$${stats.totalValue}"
}
```

### Donation Actions

```kotlin
// Load donation queue
viewModel.loadDonationQueue()

// Mark item ready for donation
viewModel.markItemReadyForDonation(itemId)

// Mark item as donated
viewModel.markItemAsDonated(itemId, "Campus Charity", 50.0)

// Load donation statistics
val dateRange = DateRange(
    startDate = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000), // 30 days ago
    endDate = System.currentTimeMillis()
)
viewModel.loadDonationStats(dateRange)
```

---

## 4. Activity Log Management

### Observing Activity Logs

```kotlin
viewModel.activityLogs.observe(viewLifecycleOwner) { logs ->
    // Update activity log list
    activityLogAdapter.submitList(logs)
}

viewModel.activityLogFilters.observe(viewLifecycleOwner) { filters ->
    // Update filter UI
    updateFilterChips(filters)
}
```

### Activity Log Actions

```kotlin
// Load activity logs
viewModel.loadActivityLogs(limit = 100)

// Load with filters
val filters = mapOf(
    "actionType" to "USER_BLOCK",
    "startDate" to "2024-01-01",
    "endDate" to "2024-12-31"
)
viewModel.loadActivityLogs(50, filters)

// Search activity logs
viewModel.searchActivityLogs("admin@gmail.com", filters)

// Update filters
viewModel.updateActivityLogFilters(filters)

// Clear filters
viewModel.clearActivityLogFilters()
```

---

## 5. Notification Management

### Observing Notifications

```kotlin
viewModel.notificationHistory.observe(viewLifecycleOwner) { notifications ->
    // Update notification history
    notificationAdapter.submitList(notifications)
}

viewModel.notificationStats.observe(viewLifecycleOwner) { stats ->
    // Display statistics
    binding.deliveryRate.text = "${stats.openRate}%"
    binding.totalSent.text = stats.totalSent.toString()
}
```

### Notification Actions

```kotlin
// Send notification
val notification = PushNotification(
    title = "New Item Match",
    body = "We found an item matching your description",
    type = NotificationType.ITEM_MATCH,
    targetUsers = listOf("user123"),
    actionUrl = "app://item/details/item123"
)
viewModel.sendNotification(notification)

// Schedule notification
val scheduledNotification = notification.copy(
    scheduledFor = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
)
viewModel.scheduleNotification(scheduledNotification)

// Load notification history
viewModel.loadNotificationHistory(limit = 50)

// Load notification stats
viewModel.loadNotificationStats(notificationId)
```

---

## 6. Data Export

### Observing Export Progress

```kotlin
viewModel.exportProgress.observe(viewLifecycleOwner) { progress ->
    // Update progress bar
    binding.progressBar.progress = progress
    binding.progressText.text = "$progress%"
}

viewModel.exportResult.observe(viewLifecycleOwner) { fileUrl ->
    if (fileUrl.isNotEmpty()) {
        // Export completed, show share dialog
        shareFile(fileUrl)
    }
}

viewModel.exportHistory.observe(viewLifecycleOwner) { history ->
    // Display export history
    exportHistoryAdapter.submitList(history)
}
```

### Export Actions

```kotlin
// Export data
val exportRequest = ExportRequest(
    format = ExportFormat.PDF,
    dataType = ExportDataType.ITEMS,
    dateRange = DateRange(startDate, endDate),
    filters = mapOf("status" to "ACTIVE")
)
viewModel.exportData(exportRequest)

// Generate PDF report
viewModel.generatePdfReport(exportRequest)

// Generate CSV export
val csvRequest = exportRequest.copy(format = ExportFormat.CSV)
viewModel.generateCsvExport(csvRequest)

// Load export history
viewModel.loadExportHistory()

// Clear export result
viewModel.clearExportResult()
```

---

## 7. Error Handling and Feedback

### Observing Status Messages

```kotlin
// Error messages
viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
    if (errorMessage.isNotEmpty()) {
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
            .setAction("Retry") { /* retry action */ }
            .show()
    }
}

// Success messages
viewModel.successMessage.observe(viewLifecycleOwner) { successMessage ->
    if (successMessage.isNotEmpty()) {
        Snackbar.make(binding.root, successMessage, Snackbar.LENGTH_SHORT).show()
    }
}

// Loading state
viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    binding.contentLayout.alpha = if (isLoading) 0.5f else 1.0f
}
```

---

## 8. Complete Example: User Details Fragment

```kotlin
class UserDetailsFragment : Fragment() {
    
    private lateinit var binding: FragmentUserDetailsBinding
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    private lateinit var userId: String
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userId = arguments?.getString("userId") ?: return
        
        setupObservers()
        setupClickListeners()
        
        // Load user details
        viewModel.loadUserDetails(userId)
    }
    
    private fun setupObservers() {
        // User details
        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            binding.apply {
                userEmail.text = user.email
                userName.text = user.displayName
                userRole.text = user.role.name
                blockStatus.text = if (user.isBlocked) "Blocked" else "Active"
                itemsReported.text = user.itemsReported.toString()
                itemsFound.text = user.itemsFound.toString()
                
                // Update button states
                blockButton.text = if (user.isBlocked) "Unblock" else "Block"
            }
        }
        
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }
        
        // Error messages
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.blockButton.setOnClickListener {
            val user = viewModel.userDetails.value ?: return@setOnClickListener
            
            if (user.isBlocked) {
                // Unblock user
                viewModel.unblockUser(userId)
            } else {
                // Show block dialog
                showBlockDialog()
            }
        }
        
        binding.editButton.setOnClickListener {
            showEditDialog()
        }
        
        binding.changeRoleButton.setOnClickListener {
            showRoleDialog()
        }
    }
    
    private fun showBlockDialog() {
        val dialog = BlockUserDialog { reason ->
            viewModel.blockUser(userId, reason)
        }
        dialog.show(parentFragmentManager, "block_user")
    }
    
    private fun showEditDialog() {
        val user = viewModel.userDetails.value ?: return
        val dialog = EditUserDialog(user) { updates ->
            viewModel.updateUserDetailsEnhanced(userId, updates)
        }
        dialog.show(parentFragmentManager, "edit_user")
    }
    
    private fun showRoleDialog() {
        val dialog = RoleChangeDialog { newRole ->
            viewModel.updateUserRoleEnhanced(userId, newRole)
        }
        dialog.show(parentFragmentManager, "change_role")
    }
}
```

---

## Best Practices

1. **Always observe LiveData in lifecycle-aware manner** using `viewLifecycleOwner`
2. **Handle loading states** to provide user feedback
3. **Display error and success messages** appropriately
4. **Refresh data after mutations** (the ViewModel handles this automatically)
5. **Use proper coroutine scopes** (the ViewModel uses `viewModelScope`)
6. **Clear observers** when appropriate (handled automatically by lifecycle)

---

## Notes

- All methods are asynchronous and use Kotlin coroutines
- LiveData updates are posted on the main thread automatically
- The ViewModel survives configuration changes
- Repository methods are called from the ViewModel, not directly from UI
- Error handling is centralized in the ViewModel
