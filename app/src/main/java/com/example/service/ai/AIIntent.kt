package com.example.service.ai

enum class AIIntent(val displayName: String, val category: String) {
    GENERAL_FARMING("General Farming", "General"),
    CROP_ADVISORY("Crop Advisory", "Crops"),
    CROP_DISEASE("Crop Disease & Pest", "Health"),
    SOIL_ADVISORY("Soil Health & NPK", "Soil"),
    IRRIGATION("Irrigation Advisory", "Water"),
    WEATHER("Weather Impact", "Weather"),
    MARKET_PRICE("Mandi Price Inquiry", "Market"),
    MARKET_COMPARISON("Market Comparison & Net Profit", "Market"),
    SELLING_ADVISORY("Selling & Buyer Advisory", "Market"),
    FERTILIZER_ADVISORY("Fertilizer Dosage & Nutrition", "Soil"),
    PEST_ADVISORY("Pest Management & Protection", "Health"),
    FARM_ANALYSIS("Farm Health Analysis", "Farm"),
    FARM_PLANNING("Crop Planning & Sowing", "Planning"),
    VOICE_QUERY("Voice Assistant Query", "Voice")
}
