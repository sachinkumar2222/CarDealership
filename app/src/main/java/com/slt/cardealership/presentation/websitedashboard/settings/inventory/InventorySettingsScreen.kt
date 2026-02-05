package com.slt.cardealership.presentation.websitedashboard.settings.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
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
fun InventorySettingsScreen(
    domainId: Int,
    viewModel: InventorySettingsViewModel = hiltViewModel()
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
            // Default Inventory View (Toggle)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Default inventory view",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                InventoryViewToggle(
                    currentView = uiState.inventorySettings?.defaultInventoryView ?: "grid",
                    onViewSelected = { viewModel.updateInventoryView(it) }
                )
            }

            // Default Inventory Makes (Multi-select)
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedDropdown(
                    label = "Default inventory Makes",
                    options = uiState.allMakes.filter { make ->
                        uiState.defaultMakes.none { it.makeId == make.id }
                    }.map { it.name },
                    selectedOption = "Select Make",
                    onOptionSelected = { selectedName ->
                        val selected = uiState.allMakes.find { it.name == selectedName }
                        selected?.let { viewModel.addMakeToCondition(it, "default") }
                    }
                )

                if (uiState.defaultMakes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.defaultMakes.forEach { makeSetting ->
                            RemovableChip(
                                label = makeSetting.makeName ?: "Unknown",
                                onRemove = { viewModel.removeMakeFromCondition(makeSetting) }
                            )
                        }
                    }
                }
            }

            // Default Inventory New Makes
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedDropdown(
                    label = "Default inventory new makes",
                    options = uiState.allMakes.filter { make ->
                        uiState.newMakes.none { it.makeId == make.id }
                    }.map { it.name },
                    selectedOption = "Select Make",
                    onOptionSelected = { selectedName ->
                        val selected = uiState.allMakes.find { it.name == selectedName }
                        selected?.let { viewModel.addMakeToCondition(it, "new") }
                    }
                )

                if (uiState.newMakes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.newMakes.forEach { makeSetting ->
                            RemovableChip(
                                label = makeSetting.makeName ?: "Unknown",
                                onRemove = { viewModel.removeMakeFromCondition(makeSetting) }
                            )
                        }
                    }
                }
            }

            // Default Inventory Used Makes
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedDropdown(
                    label = "Default inventory used makes",
                    options = uiState.allMakes.filter { make ->
                        uiState.usedMakes.none { it.makeId == make.id }
                    }.map { it.name },
                    selectedOption = "Select Make",
                    onOptionSelected = { selectedName ->
                        val selected = uiState.allMakes.find { it.name == selectedName }
                        selected?.let { viewModel.addMakeToCondition(it, "used") }
                    }
                )

                if (uiState.usedMakes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.usedMakes.forEach { makeSetting ->
                            RemovableChip(
                                label = makeSetting.makeName ?: "Unknown",
                                onRemove = { viewModel.removeMakeFromCondition(makeSetting) }
                            )
                        }
                    }
                }
            }

            // New Inventory Body Types
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedDropdown(
                    label = "New inventory body types",
                    options = uiState.allBodyTypes.filter { bodyType ->
                        uiState.newBodyTypes.none { it.bodyTypeId == bodyType.id }
                    }.map { it.name },
                    selectedOption = "Select Body Type",
                    onOptionSelected = { selectedName ->
                        val selected = uiState.allBodyTypes.find { it.name == selectedName }
                        selected?.let { viewModel.addBodyType(it) }
                    }
                )

                if (uiState.newBodyTypes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.newBodyTypes.forEach { bodyTypeSetting ->
                            RemovableChip(
                                label = bodyTypeSetting.bodyTypeName ?: "Unknown",
                                onRemove = { viewModel.removeBodyType(bodyTypeSetting) }
                            )
                        }
                    }
                }
            }

            // Default Sort
            AnimatedDropdown(
                label = "Default Sort",
                options = listOf(
                    "Price : Low To High",
                    "Price : High To Low",
                    "Year : New To Old",
                    "Year : Old To New",
                    "Distance : Nearest To Farthest",
                    "Listed : Newly Listed To Old Listed",
                    "Listed : Old Listed To Newly Listed",
                    "Mileage : High To Low",
                    "Mileage : Low To High",
                    "Make : A To Z",
                    "Make : Z To A",
                    "Model : A To Z",
                    "Model : Z To A"
                ),
                selectedOption = viewModel.getSortLabel(uiState.inventorySettings?.defaultOrder),
                onOptionSelected = { viewModel.updateDefaultSort(it) }
            )

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
fun InventoryViewToggle(currentView: String, onViewSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.height(48.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val gridSelected = currentView == "grid"

        // Grid Button
        OutlinedButton(
            onClick = { onViewSelected("grid") },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (gridSelected) BrandBlue else Color.White,
                contentColor = if (gridSelected) Color.White else Color.Gray
            ),
            border = BorderStroke(1.dp, if (gridSelected) BrandBlue else Color.LightGray),
            contentPadding = PaddingValues(0.dp) // Reset padding
        ) {
            Icon(
                // Placeholder icon, standard grid icon
                imageVector = Icons.Default.Menu, // Using Menu as placeholder for Grid if unavailable
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grid view", fontSize = 14.sp)
        }

        // List Button
        OutlinedButton(
            onClick = { onViewSelected("list") },
            modifier = Modifier.weight(1f).fillMaxHeight().offset(x = (-1).dp),
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (!gridSelected) BrandBlue else Color.White,
                contentColor = if (!gridSelected) Color.White else Color.Gray
            ),
            border = BorderStroke(1.dp, if (!gridSelected) BrandBlue else Color.LightGray),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("List view", fontSize = 14.sp)
        }
    }
}

@Composable
fun RemovableChip(label: String, onRemove: () -> Unit) {
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


