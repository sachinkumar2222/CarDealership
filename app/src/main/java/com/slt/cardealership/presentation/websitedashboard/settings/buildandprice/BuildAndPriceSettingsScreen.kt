package com.slt.cardealership.presentation.websitedashboard.settings.buildandprice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.ui.theme.BrandBlue

@Composable
fun BuildAndPriceSettingsScreen(
    domainId: Int,
    viewModel: BuildAndPriceSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.loadData(domainId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlue)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Default Build and Price Brand
            BnpSettingRow(label = "Default Build and Price Brand:", isMultiLine = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BnpMultiSelectDropdown(
                        label = "Select Brand",
                        options = uiState.allMakes.filter { make ->
                            uiState.bnpMakes.none { it.makeId == make.id }
                        }.map { it.name to it },
                        onOptionSelected = { option ->
                            if (option != null) viewModel.addBnpMake(option.second.id)
                        }
                    )
                    if (uiState.bnpMakes.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.bnpMakes.forEach { makeSetting ->
                                BnpRemovableChip(
                                    label = makeSetting.makeName ?: "Unknown",
                                    onRemove = { viewModel.removeBnpMake(makeSetting.makeId) }
                                )
                            }
                        }
                    }
                }
            }

            // Default Build and Price Body Type
            BnpSettingRow(label = "Default Build and Price Body Type:", isMultiLine = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BnpMultiSelectDropdown(
                        label = "Select Body Type",
                        options = uiState.allBodyTypes.filter { bodyType ->
                            uiState.bnpBodyTypes.none { it.bodyTypeId == bodyType.id }
                        }.map { it.name to it },
                        onOptionSelected = { option ->
                            if (option != null) viewModel.addBnpBodyType(option.second.id)
                        }
                    )
                    if (uiState.bnpBodyTypes.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.bnpBodyTypes.forEach { bodyTypeSetting ->
                                BnpRemovableChip(
                                    label = bodyTypeSetting.bodyTypeName ?: "Unknown",
                                    onRemove = { viewModel.removeBnpBodyType(bodyTypeSetting.bodyTypeId) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { viewModel.saveSettings(domainId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BnpSettingRow(
    label: String,
    isMultiLine: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun BnpRemovableChip(label: String, onRemove: () -> Unit) {
    Surface(
        color = Color(0xFFF0F2F5),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> BnpMultiSelectDropdown(
    label: String,
    options: List<Pair<String, T>>,
    onOptionSelected: (Pair<String, T>?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                focusedLabelColor = BrandBlue,
                cursorColor = BrandBlue,
                unfocusedBorderColor = Color(0xFFE9ECEF),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedContainerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.first) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
