package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.Crop

@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val farmId: String,
    val farmName: String,
    val cropName: String,
    val variety: String,
    val sowingDate: String,
    val harvestDate: String,
    val area: Double,
    val soilType: String,
    val irrigation: String,
    val notes: String,
    val healthStatus: String
) {
    fun toDomain() = Crop(
        id = id,
        userId = userId,
        farmId = farmId,
        farmName = farmName,
        cropName = cropName,
        variety = variety,
        sowingDate = sowingDate,
        harvestDate = harvestDate,
        area = area,
        soilType = soilType,
        irrigation = irrigation,
        notes = notes,
        healthStatus = healthStatus
    )

    companion object {
        fun fromDomain(crop: Crop) = CropEntity(
            id = crop.id,
            userId = crop.userId,
            farmId = crop.farmId,
            farmName = crop.farmName,
            cropName = crop.cropName,
            variety = crop.variety,
            sowingDate = crop.sowingDate,
            harvestDate = crop.harvestDate,
            area = crop.area,
            soilType = crop.soilType,
            irrigation = crop.irrigation,
            notes = crop.notes,
            healthStatus = crop.healthStatus
        )
    }
}
