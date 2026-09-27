package com.example.data.models

data class MandiPrice(
    val id: String,
    val commodity: String, // e.g. Soybean, Wheat, Gram, Cotton
    val variety: String = "Standard / FAQ",
    val mandiName: String, // e.g. Indore Mandi, Ujjain Mandi, Dewas Mandi
    val district: String = "Indore",
    val state: String = "Madhya Pradesh",
    val minPrice: Double, // ₹/quintal
    val maxPrice: Double, // ₹/quintal
    val modalPrice: Double, // ₹/quintal
    val priceChange: Double = 50.0, // ₹ change from yesterday
    val arrivalQuantityQuintals: Double = 1250.0,
    val trend: String = "Increasing", // "Increasing", "Stable", "Decreasing"
    val latitude: Double = 22.7196,
    val longitude: Double = 75.8577,
    val dataSource: String = "LIVE_API", // "LIVE_API", "OFFLINE_CACHED", "MANUAL", "DEMO"
    val lastUpdated: Long = System.currentTimeMillis()
)

data class PriceForecast(
    val commodity: String,
    val mandiName: String,
    val currentPrice: Double,
    val forecast7DayMin: Double,
    val forecast7DayMax: Double,
    val forecast30DayMin: Double,
    val forecast30DayMax: Double,
    val trend: String, // "Moderately Increasing", "High Demand Expected", "Stable"
    val confidence: String = "High (Prophet Time-Series Engine)",
    val keyDrivers: List<String> = listOf("High export demand", "Favorable harvest estimates", "Low mandi arrivals")
)
