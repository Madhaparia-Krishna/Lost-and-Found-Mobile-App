package com.example.loginandregistration

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class LostFoundItem(
    var id: String = "", // Important: To hold the Firestore document ID for updates
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",

    // Map Firestore "lost found" field to isLost property
    @get:PropertyName("lost found")
    @set:PropertyName("lost found")
    var isLost: Boolean = true, // true for lost, false for found

    var status: String = "Pending", // Add this field with a default value
    val userId: String = "",
    val userEmail: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now()
) : Serializable
