# Data Export Quick Start Guide

## Overview
This guide provides quick examples for using the data export functionality in the Lost & Found Admin Module.

## Setup

### 1. Add to build.gradle.kts (Already Done)
```kotlin
implementation("com.itextpdf:itext7-core:7.2.5")
```

### 2. Sync Gradle
Run: `./gradlew build` or sync in Android Studio

## Basic Usage

### Generate PDF Reports

#### Items Report
```kotlin
import com.example.loginandregistration.admin.utils.PdfExportGenerator
import com.example.loginandregistration.admin.models.DateRange

// In your ViewModel or Repository
suspend fun exportItemsReport(items: List<EnhancedLostFoundItem>) {
    val pdfGenerator = PdfExportGenerator(context)
    
    val result = pdfGenerator.generateItemsReport(
        items = items,
        dateRange = DateRange.lastNDays(30), // Last 30 days
        generatedBy = currentUserEmail
    )
    
    result.onSuccess { filePath ->
        // File saved successfully
        Log.d("Export", "PDF saved to: $filePath")
        // Share or open the file
    }.onFailure { error ->
        // Handle error
        Log.e("Export", "Failed to generate PDF: ${error.message}")
    }
}
```

#### Users Report
```kotlin
suspend fun exportUsersReport(users: List<EnhancedAdminUser>) {
    val pdfGenerator = PdfExportGenerator(context)
    
    val result = pdfGenerator.generateUsersReport(
        users = users,
        dateRange = DateRange.currentMonth(),
        generatedBy = currentUserEmail
    )
    
    // Handle result...
}
```

#### Activity Logs Report
```kotlin
suspend fun exportActivityReport(logs: List<ActivityLog>) {
    val pdfGenerator = PdfExportGenerator(context)
    
    val result = pdfGenerator.generateActivityReport(
        logs = logs,
        dateRange = DateRange.allTime(),
        generatedBy = currentUserEmail
    )
    
    // Handle result...
}
```

#### Comprehensive Report
```kotlin
suspend fun exportComprehensiveReport(
    items: List<EnhancedLostFoundItem>,
    users: List<EnhancedAdminUser>,
    logs: List<ActivityLog>,
    donations: List<DonationItem>,
    donationStats: DonationStats
) {
    val pdfGenerator = PdfExportGenerator(context)
    
    val result = pdfGenerator.generateComprehensiveReport(
        items = items,
        users = users,
        logs = logs,
        donations = donations,
        donationStats = donationStats,
        dateRange = DateRange.lastNDays(90),
        generatedBy = currentUserEmail
    )
    
    // Handle result...
}
```

### Generate CSV Exports

#### Items CSV
```kotlin
import com.example.loginandregistration.admin.utils.CsvExportGenerator

suspend fun exportItemsCsv(items: List<EnhancedLostFoundItem>) {
    val csvGenerator = CsvExportGenerator(context)
    
    val result = csvGenerator.generateItemsCsv(items)
    
    result.onSuccess { filePath ->
        Log.d("Export", "CSV saved to: $filePath")
    }.onFailure { error ->
        Log.e("Export", "Failed to generate CSV: ${error.message}")
    }
}
```

#### Users CSV
```kotlin
suspend fun exportUsersCsv(users: List<EnhancedAdminUser>) {
    val csvGenerator = CsvExportGenerator(context)
    val result = csvGenerator.generateUsersCsv(users)
    // Handle result...
}
```

#### Activity Logs CSV
```kotlin
suspend fun exportActivityLogsCsv(logs: List<ActivityLog>) {
    val csvGenerator = CsvExportGenerator(context)
    val result = csvGenerator.generateActivityLogsCsv(logs)
    // Handle result...
}
```

## File Management

### Share Export File
```kotlin
import com.example.loginandregistration.admin.utils.ExportFileManager

fun shareExport(filePath: String, format: ExportFormat) {
    val fileManager = ExportFileManager(context, firestore)
    
    val result = fileManager.shareExportFile(filePath, format)
    
    result.onSuccess { intent ->
        context.startActivity(intent)
    }.onFailure { error ->
        Toast.makeText(context, "Failed to share: ${error.message}", Toast.LENGTH_SHORT).show()
    }
}
```

### Open Export File
```kotlin
fun openExport(filePath: String, format: ExportFormat) {
    val fileManager = ExportFileManager(context, firestore)
    
    val result = fileManager.openExportFile(filePath, format)
    
    result.onSuccess { intent ->
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }.onFailure { error ->
        Toast.makeText(context, "Failed to open: ${error.message}", Toast.LENGTH_SHORT).show()
    }
}
```

### Get Export History
```kotlin
suspend fun loadExportHistory() {
    val fileManager = ExportFileManager(context, firestore)
    
    val result = fileManager.getExportHistory(limit = 50)
    
    result.onSuccess { exports ->
        // Display export history
        exports.forEach { export ->
            Log.d("Export", "Export: ${export.fileName} - ${export.status}")
        }
    }.onFailure { error ->
        Log.e("Export", "Failed to load history: ${error.message}")
    }
}
```

### Delete Export
```kotlin
suspend fun deleteExport(exportId: String, filePath: String) {
    val fileManager = ExportFileManager(context, firestore)
    
    val result = fileManager.deleteExportFile(exportId, filePath)
    
    result.onSuccess {
        Toast.makeText(context, "Export deleted", Toast.LENGTH_SHORT).show()
    }.onFailure { error ->
        Toast.makeText(context, "Failed to delete: ${error.message}", Toast.LENGTH_SHORT).show()
    }
}
```

### Cleanup Old Exports
```kotlin
suspend fun cleanupOldExports() {
    val fileManager = ExportFileManager(context, firestore)
    
    val result = fileManager.cleanupOldExports()
    
    result.onSuccess { deletedCount ->
        Log.d("Export", "Deleted $deletedCount old exports")
    }.onFailure { error ->
        Log.e("Export", "Cleanup failed: ${error.message}")
    }
}
```

### Check Storage Space
```kotlin
fun checkStorageSpace() {
    val fileManager = ExportFileManager(context, firestore)
    
    if (fileManager.hasEnoughStorageSpace(estimatedSizeMB = 10f)) {
        // Proceed with export
    } else {
        Toast.makeText(context, "Not enough storage space", Toast.LENGTH_SHORT).show()
    }
}
```

## Permission Handling

### Check and Request Permissions
```kotlin
import com.example.loginandregistration.admin.utils.StoragePermissionHelper

class ExportActivity : AppCompatActivity() {
    private lateinit var permissionHelper: StoragePermissionHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHelper = StoragePermissionHelper(this)
    }
    
    fun startExport() {
        if (permissionHelper.hasStoragePermission()) {
            // Proceed with export
            performExport()
        } else {
            if (permissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Show rationale
                showPermissionRationale()
            } else {
                // Request permission
                permissionHelper.requestStoragePermission(this)
            }
        }
    }
    
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage(permissionHelper.getPermissionRationaleMessage())
            .setPositiveButton("Grant") { _, _ ->
                permissionHelper.requestStoragePermission(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (permissionHelper.onPermissionResult(requestCode, permissions, grantResults)) {
            // Permission granted
            performExport()
        } else {
            // Permission denied
            Toast.makeText(
                this,
                permissionHelper.getPermissionDeniedMessage(),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun performExport() {
        // Your export logic here
    }
}
```

## Date Range Helpers

```kotlin
import com.example.loginandregistration.admin.models.DateRange

// Last 7 days
val lastWeek = DateRange.lastNDays(7)

// Last 30 days
val lastMonth = DateRange.lastNDays(30)

// Current month
val currentMonth = DateRange.currentMonth()

// All time
val allTime = DateRange.allTime()

// Custom range
val customRange = DateRange(
    startDate = startTimestamp,
    endDate = endTimestamp
)

// Check if valid
if (customRange.isValid()) {
    // Use the range
}

// Get duration
val durationDays = customRange.getDurationInDays()
```

## Complete Example in ViewModel

```kotlin
class AdminExportViewModel(
    private val context: Context,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    
    private val pdfGenerator = PdfExportGenerator(context)
    private val csvGenerator = CsvExportGenerator(context)
    private val fileManager = ExportFileManager(context, firestore)
    
    private val _exportStatus = MutableLiveData<ExportStatus>()
    val exportStatus: LiveData<ExportStatus> = _exportStatus
    
    private val _exportFilePath = MutableLiveData<String>()
    val exportFilePath: LiveData<String> = _exportFilePath
    
    fun exportData(
        format: ExportFormat,
        dataType: ExportDataType,
        dateRange: DateRange,
        items: List<EnhancedLostFoundItem> = emptyList(),
        users: List<EnhancedAdminUser> = emptyList(),
        logs: List<ActivityLog> = emptyList()
    ) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.PROCESSING
            
            val result = when (format) {
                ExportFormat.PDF -> {
                    when (dataType) {
                        ExportDataType.ITEMS -> pdfGenerator.generateItemsReport(
                            items, dateRange, getCurrentUserEmail()
                        )
                        ExportDataType.USERS -> pdfGenerator.generateUsersReport(
                            users, dateRange, getCurrentUserEmail()
                        )
                        ExportDataType.ACTIVITIES -> pdfGenerator.generateActivityReport(
                            logs, dateRange, getCurrentUserEmail()
                        )
                        else -> Result.failure(Exception("Unsupported data type"))
                    }
                }
                ExportFormat.CSV -> {
                    when (dataType) {
                        ExportDataType.ITEMS -> csvGenerator.generateItemsCsv(items)
                        ExportDataType.USERS -> csvGenerator.generateUsersCsv(users)
                        ExportDataType.ACTIVITIES -> csvGenerator.generateActivityLogsCsv(logs)
                        else -> Result.failure(Exception("Unsupported data type"))
                    }
                }
                else -> Result.failure(Exception("Unsupported format"))
            }
            
            result.onSuccess { filePath ->
                _exportStatus.value = ExportStatus.COMPLETED
                _exportFilePath.value = filePath
                
                // Save to history
                val exportRequest = ExportRequest(
                    id = UUID.randomUUID().toString(),
                    format = format,
                    dataType = dataType,
                    dateRange = dateRange,
                    requestedBy = getCurrentUserEmail(),
                    status = com.example.loginandregistration.admin.models.ExportStatus.COMPLETED,
                    fileUrl = filePath,
                    fileName = File(filePath).name,
                    completedAt = System.currentTimeMillis()
                )
                fileManager.saveExportHistory(exportRequest)
            }.onFailure { error ->
                _exportStatus.value = ExportStatus.FAILED
                Log.e("Export", "Export failed: ${error.message}")
            }
        }
    }
    
    fun shareExport(filePath: String, format: ExportFormat) {
        val result = fileManager.shareExportFile(filePath, format)
        result.onSuccess { intent ->
            // Start activity from fragment/activity
        }
    }
    
    private fun getCurrentUserEmail(): String {
        // Get from Firebase Auth or shared preferences
        return "admin@example.com"
    }
}
```

## Tips

1. **Always run exports in background**: Use coroutines or WorkManager for large datasets
2. **Check storage space**: Before starting export, verify sufficient space
3. **Handle permissions**: Check and request permissions before export
4. **Provide feedback**: Show progress indicators and completion messages
5. **Cleanup regularly**: Schedule periodic cleanup of old exports
6. **Test with large datasets**: Ensure performance with 1000+ items
7. **Error handling**: Always handle Result failures gracefully

## Troubleshooting

### PDF Generation Fails
- Check iText7 dependency is added
- Verify storage permissions
- Check available storage space
- Review error logs for specific issues

### File Sharing Doesn't Work
- Verify FileProvider is configured in AndroidManifest.xml
- Check file_paths.xml exists and is correct
- Ensure file exists before sharing
- Verify URI permissions are granted

### Permission Denied
- Check AndroidManifest.xml has required permissions
- For Android 10+, use app-specific storage (no permissions needed)
- For older versions, request runtime permissions
- Guide users to app settings if permanently denied

## Next Steps

1. Integrate into UI (AdminExportFragment)
2. Add to AdminRepository
3. Connect to ViewModel
4. Add progress indicators
5. Implement export history UI
6. Add automated testing
