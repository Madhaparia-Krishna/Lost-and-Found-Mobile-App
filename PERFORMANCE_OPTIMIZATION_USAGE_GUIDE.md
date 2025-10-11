# Performance Optimization Usage Guide

## Quick Reference for Using Performance Optimizations

This guide provides practical examples of how to use the newly implemented performance optimization features in the admin module.

---

## 1. Using Pagination

### Basic Pagination Example

```kotlin
// In your Fragment or Activity
class AdminItemsFragment : Fragment() {
    private val viewModel: AdminDashboardViewModel by viewModels()
    private val paginationHelper = PaginationHelper<EnhancedLostFoundItem>(pageSize = 50)
    
    private fun loadItems() {
        lifecycleScope.launch {
            val result = repository.getItemsPaginated(paginationHelper)
            result.onSuccess { items ->
                adapter.addItems(items)
                
                // Check if there are more pages
                if (paginationHelper.hasMore()) {
                    showLoadMoreButton()
                }
            }
        }
    }
    
    private fun loadMoreItems() {
        if (paginationHelper.hasMore()) {
            loadItems()
        }
    }
    
    private fun resetPagination() {
        paginationHelper.reset()
        adapter.clearItems()
        loadItems()
    }
}
```

### Pagination with RecyclerView Scroll Listener

```kotlin
recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        
        if (!isLoading && paginationHelper.hasMore()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                && firstVisibleItemPosition >= 0) {
                loadMoreItems()
            }
        }
    }
})
```

---

## 2. Using Cache Manager

### Basic Caching Example

```kotlin
// In your Repository or ViewModel
class AdminDashboardViewModel : ViewModel() {
    private val cacheManager = CacheManager.getInstance()
    
    fun loadUserAnalytics() {
        viewModelScope.launch {
            // Try to get from cache first
            val cached = cacheManager.get<UserAnalytics>(CacheManager.KEY_USER_ANALYTICS)
            if (cached != null) {
                _userAnalytics.value = cached
                return@launch
            }
            
            // Cache miss - fetch from repository
            val result = repository.getUserAnalyticsCached()
            result.onSuccess { analytics ->
                _userAnalytics.value = analytics
            }
        }
    }
}
```

### Using getOrPut for Automatic Caching

```kotlin
suspend fun getDashboardStats(): DashboardStats {
    return cacheManager.getOrPut(
        key = CacheManager.KEY_DASHBOARD_STATS,
        ttl = 5 * 60 * 1000L // 5 minutes
    ) {
        // This block only executes on cache miss
        computeDashboardStats()
    }
}
```

### Cache Invalidation

```kotlin
// After updating user data
suspend fun updateUser(userId: String, updates: Map<String, Any>) {
    repository.updateUserDetails(userId, updates)
    
    // Invalidate related caches
    repository.invalidateUserCache()
}

// After donation action
suspend fun markItemAsDonated(itemId: String) {
    repository.markItemAsDonated(itemId, recipient, value)
    
    // Invalidate donation caches
    repository.invalidateDonationCache()
}
```

### Cache Statistics

```kotlin
fun showCacheStats() {
    val stats = cacheManager.getStats()
    Log.d(TAG, "Cache Stats: ${stats.validEntries} valid, ${stats.expiredEntries} expired")
    
    // Clean up expired entries
    cacheManager.cleanupExpired()
}
```

---

## 3. Using Optimized Queries

### Basic Optimized Query

```kotlin
// Get items with filters
suspend fun loadFilteredItems() {
    val filters = mapOf(
        "status" to "ACTIVE",
        "category" to "Electronics"
    )
    
    val result = repository.getItemsOptimized(
        filters = filters,
        limit = 50
    )
    
    result.onSuccess { items ->
        displayItems(items)
    }
}
```

### Query with Validation

```kotlin
suspend fun searchItems(filters: Map<String, Any>) {
    // Validate query complexity
    val validation = QueryOptimizer.validateQueryComplexity(filters)
    
    if (!validation.isValid) {
        showError(validation.message)
        return
    }
    
    when (validation.complexity) {
        QueryComplexity.SIMPLE -> Log.d(TAG, "Simple query")
        QueryComplexity.MODERATE -> Log.d(TAG, "Moderate query")
        QueryComplexity.COMPLEX -> Log.d(TAG, "Complex query - may be slower")
    }
    
    val result = repository.getItemsOptimized(filters)
    // Handle result...
}
```

### Activity Logs with Date Range

```kotlin
suspend fun loadActivityLogs(startDate: Long, endDate: Long) {
    val filters = mapOf(
        "startDate" to startDate,
        "endDate" to endDate,
        "actionType" to "USER_BLOCK"
    )
    
    val result = repository.getActivityLogsOptimized(
        filters = filters,
        limit = 100
    )
    
    result.onSuccess { logs ->
        displayLogs(logs)
    }
}
```

### Optimized Search

```kotlin
suspend fun searchUsers(query: String) {
    val result = repository.searchOptimized(
        collection = "users",
        searchField = "email",
        searchValue = query,
        limit = 100
    )
    
    result.onSuccess { results ->
        displaySearchResults(results)
    }
}
```

---

## 4. Using Background Export Processing

### Queue an Export

```kotlin
class AdminExportFragment : Fragment() {
    private var currentExportWorkId: UUID? = null
    
    fun exportItems() {
        val exportRequest = ExportRequest(
            format = ExportFormat.PDF,
            dataType = ExportDataType.ITEMS,
            dateRange = DateRange(
                startDate = startDate,
                endDate = endDate
            ),
            requestedBy = "admin"
        )
        
        lifecycleScope.launch {
            val result = repository.queueExportInBackground(requireContext(), exportRequest)
            result.onSuccess { workId ->
                currentExportWorkId = workId
                observeExportProgress(workId)
            }
        }
    }
}
```

### Observe Export Progress

```kotlin
private fun observeExportProgress(workId: UUID) {
    val exportQueue = ExportQueueManager.getInstance(requireContext())
    
    exportQueue.observeExport(workId).observe(viewLifecycleOwner) { workInfo ->
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> {
                showProgress("Export queued...")
            }
            WorkInfo.State.RUNNING -> {
                val progress = workInfo.progress.getInt(ExportWorker.KEY_PROGRESS, 0)
                showProgress("Exporting... $progress%")
            }
            WorkInfo.State.SUCCEEDED -> {
                val filePath = workInfo.outputData.getString(ExportWorker.KEY_FILE_PATH)
                showSuccess("Export completed: $filePath")
                shareExportFile(filePath)
            }
            WorkInfo.State.FAILED -> {
                val error = workInfo.outputData.getString(ExportWorker.KEY_ERROR_MESSAGE)
                showError("Export failed: $error")
            }
            else -> {
                // Handle other states
            }
        }
    }
}
```

### Cancel Export

```kotlin
fun cancelExport() {
    currentExportWorkId?.let { workId ->
        repository.cancelBackgroundExport(requireContext(), workId)
        showMessage("Export cancelled")
    }
}
```

### View Active Exports

```kotlin
fun showActiveExports() {
    val activeExports = repository.getActiveBackgroundExports(requireContext())
    
    activeExports.forEach { workInfo ->
        val progress = workInfo.progress.getInt(ExportWorker.KEY_PROGRESS, 0)
        Log.d(TAG, "Export ${workInfo.id}: ${workInfo.state} - $progress%")
    }
}
```

### Export Queue Statistics

```kotlin
fun showExportQueueStats() {
    val stats = repository.getExportQueueStats(requireContext())
    
    val message = """
        Total Exports: ${stats.totalExports}
        Pending: ${stats.pendingExports}
        Running: ${stats.runningExports}
        Completed: ${stats.completedExports}
        Failed: ${stats.failedExports}
    """.trimIndent()
    
    showDialog(message)
}
```

---

## 5. Combined Usage Example

### Complete Fragment with All Optimizations

```kotlin
class AdminItemsFragment : Fragment() {
    private val viewModel: AdminDashboardViewModel by viewModels()
    private val repository = AdminRepository()
    private val paginationHelper = PaginationHelper<EnhancedLostFoundItem>(pageSize = 50)
    private val cacheManager = CacheManager.getInstance()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadItems()
        setupRefresh()
    }
    
    private fun loadItems() {
        lifecycleScope.launch {
            // Try cache first
            val cached = cacheManager.get<List<EnhancedLostFoundItem>>("items_page_0")
            if (cached != null) {
                displayItems(cached)
                return@launch
            }
            
            // Use optimized query with pagination
            val filters = getCurrentFilters()
            val result = repository.getItemsOptimized(filters, limit = 50)
            
            result.onSuccess { items ->
                displayItems(items)
                // Cache the results
                cacheManager.put("items_page_0", items, ttl = 2 * 60 * 1000L)
            }
        }
    }
    
    private fun exportItems() {
        val exportRequest = ExportRequest(
            format = ExportFormat.PDF,
            dataType = ExportDataType.ITEMS,
            dateRange = getCurrentDateRange(),
            requestedBy = "admin"
        )
        
        lifecycleScope.launch {
            val result = repository.queueExportInBackground(requireContext(), exportRequest)
            result.onSuccess { workId ->
                observeExportProgress(workId)
            }
        }
    }
    
    private fun setupRefresh() {
        swipeRefresh.setOnRefreshListener {
            // Invalidate cache
            cacheManager.invalidatePattern("items_.*")
            // Reset pagination
            paginationHelper.reset()
            // Reload
            loadItems()
        }
    }
}
```

---

## Best Practices

### Pagination
1. Use appropriate page sizes (50 for lists, 20 for dashboards)
2. Always reset pagination when filters change
3. Show loading indicators during pagination
4. Handle empty states gracefully

### Caching
1. Set appropriate TTL based on data volatility
2. Invalidate cache after data modifications
3. Use cache for frequently accessed data
4. Monitor cache hit rates
5. Clean up expired entries periodically

### Query Optimization
1. Validate query complexity before execution
2. Use selective fields when possible
3. Apply result limiting
4. Use composite indexes for complex queries
5. Monitor query performance in Firebase Console

### Background Exports
1. Show progress indicators to users
2. Allow users to cancel long-running exports
3. Handle export failures gracefully
4. Clean up old export files
5. Use appropriate constraints (network, battery)

---

## Performance Monitoring

### Key Metrics to Track

```kotlin
// Cache performance
val cacheStats = cacheManager.getStats()
Log.d(TAG, "Cache hit rate: ${cacheStats.validEntries.toFloat() / cacheStats.totalEntries}")

// Export queue performance
val exportStats = repository.getExportQueueStats(context)
Log.d(TAG, "Export success rate: ${exportStats.completedExports.toFloat() / exportStats.totalExports}")

// Query performance
val startTime = System.currentTimeMillis()
val result = repository.getItemsOptimized(filters)
val duration = System.currentTimeMillis() - startTime
Log.d(TAG, "Query took ${duration}ms")
```

---

## Troubleshooting

### Common Issues

1. **Pagination not loading more items**
   - Check if `hasMore()` returns true
   - Verify Firestore has more documents
   - Check for errors in logs

2. **Cache not working**
   - Verify cache key is correct
   - Check TTL hasn't expired
   - Ensure cache invalidation is called appropriately

3. **Queries are slow**
   - Deploy Firestore indexes
   - Reduce query complexity
   - Use result limiting
   - Check Firebase Console for index warnings

4. **Exports failing**
   - Check WorkManager constraints
   - Verify network connectivity
   - Check storage permissions
   - Review error messages in WorkInfo

---

## Additional Resources

- [Firestore Best Practices](https://firebase.google.com/docs/firestore/best-practices)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Android Performance Tips](https://developer.android.com/topic/performance)

---

## Support

For issues or questions about performance optimizations, refer to:
- `PERFORMANCE_OPTIMIZATION_SUMMARY.md` for implementation details
- Firebase Console for query performance metrics
- Android Studio Profiler for memory and CPU usage
