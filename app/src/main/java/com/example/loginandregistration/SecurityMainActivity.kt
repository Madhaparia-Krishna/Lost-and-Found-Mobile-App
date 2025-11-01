package com.example.loginandregistration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class SecurityMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Set theme back to regular theme (for Android 11 and below)
        setTheme(R.style.Theme_LoginAndRegistration)
        
        // Set condition to keep splash screen visible (false = dismiss immediately after app is ready)
        splashScreen.setKeepOnScreenCondition { false }
        
        setContentView(R.layout.activity_security_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.security_bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> {
                    // Security 'home' is the dashboard to view all reports
                    selectedFragment = SecurityDashboardFragment()
                }
                R.id.nav_claims -> {
                    // Claim review interface for security
                    selectedFragment = SecurityClaimReviewFragment()
                }
                R.id.nav_browse -> {
                    // You can keep browse or replace with another security function
                    selectedFragment = BrowseFragment()
                }
                R.id.nav_profile -> {
                    // Profile for password change and logout
                    selectedFragment = ProfileFragment()
                }
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.security_fragment_container, selectedFragment)
                    .commit()
            }
            true
        }

        // Set the default fragment to the dashboard
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }
}


