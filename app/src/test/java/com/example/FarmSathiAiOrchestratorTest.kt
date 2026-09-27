package com.example

import com.example.data.models.Crop
import com.example.data.models.Farm
import com.example.data.models.WeatherData
import com.example.service.ai.AIIntent
import com.example.service.ai.FarmAIContext
import com.example.service.ai.FarmSathiAiOrchestrator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FarmSathiAiOrchestratorTest {

    private lateinit var orchestrator: FarmSathiAiOrchestrator

    @Before
    fun setup() {
        orchestrator = FarmSathiAiOrchestrator()
    }

    @Test
    fun testIntentDetection_Irrigation_EnglishAndHindi() {
        val intentEn = orchestrator.detectIntent("Should I irrigate my soybean farm today?")
        assertEquals(AIIntent.IRRIGATION, intentEn)

        val intentHi = orchestrator.detectIntent("क्या मुझे आज अपनी फसल में सिंचाई करनी चाहिए?")
        assertEquals(AIIntent.IRRIGATION, intentHi)
    }

    @Test
    fun testIntentDetection_Market_EnglishAndHindi() {
        val intentEn = orchestrator.detectIntent("What is the mandi price for wheat in Indore?")
        assertEquals(AIIntent.MARKET_PRICE, intentEn)

        val intentCompare = orchestrator.detectIntent("Which mandi gives the best profit for soybean?")
        assertEquals(AIIntent.MARKET_COMPARISON, intentCompare)

        val intentHi = orchestrator.detectIntent("आज सोयाबीन का मंडी भाव क्या चल रहा है?")
        assertEquals(AIIntent.MARKET_PRICE, intentHi)
    }

    @Test
    fun testIntentDetection_PestAndDisease() {
        val intentEn = orchestrator.detectIntent("Yellow spots on leaves and fungus attack")
        assertEquals(AIIntent.CROP_DISEASE, intentEn)

        val intentPest = orchestrator.detectIntent("Aphid and insect pest on rice crop")
        assertEquals(AIIntent.PEST_ADVISORY, intentPest)
    }

    @Test
    fun testIntentDetection_SoilAndFertilizer() {
        val intentUrea = orchestrator.detectIntent("How much urea and DAP fertilizer for 2 acres?")
        assertEquals(AIIntent.FERTILIZER_ADVISORY, intentUrea)

        val intentPh = orchestrator.detectIntent("My soil pH is 5.5, what should I do?")
        assertEquals(AIIntent.SOIL_ADVISORY, intentPh)
    }

    @Test
    fun testOrchestrator_ProcessQueryWithEnrichedMath() = runBlocking {
        val context = FarmAIContext(
            farmerName = "Ramesh Patel",
            location = "Indore, MP",
            preferredLanguage = "Hindi",
            primaryFarm = Farm("f1", "u1", "Green Valley", "Indore", 4.5, "Black Soil", "Drip", "Main Farm"),
            primaryCrop = Crop("c1", "u1", "f1", "Green Valley", "Soybean", "JS-335", "2026-06-15", "2026-10-15", 4.5, "Good", "Healthy"),
            weather = WeatherData("Indore", 29.5, "Partly Cloudy", 62, 12.0, 0.0, false, 82)
        )

        val result = orchestrator.processQuery("Which mandi gives best profit for soybean?", context)
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response!!.answer.isNotBlank())
    }

    @Test
    fun testDailyBriefingAndHealthScore_Generation() = runBlocking {
        val context = FarmAIContext(
            farmerName = "Ramesh Patel",
            location = "Indore, MP",
            preferredLanguage = "English",
            primaryCrop = Crop("c1", "u1", "f1", "Green Valley", "Soybean", "JS-335", "2026-06-15", "2026-10-15", 4.5, "Good", "Healthy"),
            weather = WeatherData("Indore", 30.0, "Sunny", 55, 10.0, 0.0, false, 85)
        )

        val briefing = orchestrator.getDailyBriefing(context)
        assertTrue(briefing.isSuccess)
        assertNotNull(briefing.getOrNull()?.weatherBrief)
        assertNotNull(briefing.getOrNull()?.cropBrief)

        val score = orchestrator.getFarmHealthScore(context)
        assertTrue(score.isSuccess)
        assertTrue(score.getOrNull()!!.overallScore in 0..100)
    }
}
