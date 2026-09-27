package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.Farm

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val area: Double,
    val areaUnit: String,
    val soilType: String,
    val irrigationType: String,
    val notes: String,
    val createdAt: Long
) {
    fun toDomain() = Farm(
        id = id,
        userId = userId,
        name = name,
        location = location,
        latitude = latitude,
        longitude = longitude,
        area = area,
        areaUnit = areaUnit,
        soilType = soilType,
        irrigationType = irrigationType,
        notes = notes,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(farm: Farm) = FarmEntity(
            id = farm.id,
            userId = farm.userId,
            name = farm.name,
            location = farm.location,
            latitude = farm.latitude,
            longitude = farm.longitude,
            area = farm.area,
            areaUnit = farm.areaUnit,
            soilType = farm.soilType,
            irrigationType = farm.irrigationType,
            notes = farm.notes,
            createdAt = farm.createdAt
        )
    }
}
