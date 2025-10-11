package com.example.loginandregistration.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    fun initialize(context: Context) {
        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Firebase initialized successfully")
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }
    
    fun isFirebaseConnected(): Boolean {
        return try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            false
        }
    }
    
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    
    fun getFirestore() = FirebaseFirestore.getInstance()
    
    fun getStorage() = FirebaseStorage.getInstance()
    
    fun testFirestoreConnection(callback: (Boolean, String?) -> Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Simple test - just try to get Firestore instance
            Log.d(TAG, "Testing Firestore connection...")
            
            // Test with a simple operation
            firestore.collection("items")
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d(TAG, "Firestore connection test successful - found ${querySnapshot.size()} items")
                    callback(true, "Connected to Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Firestore connection test failed", e)
                    // Still consider it successful if it's just a permission error
                    if (e.message?.contains("permission") == true) {
                        callback(true, "Connected to Firestore (permission check needed)")
                    } else {
                        callback(false, "Failed to connect to Firestore: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error testing Firestore connection", e)
            callback(false, "Error testing connection: ${e.message}")
        }
    }
}