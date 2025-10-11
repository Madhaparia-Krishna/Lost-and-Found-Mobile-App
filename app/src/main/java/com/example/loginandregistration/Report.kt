package com.example.loginandregistration

/**
 * This is the blueprint for a Report object in your app.
 * The property names (itemName, reportedBy, etc.) MUST EXACTLY MATCH
 * the field names in your Firestore documents.
 */
data class Report(
    val itemName: String = "",
    val reportedBy: String = "",
    val status: String = ""
    // Add any other fields from your Firebase documents here
)
