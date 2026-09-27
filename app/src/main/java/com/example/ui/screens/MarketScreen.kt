package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BuyerProfile
import com.example.data.models.MandiPrice
import com.example.service.MandiMarketEngine
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCommodity by viewModel.selectedCommodity.collectAsState()
    val mandiPrices by viewModel.mandiPrices.collectAsState()
    val buyersList by viewModel.buyersList.collectAsState()
    val bestMarketResult by viewModel.recommendedMarketResult.collectAsState()
    val sellingRequests by viewModel.sellingRequests.collectAsState()
    val logisticsRequests by viewModel.logisticsRequests.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) } // 0: Mandi Prices & Forecast, 1: Best Market Recommender, 2: Buyer Marketplace, 3: Logistics Booking

    var showSellDialog by remember { mutableStateOf<BuyerProfile?>(null) }
    var showLogisticsDialog by remember { mutableStateOf(false) }

    val filteredMandiPrices = remember(mandiPrices, searchQuery) {
        if (searchQuery.isBlank()) mandiPrices
        else mandiPrices.filter {
            it.mandiName.contains(searchQuery, ignoreCase = true) ||
                    it.district.contains(searchQuery, ignoreCase = true) ||
                    it.commodity.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mandi Market & Buyer Ecosystem",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Agmarknet Live Prices • Net Profit Engine • Direct Buyers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshWeather() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Market Data")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("market_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Mandi, District or Variety...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") } }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("market_search_input")
            )

            // Commodity Selector Row
            val commodities = listOf("Soybean", "Wheat", "Gram", "Mustard", "Cotton")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commodities) { com ->
                    val selected = com == selectedCommodity
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectCommodity(com) },
                        label = { Text(com, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.testTag("commodity_chip_$com")
                    )
                }
            }

            // Market Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Live Mandi", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("market_tab_live")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Best Market", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("market_tab_recommender")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Buyers (${buyersList.size})", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("market_tab_buyers")
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Logistics", style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("market_tab_logistics")
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    0 -> { // Live Mandi & Price Forecast
                        item {
                            Text(
                                text = "Live Mandi Prices for $selectedCommodity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (filteredMandiPrices.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("No mandis found matching '$searchQuery'")
                                    }
                                }
                            }
                        } else {
                            items(filteredMandiPrices) { mandi ->
                                MandiPriceCard(mandi = mandi)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AI Price Forecast Engine (7-Day & 30-Day)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        item {
                            val sampleMandi = mandiPrices.firstOrNull()
                            if (sampleMandi != null) {
                                val forecast = MandiMarketEngine.getPriceForecast(
                                    selectedCommodity,
                                    sampleMandi.mandiName,
                                    sampleMandi.modalPrice
                                )
                                PriceForecastCard(forecast = forecast)
                            }
                        }
                    }

                    1 -> { // Best Market Recommender
                        item {
                            Text(
                                text = "Net Profit Market Recommendation Tool",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Calculates (Quantity × Mandi Price) − Transport Cost to give true Net Revenue.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val currentBestResult = bestMarketResult
                        if (currentBestResult != null) {
                            item {
                                BestMarketRecommendationCard(
                                    result = currentBestResult,
                                    commodity = selectedCommodity,
                                    onBookLogistics = { showLogisticsDialog = true }
                                )
                            }
                        }
                    }

                    2 -> { // Buyer Marketplace
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Verified Direct Buyers for $selectedCommodity",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Sell directly to processors, exporters & mills without middlemen.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(buyersList) { buyer ->
                            BuyerProfileCard(
                                buyer = buyer,
                                onExpressInterest = { showSellDialog = buyer }
                            )
                        }

                        if (sellingRequests.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Your Submitted Selling Requests (${sellingRequests.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            items(sellingRequests) { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(req.buyerName, fontWeight = FontWeight.Bold)
                                            Badge { Text(req.status) }
                                        }
                                        Text("${req.quantityQuintals} Quintals ${req.cropName} @ ₹${req.expectedPricePerQuintal}/Qtl", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Logistics Booking
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Logistics & Transport Engine",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Book verified trucks & tractor trolleys for farm-to-mandi transport.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { showLogisticsDialog = true },
                                    modifier = Modifier.testTag("book_transport_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Book Truck")
                                }
                            }
                        }

                        if (logisticsRequests.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Logistics Booked Yet", fontWeight = FontWeight.Bold)
                                        Text("Book a transport vehicle to move harvest to Mandi seamlessly.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        } else {
                            items(logisticsRequests) { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(req.vehicleType, fontWeight = FontWeight.Bold)
                                            Badge { Text(req.status) }
                                        }
                                        Text("Pickup: ${req.pickupLocation} -> Destination: ${req.destinationMandi}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Crop: ${req.quantityQuintals} Qtl ${req.cropName} | Pickup Date: ${req.pickupDate}", style = MaterialTheme.typography.bodySmall)
                                        Text("Estimated Fare: ₹${String.format("%.0f", req.estimatedCostInr)} (${req.estimatedDistanceKm} km)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showSellDialog != null) {
        val buyer = showSellDialog!!
        var qtyText by remember { mutableStateOf("30.0") }
        var priceText by remember { mutableStateOf(buyer.offeredPricePerQuintal.toString()) }

        AlertDialog(
            onDismissRequest = { showSellDialog = null },
            title = { Text("Express Interest to ${buyer.businessName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Crop: $selectedCommodity", fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantity (Quintals)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Expected Price (₹/Quintal)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = qtyText.toDoubleOrNull() ?: 30.0
                        val p = priceText.toDoubleOrNull() ?: buyer.offeredPricePerQuintal
                        viewModel.createSellingRequest(buyer, q, p)
                        showSellDialog = null
                    }
                ) {
                    Text("Submit Offer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogisticsDialog) {
        var mandiName by remember { mutableStateOf("Indore Mandi") }
        var cropName by remember { mutableStateOf(selectedCommodity) }
        var qtyText by remember { mutableStateOf("35.0") }
        var vehicleType by remember { mutableStateOf("Tractor Trolley") }
        var dateText by remember { mutableStateOf("Tomorrow Morning") }

        AlertDialog(
            onDismissRequest = { showLogisticsDialog = false },
            title = { Text("Book Transport Vehicle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = mandiName,
                        onValueChange = { mandiName = it },
                        label = { Text("Destination Mandi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantity (Quintals)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Vehicle Type:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    val vehicles = listOf("Tractor Trolley", "Small Truck (Ace)", "Medium Truck (Eicher)", "Heavy Truck")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(vehicles) { v ->
                            FilterChip(
                                selected = v == vehicleType,
                                onClick = { vehicleType = v },
                                label = { Text(v, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = qtyText.toDoubleOrNull() ?: 35.0
                        viewModel.bookLogistics(mandiName, cropName, q, vehicleType, dateText)
                        showLogisticsDialog = false
                    }
                ) {
                    Text("Confirm Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogisticsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MandiPriceCard(mandi: MandiPrice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(mandi.mandiName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        val statusLabel = when (mandi.dataSource) {
                            "LIVE_API" -> "LIVE"
                            "OFFLINE_CACHED" -> "CACHED"
                            "MANUAL" -> "MANUAL"
                            else -> "DEMO"
                        }
                        val statusBg = when (mandi.dataSource) {
                            "LIVE_API" -> Color(0xFFE8F5E9)
                            "OFFLINE_CACHED" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFECEFF1)
                        }
                        val statusTextColor = when (mandi.dataSource) {
                            "LIVE_API" -> Color(0xFF2E7D32)
                            "OFFLINE_CACHED" -> Color(0xFFE65100)
                            else -> Color(0xFF37474F)
                        }
                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = statusTextColor
                            )
                        }
                    }
                    Text("${mandi.district}, ${mandi.state} • ${mandi.variety}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = if (mandi.priceChange >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mandi.priceChange >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (mandi.priceChange >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (mandi.priceChange >= 0) "+" else ""}${mandi.priceChange.toInt()} ₹",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (mandi.priceChange >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Modal Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${mandi.modalPrice.toInt()} / Qtl", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Arrivals & Range", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${mandi.minPrice.toInt()} - ₹${mandi.maxPrice.toInt()} (${mandi.arrivalQuantityQuintals.toInt()} Qtl)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}

@Composable
fun PriceForecastCard(forecast: com.example.data.models.PriceForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Time-Series Forecast (${forecast.commodity})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("7-Day Forecast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${forecast.forecast7DayMin.toInt()} - ₹${forecast.forecast7DayMax.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("30-Day Forecast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${forecast.forecast30DayMin.toInt()} - ₹${forecast.forecast30DayMax.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Trend: ${forecast.trend} • Confidence: ${forecast.confidence}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BestMarketRecommendationCard(
    result: com.example.data.repository.MarketRecommendation,
    commodity: String,
    onBookLogistics: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFD84315))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Highest Net Profit Recommender", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(result.explanation, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Top Recommended Market", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(result.bestOverall.mandiName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Estimated Net Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%.0f", result.netRevenue)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBookLogistics,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Book Transport to ${result.bestOverall.mandiName}")
            }
        }
    }
}

@Composable
fun BuyerProfileCard(buyer: BuyerProfile, onExpressInterest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(buyer.businessName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        if (buyer.verifiedStatus) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("${buyer.contactName} • ${buyer.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Badge { Text("★ ${buyer.rating}") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Offered Price: ₹${buyer.offeredPricePerQuintal.toInt()} / Qtl", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Req: ${buyer.quantityRequiredQuintals.toInt()} Qtl • ${buyer.demandPeriod}", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onExpressInterest,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Sell Direct")
                }
            }
        }
    }
}
