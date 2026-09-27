package com.example.service

import com.example.data.models.Crop
import com.example.data.models.IrrigationAdvisory
import com.example.data.models.NpkAdvisory
import com.example.data.models.SoilReading
import com.example.data.models.TelemetryData
import com.example.data.models.WeatherData

object SoilAndTelemetryEngine {

    fun generateNpkAdvisory(
        crop: Crop?,
        soil: SoilReading?
    ): NpkAdvisory {
        val cropName = crop?.cropName ?: "General Crop"
        val n = soil?.nitrogen ?: 120.0
        val p = soil?.phosphorus ?: 22.0
        val k = soil?.potassium ?: 180.0
        val ph = soil?.ph ?: 6.8

        val nStatus = when {
            n < 140.0 -> "Deficient"
            n > 280.0 -> "Excess"
            else -> "Optimal"
        }

        val pStatus = when {
            p < 20.0 -> "Deficient"
            p > 50.0 -> "Excess"
            else -> "Optimal"
        }

        val kStatus = when {
            k < 150.0 -> "Deficient"
            k > 300.0 -> "Excess"
            else -> "Optimal"
        }

        val actions = mutableListOf<String>()
        var dosage = "100 kg Urea + 50 kg DAP / acre"
        val warnings = mutableListOf<String>()

        if (nStatus == "Deficient") {
            actions.add("Apply split dose of Nitrogen (Urea 45% N) during vegetative stage.")
            actions.add("Incorporate neem-coated urea to minimize leaching losses.")
        } else if (nStatus == "Excess") {
            actions.add("Reduce nitrogenous fertilizer application to prevent vegetative overgrowth and lodging risk.")
        }

        if (pStatus == "Deficient") {
            actions.add("Apply Single Super Phosphate (SSP) or DAP directly near root zone.")
        }

        if (kStatus == "Deficient") {
            actions.add("Apply Muriate of Potash (MOP) to enhance drought resistance and grain filling.")
        }

        if (ph < 6.0) {
            warnings.add("Soil pH is acidic (${ph}). Apply agricultural lime to improve nutrient uptake.")
        } else if (ph > 8.2) {
            warnings.add("Soil pH is alkaline (${ph}). Apply gypsum or organic compost to lower salinity.")
        }

        if (actions.isEmpty()) {
            actions.add("Maintain current organic compost top-dressing. Soil NPK levels are balanced.")
            dosage = "Maintenance: 25 kg Neem Cake + Organic Manure per acre"
        }

        warnings.add("Always wear protective gloves when handling chemical fertilizers. Verify with local KVK guidelines.")

        return NpkAdvisory(
            cropName = cropName,
            cropStage = "Vegetative / Flowering Stage",
            nStatus = nStatus,
            pStatus = pStatus,
            kStatus = kStatus,
            recommendedActions = actions,
            fertilizerDosage = dosage,
            confidence = if (soil != null) "High (Soil Lab / Sensor Telemetry)" else "General Model Estimate",
            warnings = warnings
        )
    }

    fun generateIrrigationAdvisory(
        crop: Crop?,
        telemetry: TelemetryData?,
        weather: WeatherData
    ): IrrigationAdvisory {
        val cropName = crop?.cropName ?: "Active Crops"
        val moisture = telemetry?.soilMoisture ?: 24.0
        val temp = telemetry?.airTemperature ?: weather.temperature
        val rainProb = weather.rainProbability

        val factors = listOf(
            "Soil Moisture: ${String.format("%.1f", moisture)}%",
            "Rain Probability: $rainProb%",
            "Ambient Temperature: ${String.format("%.1f", temp)}°C",
            "Crop Growth Stage: Vegetative / Grain Formation"
        )

        val (rec, liters) = when {
            moisture < 25.0 && rainProb < 30 -> Pair(
                "Irrigation Needed Within Next 24 Hours",
                "15,000 - 18,000 Liters / Acre (Drip / Sprinkler recommended)"
            )
            moisture < 30.0 && rainProb < 50 -> Pair(
                "Schedule Irrigation in 48 Hours",
                "10,000 - 12,000 Liters / Acre"
            )
            rainProb >= 60 -> Pair(
                "Postpone Irrigation - Heavy Rain Expected",
                "0 Liters (Natural precipitation expected)"
            )
            else -> Pair(
                "Adequate Soil Moisture - No Irrigation Needed",
                "0 Liters (Monitor moisture levels daily)"
            )
        }

        return IrrigationAdvisory(
            cropName = cropName,
            recommendation = rec,
            soilMoisturePercent = moisture,
            rainProbabilityPercent = rainProb,
            temperatureCelsius = temp,
            factorsConsidered = factors,
            recommendedLitersPerAcre = liters
        )
    }

    fun validateTelemetry(telemetry: TelemetryData): Result<TelemetryData> {
        if (telemetry.airTemperature < -20.0 || telemetry.airTemperature > 65.0) {
            return Result.failure(Exception("Invalid temperature reading: ${telemetry.airTemperature}°C"))
        }
        if (telemetry.soilMoisture < 0.0 || telemetry.soilMoisture > 100.0) {
            return Result.failure(Exception("Invalid soil moisture reading: ${telemetry.soilMoisture}%"))
        }
        if (telemetry.humidity < 0.0 || telemetry.humidity > 100.0) {
            return Result.failure(Exception("Invalid relative humidity: ${telemetry.humidity}%"))
        }
        return Result.success(telemetry)
    }
}
