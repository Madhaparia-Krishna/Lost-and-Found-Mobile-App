package com.example.loginandregistration

import android.app.Application
import android.util.Log
import com.example.loginandregistration.admin.utils.NotificationChannelManager
import com.example.loginandregistration.firebase.FirebaseManager

class LostFoundApplication : Application() {
    
    companion object {
        private const val TAG = "LostFoundApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Application starting...")
        
        // Initialize Firebase
        FirebaseManager.initialize(this)
        
        // Create notification channels
        NotificationChannelManager.createNotificationChannels(this)
        Log.d(TAG, "Notification channels created")
        
        // Test Firebase connection
        FirebaseManager.testFirestoreConnection { isConnected, message ->
            if (isConnected) {
                Log.i(TAG, "Firebase connection verified: $message")
            } else {
                Log.e(TAG, "Firebase connection failed: $message")
            }
        }
        
        Log.d(TAG, "Application initialized successfully")
    }
}