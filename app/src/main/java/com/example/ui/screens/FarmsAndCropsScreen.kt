package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.*
import com.example.ui.components.AddCropDialog
import com.example.ui.components.AddFarmDialog
import com.example.ui.components.CropCard
import com.example.ui.components.FarmCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FarmsAndCropsScreen(
    viewModel: MainViewModel,
    farms: List<Farm>,
    crops: List<Crop>,
    onAddFarm: (name: String, location: String, area: Double, soilType: String, irrigation: String, notes: String) -> Unit,
    onDeleteFarm: (Farm) -> Unit,
    onAddCrop: (farmId: String, farmName: String, cropName: String, variety: String, sowingDate: String, harvestDate: String, area: Double, notes: String) -> Unit,
    onDeleteCrop: (Crop) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Farms & Crops, 1: Soil Health & IoT Telemetry, 2: NPK & Irrigation Advisory, 3: Yield & Harvest Forecast
    var showAddFarmDialog by remember { mutableStateOf(false) }
    var showAddCropDialog by remember { mutableStateOf(false) }
    var showAddSoilDialog by remember { mutableStateOf(false) }

    val soilReadings by viewModel.soilReadings.collectAsState()
    val telemetryReadings by viewModel.telemetryReadings.collectAsState()
    val npkAdvisory by viewModel.npkAdvisory.collectAsState()
    val irrigationAdvisory by viewModel.irrigationAdvisory.collectAsState()
    val yieldForecasts by viewModel.yieldForecasts.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddFarmDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Add Farm") },
                    modifier = Modifier.testTag("add_farm_fab")
                )
            } else if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddSoilDialog = true },
                    icon = { Icon(Icons.Default.Science, contentDescription = "Add Soil Reading") },
                    text = { Text("Log Soil Test") },
                    modifier = Modifier.testTag("add_soil_fab")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Farms & Crops (${farms.size})") },
                    icon = { Icon(Icons.Default.Agriculture, contentDescription = null) },
                    modifier = Modifier.testTag("tab_farms_crops")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Soil Health & IoT") },
                    icon = { Icon(Icons.Default.Science, contentDescription = null) },
                    modifier = Modifier.testTag("tab_soil_iot")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("NPK & Irrigation") },
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                    modifier = Modifier.testTag("tab_npk_irrigation")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Yield & Harvest") },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                    modifier = Modifier.testTag("tab_yield_harvest")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> { // Farms & Crops
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Registered Farms (${farms.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { showAddFarmDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Farm")
                                }
                            }
                        }

                        items(farms) { farm ->
                            FarmCard(farm = farm, onClick = {}, onDelete = { onDeleteFarm(farm) })
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Active Crops (${crops.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { showAddCropDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Crop")
                                }
                            }
                        }

                        items(crops) { crop ->
                            CropCard(crop = crop, onClick = {}, onDelete = { onDeleteCrop(crop) })
                        }
                    }
                }

                1 -> { // Soil Health & IoT
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Soil Health & Telemetry Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Tracks NPK, pH, organic matter, and live IoT sensor microclimate telemetry.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        items(soilReadings) { soil ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${soil.farmName} • ${soil.soilType}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Badge { Text(soil.dataSource) }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Soil pH", style = MaterialTheme.typography.labelSmall)
                                            Text("${soil.ph}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column {
                                            Text("Nitrogen (N)", style = MaterialTheme.typography.labelSmall)
                                            Text("${soil.nitrogen} kg/ha", fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Phosphorus (P)", style = MaterialTheme.typography.labelSmall)
                                            Text("${soil.phosphorus} kg/ha", fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Potassium (K)", style = MaterialTheme.typography.labelSmall)
                                            Text("${soil.potassium} kg/ha", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Microclimate IoT Telemetry Node", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        items(telemetryReadings) { node ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Device: ${node.deviceId} (${node.dataSource})", fontWeight = FontWeight.Bold)
                                    Text("Air Temp: ${node.airTemperature}°C | Soil Temp: ${node.soilTemperature}°C", style = MaterialTheme.typography.bodySmall)
                                    Text("Soil Moisture: ${node.soilMoisture}% | Humidity: ${node.humidity}%", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                2 -> { // NPK & Irrigation Advisory
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("NPK Nutrient Advisory Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Crop: ${npkAdvisory.cropName} (${npkAdvisory.cropStage})", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Status: N (${npkAdvisory.nStatus}) | P (${npkAdvisory.pStatus}) | K (${npkAdvisory.kStatus})", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Recommended Fertilizer Dosage:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text(npkAdvisory.fertilizerDosage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    npkAdvisory.recommendedActions.forEach { act ->
                                        Text("• $act", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Irrigation Advisory Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(irrigationAdvisory.recommendation, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Target Liters: ${irrigationAdvisory.recommendedLitersPerAcre}", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    irrigationAdvisory.factorsConsidered.forEach { f ->
                                        Text("• $f", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> { // Yield & Harvest Forecast
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Yield & Optimal Harvest Window Forecast", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("AI machine learning pipeline combining soil fertility, area, and growth stage.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        items(yieldForecasts) { fc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${fc.cropName} (${fc.variety})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Badge { Text("${fc.daysRemainingToHarvest} Days to Harvest") }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Estimated Total Harvest: ${fc.estimatedTotalMinQuintals} - ${fc.estimatedTotalMaxQuintals} Quintals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Per Acre Yield: ${fc.estimatedYieldMinPerAcre} - ${fc.estimatedYieldMaxPerAcre} Qtl/Acre", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Optimal Harvest Dates: ${fc.harvestWindowStart} to ${fc.harvestWindowEnd}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFarmDialog) {
        AddFarmDialog(
            onDismiss = { showAddFarmDialog = false },
            onConfirm = { name, location, area, soilType, irrigation, notes ->
                onAddFarm(name, location, area, soilType, irrigation, notes)
                showAddFarmDialog = false
            }
        )
    }

    if (showAddCropDialog) {
        AddCropDialog(
            farms = farms,
            onDismiss = { showAddCropDialog = false },
            onConfirm = { farmId, farmName, cropName, variety, sowingDate, harvestDate, area, notes ->
                onAddCrop(farmId, farmName, cropName, variety, sowingDate, harvestDate, area, notes)
                showAddCropDialog = false
            }
        )
    }

    if (showAddSoilDialog) {
        var phText by remember { mutableStateOf("6.8") }
        var nText by remember { mutableStateOf("135.0") }
        var pText by remember { mutableStateOf("25.0") }
        var kText by remember { mutableStateOf("190.0") }
        var moistureText by remember { mutableStateOf("28.0") }
        var tempText by remember { mutableStateOf("26.0") }

        AlertDialog(
            onDismissRequest = { showAddSoilDialog = false },
            title = { Text("Log Soil Test / Lab Result") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = phText, onValueChange = { phText = it }, label = { Text("Soil pH (e.g., 6.8)") })
                    OutlinedTextField(value = nText, onValueChange = { nText = it }, label = { Text("Nitrogen (N kg/ha)") })
                    OutlinedTextField(value = pText, onValueChange = { pText = it }, label = { Text("Phosphorus (P kg/ha)") })
                    OutlinedTextField(value = kText, onValueChange = { kText = it }, label = { Text("Potassium (K kg/ha)") })
                    OutlinedTextField(value = moistureText, onValueChange = { moistureText = it }, label = { Text("Soil Moisture %") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ph = phText.toDoubleOrNull() ?: 6.8
                        val n = nText.toDoubleOrNull() ?: 135.0
                        val p = pText.toDoubleOrNull() ?: 25.0
                        val k = kText.toDoubleOrNull() ?: 190.0
                        val m = moistureText.toDoubleOrNull() ?: 28.0
                        val t = tempText.toDoubleOrNull() ?: 26.0
                        viewModel.addSoilReading(ph, n, p, k, m, t)
                        showAddSoilDialog = false
                    }
                ) { Text("Save Soil Reading") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSoilDialog = false }) { Text("Cancel") }
            }
        )
    }
}
