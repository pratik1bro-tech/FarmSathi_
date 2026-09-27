package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.*
import com.example.ui.components.ScanCard
import com.example.ui.components.ScreenTab
import com.example.ui.components.WeatherCard

@Composable
fun HomeScreen(
    user: User,
    farms: List<Farm>,
    crops: List<Crop>,
    scans: List<CropScan>,
    notifications: List<FarmNotification>,
    weather: WeatherData,
    isWeatherLoading: Boolean,
    onRefreshWeather: () -> Unit,
    onNavigateTab: (ScreenTab) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val unreadCount = notifications.count { !it.isRead }
    val primaryFarm = farms.firstOrNull()
    val primaryCrop = crops.firstOrNull()
    val latestScan = scans.firstOrNull()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Avatar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Namaste, ${user.name} 🙏",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (primaryFarm != null) "${primaryFarm.name} • ${primaryFarm.location}" else user.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.testTag("notification_icon_button")
                        ) {
                            Icon(
                                imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Farmland Banner with Farm Status Summary
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_farmland_1787208209953),
                        contentDescription = "Hero Farmland",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "FARM LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (primaryCrop != null) "${primaryCrop.cropName} (${primaryCrop.variety})" else "Smart Farm Advisor",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "What is happening with your farm today • Real-time AI Intelligence",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFEF3C7)
                            )
                        }
                    }
                }
            }

            // 1. Important Farm Alert (Prominent Alert Card)
            ImportantFarmAlertCard(
                weather = weather,
                latestScan = latestScan,
                onActionClick = { onNavigateTab(ScreenTab.AI) }
            )

            // 2. Today's Farm Status Grid (Compact Cards)
            Text(
                text = "TODAY'S FARM STATUS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricChip(
                    title = "Crop Health",
                    status = if (latestScan?.severity == "High") "Action Needed" else "Good Condition",
                    subValue = if (latestScan != null) latestScan.cropName else "Monitored",
                    icon = Icons.Default.HealthAndSafety,
                    color = if (latestScan?.severity == "High") Color(0xFFC62828) else Color(0xFF2E7D32),
                    bgColor = if (latestScan?.severity == "High") Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    onClick = { onNavigateTab(ScreenTab.AI) },
                    modifier = Modifier.weight(1f)
                )

                StatusMetricChip(
                    title = "Soil Moisture",
                    status = "28.5% (Optimal)",
                    subValue = "pH 6.8 • Ideal",
                    icon = Icons.Default.WaterDrop,
                    color = Color(0xFF1565C0),
                    bgColor = Color(0xFFE3F2FD),
                    onClick = { onNavigateTab(ScreenTab.FARMS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricChip(
                    title = "Weather",
                    status = "${weather.temperature.toInt()}°C • ${weather.condition}",
                    subValue = "Rain Risk: Low",
                    icon = Icons.Default.WbSunny,
                    color = Color(0xFFE65100),
                    bgColor = Color(0xFFFFF3E0),
                    onClick = { onRefreshWeather() },
                    modifier = Modifier.weight(1f)
                )

                StatusMetricChip(
                    title = "Irrigation",
                    status = "Normal Schedule",
                    subValue = "Target: 400L/Acre",
                    icon = Icons.Default.Opacity,
                    color = Color(0xFF00838F),
                    bgColor = Color(0xFFE0F7FA),
                    onClick = { onNavigateTab(ScreenTab.FARMS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. FarmSathi AI CTA (Clean, prominent invitation to text or speak with AI)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_ask_farmsathi_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = "FarmSathi AI",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ask FarmSathi AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Speak in Hindi or English for instant farming advice",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateTab(ScreenTab.AI) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_ai_voice_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Assistant", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voice Assistant")
                        }

                        OutlinedButton(
                            onClick = { onNavigateTab(ScreenTab.AI) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_ai_chat_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Chat")
                        }
                    }
                }
            }

            // 4. Market Opportunity Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_market_opportunity_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Market Opportunity",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFE65100)
                            )
                        }
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+₹70 / Qtl Today",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Target Crop", style = MaterialTheme.typography.labelSmall, color = Color(0xFF795548))
                            Text(
                                text = if (primaryCrop != null) primaryCrop.cropName else "Soybean (Yellow)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text("Best Mandi: Indore (18 km)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF795548))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current Mandi Rate", style = MaterialTheme.typography.labelSmall, color = Color(0xFF795548))
                            Text("₹4,980 / Qtl", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                            Text("Est. Net Profit: ₹1,42,400", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigateTab(ScreenTab.MARKET) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Compare Mandis & Sell Direct →", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. Crop Health & Doctor Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Crop Doctor & Health Scans",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gemini multimodal vision diagnosis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = { onNavigateTab(ScreenTab.AI) }) {
                    Text("Scan Leaf +", fontWeight = FontWeight.Bold)
                }
            }

            if (scans.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "No Scans",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No leaf scans recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Take a photo of any damaged leaf to diagnose disease and get remedies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigateTab(ScreenTab.AI) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Leaf Now")
                        }
                    }
                }
            } else {
                scans.take(2).forEach { scan ->
                    ScanCard(scan = scan)
                }
            }

            // 6. Quick Module Navigation
            Text(
                text = "EXPLORE PLATFORM MODULES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModuleQuickCard(
                    title = "Farms & Soil",
                    subtitle = "IoT & NPK",
                    icon = Icons.Default.Agriculture,
                    onClick = { onNavigateTab(ScreenTab.FARMS) },
                    modifier = Modifier.weight(1f)
                )

                ModuleQuickCard(
                    title = "Crop Doctor",
                    subtitle = "AI Diagnosis",
                    icon = Icons.Default.HealthAndSafety,
                    onClick = { onNavigateTab(ScreenTab.AI) },
                    modifier = Modifier.weight(1f)
                )

                ModuleQuickCard(
                    title = "Marketplace",
                    subtitle = "Mandi & Buyers",
                    icon = Icons.Default.Storefront,
                    onClick = { onNavigateTab(ScreenTab.MARKET) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ImportantFarmAlertCard(
    weather: WeatherData,
    latestScan: CropScan?,
    onActionClick: () -> Unit
) {
    val isRainExpected = weather.condition.contains("Rain", ignoreCase = true)
    val hasCropDisease = latestScan != null && latestScan.severity == "High"

    val title = when {
        hasCropDisease -> "High Disease Risk Detected in ${latestScan.cropName}"
        isRainExpected -> "Rain Expected in ${weather.locationName}"
        else -> "Favorable Weather for Crop Growth"
    }

    val description = when {
        hasCropDisease -> "Possible ${latestScan.possibleIssue}. Immediate biological or targeted spray advised."
        isRainExpected -> "Postpone urea broadcasting and pesticide spraying until clear skies."
        else -> "Optimal soil temperature and moisture for healthy vegetative growth."
    }

    val cardBg = if (hasCropDisease) Color(0xFFFFEBEE) else if (isRainExpected) Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
    val borderColor = if (hasCropDisease) Color(0xFFFFCDD2) else if (isRainExpected) Color(0xFFBBDEFB) else Color(0xFFC8E6C9)
    val iconColor = if (hasCropDisease) Color(0xFFC62828) else if (isRainExpected) Color(0xFF1565C0) else Color(0xFF2E7D32)
    val icon = if (hasCropDisease) Icons.Default.Warning else if (isRainExpected) Icons.Default.WaterDrop else Icons.Default.CheckCircle

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Alert Icon",
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF37474F)
                )
            }
            if (hasCropDisease) {
                IconButton(onClick = onActionClick) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "View Details", tint = iconColor)
                }
            }
        }
    }
}

@Composable
private fun StatusMetricChip(
    title: String,
    status: String,
    subValue: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                maxLines = 1
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ModuleQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
