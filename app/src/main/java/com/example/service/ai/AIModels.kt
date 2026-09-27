package com.example.service.ai

import com.example.data.models.*

data class FarmAIContext(
    val farmerName: String,
    val location: String,
    val preferredLanguage: String = "English",
    val farmingExperience: String = "Medium",
    val primaryFarm: Farm? = null,
    val primaryCrop: Crop? = null,
    val allFarms: List<Farm> = emptyList(),
    val allCrops: List<Crop> = emptyList(),
    val weather: WeatherData = WeatherData(),
    val isLiveWeather: Boolean = false,
    val latestSoil: SoilReading? = null,
    val latestTelemetry: TelemetryData? = null,
    val isSensorLive: Boolean = false,
    val relevantMandiPrices: List<MandiPrice> = emptyList(),
    val isLiveMarketData: Boolean = false,
    val latestCropScan: CropScan? = null,
    val calculatedNetReturn: Double? = null,
    val estimatedGrossRevenue: Double? = null,
    val estimatedTransportCost: Double? = null,
    val bestMandiName: String? = null,
    val bestMandiDistanceKm: Double? = null
)

data class AIRequest(
    val query: String,
    val intent: AIIntent = AIIntent.GENERAL_FARMING,
    val context: FarmAIContext,
    val chatHistory: List<Pair<String, String>> = emptyList()
)

data class AIResponse(
    val intent: AIIntent,
    val answer: String,
    val summary: String,
    val recommendation: String,
    val reasoning: String,
    val actions: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val confidence: String = "High",
    val sources: List<String> = emptyList(),
    val requiresExpert: Boolean = false,
    val isEstimate: Boolean = false,
    val calculatedNetReturn: Double? = null,
    val rawText: String? = null
)

data class FarmBriefing(
    val weatherBrief: String,
    val cropBrief: String,
    val irrigationBrief: String,
    val marketBrief: String,
    val urgentAlert: String? = null,
    val summaryText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FarmHealthScore(
    val overallScore: Int, // 0 - 100
    val cropHealthScore: Int, // 0 - 100
    val soilHealthScore: Int, // 0 - 100
    val weatherRiskScore: Int, // 0 - 100 (100 = low risk/favorable)
    val marketOpportunityScore: Int, // 0 - 100
    val statusLabel: String, // "Optimal", "Good", "Needs Attention", "Critical"
    val summaryExplanation: String,
    val factors: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
