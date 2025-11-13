package com.example.loginandregistration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.loginandregistration.databinding.ActivitySecurityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SecurityMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Set theme back to regular theme (for Android 11 and below)
        setTheme(R.style.Theme_LoginAndRegistration)
        
        // Set condition to keep splash screen visible (false = dismiss immediately after app is ready)
        splashScreen.setKeepOnScreenCondition { false }
        
        binding = ActivitySecurityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Get the NavHostFragment and NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_security) as NavHostFragment
        val navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.securityBottomNavView

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_security_dashboard,
                R.id.navigation_security_reports,
                R.id.navigation_security_create,
                R.id.navigation_security_profile
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}


