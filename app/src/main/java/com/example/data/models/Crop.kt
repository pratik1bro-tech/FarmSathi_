package com.example.data.models

data class Crop(
    val id: String,
    val userId: String,
    val farmId: String,
    val farmName: String,
    val cropName: String,
    val variety: String = "Hybrid High-Yield",
    val sowingDate: String = "2026-06-15",
    val harvestDate: String = "2026-10-20",
    val area: Double = 1.5,
    val soilType: String = "Black Clay Soil",
    val irrigation: String = "Drip Irrigation",
    val notes: String = "",
    val healthStatus: String = "Healthy" // "Healthy", "Needs Attention", "Critical"
)
