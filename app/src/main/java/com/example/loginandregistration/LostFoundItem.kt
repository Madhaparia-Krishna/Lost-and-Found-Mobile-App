package com.example.loginandregistration

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class LostFoundItem(

    var id: String = "", // Important: To hold the Firestore document ID for updates
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    val isLost: Boolean = false,
    var status: String = "Pending", // Add this field with a default value
    val userId: String = "",
    val userEmail: String = "",
    val timestamp: Timestamp? = null
)
