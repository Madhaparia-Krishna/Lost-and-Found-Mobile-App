package com.example.loginandregistration

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.io.Serializable
import java.util.*

data class LostFoundItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    
    // Map Firestore "lost found" field to isLost property
    @get:PropertyName("lost found")
    @set:PropertyName("lost found")
    var isLost: Boolean = true, // true for lost, false for found
    
    val userId: String = "",
    val userEmail: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now()
) : Serializable {
    // Constructor for compatibility with Firestore
    constructor() : this("", "", "", "", "", true, "", "", "", Timestamp.now())
}