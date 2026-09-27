package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.models.CropScan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiApiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val DEFAULT_MODEL = "gemini-3.5-flash"
    private const val PRO_MODEL = "gemini-3.1-pro-preview"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    /**
     * AI Text Chat Assistant Call
     */
    suspend fun chatWithFarmSathi(
        userMessage: String,
        farmerContext: String,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    Exception("Gemini API key is not configured in Secrets panel or .env. Please enter a valid key in Secrets panel.")
                )
            }

            val contentsArray = JSONArray()

            // System agricultural context
            val systemInstruction = "You are FarmSathi, an expert, empathetic, and trustworthy AI agricultural assistant. " +
                    "Help farmers with crop diseases, irrigation, soil health, weather impact, fertilizers, and market guidance. " +
                    "Context about the farmer: $farmerContext. " +
                    "Provide practical, clear steps. Keep tone helpful and structured. " +
                    "Format responses with headings, bullet points, and warnings when uncertain."

            // Append history
            for ((sender, text) in chatHistory) {
                val role = if (sender == "user") "user" else "model"
                val item = JSONObject()
                item.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                item.put("parts", parts)
                contentsArray.put(item)
            }

            // Append current prompt
            val userItem = JSONObject()
            userItem.put("role", "user")
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", userMessage))
            userItem.put("parts", userParts)
            contentsArray.put(userItem)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("topP", 0.95)
                })
            }

            val endpoint = "$BASE_URL/$DEFAULT_MODEL:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errObj = try { JSONObject(responseStr).getJSONObject("error").getString("message") } catch (e: Exception) { responseStr }
                return@withContext Result.failure(Exception("AI Error ($responseStr): $errObj"))
            }

            val resJson = JSONObject(responseStr)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val text = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).optString("text")
                return@withContext Result.success(text)
            } else {
                return@withContext Result.failure(Exception("No AI response candidate returned."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Multimodal Image Analysis for Crop Health & Diagnosis
     */
    suspend fun analyzeCropImage(
        bitmap: Bitmap,
        cropName: String,
        userId: String,
        cropId: String
    ): Result<CropScan> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    Exception("Gemini API Key missing. Please configure GEMINI_API_KEY in Secrets panel.")
                )
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val prompt = """
                You are FarmSathi AI Multimodal Crop Doctor.
                Examine this image of $cropName crop.
                Provide an accurate diagnostic analysis in strictly valid JSON format with keys:
                {
                  "possible_issue": "Short name of disease/pest/deficiency or Healthy",
                  "confidence": "High / Medium / Low",
                  "severity": "Low / Medium / High",
                  "observations": ["observation 1", "observation 2"],
                  "recommended_actions": ["action 1", "action 2"],
                  "warnings": ["warning or pesticide safety precaution"],
                  "needs_expert": true or false
                }
                If image quality is unreadable or blurry, specify "Insufficient Image Quality" in possible_issue and set needs_expert to true.
            """.trimIndent()

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            partsArray.put(JSONObject().put("inlineData", JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Image)
            }))

            val contentItem = JSONObject().apply {
                put("parts", partsArray)
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().put(contentItem))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val endpoint = "$BASE_URL/$DEFAULT_MODEL:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Vision Analysis Failed: ${response.code} $responseStr"))
            }

            val resJson = JSONObject(responseStr)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No AI result candidate received."))
            }

            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            
            // Parse structured JSON
            val cleanJson = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val structured = JSONObject(cleanJson)

            val issue = structured.optString("possible_issue", "Unknown Issue")
            val conf = structured.optString("confidence", "Medium")
            val sev = structured.optString("severity", "Medium")
            val expert = structured.optBoolean("needs_expert", false)

            val obsList = mutableListOf<String>()
            val obsArray = structured.optJSONArray("observations")
            if (obsArray != null) {
                for (i in 0 until obsArray.length()) obsList.add(obsArray.getString(i))
            }

            val actList = mutableListOf<String>()
            val actArray = structured.optJSONArray("recommended_actions")
            if (actArray != null) {
                for (i in 0 until actArray.length()) actList.add(actArray.getString(i))
            }

            val warnList = mutableListOf<String>()
            val warnArray = structured.optJSONArray("warnings")
            if (warnArray != null) {
                for (i in 0 until warnArray.length()) warnList.add(warnArray.getString(i))
            }

            val scan = CropScan(
                id = "scan_${System.currentTimeMillis()}",
                userId = userId,
                cropId = cropId,
                cropName = cropName,
                imagePathOrBase64 = base64Image,
                possibleIssue = issue,
                severity = sev,
                confidence = conf,
                observations = obsList.ifEmpty { listOf("Visual leaf examination performed.") },
                recommendedActions = actList.ifEmpty { listOf("Monitor crop regularly and maintain optimal soil moisture.") },
                warnings = warnList.ifEmpty { listOf("Verify pesticide dosage with local Krishi Vigyan Kendra (KVK) officer.") },
                needsExpert = expert,
                timestamp = System.currentTimeMillis()
            )

            Result.success(scan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
