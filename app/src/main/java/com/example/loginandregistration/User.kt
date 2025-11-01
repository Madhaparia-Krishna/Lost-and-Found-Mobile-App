package com.example.loginandregistration

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val fcmToken: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) : Serializable {
    // Constructor for compatibility with Firestore
    constructor() : this("", "", "", "", "", "", Timestamp.now(), Timestamp.now())
}
