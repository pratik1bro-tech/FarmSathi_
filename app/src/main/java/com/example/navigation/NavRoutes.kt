package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    
    object Home : Screen("home")
    object Farms : Screen("farms")
    object FarmDetails : Screen("farm_details/{farmId}") {
        fun createRoute(farmId: String) = "farm_details/$farmId"
    }
    object Crops : Screen("crops")
    object CropDetails : Screen("crop_details/{cropId}") {
        fun createRoute(cropId: String) = "crop_details/$cropId"
    }
    object CropScanner : Screen("crop_scanner")
    object ScanHistory : Screen("scan_history")
    object Weather : Screen("weather")
    
    object Market : Screen("market")
    object MandiDetails : Screen("mandi_details/{mandiId}") {
        fun createRoute(mandiId: String) = "mandi_details/$mandiId"
    }
    object BuyerDetails : Screen("buyer_details/{buyerId}") {
        fun createRoute(buyerId: String) = "buyer_details/$buyerId"
    }
    
    object AIHub : Screen("ai_hub")
    object AIChat : Screen("ai_chat")
    object VoiceAssistant : Screen("voice_assistant")
    
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}
