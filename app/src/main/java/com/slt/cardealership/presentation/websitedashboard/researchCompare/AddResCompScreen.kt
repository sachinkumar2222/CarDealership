package com.slt.cardealership.presentation.websitedashboard.researchCompare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.ResearchMake
import com.slt.cardealership.domain.model.ResearchModel
import com.slt.cardealership.domain.model.ResearchYear
import com.slt.cardealership.domain.model.ResearchTrim
import com.slt.cardealership.domain.model.ResearchCompareCategory
import com.slt.cardealership.ui.theme.BrandDarkBlue
import com.slt.cardealership.ui.theme.LightBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResCompScreen(
    navController: NavController,
    domainId: Int,
    domainName: String, // Pass domainName if possible, or ViewMode fetches    viewModel: WebsiteResCompViewModel = hiltViewModel()
) {
    // ... Existing implementation ...
}

@Composable
fun AddResCompScreenWrapper(
    navController: NavController,
    domainId: Int,
    domainName: String,
    researchCompareId: String? = null,
    viewModel: AddResCompViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.init(domainId)
        if (researchCompareId != null) {
            viewModel.loadResearchCompare(researchCompareId)
        }
    }

    AddResCompScreen(
        navController = navController,
        domainId = domainId,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResCompScreen(
    navController: NavController,
    domainId: Int,
    viewModel: AddResCompViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicleSelectionState by viewModel.vehicleSelectionState.collectAsState()
    val scope = rememberCoroutineScope()
    // Bottom Sheet Control
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Research Compare", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.saveComparison(domainId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = com.slt.cardealership.ui.theme.BrandBlue),
                enabled = !uiState.isLoading && uiState.isVehicle1Added && uiState.isVehicle2Added && uiState.selectedCategory != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save", modifier = Modifier.padding(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Slot 1
            VehicleSlot(
                isAdded = uiState.isVehicle1Added,
                vehicleName = uiState.vehicle1?.let { "${it.year} ${it.makeName} ${it.modelName}" } ?: "",
                onClick = {
                    viewModel.startSelectingVehicle(1)
                    showBottomSheet = true
                }
            )

            // Slot 2
            VehicleSlot(
                isAdded = uiState.isVehicle2Added,
                vehicleName = uiState.vehicle2?.let { "${it.year} ${it.makeName} ${it.modelName}" } ?: "",
                onClick = {
                    // Only active if slot 1 is filled? User said "second one add vehicle will active" later.
                    // But usually good UX allows it if logic permits. Sticking to enable if 1 is added?
                    // User: "secont one add vehicle will active and same process follow after fill the form of second one"
                    // Implies sequential.
                    if (uiState.isVehicle1Added) {
                        viewModel.startSelectingVehicle(2)
                        showBottomSheet = true
                    }
                },
                isActive = uiState.isVehicle1Added
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            Text("Category *", style = MaterialTheme.typography.labelLarge)
            CategoryDropdown(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            ManageVehicleSheetContent(
                state = vehicleSelectionState,
                onMakeSelected = { viewModel.onMakeSelected(it) },
                onModelSelected = { viewModel.onModelSelected(it) },
                onYearSelected = { viewModel.onYearSelected(it) },
                onTrimSelected = { viewModel.onTrimSelected(it) },
                onSave = {
                    viewModel.saveVehicleSelection()
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun VehicleSlot(
    isAdded: Boolean,
    vehicleName: String,
    onClick: () -> Unit,
    isActive: Boolean = true
) {
    if (isAdded) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(vehicleName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Empty Slot - Dashed Border
        val stroke = remember { Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) Color.White else Color(0xFFF5F5F5))
                .clickable(enabled = isActive) { onClick() }
                .drawBehind {
                    drawRoundRect(
                        color = if (isActive) Color.Gray else Color.LightGray,
                        style = stroke,
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, if (isActive) Color.Black else Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (isActive) Color.Black else Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Add Vehicle",
                    color = if (isActive) Color.Black else Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<ResearchCompareCategory>,
    selectedCategory: ResearchCompareCategory?,
    onCategorySelected: (ResearchCompareCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory?.let { it.name ?: "Unknown Category" } ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name ?: "Unknown Category") },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageVehicleSheetContent(
    state: VehicleSelectionState,
    onMakeSelected: (ResearchMake) -> Unit,
    onModelSelected: (ResearchModel) -> Unit,
    onYearSelected: (ResearchYear) -> Unit,
    onTrimSelected: (ResearchTrim) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Vehicle", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider()

        // Make Dropdown
        Text("Make *", color = Color.Gray)
        DropdownField(
            label = "Select Make",
            value = state.selectedMake?.name,
            items = state.makes,
            itemLabel = { it.name },
            onItemSelected = onMakeSelected
        )

        // Model Dropdown
        Text("Model *", color = Color.Gray)
        DropdownField(
            label = "Select Model",
            value = state.selectedModel?.name,
            items = state.models,
            itemLabel = { it.name },
            onItemSelected = onModelSelected,
            enabled = state.selectedMake != null
        )

        // Year Dropdown
        Text("Year *", color = Color.Gray)
        DropdownField(
            label = "Select Year",
            value = state.selectedYear?.year?.toString(),
            items = state.years,
            itemLabel = { it.year.toString() },
            onItemSelected = onYearSelected,
            enabled = state.selectedModel != null
        )

        // Trim Dropdown
        Text("Trim", color = Color.Gray)
        DropdownField(
            label = "Select Trim",
            value = state.selectedTrim?.name,
            items = state.trims,
            itemLabel = { it.name },
            onItemSelected = onTrimSelected,
            enabled = state.selectedYear != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), // Brand Blue
            enabled = state.selectedTrim != null // Enable only when Trim is selected? Or strict?
        ) {
            Text("Save", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    value: String?,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value ?: label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF0F0F0),
                focusedBorderColor = Color(0xFF2196F3),
                focusedLabelColor = Color(0xFF2196F3)
            ),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
