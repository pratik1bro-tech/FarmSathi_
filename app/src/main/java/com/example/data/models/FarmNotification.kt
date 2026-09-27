package com.example.data.models

data class FarmNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String = "info", // "weather", "scan", "crop", "market", "farm", "info"
    val destination: String = "home", // "weather", "cropDoctor", "market", "farmDetail", "notifications"
    val relatedFarmId: String? = null,
    val relatedCropId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

