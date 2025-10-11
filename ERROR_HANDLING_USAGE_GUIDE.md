# Error Handling and User Feedback - Usage Guide

This guide explains how to use the comprehensive error handling and user feedback system implemented for the admin module.

## Overview

The error handling system consists of:
1. **AdminError** - Sealed class for type-safe error handling
2. **RetryHelper** - Automatic retry logic with exponential backoff
3. **ErrorHandler** - Centralized error handling utilities
4. **ErrorMessageMapper** - User-friendly error messages
5. **SnackbarHelper** - Display errors and messages to users
6. **LoadingStateManager** - Manage loading states
7. **SuccessFeedbackHelper** - Show success messages
8. **ConfirmationDialogHelper** - Confirmation dialogs for destructive actions
9. **OperationNotificationHelper** - System notifications for operations

## 1. Error Handling in Repository Methods

### Basic Error Handling

```kotlin
suspend fun getUserDetails(userId: String): Result<EnhancedAdminUser> {
    return executeRepositoryOperation("getUserDetails") {
        requireAdminAccess()
        
        val document = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
        
        document.toObject(EnhancedAdminUser::class.java)
            ?: throw NoSuchElementException("User not found")
    }
}
```

### Error Handling with Retry

```kotlin
suspend fun updateUserDetails(userId: String, updates: Map<String, Any>): Result<Unit> {
    return executeRepositoryOperationWithRetry("updateUserDetails", maxRetries = 3) {
        requireAdminAccess()
        
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(updates)
            .await()
    }
}
```

### Firestore-Specific Error Handling

```kotlin
suspend fun getItems(): Result<List<Item>> {
    return executeFirestoreOperation("getItems") {
        firestore.collection(ITEMS_COLLECTION)
            .get()
            .await()
            .toObjects(Item::class.java)
    }
}
```

## 2. Error Handling in ViewModels

### Using LoadingStateManager

```kotlin
class AdminDashboardViewModel : ViewModel() {
    private val loadingStateManager = LoadingStateManager()
    val isLoading: LiveData<Boolean> = loadingStateManager.isLoading
    val loadingMessage: LiveData<String> = loadingStateManager.loadingMessage
    
    private val _error = MutableLiveData<AdminError?>()
    val error: LiveData<AdminError?> = _error
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    fun blockUser(userId: String, reason: String) {
        viewModelScope.launch {
            loadingStateManager.startLoading("Blocking user...")
            
            repository.blockUser(userId, reason).fold(
                onSuccess = {
                    _successMessage.value = SuccessFeedbackHelper.Messages.USER_BLOCKED
                },
                onFailure = { exception ->
                    _error.value = exception.toAdminError()
                }
            )
            
            loadingStateManager.stopLoading()
        }
    }
}
```

### Using LoadingState Sealed Class

```kotlin
class AdminDashboardViewModel : ViewModel() {
    private val _userListState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val userListState: LiveData<LoadingState> = _userListState
    
    fun loadUsers() {
        viewModelScope.launch {
            _userListState.value = LoadingState.Loading("Loading users...")
            
            repository.getAllUsers().fold(
                onSuccess = { users ->
                    _userListState.value = LoadingState.Success(users)
                },
                onFailure = { exception ->
                    val error = exception.toAdminError()
                    _userListState.value = LoadingState.Error(error.toUserMessage(), exception)
                }
            )
        }
    }
}
```

## 3. Displaying Errors in Fragments

### Using SnackbarHelper

```kotlin
class AdminUsersFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                view.showError(it) {
                    // Retry action
                    viewModel.retryLastOperation()
                }
            }
        }
        
        // Observe success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                view.showSuccess(it)
            }
        }
    }
}
```

### Using Extension Functions

```kotlin
// Show error
binding.root.showError("Failed to load users", "Retry") {
    viewModel.loadUsers()
}

// Show success
binding.root.showSuccess("User blocked successfully")

// Show warning
binding.root.showWarning("This action cannot be undone", "Continue") {
    performAction()
}
```

## 4. Loading States

### Show/Hide Progress Bar

```kotlin
viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
    binding.progressBar.setLoadingState(isLoading)
    LoadingIndicatorHelper.setViewsEnabled(!isLoading, 
        binding.btnSave, 
        binding.btnCancel
    )
}
```

### Skeleton Screens for Lists

```kotlin
viewModel.userListState.observe(viewLifecycleOwner) { state ->
    when (state) {
        is LoadingState.Loading -> {
            SkeletonScreenHelper.showSkeleton(
                binding.recyclerView,
                binding.skeletonLayout
            )
        }
        is LoadingState.Success<*> -> {
            SkeletonScreenHelper.hideSkeleton(
                binding.recyclerView,
                binding.skeletonLayout
            )
            val users = state.data as List<AdminUser>
            adapter.submitList(users)
        }
        is LoadingState.Error -> {
            SkeletonScreenHelper.hideSkeleton(
                binding.recyclerView,
                binding.skeletonLayout
            )
            binding.root.showError(state.message)
        }
        is LoadingState.Idle -> {
            // Do nothing
        }
    }
}
```

### Progress Dialog for Long Operations

```kotlin
lifecycleScope.launch {
    val progressDialog = requireContext().createProgressDialog()
    progressDialog.show("Exporting data...", cancelable = false)
    
    try {
        val result = viewModel.exportData(exportRequest)
        progressDialog.dismiss()
        
        result.fold(
            onSuccess = { fileName ->
                binding.root.showSuccess("Export completed: $fileName")
            },
            onFailure = { exception ->
                binding.root.showError(exception.toAdminError())
            }
        )
    } catch (e: Exception) {
        progressDialog.dismiss()
        binding.root.showError(e.toAdminError())
    }
}
```

## 5. Confirmation Dialogs

### Delete Confirmation

```kotlin
binding.btnDelete.setOnClickListener {
    requireContext().showDeleteConfirmation("user") {
        viewModel.deleteUser(userId)
    }
}
```

### Block User Confirmation

```kotlin
binding.btnBlock.setOnClickListener {
    requireContext().showBlockUserConfirmation(userName) {
        viewModel.blockUser(userId, reason)
    }
}
```

### Custom Confirmation

```kotlin
ConfirmationDialogHelper.showConfirmation(
    context = requireContext(),
    title = "Change Status?",
    message = "Are you sure you want to mark this item as donated?",
    positiveButtonText = "Mark as Donated",
    negativeButtonText = "Cancel",
    onConfirm = {
        viewModel.markAsDonated(itemId)
    }
)
```

## 6. Success Feedback

### Success Snackbar

```kotlin
viewModel.successMessage.observe(viewLifecycleOwner) { message ->
    message?.let {
        binding.root.showSuccessFeedback(it)
    }
}
```

### Operation-Specific Success Messages

```kotlin
// After blocking user
binding.root.showSuccessFeedback(SuccessFeedbackHelper.Messages.USER_BLOCKED)

// After updating item
binding.root.showUpdateSuccess("Item")

// After deleting
binding.root.showDeleteSuccess("User")
```

### System Notifications

```kotlin
// Initialize notification channel (in Application class or MainActivity)
OperationNotificationHelper.createNotificationChannel(this)

// Show export complete notification
requireContext().showExportCompleteNotification("users_export.pdf")

// Show custom success notification
requireContext().showOperationSuccessNotification(
    "Backup Complete",
    "Database backup completed successfully"
)
```

## 7. Complete Example: Block User Flow

```kotlin
class AdminUsersFragment : Fragment() {
    
    private fun blockUser(user: AdminUser) {
        // Step 1: Show confirmation dialog
        requireContext().showBlockUserConfirmation(user.displayName) {
            // Step 2: Show input dialog for reason
            showBlockReasonDialog(user)
        }
    }
    
    private fun showBlockReasonDialog(user: AdminUser) {
        val input = EditText(requireContext())
        
        AlertDialog.Builder(requireContext())
            .setTitle("Block Reason")
            .setMessage("Please provide a reason for blocking this user:")
            .setView(input)
            .setPositiveButton("Block") { _, _ ->
                val reason = input.text.toString()
                if (reason.isBlank()) {
                    binding.root.showError("Please provide a reason")
                    return@setPositiveButton
                }
                performBlockUser(user.uid, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performBlockUser(userId: String, reason: String) {
        lifecycleScope.launch {
            // Show loading
            val progressDialog = requireContext().createProgressDialog()
            progressDialog.show("Blocking user...", cancelable = false)
            
            // Perform operation
            val result = viewModel.blockUser(userId, reason)
            
            // Hide loading
            progressDialog.dismiss()
            
            // Handle result
            result.fold(
                onSuccess = {
                    binding.root.showSuccessFeedback(
                        SuccessFeedbackHelper.Messages.USER_BLOCKED
                    )
                    viewModel.refreshUsers()
                },
                onFailure = { exception ->
                    binding.root.showError(exception.toAdminError()) {
                        // Retry
                        performBlockUser(userId, reason)
                    }
                }
            )
        }
    }
}
```

## 8. Error Types and When to Use Them

### NetworkError
- Connection timeouts
- No internet connection
- Server unavailable

### AuthenticationError
- Session expired
- Not logged in
- Invalid credentials

### PermissionError
- Not admin
- Insufficient permissions
- Access denied

### ValidationError
- Invalid input
- Missing required fields
- Format errors

### NotFoundError
- User not found
- Item not found
- Document doesn't exist

### FirestoreError
- Database errors
- Query failures
- Write conflicts

### ExportError
- PDF generation failed
- CSV export failed
- Storage issues

### NotificationError
- FCM delivery failed
- Invalid token
- Quota exceeded

### StorageError
- File read/write errors
- Permission denied
- Insufficient space

## 9. Best Practices

1. **Always wrap repository operations** with error handling
2. **Use retry logic** for network and Firestore operations
3. **Show loading indicators** for all async operations
4. **Provide user-friendly error messages** using ErrorMessageMapper
5. **Confirm destructive actions** with dialogs
6. **Show success feedback** after operations complete
7. **Log errors** for debugging while showing user-friendly messages
8. **Handle edge cases** like empty states and network errors
9. **Use appropriate error types** for better error handling
10. **Test error scenarios** to ensure proper handling

## 10. Testing Error Handling

```kotlin
@Test
fun `test error handling in repository`() = runTest {
    // Arrange
    val repository = AdminRepository()
    val invalidUserId = "invalid_id"
    
    // Act
    val result = repository.getUserDetails(invalidUserId)
    
    // Assert
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    
    val adminError = exception!!.toAdminError()
    assertTrue(adminError is AdminError.NotFoundError)
}
```

## Summary

This comprehensive error handling system provides:
- ✅ Type-safe error handling with AdminError sealed class
- ✅ Automatic retry logic with exponential backoff
- ✅ User-friendly error messages
- ✅ Loading states and indicators
- ✅ Success feedback mechanisms
- ✅ Confirmation dialogs for destructive actions
- ✅ System notifications for background operations
- ✅ Consistent error handling across the app

All components work together to provide a robust and user-friendly error handling experience.
