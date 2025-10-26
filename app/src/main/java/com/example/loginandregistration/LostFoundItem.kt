package com.example.loginandregistration

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class LostFoundItem(
    var id: String = "", // To hold the Firestore document ID for updates
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val imageUrl: String = "",
    var status: String = "Pending",
    val timestamp: Timestamp = Timestamp.now(),

    // --- THIS IS THE FIX ---
    // Only one 'isLost' property is kept, which correctly maps to the
    // "lost found" field in your Firestore database.
    @get:PropertyName("lost found")
    @set:PropertyName("lost found")
    var isLost: Boolean = true // true for "Lost", false for "Found"

) : Serializable {
    // A no-argument constructor is required by Firebase for deserialization
    constructor() : this(
        id = "",
        name = "",
        description = "",
        location = "",
        contactInfo = "",
        userId = "",
        userEmail = "",
        imageUrl = "",
        status = "Pending",
        timestamp = Timestamp.now(),
        isLost = true
    )
}
