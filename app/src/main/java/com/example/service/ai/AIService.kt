package com.example.service.ai

import android.graphics.Bitmap
import com.example.data.models.CropScan

/**
 * Central AI Service interface for FarmSathi.
 * Encapsulates all LLM intelligence operations (reasoning, advice generation,
 * multimodal diagnosis, daily briefing, and transparent farm health scoring).
 */
interface AIService {

    suspend fun generateResponse(request: AIRequest): Result<AIResponse>

    suspend fun chatWithFarmSathi(
        userMessage: String,
        context: FarmAIContext,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): Result<AIResponse>

    suspend fun analyzeCropImage(
        bitmap: Bitmap,
        cropName: String,
        userId: String,
        cropId: String
    ): Result<CropScan>

    suspend fun generateFarmBriefing(context: FarmAIContext): Result<FarmBriefing>

    suspend fun generateFarmHealthScore(context: FarmAIContext): Result<FarmHealthScore>
}
