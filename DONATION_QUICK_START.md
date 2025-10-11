# Donation Management System - Quick Start Guide

## Setup

### 1. Initialize Auto-Flagging (One-time setup)
Add this to your Application class or AdminDashboardActivity's onCreate:

```kotlin
import com.example.loginandregistration.admin.utils.DonationWorkScheduler

class LostFoundApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule daily auto-flagging at midnight
        DonationWorkScheduler.scheduleDailyAutoFlag(this)
    }
}
```

## Basic Usage Examples

### Get Donation Queue
```kotlin
// In your ViewModel
viewModelScope.launch {
    repository.getDonationQueue().collect { donations ->
        _donationQueue.value = donations
    }
}
```

### Filter Donation Queue
```kotlin
// Filter by status
val filter = DonationFilter.pendingOnly()

// Filter by multiple criteria
val filter = DonationFilter(
    status = DonationStatus.PENDING,
    category = "Electronics",
    minAge = 365, // Items older than 1 year
    location = "Library"
)

// Apply filter
repository.getFilteredDonationQueue(filter).collect { donations ->
    _filteredDonations.value = donations
}
```

### Mark Item Ready for Donation
```kotlin
viewModelScope.launch {
    repository.markItemReadyForDonation(itemId).fold(
        onSuccess = {
            _successMessage.value = "Item marked as ready for donation"
        },
        onFailure = { error ->
            _errorMessage.value = "Failed: ${error.message}"
        }
    )
}
```

### Complete Donation
```kotlin
viewModelScope.launch {
    repository.markItemAsDonated(
        itemId = itemId,
        recipient = "Campus Food Bank",
        value = 75.50
    ).fold(
        onSuccess = {
            _successMessage.value = "Donation completed successfully"
        },
        onFailure = { error ->
            _errorMessage.value = "Failed: ${error.message}"
        }
    )
}
```

### Get Donation Statistics
```kotlin
// Get stats for last 30 days
val dateRange = DateRange.lastNDays(30)

repository.getDonationStats(dateRange).collect { stats ->
    println("Total donated: ${stats.totalDonated}")
    println("Total value: $${stats.totalValue}")
    println("Pending: ${stats.pendingDonations}")
    println("Ready: ${stats.readyForDonation}")
    println("Donation rate: ${stats.getDonationRate()}%")
}
```

### Get Donation History
```kotlin
viewModelScope.launch {
    val dateRange = DateRange.currentMonth()
    
    repository.getDonationHistory(dateRange).fold(
        onSuccess = { history ->
            _donationHistory.value = history
        },
        onFailure = { error ->
            _errorMessage.value = "Failed to load history: ${error.message}"
        }
    )
}
```

## Testing & Debugging

### Trigger Auto-Flag Immediately
```kotlin
// For testing purposes
DonationWorkScheduler.runAutoFlagNow(context)
```

### Check Work Status
```kotlin
val status = DonationWorkScheduler.getWorkStatus(context)
println("Auto-flag work status: $status")
```

### Cancel Auto-Flagging
```kotlin
DonationWorkScheduler.cancelAutoFlag(context)
```

## Filter Examples

### Predefined Filters
```kotlin
// Only pending items
DonationFilter.pendingOnly()

// Only ready items
DonationFilter.readyOnly()

// Only donated items
DonationFilter.donatedOnly()

// Items older than 2 years
DonationFilter.olderThan(730)
```

### Custom Filters
```kotlin
// Electronics pending donation
DonationFilter(
    category = "Electronics",
    status = DonationStatus.PENDING
)

// Items in Library, 1-2 years old
DonationFilter(
    location = "Library",
    minAge = 365,
    maxAge = 730
)

// Chain filters
var filter = DonationFilter()
filter = filter.withCategory("Books")
filter = filter.withStatus(DonationStatus.READY)
filter = filter.withLocation("Library")
```

## Error Handling

All repository methods return `Result<T>` or emit values through `Flow`. Always handle both success and failure cases:

```kotlin
repository.markItemAsDonated(itemId, recipient, value).fold(
    onSuccess = { 
        // Handle success
    },
    onFailure = { error ->
        when (error) {
            is SecurityException -> {
                // Admin access required
            }
            is IllegalArgumentException -> {
                // Invalid input (e.g., blank recipient)
            }
            is NoSuchElementException -> {
                // Item not found
            }
            else -> {
                // General error
            }
        }
    }
)
```

## Common Workflows

### Complete Donation Workflow
```kotlin
// 1. Item is auto-flagged (or manually flagged)
repository.markItemForDonation(itemId)

// 2. Admin reviews and marks ready
repository.markItemReadyForDonation(itemId)

// 3. Admin completes donation
repository.markItemAsDonated(itemId, "Charity Name", 100.0)
```

### Manual Flagging
```kotlin
// Admin can manually flag any active item
repository.markItemForDonation(itemId)
```

## UI Integration Tips

### Display Item Age
```kotlin
val ageInDays = donationItem.getAgeInDays()
val ageText = when {
    ageInDays < 365 -> "$ageInDays days old"
    ageInDays < 730 -> "${ageInDays / 365} year old"
    else -> "${ageInDays / 365} years old"
}
```

### Status Badge Colors
```kotlin
val statusColor = when (donationItem.status) {
    DonationStatus.PENDING -> Color.YELLOW
    DonationStatus.READY -> Color.GREEN
    DonationStatus.DONATED -> Color.BLUE
}
```

### Filter Chips
```kotlin
// Show active filters as chips
if (filter.hasActiveFilters()) {
    if (filter.category != null) {
        Chip("Category: ${filter.category}")
    }
    if (filter.status != null) {
        Chip("Status: ${filter.status}")
    }
    // ... etc
}
```

## Performance Tips

1. **Use Flows for real-time updates** - Don't poll, use the Flow-based methods
2. **Apply server-side filters** - Status, category, and location are filtered at query level
3. **Cache statistics** - Stats are automatically cached for 5 minutes
4. **Limit results** - Use pagination for large datasets (future enhancement)

## Firestore Indexes Required

Make sure these indexes exist in Firestore:

```
Collection: donations
- status ASC, eligibleAt ASC
- category ASC, eligibleAt ASC
- location ASC, eligibleAt ASC
```

## Security Notes

- All donation methods require admin access
- Activity logging is automatic for audit trail
- Input validation is performed on all methods
- Firestore security rules should restrict donation collection to admins only
