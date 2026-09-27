package com.example.data.models

data class Farm(
    val id: String,
    val userId: String,
    val name: String,
    val location: String,
    val latitude: Double = 23.2599,
    val longitude: Double = 77.4126,
    val area: Double = 2.5,
    val areaUnit: String = "Acres", // "Acres", "Hectares", "Bigha"
    val soilType: String = "Black Soil (Regur)",
    val irrigationType: String = "Drip & Canal",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
