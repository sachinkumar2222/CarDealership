package com.slt.cardealership.presentation.inventory

import com.slt.cardealership.presentation.navigation.HomeRoutes

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.presentation.common.*
// Removed invalid import
import com.slt.cardealership.presentation.vehicle.AddEditVehicleFormState
import com.slt.cardealership.presentation.vehicle.InventoryEvent
import com.slt.cardealership.presentation.vehicle.VehicleViewModel
import com.slt.cardealership.ui.theme.BrandBlue
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddVehicleScreen(
    navController: NavController,
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(HomeRoutes.Dashboard::class)
    }
    val viewModel: VehicleViewModel = hiltViewModel(parentEntry)

    // Collect state directly from the ViewModel
    val formState by viewModel.formState.collectAsState()
    val modelState by viewModel.modelState.collectAsState()

    // Define the callback function to update the ViewModel's state
    val onStateChange: (AddEditVehicleFormState) -> Unit = { newState ->
        viewModel.onFormStateChange(newState)
    }

    val flagOptions = remember {
        listOf(
            "Carfax", "1 Owner", "AutoCheck", "Dealer Certified",
            "Warranty", "Factory Warranty", "Green Vehicle", "Ext Warranty"
        )
    }

    val context = LocalContext.current

    LaunchedEffect(formState.make) {
        if (formState.make.isNotBlank()) {
            viewModel.fetchModelsForMake(formState.make)
        }
    }

    // Handle navigation on success
    LaunchedEffect(formState.isSaveSuccess) {
        if (formState.isSaveSuccess) {
            viewModel.onSaveSuccessConsumed()
            navController.popBackStack()
        }
    }

    // Handle single-time events
    LaunchedEffect(key1 = Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is InventoryEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    val isFormValid by derivedStateOf {
        formState.vin.isNotBlank() && formState.make.isNotBlank() && formState.model.isNotBlank() && formState.year.isNotBlank()
    }

    Scaffold(
        containerColor = Color(0xFFF0F2F5), // Match AddAdsScreen background
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { 
                    Text(
                        if (formState.isEditing) "Edit Vehicle" else "Add New Vehicle",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (formState.isSaving) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                    }

                    if (formState.formError != null) {
                        Text(
                            text = formState.formError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // --- Primary Info ---
                    SectionTitle(title = "Primary Info")
                    
                    LabeledTextField(
                        label = "VIN *",
                        value = formState.vin,
                        onValueChange = { onStateChange(formState.copy(vin = it)) },
                        placeholder = "Enter VIN",
                        isError = formState.formError != null && formState.vin.isBlank()
                    )

                    AnimatedDropdown(
                        label = "Make *",
                        options = viewModel.makeOptions,
                        selectedOption = formState.make,
                        onOptionSelected = { selectedMake ->
                            onStateChange(formState.copy(make = selectedMake, model = ""))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    AnimatedDropdown(
                        label = "Model *",
                        options = modelState.models.map { it.name },
                        selectedOption = formState.model,
                        onOptionSelected = { selectedModel ->
                            onStateChange(formState.copy(model = selectedModel))
                        },
                        enabled = formState.make.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedDropdown(
                        label = "Year *",
                        options = (2025 downTo 2000).map { it.toString() },
                        selectedOption = formState.year,
                        onOptionSelected = { onStateChange(formState.copy(year = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    LabeledTextField(
                        label = "Trim Name *",
                        value = formState.trim,
                        onValueChange = { onStateChange(formState.copy(trim = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    // --- Vehicle Details ---
                    SectionTitle(title = "Vehicle Details")
                    
                    AnimatedDropdown(
                        label = "Status *",
                        options = listOf("Available", "Sold"),
                        selectedOption = formState.status,
                        onOptionSelected = { onStateChange(formState.copy(status = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedDropdown(
                        label = "Condition *",
                        options = listOf("New", "Used", "CPO"),
                        selectedOption = formState.condition,
                        onOptionSelected = { onStateChange(formState.copy(condition = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedDropdown(
                         label = "Certified *",
                         options = listOf("Yes", "No"),
                         selectedOption = formState.certified,
                         onOptionSelected = { onStateChange(formState.copy(certified = it)) },
                         modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedDropdown(
                        label = "Drivetrain",
                        options = viewModel.drivetrainOptions,
                        selectedOption = formState.drivetrain,
                        onOptionSelected = { onStateChange(formState.copy(drivetrain = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedDropdown(
                        label = "Body Type",
                        options = viewModel.bodyTypeOptions,
                        selectedOption = formState.bodyType,
                        onOptionSelected = { onStateChange(formState.copy(bodyType = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    AnimatedDropdown(
                        label = "Transmission",
                        options = viewModel.transmissionOptions,
                        selectedOption = formState.transmissionType,
                        onOptionSelected = { onStateChange(formState.copy(transmissionType = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedDropdown(
                        label = "Fuel Type",
                        options = viewModel.fuelTypeOptions,
                        selectedOption = formState.fuelType,
                        onOptionSelected = { onStateChange(formState.copy(fuelType = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    // --- Color & Stock ---
                    SectionTitle(title = "Color & Stock")
                    
                    AnimatedDropdown(
                        label = "Interior",
                        options = viewModel.colorOptions,
                        selectedOption = formState.interiorColor,
                        onOptionSelected = { onStateChange(formState.copy(interiorColor = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedDropdown(
                        label = "Exterior",
                        options = viewModel.colorOptions,
                        selectedOption = formState.exteriorColor,
                        onOptionSelected = { onStateChange(formState.copy(exteriorColor = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    LabeledTextField(
                        label = "Stock #",
                        value = formState.stockNumber,
                        onValueChange = { onStateChange(formState.copy(stockNumber = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    LabeledTextField(
                        label = "Mileage",
                        value = formState.mileage,
                        onValueChange = { onStateChange(formState.copy(mileage = it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    // --- Pricing & Specs ---
                    SectionTitle(title = "Pricing & Specs")
                    
                    LabeledTextField(
                        label = "Dealer Price",
                        value = formState.dealerPrice,
                        onValueChange = { onStateChange(formState.copy(dealerPrice = it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    LabeledTextField(
                        label = "Our Price",
                        value = formState.ourPrice,
                        onValueChange = { onStateChange(formState.copy(ourPrice = it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    LabeledTextField(
                        label = "Cylinders",
                        value = formState.engineCylinders,
                        onValueChange = { onStateChange(formState.copy(engineCylinders = it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    LabeledTextField(
                        label = "Doors",
                        value = formState.doors,
                        onValueChange = { onStateChange(formState.copy(doors = it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    LabeledTextField(
                        label = "VDP Link",
                        value = formState.vdpLink,
                        onValueChange = { onStateChange(formState.copy(vdpLink = it)) }
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    // --- Features & Notes ---
                    SectionTitle(title = "Features & Notes")
                    
                    LabeledTextField(
                        label = "Features (Comma separated)",
                        value = formState.features,
                        onValueChange = { onStateChange(formState.copy(features = it)) },
                        minLines = 3,
                        singleLine = false
                    )
                    
                    LabeledTextField(
                       label = "Comments / Dealer Notes",
                       value = formState.comments,
                       onValueChange = { onStateChange(formState.copy(comments = it)) },
                       minLines = 3,
                       singleLine = false
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    // --- Inventory Flags ---
                    SectionTitle(title = "Inventory Flags")
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        flagOptions.forEach { flag ->
                            val isSelected = formState.inventoryFlags[flag] ?: false
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val updatedFlags = formState.inventoryFlags.toMutableMap()
                                    updatedFlags[flag] = !isSelected
                                    onStateChange(formState.copy(inventoryFlags = updatedFlags))
                                },
                                label = { Text(flag) },
                                leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null) } } else { null },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue.copy(alpha = 0.1f),
                                    selectedLabelColor = BrandBlue,
                                    selectedLeadingIconColor = BrandBlue
                                )
                            )
                        }
                    }
                }
            }
            
            // Save Button
            Button(
                onClick = { viewModel.saveVehicle() },
                enabled = isFormValid && !formState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                 if (formState.isSaving) {
                     CircularProgressIndicator(
                         modifier = Modifier.size(24.dp),
                         color = Color.White,
                         strokeWidth = 2.dp
                     )
                 } else {
                     Text(
                         if (formState.isEditing) "Update Vehicle" else "Save Vehicle",
                         style = MaterialTheme.typography.titleMedium, 
                         color = Color.White,
                         fontWeight = FontWeight.SemiBold
                     )
                 }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
