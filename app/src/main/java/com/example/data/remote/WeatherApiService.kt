package com.example.data.remote

import com.example.data.models.DayForecast
import com.example.data.models.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WeatherApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLiveWeather(
        lat: Double = 23.2599,
        lng: Double = 77.4126,
        locationName: String = "Madhya Pradesh, India"
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lng" +
                    "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                    "&timezone=auto"

            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""

            if (!response.isSuccessful || responseStr.isBlank()) {
                return@withContext Result.failure(Exception("Weather API HTTP Error ${response.code}"))
            }

            val json = JSONObject(responseStr)
            val current = json.getJSONObject("current")
            val temp = current.optDouble("temperature_2m", 28.0)
            val humidity = current.optInt("relative_humidity_2m", 65)
            val windSpeed = current.optDouble("wind_speed_10m", 11.2)
            val weatherCode = current.optInt("weather_code", 0)

            val condition = mapWeatherCode(weatherCode)

            // Daily forecast parsing
            val dailyList = mutableListOf<DayForecast>()
            val daily = json.optJSONObject("daily")
            if (daily != null) {
                val times = daily.optJSONArray("time")
                val maxTemps = daily.optJSONArray("temperature_2m_max")
                val minTemps = daily.optJSONArray("temperature_2m_min")
                val codes = daily.optJSONArray("weather_code")
                val rainProbs = daily.optJSONArray("precipitation_probability_max")

                val count = times?.length() ?: 0
                for (i in 0 until minOf(count, 5)) {
                    val dateStr = times?.optString(i) ?: "Day ${i + 1}"
                    val maxT = maxTemps?.optDouble(i) ?: (temp + 2)
                    val minT = minTemps?.optDouble(i) ?: (temp - 5)
                    val wCode = codes?.optInt(i) ?: 0
                    val rainProb = rainProbs?.optInt(i) ?: 10

                    dailyList.add(
                        DayForecast(
                            dayName = formatDayName(dateStr, i),
                            tempMax = maxT,
                            tempMin = minT,
                            condition = mapWeatherCode(wCode),
                            rainProb = rainProb
                        )
                    )
                }
            }

            val weatherData = WeatherData(
                locationName = locationName,
                temperature = temp,
                condition = condition,
                humidity = humidity,
                windSpeed = windSpeed,
                rainProbability = dailyList.firstOrNull()?.rainProb ?: 15,
                forecast = dailyList,
                isLive = true,
                lastUpdated = System.currentTimeMillis()
            )

            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatDayName(dateStr: String, index: Int): String {
        if (index == 0) return "Today"
        if (index == 1) return "Tomorrow"
        return dateStr.takeLast(5)
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky ☀️"
            1, 2, 3 -> "Partly Cloudy ⛅"
            45, 48 -> "Foggy 🌫️"
            51, 53, 55 -> "Light Drizzle 🌧️"
            61, 63, 65 -> "Rain 🌧️"
            80, 81, 82 -> "Showers ⛈️"
            95, 96, 99 -> "Thunderstorm 🌩️"
            else -> "Overcast ☁️"
        }
    }
}
