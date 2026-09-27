package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.FarmNotification

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val destination: String = "home",
    val relatedFarmId: String? = null,
    val relatedCropId: String? = null,
    val timestamp: Long,
    val isRead: Boolean
) {
    fun toDomain() = FarmNotification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type,
        destination = destination,
        relatedFarmId = relatedFarmId,
        relatedCropId = relatedCropId,
        timestamp = timestamp,
        isRead = isRead
    )

    companion object {
        fun fromDomain(n: FarmNotification) = NotificationEntity(
            id = n.id,
            userId = n.userId,
            title = n.title,
            message = n.message,
            type = n.type,
            destination = n.destination,
            relatedFarmId = n.relatedFarmId,
            relatedCropId = n.relatedCropId,
            timestamp = n.timestamp,
            isRead = n.isRead
        )
    }
}
