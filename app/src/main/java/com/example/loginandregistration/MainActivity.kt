package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.admin.utils.NotificationPermissionHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Set theme back to regular theme (for Android 11 and below)
        setTheme(R.style.Theme_LoginAndRegistration)
        
        // Set condition to keep splash screen visible (false = dismiss immediately after app is ready)
        splashScreen.setKeepOnScreenCondition { false }
        
        auth = Firebase.auth

        // Check if user is signed in
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }
        
        // Check if user is admin and redirect to admin dashboard
        if (auth.currentUser?.email == "admin@gmail.com") {
            navigateToAdminDashboard()
            return
        }
        
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home // Highlight home icon
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_browse -> replaceFragment(BrowseFragment())
                R.id.nav_report -> replaceFragment(ReportFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                else -> false
            }
            true
        }
        
        // Request notification permission on Android 13+
        NotificationPermissionHelper.checkAndRequestPermission(this, showRationale = true)
        
        // Handle notification deep linking
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
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    
    /**
     * Handles deep linking from notifications
     * Requirements: 6.6
     */
    private fun handleNotificationIntent(intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type")
        val actionUrl = intent.getStringExtra("action_url")
        val notificationId = intent.getStringExtra("notification_id")
        
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
                actionUrl.startsWith("request/") -> {
                    val requestId = actionUrl.removePrefix("request/")
                    navigateToRequestStatus(requestId)
                }
                actionUrl == "profile" -> {
                    replaceFragment(ProfileFragment())
                    bottomNav.selectedItemId = R.id.nav_profile
                }
                actionUrl == "browse" -> {
                    replaceFragment(BrowseFragment())
                    bottomNav.selectedItemId = R.id.nav_browse
                }
                else -> {
                    // Default to home if action URL is not recognized
                    replaceFragment(HomeFragment())
                    bottomNav.selectedItemId = R.id.nav_home
                }
            }
        }
        
        // Handle deep link URIs (lostfound://notification/...)
        intent.data?.let { uri ->
            android.util.Log.d("MainActivity", "Handling deep link URI: $uri")
            when (uri.host) {
                "notification" -> {
                    val path = uri.path?.removePrefix("/") ?: ""
                    when {
                        path.startsWith("item/") -> {
                            val itemId = path.removePrefix("item/")
                            navigateToItemDetails(itemId)
                        }
                        path.startsWith("request/") -> {
                            val requestId = path.removePrefix("request/")
                            navigateToRequestStatus(requestId)
                        }
                        path == "profile" -> {
                            replaceFragment(ProfileFragment())
                            bottomNav.selectedItemId = R.id.nav_profile
                        }
                        path == "browse" -> {
                            replaceFragment(BrowseFragment())
                            bottomNav.selectedItemId = R.id.nav_browse
                        }
                        else -> {
                            replaceFragment(HomeFragment())
                            bottomNav.selectedItemId = R.id.nav_home
                        }
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
        // Navigate to browse fragment which shows items
        // In a real implementation, you would pass the itemId to show specific item details
        replaceFragment(BrowseFragment())
        bottomNav.selectedItemId = R.id.nav_browse
        
        // TODO: Implement item details dialog or fragment that shows specific item
        // For now, just navigate to browse where user can find the item
    }
    
    /**
     * Navigate to request status from notification
     * Requirements: 6.6
     */
    private fun navigateToRequestStatus(requestId: String) {
        // Navigate to profile where user can see their requests
        replaceFragment(ProfileFragment())
        bottomNav.selectedItemId = R.id.nav_profile
        
        // TODO: Implement request status view in profile
        // For now, just navigate to profile
    }
    
    /**
     * Mark notification as opened in Firestore
     */
    private fun markNotificationAsOpened(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // Use coroutine to update in background
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val repository = com.example.loginandregistration.admin.repository.AdminRepository()
                repository.markNotificationAsOpened(notificationId, userId)
            } catch (e: Exception) {
                // Silently fail - not critical
                android.util.Log.e("MainActivity", "Failed to mark notification as opened", e)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun navigateToAdminDashboard() {
        val intent = Intent(this, com.example.loginandregistration.admin.AdminDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Example: If you want a logout option from an ActionBar/Toolbar menu instead
    // override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    //     menuInflater.inflate(R.menu.main_activity_options_menu, menu) // Create this menu file
    //     return true
    // }

    // override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //     return when (item.itemId) {
    //         R.id.action_logout -> {
    //             logoutUserAndNavigateToLogin()
    //             true
    //         }
    //         else -> super.onOptionsItemSelected(item)
    //     }
    // }

    // private fun logoutUserAndNavigateToLogin() {
    //     auth.signOut()
    //     // Optional: Google Sign Out if you use it
    //     // val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    //     // val googleSignInClient = GoogleSignIn.getClient(this, gso)
    //     // googleSignInClient.signOut().addOnCompleteListener { navigateToLogin() }
    //     navigateToLogin()
    // }
}
