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
import com.slt.cardealership.presentation.common.AnimatedDropdown
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
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedDropdown(
                    label = "Default Build and Price Brand",
                    options = uiState.allMakes.filter { make ->
                        uiState.bnpMakes.none { it.makeId == make.id }
                    }.map { it.name },
                    selectedOption = "Select Brand",
                    onOptionSelected = { selectedName ->
                        val selected = uiState.allMakes.find { it.name == selectedName }
                        selected?.let { viewModel.addBnpMake(it.id) }
                    }
                )

                if (uiState.bnpMakes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
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

            // Default Build and Price Body Type
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedDropdown(
                    label = "Default Build and Price Body Type",
                    options = uiState.allBodyTypes.filter { bodyType ->
                        uiState.bnpBodyTypes.none { it.bodyTypeId == bodyType.id }
                    }.map { it.name },
                    selectedOption = "Select Body Type",
                    onOptionSelected = { selectedName ->
                        val selected = uiState.allBodyTypes.find { it.name == selectedName }
                        selected?.let { viewModel.addBnpBodyType(it.id) }
                    }
                )

                if (uiState.bnpBodyTypes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
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


