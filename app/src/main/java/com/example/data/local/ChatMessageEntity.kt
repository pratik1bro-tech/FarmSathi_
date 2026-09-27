package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sender: String,
    val text: String,
    val timestamp: Long
) {
    fun toDomain() = ChatMessage(
        id = id,
        userId = userId,
        sender = sender,
        text = text,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(msg: ChatMessage) = ChatMessageEntity(
            id = msg.id,
            userId = msg.userId,
            sender = msg.sender,
            text = msg.text,
            timestamp = msg.timestamp
        )
    }
}
