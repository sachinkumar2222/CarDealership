package com.slt.cardealership.presentation.inventory

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Correct import
import androidx.navigation.NavController
import com.slt.cardealership.presentation.home.HomeRoutes
import com.slt.cardealership.presentation.vehicle.AddEditVehicleFormState // *** 1. ADD THIS IMPORT ***
import com.slt.cardealership.presentation.vehicle.InventoryEvent       // *** 2. ADD THIS IMPORT ***
import com.slt.cardealership.presentation.vehicle.VehicleViewModel     // *** 3. ADD THIS IMPORT ***
import kotlinx.coroutines.flow.collectLatest // Needed for event collection

// Define gradient and a solid color for consistency
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3), // Light Blue
        Color(0xFF1565C0)  // Dark Blue
    )
)
private val themeColor = Color(0xFF2196F3)

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddVehicleScreen(
    navController: NavController,
   // viewModel: VehicleViewModel = hiltViewModel() // *** 4. CHANGED TO VehicleViewModel ***
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

    // Handle single-time events (just errors now)
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
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                // Determine title based on editing state
                title = { Text(
                    if (formState.isEditing) "Edit Vehicle" else "Add New Vehicle",
                    fontWeight = FontWeight.Bold
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("AddVehicleScreen", "Back button clicked!")

                        // *** Log the current back stack entries ***
                        val backStack = navController.currentBackStack.value
                        Log.d("AddVehicleScreen", "Current Back Stack (${backStack.size} entries):")
                        backStack.forEachIndexed { index, entry ->
                            Log.d("AddVehicleScreen", "  [$index]: Route=${entry.destination.route}, ID=${entry.id}")
                        }
                        // Log the destination *before* this one, if it exists
                        val previousEntry = navController.previousBackStackEntry
                        Log.d("AddVehicleScreen", "Previous Entry: Route=${previousEntry?.destination?.route}, ID=${previousEntry?.id}")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                GradientButton(
                    // Call viewModel.saveVehicle() on click
                    onClick = { viewModel.saveVehicle() },
                    // Disable button if form is invalid OR if saving is in progress
                    enabled = isFormValid && !formState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(50.dp)
                ) {
                    // Show ProgressIndicator when saving
                    if (formState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (formState.isEditing) "Update Vehicle" else "Save Vehicle",
                            style = MaterialTheme.typography.titleMedium, color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (formState.formError != null) {
                Text(
                    text = formState.formError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- Connect all input fields to use formState and onStateChange ---
            FormSection(title = "Primary Info") {
                FormTextField(
                    value = formState.vin,
                    // Use the onStateChange lambda with .copy()
                    onValueChange = { onStateChange(formState.copy(vin = it)) },
                    label = "VIN*",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // TODO: These dropdowns should be populated from the ViewModel
                    FormDropdown(
                        label = "Make*",
                        options = viewModel.makeOptions,
                        selectedOption = formState.make,
                        onOptionSelected = { selectedMake ->
                            // When make changes, clear the model selection
                            onStateChange(formState.copy(make = selectedMake, model = ""))
                            // LaunchedEffect above will trigger fetchModelsForMake
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FormDropdown(
                        label = "Model*",
                        // Use names from fetched models
                        options = modelState.models.map { it.name },
                        selectedOption = formState.model,
                        onOptionSelected = { selectedModel ->
                            onStateChange(formState.copy(model = selectedModel))
                        },
                        // Optionally disable while loading models
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormDropdown("Year*", (2025 downTo 2000).map { it.toString() }, formState.year, { onStateChange(formState.copy(year = it)) }, Modifier.weight(1f))
                    FormTextField(value = formState.trim, onValueChange = { onStateChange(formState.copy(trim = it)) }, label = "Trim Name*", modifier = Modifier.weight(1f))
                }
            }

            FormSection(title = "Vehicle Details") {
                StyledRadioGroup("Status*", listOf("Available", "Sold"), formState.status, { onStateChange(formState.copy(status = it)) })
                StyledRadioGroup("Condition*", listOf("New", "Used", "CPO"), formState.condition, { onStateChange(formState.copy(condition = it)) })
                StyledRadioGroup("Certified*", listOf("Yes", "No"), formState.certified, { onStateChange(formState.copy(certified = it)) })

                // TODO: These dropdowns should be populated from the ViewModel
                FormDropdown("Drivetrain*", viewModel.drivetrainOptions, formState.drivetrain, { onStateChange(formState.copy(drivetrain = it)) })
                FormDropdown("Body Type*", viewModel.bodyTypeOptions, formState.bodyType, { onStateChange(formState.copy(bodyType = it)) })
                FormDropdown("Transmission*", viewModel.transmissionOptions, formState.transmissionType, { onStateChange(formState.copy(transmissionType = it)) })
                FormDropdown("Fuel Type*", viewModel.fuelTypeOptions, formState.fuelType, { onStateChange(formState.copy(fuelType = it)) })
            }

            FormSection(title = "Color & Stock") {
                // TODO: These dropdowns should be populated from the ViewModel
                FormDropdown("Interior Color*", viewModel.colorOptions, formState.interiorColor, { onStateChange(formState.copy(interiorColor = it)) })
                FormDropdown("Exterior Color*", viewModel.colorOptions, formState.exteriorColor, { onStateChange(formState.copy(exteriorColor = it)) })
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormTextField(value = formState.stockNumber, onValueChange = { onStateChange(formState.copy(stockNumber = it)) }, label = "Stock Number", modifier = Modifier.weight(1f))
                    FormTextField(value = formState.mileage, onValueChange = { onStateChange(formState.copy(mileage = it)) }, label = "Mileage", modifier = Modifier.weight(1f))
                }
            }

            FormSection(title = "Pricing & Specs") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormTextField(value = formState.dealerPrice, onValueChange = { onStateChange(formState.copy(dealerPrice = it)) }, label = "Dealer Price", modifier = Modifier.weight(1f))
                    FormTextField(value = formState.ourPrice, onValueChange = { onStateChange(formState.copy(ourPrice = it)) }, label = "Our Price", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormTextField(value = formState.engineCylinders, onValueChange = { onStateChange(formState.copy(engineCylinders = it)) }, label = "Cylinders", modifier = Modifier.weight(1f))
                    FormTextField(value = formState.doors, onValueChange = { onStateChange(formState.copy(doors = it)) }, label = "Doors", modifier = Modifier.weight(1f))
                }
                FormTextField(value = formState.vdpLink, onValueChange = { onStateChange(formState.copy(vdpLink = it)) }, label = "VDP Link", modifier = Modifier.fillMaxWidth())
            }

            FormSection(title = "Features & Notes") {
                FormTextField(value = formState.features, onValueChange = { onStateChange(formState.copy(features = it)) }, label = "Features (Comma separated)", modifier = Modifier.fillMaxWidth().height(120.dp))
                FormTextField(value = formState.comments, onValueChange = { onStateChange(formState.copy(comments = it)) }, label = "Comments / Dealer Notes", modifier = Modifier.fillMaxWidth().height(120.dp))
            }

            FormSection(title = "Inventory Flags") {
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
                                // Pass the updated state with the new map
                                onStateChange(formState.copy(inventoryFlags = updatedFlags))
                            },
                            label = { Text(flag) },
                            leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null) } } else { null },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.2f),
                                selectedLabelColor = themeColor,
                                selectedLeadingIconColor = themeColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
        }
    }
}

@Composable
private fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (enabled) Modifier
                    .background(blueGradient)
                    .clickable(onClick = onClick)
                else Modifier.background(Color.Gray.copy(alpha = 0.5f))
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = themeColor,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.8f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.8f))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColor,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f)
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun StyledRadioGroup(title: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.Black.copy(alpha = 0.7f))
        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) themeColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) themeColor else Color.DarkGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (index < options.lastIndex) {
                        Divider(modifier = Modifier.width(1.dp).height(48.dp), color = Color.LightGray.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}
