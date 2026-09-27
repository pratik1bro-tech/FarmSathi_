package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.models.Farm

@Composable
fun AddCropDialog(
    farms: List<Farm>,
    onDismiss: () -> Unit,
    onConfirm: (farmId: String, farmName: String, cropName: String, variety: String, sowingDate: String, harvestDate: String, area: Double, notes: String) -> Unit
) {
    var selectedFarm by remember { mutableStateOf(farms.firstOrNull()) }
    var cropName by remember { mutableStateOf("Soybean") }
    var variety by remember { mutableStateOf("JS-335") }
    var sowingDate by remember { mutableStateOf("2026-06-15") }
    var harvestDate by remember { mutableStateOf("2026-10-20") }
    var areaText by remember { mutableStateOf("2.0") }
    var notes by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Crop to Farm") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (farms.isEmpty()) {
                    Text(
                        text = "Please add a farm first before adding crops.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Selected Farm: ${selectedFarm?.name ?: "None"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop Name *") },
                        placeholder = { Text("e.g. Wheat, Cotton, Rice, Soybean") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_crop_name_input")
                    )

                    OutlinedTextField(
                        value = variety,
                        onValueChange = { variety = it },
                        label = { Text("Variety / Hybrid Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sowingDate,
                        onValueChange = { sowingDate = it },
                        label = { Text("Sowing Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = harvestDate,
                        onValueChange = { harvestDate = it },
                        label = { Text("Expected Harvest Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        label = { Text("Cultivated Area (Acres)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = farms.isNotEmpty(),
                onClick = {
                    val farm = selectedFarm
                    if (farm == null) {
                        errorMsg = "Please select a valid farm."
                        return@Button
                    }
                    if (cropName.isBlank()) {
                        errorMsg = "Crop name is required."
                        return@Button
                    }
                    val area = areaText.toDoubleOrNull() ?: 1.0
                    onConfirm(farm.id, farm.name, cropName, variety, sowingDate, harvestDate, area, notes)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_crop_button")
            ) {
                Text("Save Crop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
