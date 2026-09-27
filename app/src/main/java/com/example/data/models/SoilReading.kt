package com.example.data.models

data class SoilReading(
    val id: String,
    val userId: String,
    val farmId: String,
    val farmName: String,
    val ph: Double,
    val nitrogen: Double, // kg/ha
    val phosphorus: Double, // kg/ha
    val potassium: Double, // kg/ha
    val soilMoisture: Double, // %
    val soilTemperature: Double, // °C
    val electricalConductivity: Double = 0.8, // dS/m
    val organicMatter: Double = 1.2, // %
    val soilType: String = "Black Cotton Soil",
    val dataSource: String = "Manual Entry", // "Manual Entry", "IoT Sensor", "Cloud Telemetry"
    val timestamp: Long = System.currentTimeMillis()
)
