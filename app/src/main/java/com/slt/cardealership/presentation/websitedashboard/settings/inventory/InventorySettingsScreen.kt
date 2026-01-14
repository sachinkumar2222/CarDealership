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
import com.slt.cardealership.presentation.websitedashboard.settings.DropdownSelector
import com.slt.cardealership.presentation.websitedashboard.settings.ThemeSettingsViewModel
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
            InventorySettingRow(label = "Default inventory view:") {
                InventoryViewToggle(
                    currentView = uiState.inventorySettings?.defaultInventoryView ?: "grid",
                    onViewSelected = { viewModel.updateInventoryView(it) }
                )
            }

            // Default Inventory Makes (Multi-select)
            InventorySettingRow(label = "Default inventory Makes:", isMultiLine = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MultiSelectDropdown(
                        label = "Select Make",
                        options = uiState.allMakes.filter { make ->
                            uiState.defaultMakes.none { it.makeId == make.id }
                        }.map { it.name to it },
                        onOptionSelected = { option ->
                            if (option != null) viewModel.addMakeToCondition(option.second, "default")
                        }
                    )
                    if (uiState.defaultMakes.isNotEmpty()) {
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
            }

            // Default Inventory New Makes
            InventorySettingRow(label = "Default inventory new makes:", isMultiLine = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MultiSelectDropdown(
                        label = "Select Make",
                        options = uiState.allMakes.filter { make ->
                            uiState.newMakes.none { it.makeId == make.id }
                        }.map { it.name to it },
                        onOptionSelected = { option ->
                            if (option != null) viewModel.addMakeToCondition(option.second, "new")
                        }
                    )
                    if (uiState.newMakes.isNotEmpty()) {
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
            }

            // Default Inventory Used Makes
            InventorySettingRow(label = "Default inventory used makes:", isMultiLine = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MultiSelectDropdown(
                        label = "Select Make",
                        options = uiState.allMakes.filter { make ->
                            uiState.usedMakes.none { it.makeId == make.id }
                        }.map { it.name to it },
                        onOptionSelected = { option ->
                            if (option != null) viewModel.addMakeToCondition(option.second, "used")
                        }
                    )
                    if (uiState.usedMakes.isNotEmpty()) {
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
            }

            // New Inventory Body Types
            InventorySettingRow(label = "New inventory body types:", isMultiLine = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MultiSelectDropdown(
                        label = "Select Body Type",
                        options = uiState.allBodyTypes.filter { bodyType ->
                            uiState.newBodyTypes.none { it.bodyTypeId == bodyType.id }
                        }.map { it.name to it },
                        onOptionSelected = { option ->
                            if (option != null) viewModel.addBodyType(option.second)
                        }
                    )
                    if (uiState.newBodyTypes.isNotEmpty()) {
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
            }

            // Default Sort
            InventorySettingRow(label = "Default Sort:") {
                DropdownSelector(
                    selectedItem = viewModel.getSortLabel(uiState.inventorySettings?.defaultOrder),
                    items = listOf(
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
                    onItemSelected = { viewModel.updateDefaultSort(it) }
                )
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
fun InventorySettingRow(
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdown(
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
