package com.example.data.models

data class ChatMessage(
    val id: String,
    val userId: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
