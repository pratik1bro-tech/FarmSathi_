package com.example.data.models

data class YieldForecast(
    val id: String,
    val userId: String,
    val cropId: String,
    val cropName: String,
    val variety: String,
    val farmAreaAcres: Double,
    val sowingDate: String,
    val estimatedYieldMinPerAcre: Double, // quintals/acre
    val estimatedYieldMaxPerAcre: Double, // quintals/acre
    val estimatedTotalMinQuintals: Double,
    val estimatedTotalMaxQuintals: Double,
    val harvestWindowStart: String,
    val harvestWindowEnd: String,
    val daysRemainingToHarvest: Int,
    val confidence: String = "Medium-High",
    val influencingFactors: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class NpkAdvisory(
    val cropName: String,
    val cropStage: String,
    val nStatus: String, // "Optimal", "Deficient", "Excess"
    val pStatus: String,
    val kStatus: String,
    val recommendedActions: List<String>,
    val fertilizerDosage: String,
    val confidence: String,
    val warnings: List<String>
)

data class IrrigationAdvisory(
    val cropName: String,
    val recommendation: String, // "Irrigation Recommended Within 24h", "Adequate Moisture - No Irrigation Needed"
    val soilMoisturePercent: Double,
    val rainProbabilityPercent: Int,
    val temperatureCelsius: Double,
    val factorsConsidered: List<String>,
    val recommendedLitersPerAcre: String
)

data class SyncQueueItem(
    val id: String,
    val userId: String,
    val actionType: String, // "ADD_SOIL", "ADD_TELEMETRY", "CREATE_SELLING", "BOOK_LOGISTICS"
    val payloadSummary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
