package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.OnboardingPreferences
import com.example.ui.components.FarmSathiBottomBar
import com.example.ui.components.ScreenTab
import com.example.ui.screens.*
import com.example.ui.theme.FarmSathiTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private var initialDestination: String? = null
    private var initialCropId: String? = null
    private var initialFarmId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        parseIntentExtras(intent)

        setContent {
            FarmSathiTheme {
                val viewModel: MainViewModel = viewModel()

                val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

                val farms by viewModel.farms.collectAsStateWithLifecycle()
                val crops by viewModel.crops.collectAsStateWithLifecycle()
                val scans by viewModel.scans.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()

                val weatherData by viewModel.weatherData.collectAsStateWithLifecycle()
                val isWeatherLoading by viewModel.weatherLoading.collectAsStateWithLifecycle()
                val weatherError by viewModel.weatherError.collectAsStateWithLifecycle()

                val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
                val scanError by viewModel.scanError.collectAsStateWithLifecycle()
                val currentScanResult by viewModel.currentScanResult.collectAsStateWithLifecycle()

                val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
                val chatError by viewModel.chatError.collectAsStateWithLifecycle()

                val context = applicationContext
                val onboardingCompleted = remember { OnboardingPreferences.isOnboardingCompleted(context) }

                var currentScreenState by remember {
                    mutableStateOf(
                        if (!onboardingCompleted) "SPLASH"
                        else if (!isAuthenticated) "AUTH"
                        else "MAIN"
                    )
                }

                var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
                var showNotificationsScreen by remember { mutableStateOf(false) }

                // Handle initial notification intent destination once in MAIN
                LaunchedEffect(currentScreenState, initialDestination) {
                    if (currentScreenState == "MAIN" && initialDestination != null) {
                        when (initialDestination) {
                            "weather" -> currentTab = ScreenTab.HOME
                            "cropDoctor", "scanner" -> currentTab = ScreenTab.AI
                            "market" -> currentTab = ScreenTab.MARKET
                            "farmDetail" -> currentTab = ScreenTab.FARMS
                            "notifications" -> showNotificationsScreen = true
                        }
                        initialDestination = null // consume once
                    }
                }

                // Back handler for notifications screen
                BackHandler(enabled = showNotificationsScreen) {
                    showNotificationsScreen = false
                }

                when (currentScreenState) {
                    "SPLASH" -> {
                        SplashScreen(
                            onSplashFinished = {
                                val completed = OnboardingPreferences.isOnboardingCompleted(context)
                                currentScreenState = if (!completed) "ONBOARDING"
                                else if (!isAuthenticated) "AUTH"
                                else "MAIN"
                            }
                        )
                    }

                    "ONBOARDING" -> {
                        OnboardingScreen(
                            onOnboardingCompleted = {
                                OnboardingPreferences.setOnboardingCompleted(context, true)
                                currentScreenState = if (isAuthenticated) "MAIN" else "AUTH"
                            }
                        )
                    }

                    "AUTH" -> {
                        val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
                        val authError by viewModel.authError.collectAsStateWithLifecycle()

                        LaunchedEffect(isAuthenticated) {
                            if (isAuthenticated) {
                                currentScreenState = "MAIN"
                            }
                        }

                        AuthScreen(
                            isLoading = authLoading,
                            errorMessage = authError,
                            onLogin = { email, pass ->
                                viewModel.login(email, pass)
                            },
                            onSignup = { name, email, pass, phone, lang, loc, exp ->
                                viewModel.signup(name, email, pass, phone, lang, loc, exp)
                            },
                            onForgotPassword = { email ->
                                viewModel.resetPassword(email)
                            }
                        )
                    }

                    "MAIN" -> {
                        val user = currentUser
                        if (user == null || !isAuthenticated) {
                            LaunchedEffect(Unit) {
                                currentScreenState = "AUTH"
                            }
                        } else {
                            Scaffold(
                                bottomBar = {
                                    if (!showNotificationsScreen) {
                                        FarmSathiBottomBar(
                                            currentTab = currentTab,
                                            onTabSelected = { tab ->
                                                showNotificationsScreen = false
                                                currentTab = tab
                                            }
                                        )
                                    }
                                }
                            ) { padding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding)
                                ) {
                                    if (showNotificationsScreen) {
                                        NotificationsScreen(
                                            notifications = notifications,
                                            onMarkAllRead = { viewModel.markNotificationsRead() },
                                            onDeleteNotification = { viewModel.deleteNotification(it) },
                                            onNotificationClick = { notif ->
                                                showNotificationsScreen = false
                                                when (notif.destination) {
                                                    "weather" -> currentTab = ScreenTab.HOME
                                                    "cropDoctor", "scanner" -> currentTab = ScreenTab.AI
                                                    "market" -> currentTab = ScreenTab.MARKET
                                                    "farmDetail" -> currentTab = ScreenTab.FARMS
                                                    else -> {}
                                                }
                                            }
                                        )
                                    } else {
                                        when (currentTab) {
                                            ScreenTab.HOME -> {
                                                HomeScreen(
                                                    user = user,
                                                    farms = farms,
                                                    crops = crops,
                                                    scans = scans,
                                                    notifications = notifications,
                                                    weather = weatherData,
                                                    isWeatherLoading = isWeatherLoading,
                                                    onRefreshWeather = { viewModel.refreshWeather() },
                                                    onNavigateTab = { tab -> currentTab = tab },
                                                    onOpenNotifications = { showNotificationsScreen = true }
                                                )
                                            }

                                            ScreenTab.FARMS -> {
                                                FarmsAndCropsScreen(
                                                    viewModel = viewModel,
                                                    farms = farms,
                                                    crops = crops,
                                                    onAddFarm = { name, loc, area, soil, irr, notes ->
                                                        viewModel.addFarm(name, loc, area, soil, irr, notes)
                                                    },
                                                    onDeleteFarm = { viewModel.deleteFarm(it) },
                                                    onAddCrop = { fId, fName, cName, varName, sowing, harvest, area, notes ->
                                                        viewModel.addCrop(fId, fName, cName, varName, sowing, harvest, area, notes)
                                                    },
                                                    onDeleteCrop = { viewModel.deleteCrop(it) }
                                                )
                                            }

                                            ScreenTab.AI -> {
                                                AiHubScreen(viewModel = viewModel)
                                            }

                                            ScreenTab.MARKET -> {
                                                MarketScreen(viewModel = viewModel)
                                            }

                                            ScreenTab.PROFILE -> {
                                                ProfileSettingsScreen(
                                                    user = user,
                                                    onUpdateProfile = { name, phone, lang, loc, exp ->
                                                        viewModel.updateProfile(name, phone, lang, loc, exp)
                                                    },
                                                    onSwitchUser = { target ->
                                                        viewModel.switchUserForTesting(target)
                                                    },
                                                    onLogout = {
                                                        viewModel.logout()
                                                        currentScreenState = "AUTH"
                                                    },
                                                    onRestartOnboarding = {
                                                        OnboardingPreferences.setOnboardingCompleted(context, false)
                                                        currentScreenState = "ONBOARDING"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntentExtras(intent)
    }

    private fun parseIntentExtras(intent: Intent?) {
        intent?.let {
            initialDestination = it.getStringExtra("destination") ?: it.getStringExtra("extra_destination")
            initialCropId = it.getStringExtra("relatedCropId") ?: it.getStringExtra("extra_crop_id")
            initialFarmId = it.getStringExtra("relatedFarmId") ?: it.getStringExtra("extra_farm_id")
        }
    }
}
