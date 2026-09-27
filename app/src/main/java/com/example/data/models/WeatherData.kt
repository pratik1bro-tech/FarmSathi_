package com.example.data.models

data class DayForecast(
    val dayName: String,
    val tempMax: Double,
    val tempMin: Double,
    val condition: String,
    val rainProb: Int
)

data class WeatherData(
    val locationName: String = "Madhya Pradesh, IN",
    val temperature: Double = 28.5,
    val condition: String = "Partly Cloudy",
    val humidity: Int = 68,
    val windSpeed: Double = 12.4,
    val rainProbability: Int = 15,
    val forecast: List<DayForecast> = emptyList(),
    val isLive: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
