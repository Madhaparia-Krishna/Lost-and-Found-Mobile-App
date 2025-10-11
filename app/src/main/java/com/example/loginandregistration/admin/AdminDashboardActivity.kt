package com.example.loginandregistration.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.loginandregistration.Login
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.repository.AdminRepository
import com.example.loginandregistration.admin.utils.NotificationPermissionHelper
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

class AdminDashboardActivity : AppCompatActivity() {
    
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: AdminDashboardViewModel
    private val adminRepository = AdminRepository()
    
    companion object {
        private const val TAG = "AdminDashboardActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "AdminDashboardActivity onCreate started")
        
        // Check admin access
        if (!adminRepository.isAdminUser()) {
            Log.w(TAG, "Access denied - not admin user")
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }
        
        Log.d(TAG, "Admin access confirmed, setting up dashboard")
        setContentView(R.layout.activity_admin_dashboard)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[AdminDashboardViewModel::class.java]
        
        // Test Firebase connection and initialize admin user
        testFirebaseConnection()
        
        try {
            // Setup navigation
            val navView: BottomNavigationView = findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_dashboard,
                    R.id.navigation_items,
                    R.id.navigation_users,
                    R.id.navigation_donations,
                    R.id.navigation_activity_log,
                    R.id.navigation_notifications
                )
            )
            
            // Removed setupActionBarWithNavController() to fix IllegalStateException
            // Bottom navigation works without ActionBar integration
            navView.setupWithNavController(navController)
            
            // Set title
            supportActionBar?.title = "Admin Dashboard"
            
            // Request notification permission for admin
            NotificationPermissionHelper.checkAndRequestPermission(this, showRationale = true)
            
            Log.d(TAG, "Navigation setup completed successfully")
            
            // Handle notification deep linking
            handleNotificationIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation", e)
            Toast.makeText(this, "Error setting up dashboard: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        val granted = NotificationPermissionHelper.handlePermissionResult(
            requestCode,
            permissions,
            grantResults
        )
        
        if (!granted && requestCode == NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // Permission denied - show dialog if permanently denied
            if (!NotificationPermissionHelper.shouldShowPermissionRationale(this)) {
                NotificationPermissionHelper.showPermissionDeniedDialog(this)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, Login::class.java))
                finish()
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshData()
                Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_export -> {
                navigateToExport()
                true
            }
            R.id.action_create_test_data -> {
                viewModel.createTestData()
                Toast.makeText(this, "Creating test data...", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Navigate to export fragment
     * Requirements: 8.1, 14.1
     */
    private fun navigateToExport() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_export)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to export", e)
            Toast.makeText(this, "Error opening export: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Handles deep linking from notifications
     * Requirements: 6.6
     */
    private fun handleNotificationIntent(intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type")
        val actionUrl = intent.getStringExtra("action_url")
        val notificationId = intent.getStringExtra("notification_id")
        
        Log.d(TAG, "Handling notification intent: type=$notificationType, url=$actionUrl")
        
        if (notificationType != null && actionUrl != null) {
            // Mark notification as opened
            if (notificationId != null) {
                markNotificationAsOpened(notificationId)
            }
            
            // Navigate based on action URL
            when {
                actionUrl.startsWith("item/") -> {
                    val itemId = actionUrl.removePrefix("item/")
                    navigateToItemDetails(itemId)
                }
                actionUrl.startsWith("user/") -> {
                    val userId = actionUrl.removePrefix("user/")
                    navigateToUserDetails(userId)
                }
                actionUrl.startsWith("donation/") -> {
                    val donationId = actionUrl.removePrefix("donation/")
                    navigateToDonationDetails(donationId)
                }
                actionUrl == "donations" -> {
                    navigateToDonations()
                }
                actionUrl == "activity_log" -> {
                    navigateToActivityLog()
                }
                actionUrl == "notifications" -> {
                    navigateToNotifications()
                }
                actionUrl == "dashboard" -> {
                    navigateToDashboard()
                }
                else -> {
                    Log.w(TAG, "Unknown action URL: $actionUrl")
                }
            }
        }
        
        // Handle deep link URIs (lostfound://admin/...)
        intent.data?.let { uri ->
            Log.d(TAG, "Handling deep link URI: $uri")
            when (uri.host) {
                "admin" -> {
                    val path = uri.path?.removePrefix("/") ?: ""
                    when {
                        path.startsWith("item/") -> {
                            val itemId = path.removePrefix("item/")
                            navigateToItemDetails(itemId)
                        }
                        path.startsWith("user/") -> {
                            val userId = path.removePrefix("user/")
                            navigateToUserDetails(userId)
                        }
                        path == "donations" -> navigateToDonations()
                        path == "activity_log" -> navigateToActivityLog()
                        path == "notifications" -> navigateToNotifications()
                        else -> navigateToDashboard()
                    }
                }
            }
        }
    }
    
    /**
     * Navigate to item details from notification
     * Requirements: 6.6
     */
    private fun navigateToItemDetails(itemId: String) {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_items)
            // TODO: Pass itemId to fragment to show specific item details
            Toast.makeText(this, "Navigating to item: $itemId", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to item details", e)
        }
    }
    
    /**
     * Navigate to user details from notification
     * Requirements: 6.6
     */
    private fun navigateToUserDetails(userId: String) {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_users)
            // TODO: Pass userId to fragment to show specific user details
            Toast.makeText(this, "Navigating to user: $userId", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to user details", e)
        }
    }
    
    /**
     * Navigate to donation details from notification
     * Requirements: 6.6
     */
    private fun navigateToDonationDetails(donationId: String) {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_donations)
            // TODO: Pass donationId to fragment to show specific donation details
            Toast.makeText(this, "Navigating to donation: $donationId", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to donation details", e)
        }
    }
    
    /**
     * Navigate to donations tab
     * Requirements: 6.6
     */
    private fun navigateToDonations() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_donations)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to donations", e)
        }
    }
    
    /**
     * Navigate to activity log tab
     * Requirements: 6.6
     */
    private fun navigateToActivityLog() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_activity_log)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to activity log", e)
        }
    }
    
    /**
     * Navigate to notifications tab
     * Requirements: 6.6
     */
    private fun navigateToNotifications() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_notifications)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to notifications", e)
        }
    }
    
    /**
     * Navigate to dashboard tab
     * Requirements: 6.6
     */
    private fun navigateToDashboard() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_activity_admin)
            navController.navigate(R.id.navigation_dashboard)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to dashboard", e)
        }
    }
    
    /**
     * Mark notification as opened in Firestore
     * Requirements: 6.6
     */
    private fun markNotificationAsOpened(notificationId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Use coroutine to update in background
        Thread {
            try {
                runBlocking {
                    adminRepository.markNotificationAsOpened(notificationId, userId)
                    Log.d(TAG, "Marked notification as opened: $notificationId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark notification as opened", e)
            }
        }.start()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_admin)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    private fun testFirebaseConnection() {
        Log.d(TAG, "Testing Firebase connection...")
        
        // Use lifecycle-aware coroutines instead of callbacks
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                adminRepository.checkFirebaseConnection { isConnected, message ->
                    // Switch to main thread for UI updates
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (isConnected) {
                            Log.d(TAG, "Firebase connection successful: $message")
                            Toast.makeText(this@AdminDashboardActivity, "Connected to Firebase", Toast.LENGTH_SHORT).show()
                            initializeAdminUser()
                        } else {
                            Log.e(TAG, "Firebase connection failed: $message")
                            Toast.makeText(this@AdminDashboardActivity, "Firebase connection failed: $message", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error testing Firebase connection", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Connection test failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun initializeAdminUser() {
        // Use lifecycle-aware coroutines for background work
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                adminRepository.initializeAdminUser()
                    .onSuccess {
                        Log.d(TAG, "Admin user initialized successfully")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to initialize admin user", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing admin user", e)
            }
        }
    }
    
    // Methods called by AdminProfileFragment
    fun refreshData() {
        viewModel.refreshData()
        Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show()
    }
    
    fun createTestData() {
        viewModel.createTestData()
        Toast.makeText(this, "Creating test data...", Toast.LENGTH_SHORT).show()
    }
}