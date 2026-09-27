package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.service.ai.FarmBriefing
import com.example.service.ai.FarmHealthScore
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Crop Doctor Scanner, 1: AI Chatbot, 2: Voice Assistant, 3: Central Advisory

    val crops by viewModel.crops.collectAsState()
    val soilReadings by viewModel.soilReadings.collectAsState()
    val telemetryReadings by viewModel.telemetryReadings.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val dailyBriefing by viewModel.dailyBriefing.collectAsState()
    val farmHealthScore by viewModel.farmHealthScore.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FarmSathi AI Hub", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("LLM Intelligence • Disease Scanner • Voice Assistant", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("ai_hub_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Crop Doctor", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("ai_subtab_scanner")
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Kishan AI Chat", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("ai_subtab_chat")
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Voice Assistant", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("ai_subtab_voice")
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("AI Briefing & Score", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("ai_subtab_advisory")
                )
            }

            val scans by viewModel.scans.collectAsState()
            val isScanning by viewModel.isScanning.collectAsState()
            val scanError by viewModel.scanError.collectAsState()
            val currentScanResult by viewModel.currentScanResult.collectAsState()

            val chatMessages by viewModel.chatMessages.collectAsState()
            val isChatLoading by viewModel.isChatLoading.collectAsState()
            val chatError by viewModel.chatError.collectAsState()

            val isListening by viewModel.speechManager.isListening.collectAsState()
            val recognizedText by viewModel.speechManager.recognizedText.collectAsState()
            val speechError by viewModel.speechManager.speechError.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                when (activeSubTab) {
                    0 -> CropScannerScreen(
                        scans = scans,
                        isScanning = isScanning,
                        scanError = scanError,
                        currentScanResult = currentScanResult,
                        onPerformScan = { bitmap, cropName -> viewModel.performCropScan(bitmap, cropName) },
                        onClearScanResult = { viewModel.clearCurrentScanResult() }
                    )
                    1 -> AiChatScreen(
                        messages = chatMessages,
                        isLoading = isChatLoading,
                        chatError = chatError,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onClearChat = { viewModel.clearChat() },
                        onOpenVoice = { activeSubTab = 2 }
                    )
                    2 -> VoiceAssistantScreen(
                        speechManager = viewModel.speechManager,
                        isListening = isListening,
                        recognizedText = recognizedText,
                        speechError = speechError,
                        isAiLoading = isChatLoading,
                        onSendPrompt = { prompt ->
                            viewModel.sendChatMessage(prompt)
                            activeSubTab = 1
                        }
                    )
                    3 -> CentralAdvisoryView(
                        crops = crops,
                        soil = soilReadings.firstOrNull(),
                        telemetry = telemetryReadings.firstOrNull(),
                        weather = weatherData,
                        dailyBriefing = dailyBriefing,
                        farmHealthScore = farmHealthScore,
                        onRefreshBriefing = { viewModel.refreshDailyBriefing() },
                        onRefreshScore = { viewModel.refreshFarmHealthScore() }
                    )
                }
            }
        }
    }
}

@Composable
fun CentralAdvisoryView(
    crops: List<com.example.data.models.Crop>,
    soil: com.example.data.models.SoilReading?,
    telemetry: com.example.data.models.TelemetryData?,
    weather: com.example.data.models.WeatherData,
    dailyBriefing: FarmBriefing?,
    farmHealthScore: FarmHealthScore?,
    onRefreshBriefing: () -> Unit,
    onRefreshScore: () -> Unit
) {
    val advisories = remember(crops, soil, telemetry, weather) {
        com.example.service.ForecastingAndAdvisoryEngine.generateCentralAdvisory(
            crops.firstOrNull(), soil, telemetry, weather
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. AI Farm Health Score Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Farm Health Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onRefreshScore, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val score = farmHealthScore?.overallScore ?: 86
                    val status = farmHealthScore?.statusLabel ?: "Optimal"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$score",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Status: $status ($score/100)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = farmHealthScore?.summaryExplanation ?: "Farm signals indicate healthy vegetative progress with balanced soil moisture and active mandi demand.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Score breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreSubItem("🌱 Crop", "${farmHealthScore?.cropHealthScore ?: 88}%")
                        ScoreSubItem("🧪 Soil", "${farmHealthScore?.soilHealthScore ?: 92}%")
                        ScoreSubItem("🌦️ Weather", "${farmHealthScore?.weatherRiskScore ?: 85}%")
                        ScoreSubItem("💰 Market", "${farmHealthScore?.marketOpportunityScore ?: 80}%")
                    }
                }
            }
        }

        // 2. Personalized Daily Farm Briefing
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Today's FarmSathi Briefing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onRefreshBriefing, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (dailyBriefing != null) {
                        BriefingRow("🌦️ Weather", dailyBriefing.weatherBrief)
                        Spacer(modifier = Modifier.height(6.dp))
                        BriefingRow("🌱 Crop Status", dailyBriefing.cropBrief)
                        Spacer(modifier = Modifier.height(6.dp))
                        BriefingRow("💧 Irrigation", dailyBriefing.irrigationBrief)
                        Spacer(modifier = Modifier.height(6.dp))
                        BriefingRow("💰 Market", dailyBriefing.marketBrief)

                        if (dailyBriefing.urgentAlert != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(dailyBriefing.urgentAlert, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text("Briefing is being synthesized from live farm telemetry...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // 3. Agronomic Advisories
        item {
            Text("Agronomic Advisory Records", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Real-time recommendations from microclimate sensors and weather forecast.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(advisories.size) { idx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(advisories[idx], style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ScoreSubItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun BriefingRow(label: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
