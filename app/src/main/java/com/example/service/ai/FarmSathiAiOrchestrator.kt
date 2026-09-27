package com.example.service.ai

import com.example.service.MandiMarketEngine

/**
 * FarmSathi AI Orchestrator.
 * Central coordinator responsible for:
 * 1. Intent Detection (English, Hindi, Hinglish)
 * 2. Intent-driven contextual data retrieval (only fetching relevant data)
 * 3. Verified mathematical calculations (e.g. Net Profit, Logistics Cost)
 * 4. LLM reasoning dispatch and response structuring
 */
class FarmSathiAiOrchestrator(
    private val aiService: AIService = GeminiAIService()
) {

    private var cachedBriefing: Pair<Long, FarmBriefing>? = null
    private var cachedHealthScore: Pair<Long, FarmHealthScore>? = null
    private val cacheTtlMillis = 5 * 60 * 1000L // 5 minutes cache

    /**
     * Detects agricultural intent from user text query.
     */
    fun detectIntent(query: String): AIIntent {
        val q = query.lowercase().trim()

        return when {
            // Market / Mandi / Price / Selling / Buyer
            q.contains("mandi") || q.contains("price") || q.contains("bhav") || q.contains("भाव") ||
                    q.contains("मंडी") || q.contains("rate") || q.contains("दाम") || q.contains("कीमत") -> {
                if (q.contains("compare") || q.contains("best") || q.contains("which") || q.contains("profit") ||
                    q.contains("कहाँ") || q.contains("तुलना") || q.contains("ज्यादा") || q.contains("net return")) {
                    AIIntent.MARKET_COMPARISON
                } else if (q.contains("sell") || q.contains("buyer") || q.contains("बेचना") || q.contains("खरीदार")) {
                    AIIntent.SELLING_ADVISORY
                } else {
                    AIIntent.MARKET_PRICE
                }
            }

            // Irrigation / Watering
            q.contains("irrigate") || q.contains("irrigation") || q.contains("water") || q.contains("पानी") ||
                    q.contains("सिंचाई") || q.contains("sinchai") || q.contains("drip") || q.contains("sprinkler") -> {
                AIIntent.IRRIGATION
            }

            // Weather / Rain / Temperature
            q.contains("weather") || q.contains("rain") || q.contains("barish") || q.contains("मौसम") ||
                    q.contains("बारिश") || q.contains("तापमान") || q.contains("temp") || q.contains("humidity") -> {
                AIIntent.WEATHER
            }

            // Disease / Pest / Symptoms / Leaf
            q.contains("disease") || q.contains("pest") || q.contains("insect") || q.contains("leaf") ||
                    q.contains("yellow") || q.contains("spots") || q.contains("कीड़ा") || q.contains("रोग") ||
                    q.contains("पत्ते") || q.contains("पीले") || q.contains("फफूंद") || q.contains("fungus") -> {
                if (q.contains("pest") || q.contains("insect") || q.contains("कीड़ा") || q.contains("aphid")) {
                    AIIntent.PEST_ADVISORY
                } else {
                    AIIntent.CROP_DISEASE
                }
            }

            // Fertilizer / NPK / Soil
            q.contains("fertilizer") || q.contains("urea") || q.contains("dap") || q.contains("npk") ||
                    q.contains("खाद") || q.contains("यूरिया") || q.contains("पोषक") -> {
                AIIntent.FERTILIZER_ADVISORY
            }

            q.contains("soil") || q.contains("ph") || q.contains("मिट्टी") || q.contains("matti") ||
                    q.contains("nam") || q.contains("moisture") -> {
                AIIntent.SOIL_ADVISORY
            }

            // Crop Planning / Sowing
            q.contains("plant") || q.contains("sow") || q.contains("variety") || q.contains("season") ||
                    q.contains("बोना") || q.contains("फसल") || q.contains("बीज") || q.contains("seed") -> {
                AIIntent.FARM_PLANNING
            }

            // Farm Analysis / Health Score
            q.contains("score") || q.contains("overview") || q.contains("summary") || q.contains("analysis") ||
                    q.contains("हाल") || q.contains("स्थिति") || q.contains("health") -> {
                AIIntent.FARM_ANALYSIS
            }

            else -> AIIntent.GENERAL_FARMING
        }
    }

    /**
     * Process query through orchestrator with rich contextual enrichment.
     */
    suspend fun processQuery(
        query: String,
        context: FarmAIContext,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): Result<AIResponse> {
        val intent = detectIntent(query)
        val enrichedContext = enrichContextForIntent(context, intent)
        val request = AIRequest(
            query = query,
            intent = intent,
            context = enrichedContext,
            chatHistory = chatHistory
        )
        return aiService.generateResponse(request)
    }

    /**
     * Enriches context based on intent, performing verified math calculations.
     */
    private fun enrichContextForIntent(context: FarmAIContext, intent: AIIntent): FarmAIContext {
        val cropName = context.primaryCrop?.cropName ?: "Soybean"
        val regionalPrices = if (context.relevantMandiPrices.isEmpty()) {
            MandiMarketEngine.getRegionalMandiPrices(cropName)
        } else context.relevantMandiPrices

        return when (intent) {
            AIIntent.MARKET_PRICE, AIIntent.MARKET_COMPARISON, AIIntent.SELLING_ADVISORY -> {
                // Calculate verified math
                val quantityQtl = 30.0 // Standard demonstration batch
                val bestMandi = regionalPrices.maxByOrNull { it.modalPrice }
                val mandiPrice = bestMandi?.modalPrice ?: 4980.0
                val distanceKm = 18.0
                val grossRevenue = mandiPrice * quantityQtl
                val transportCost = MandiMarketEngine.calculateLogisticsCost("Tractor Trolley", distanceKm, quantityQtl)
                val netReturn = grossRevenue - transportCost

                context.copy(
                    relevantMandiPrices = regionalPrices,
                    calculatedNetReturn = netReturn,
                    estimatedGrossRevenue = grossRevenue,
                    estimatedTransportCost = transportCost,
                    bestMandiName = bestMandi?.mandiName ?: "Indore Mandi",
                    bestMandiDistanceKm = distanceKm
                )
            }
            else -> {
                context.copy(relevantMandiPrices = regionalPrices)
            }
        }
    }

    /**
     * Retrieves or generates daily farm briefing with caching.
     */
    suspend fun getDailyBriefing(context: FarmAIContext): Result<FarmBriefing> {
        val now = System.currentTimeMillis()
        val cached = cachedBriefing
        if (cached != null && (now - cached.first) < cacheTtlMillis) {
            return Result.success(cached.second)
        }

        val res = aiService.generateFarmBriefing(context)
        res.onSuccess { briefing ->
            cachedBriefing = Pair(now, briefing)
        }
        return res
    }

    /**
     * Retrieves or calculates farm health score with caching.
     */
    suspend fun getFarmHealthScore(context: FarmAIContext): Result<FarmHealthScore> {
        val now = System.currentTimeMillis()
        val cached = cachedHealthScore
        if (cached != null && (now - cached.first) < cacheTtlMillis) {
            return Result.success(cached.second)
        }

        val res = aiService.generateFarmHealthScore(context)
        res.onSuccess { score ->
            cachedHealthScore = Pair(now, score)
        }
        return res
    }
}
