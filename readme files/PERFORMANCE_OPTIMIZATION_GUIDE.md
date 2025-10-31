# Performance Optimization Guide

## Overview
This document describes the performance optimizations implemented to eliminate UI lag and main thread blocking in the Lost and Found application.

**Requirements**: 9.6, 9.7

## StrictMode Implementation

### What is StrictMode?
StrictMode is a developer tool that detects accidental disk or network access on the main thread, as well as other performance issues.

### Implementation Location
`LostFoundApplication.kt` - Enabled in debug builds only

### What StrictMode Detects
- **Thread Policy Violations**:
  - Disk reads/writes on main thread
  - Network operations on main thread
  - Custom slow code detection
  - Unbuffered I/O (Android P+)

- **VM Policy Violations**:
  - Memory leaks
  - Leaked closable objects (unclosed files, cursors)
  - Content URI without permission
  - Untagged sockets
  - Unsafe intent launches (Android S+)

### How to Use
1. Run the app in debug mode
2. Check logcat for StrictMode violations
3. Look for messages starting with "StrictMode policy violation"
4. Fix violations by moving operations to background threads

## Threading Optimizations

### Dispatcher Usage

#### Dispatchers.IO
Used for I/O-bound operations:
- Network requests (Firestore queries)
- Database operations
- File operations
- Reading/writing to disk

**Example**:
```kotlin
withContext(Dispatchers.IO) {
    firestore.collection("items").get().await()
}
```

#### Dispatchers.Default
Used for CPU-intensive operations:
- Data processing
- Analytics calculations
- Sorting large lists
- Complex filtering operations
- Grouping and aggregations

**Example**:
```kotlin
withContext(Dispatchers.Default) {
    items.filter { /* complex condition */ }
        .sortedByDescending { it.score }
        .take(10)
}
```

#### Dispatchers.Main
Used for UI updates:
- Updating LiveData
- Modifying UI elements
- Showing dialogs

**Example**:
```kotlin
withContext(Dispatchers.Main) {
    _items.value = processedItems
}
```

### Optimized Components

#### 1. AdminRepository
**File**: `admin/repository/AdminRepository.kt`

**Optimizations**:
- `computeUserAnalytics()`: Heavy data processing moved to Dispatchers.Default
  - User counting and filtering
  - Grouping by role
  - Top contributors calculation (map, sort, take)
  
- `computeDonationStats()`: Heavy aggregations moved to Dispatchers.Default
  - Donation counting and filtering
  - Category grouping
  - Monthly aggregations

**Before**:
```kotlin
private suspend fun computeUserAnalytics(): UserAnalytics {
    val users = firestore.collection("users").get().await()
    val activeUsers = users.count { !it.isBlocked } // Main thread!
    // ... more operations
}
```

**After**:
```kotlin
private suspend fun computeUserAnalytics(): UserAnalytics {
    val users = withContext(Dispatchers.IO) {
        firestore.collection("users").get().await()
    }
    
    return withContext(Dispatchers.Default) {
        val activeUsers = users.count { !it.isBlocked } // Background thread!
        // ... more operations
    }
}
```

#### 2. AdminDashboardViewModel
**File**: `admin/viewmodel/AdminDashboardViewModel.kt`

**Optimizations**:
- `searchItems()`: Filtering moved to Dispatchers.Default
- `searchUsers()`: Filtering moved to Dispatchers.Default

**Before**:
```kotlin
fun searchItems(query: String) {
    val filtered = items.filter { /* condition */ } // Main thread!
    _filteredItems.value = filtered
}
```

**After**:
```kotlin
fun searchItems(query: String) {
    viewModelScope.launch(Dispatchers.Default) {
        val filtered = items.filter { /* condition */ } // Background thread!
        withContext(Dispatchers.Main) {
            _filteredItems.value = filtered
        }
    }
}
```

#### 3. PerformanceHelper Utility
**File**: `admin/utils/PerformanceHelper.kt`

A helper object providing convenient methods for running operations on appropriate dispatchers:

**Methods**:
- `executeHeavyComputation()`: For CPU-intensive operations
- `executeIoOperation()`: For I/O operations
- `processListHeavy()`: For list transformations
- `filterListHeavy()`: For list filtering
- `sortListHeavy()`: For list sorting
- `groupListHeavy()`: For list grouping
- `calculateStatistics()`: For aggregations

**Usage Example**:
```kotlin
val topUsers = PerformanceHelper.processListHeavy(users) { list ->
    list.sortedByDescending { it.score }.take(10)
}
```

## Already Optimized Components

### 1. Fragments
- `HomeFragment.kt`: Uses Flow with Dispatchers.IO
- `BrowseFragment.kt`: Uses Flow with Dispatchers.IO
- All Firestore operations use coroutines with proper dispatchers

### 2. Adapters
- `ItemsAdapter.kt`: Uses DiffUtil for efficient updates
- `AdminUsersAdapter.kt`: Uses DiffUtil
- `AdminItemsAdapter.kt`: Uses DiffUtil
- All adapters use Glide for efficient image loading

### 3. Image Loading
- All adapters use Glide with:
  - Placeholder images
  - Error handling
  - Disk and memory caching
  - Background decoding

## Performance Testing

### How to Test
1. **Enable StrictMode**: Run app in debug mode
2. **Check Logcat**: Look for violations
3. **Use Android Profiler**:
   - Open Android Studio Profiler
   - Monitor CPU usage
   - Check for main thread spikes
   - Monitor frame rendering

### Performance Targets
- **Frame Drops**: < 10 frames per scroll operation
- **Main Thread**: No blocking operations > 16ms
- **Scroll Performance**: Smooth 60fps scrolling
- **Search Performance**: Results in < 100ms for 1000 items

### Common Issues to Watch For
1. **Large List Operations**: Always use Dispatchers.Default
2. **Firestore Queries**: Always use Dispatchers.IO
3. **Image Loading**: Always use Glide/Coil
4. **RecyclerView Updates**: Always use DiffUtil
5. **Search/Filter**: Always use background threads for > 100 items

## Best Practices

### DO:
✅ Use Dispatchers.IO for network/database operations
✅ Use Dispatchers.Default for heavy computations
✅ Use DiffUtil for RecyclerView updates
✅ Use Glide/Coil for image loading
✅ Use Flow for real-time updates
✅ Cache expensive calculations
✅ Test with StrictMode enabled

### DON'T:
❌ Perform Firestore queries on main thread
❌ Filter/sort large lists on main thread
❌ Use notifyDataSetChanged() for RecyclerView
❌ Load images without caching library
❌ Block main thread with Thread.sleep()
❌ Use runBlocking in production code
❌ Ignore StrictMode violations

## Monitoring and Debugging

### StrictMode Logs
Look for these patterns in logcat:
```
StrictMode policy violation: android.os.strictmode.DiskReadViolation
StrictMode policy violation: android.os.strictmode.NetworkViolation
```

### Android Profiler
1. Open View > Tool Windows > Profiler
2. Select your device and app
3. Click on CPU timeline
4. Look for:
   - Main thread spikes
   - Long-running methods
   - Frame drops (yellow/red bars)

### Frame Rendering
1. Enable "Profile GPU Rendering" in Developer Options
2. Look for bars exceeding the green line (16ms)
3. Optimize methods causing frame drops

## Future Optimizations

### Potential Improvements
1. **Pagination**: Load items in pages instead of all at once
2. **Lazy Loading**: Load images only when visible
3. **Database Indexing**: Add Firestore indexes for common queries
4. **Caching**: Implement more aggressive caching strategies
5. **Background Sync**: Use WorkManager for background operations

### When to Optimize Further
- If list has > 1000 items, implement pagination
- If images are large, implement progressive loading
- If analytics are slow, implement more caching
- If search is slow, implement debouncing

## Summary

All heavy operations have been moved off the main thread:
- ✅ Firestore operations use Dispatchers.IO
- ✅ Data processing uses Dispatchers.Default
- ✅ UI updates use Dispatchers.Main
- ✅ StrictMode enabled in debug builds
- ✅ RecyclerViews use DiffUtil
- ✅ Images use Glide with caching

The app should now provide smooth, responsive UI with < 10 frame drops per operation.
