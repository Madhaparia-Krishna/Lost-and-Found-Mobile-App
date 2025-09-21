package com.example.loginandregistration

import com.google.firebase.Timestamp
import java.util.*

data class LostFoundItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    val isLost: Boolean = true, // true for lost, false for found
    val userId: String = "",
    val userEmail: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    // Constructor for compatibility with Firestore
    constructor() : this("", "", "", "", "", true, "", "", "", Timestamp.now())
}