package com.example.data.models

data class CropScan(
    val id: String,
    val userId: String,
    val cropId: String,
    val cropName: String,
    val imagePathOrBase64: String,
    val possibleIssue: String,
    val severity: String, // "Low", "Medium", "High"
    val confidence: String,
    val observations: List<String>,
    val recommendedActions: List<String>,
    val warnings: List<String>,
    val needsExpert: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
