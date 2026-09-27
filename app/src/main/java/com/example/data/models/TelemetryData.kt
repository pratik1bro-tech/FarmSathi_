package com.example.data.models

data class TelemetryData(
    val id: String,
    val userId: String,
    val farmId: String,
    val deviceId: String = "ESP32_AGRI_NODE_01",
    val airTemperature: Double, // °C
    val humidity: Double, // %
    val soilMoisture: Double, // %
    val soilTemperature: Double, // °C
    val rainfall: Double = 0.0, // mm
    val windSpeed: Double = 10.0, // km/h
    val lightIntensity: Double = 35000.0, // Lux
    val leafWetness: Double = 15.0, // %
    val dataSource: String = "IoT Sensor", // "IoT Sensor", "Manual Entry", "Cloud Telemetry"
    val timestamp: Long = System.currentTimeMillis()
)
