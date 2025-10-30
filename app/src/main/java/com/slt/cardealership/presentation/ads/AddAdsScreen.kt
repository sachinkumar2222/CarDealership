package com.slt.cardealership.presentation.ads

import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.R
import com.slt.cardealership.presentation.home.HomeRoutes
// Import the ViewModel and states
import com.slt.cardealership.presentation.vehicle.VehicleViewModel
import kotlinx.coroutines.flow.collectLatest

// --- Constants ---
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
)
private val themeColor = Color(0xFF2196F3)

// Mock Goal Icons (replace with real R.drawable IDs if they differ)
// These map to the 'name' field from the AdvertisementGoal API
private val goalIcons = mapOf(
    "Sales" to R.drawable.ad_goal1,
    "Website Traffic" to R.drawable.ad_goal2,
    "Leads" to R.drawable.ad_goal3,
    "Product offers" to R.drawable.ad_goal4,
    "Create a custom goal" to R.drawable.ad_goal5
)

// *** FIX 1: Moved makeMap from ViewModel to here to solve privacy error ***
// This map is used by onMakeChange to find the makeId
private val makeMap = mapOf(
    "acura" to 2, "alfa romeo" to 3, "aston martin" to 5, "audi" to 6, "azbv" to 82,
    "b acura" to 87, "bentley" to 7, "bmw" to 8, "buick" to 10, "cadillac" to 11,
    "chevrolet" to 12, "chrysler" to 13, "dodge" to 16, "ferrari" to 18, "fiat" to 19,
    "ford" to 21, "genesis" to 22, "gmc" to 24, "hindustan" to 83, "honda" to 25,
    "hyundai" to 27, "ineos" to 70, "jaguar" to 30, "jeep" to 31, "lamborghini" to 34,
    "land rover" to 35, "lexus" to 36, "lincoln" to 37, "lotus" to 38, "lucid" to 39,
    "maserati" to 40, "mazda" to 42, "mclaren" to 43, "merc" to 84, "mercedes" to 79,
    "mercedes-benz" to 44, "mini" to 46, "mitsubishi" to 47, "naman" to 81, "nissan" to 48,
    "polestar" to 52, "porsche" to 54, "r acura" to 86, "ram" to 55, "rivian" to 56,
    "rolls-royce" to 57, "sasta" to 80, "simran" to 89, "subaru" to 62, "tesla" to 64,
    "test make" to 85, "toyota" to 33,
    "vinfast" to 66, "volkswagen" to 67, "volvo" to 68
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAdsScreen(
    navController: NavController,
    // Scope the ViewModel to the parent graph
    parentEntry: androidx.navigation.NavBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(HomeRoutes.Dashboard::class)
    },
    viewModel: AdsViewModel = hiltViewModel(parentEntry),
    // We also need the VehicleViewModel to get the list of makes
    // We scope it the same way
    vehicleViewModel: VehicleViewModel = hiltViewModel(parentEntry)
) {
    val formState by viewModel.formState.collectAsState()
    val goalState by viewModel.goalState.collectAsState()
    val goalTypeState by viewModel.goalTypeState.collectAsState()
    val modelState by viewModel.modelState.collectAsState()

    val context = LocalContext.current

    // Handle navigation and error events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AdsEvent.NavigateBack -> navController.popBackStack()
                is AdsEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AdsEvent.ShowSuccess -> {
                    // Handled by AdsScreen
                }
            }
        }
    }

    // --- Event Handlers ---

    // Callback to update the form state in the ViewModel
    val onFormChange = remember<(AdFormState) -> Unit> {
        { newState -> viewModel.onFormStateChange(newState) }
    }

    // Callback for when the "Ad Type" dropdown changes
    val onAdTypeChange = remember<(String) -> Unit> {
        { newType ->
            val showLocation = newType in listOf("General", "Co-op")
            onFormChange(
                formState.copy(
                    adType = newType,
                    showLocation = showLocation,
                    // Reset all conditional fields when type changes
                    condition = null,
                    makeName = null,
                    makeId = null,
                    modelName = null,
                    modelId = null,
                    year = null
                )
            )
        }
    }

    val onGoalChange = remember<(String) -> Unit> {
        { goalName ->
            val goal = goalState.goals.find { it.name == goalName }
            if (goal != null) {
                onFormChange(
                    formState.copy(
                        selectedGoalName = goalName,
                        selectedGoalId = goal.id,
                        // --- ADD THESE TWO LINES ---
                        selectedGoalTypeName = "",
                        selectedGoalTypeId = 0
                        // ---------------------------
                    )
                )
                viewModel.fetchGoalTypes(goal.id)
            }
        }
    }
    val onGoalTypeChange = remember<(String) -> Unit> {
        { goalTypeName ->
            val goalType = goalTypeState.types.find { it.name == goalTypeName }
            onFormChange(
                formState.copy(
                    selectedGoalTypeName = goalTypeName,
                    selectedGoalTypeId = goalType?.id ?: 0
                )
            )
        }
    }

    // Callback for when "Make" dropdown changes
    val onMakeChange = remember<(String) -> Unit> {
        { makeName ->
            // *** FIX 1 (cont.): Use the local makeMap ***
            val makeId = makeMap[makeName.lowercase()] ?: 0
            onFormChange(formState.copy(makeName = makeName, makeId = makeId, modelName = null, modelId = null))
            // Only fetch models if ad type is 'General'
            if (formState.adType == "General") {
                viewModel.fetchModelsForMake(makeId)
            }
        }
    }

    // Callback for when "Model" dropdown changes
    val onModelChange = remember<(String) -> Unit> {
        { modelName ->
            val model = modelState.models.find { it.name == modelName }
            onFormChange(formState.copy(modelName = modelName, modelId = model?.id))
        }
    }

    // --- UI ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    if (formState.isEditing) "Edit Advertisement" else "Add Advertisement",
                    fontWeight = FontWeight.Bold
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Make the whole form scrollable
        ) {
            // Main content card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Padding for the card
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Space between elements
                ) {
                    // Show a loading spinner if the form is loading (for editing)
                    if (formState.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    // Show form-wide errors (e.g., validation fail)
                    formState.formError?.let {
                      //  Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                        Log.d("AddAdsScreen", "Form error: $it")
                    }

                    SectionTitle(title = "Advertisement Details")
                    OutlinedTextField(
                        value = formState.adName,
                        onValueChange = { onFormChange(formState.copy(adName = it)) },
                        label = { Text("Advertisement name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = formState.formError != null && formState.adName.isBlank()
                    )

                    OutlinedTextField(
                        value = formState.adDescription,
                        onValueChange = { onFormChange(formState.copy(adDescription = it)) },
                        label = { Text("Advertisement Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Select Advertisement Goal *")

                    // --- Dynamic Goal Selection ---
                    if (goalState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        // *** FIX 4: Assign error to local val before check ***
                        val goalError = goalState.error
                        if (goalError != null) {
                            Text(goalError, color = MaterialTheme.colorScheme.error)
                        } else {
                            // Create a GoalSelectionCard for each goal from the API
                            goalState.goals.forEach { goal ->
                                GoalSelectionCard(
                                    title = goal.name,
                                    subtitle = goal.description ?: "Select this goal",
                                    // Use default icon if not found in map
                                    icon = goalIcons.getOrDefault(goal.name, R.drawable.ad_goal5),
                                    selectedGoal = formState.selectedGoalName,
                                    onSelect = onGoalChange
                                )
                            }
                        }
                    }
                    // --- End Dynamic Goal Selection ---

                    OutlinedTextField(
                        value = formState.goalUrl,
                        onValueChange = { onFormChange(formState.copy(goalUrl = it)) },
                        label = { Text("Goal Url *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = formState.formError != null && formState.goalUrl.isBlank()
                    )

                    // --- Dynamic Goal Type Dropdown ---
                    // Appears only if goal types are available for the selected goal
                    AnimatedVisibility(visible = goalTypeState.types.isNotEmpty() && !goalTypeState.isLoading) {
                        FormDropdown(
                            label = "Goal Category Type",
                            options = goalTypeState.types.map { it.name },
                            selectedOption = formState.selectedGoalTypeName,
                            onOptionSelected = onGoalTypeChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Advertisement Type *")
                    // Ad Type Dropdown
                    FormDropdown(
                        label = "Select Type *",
                        options = viewModel.adTypeOptions,
                        selectedOption = formState.adType,
                        onOptionSelected = onAdTypeChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- *** CONDITIONAL UI *** ---

                    // Fields for "General" and "Co-op"
                    AnimatedVisibility(visible = formState.adType in listOf("General", "Co-op")) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp) // Add padding when this section appears
                        ) {
                            FormDropdown(
                                label = "Condition *",
                                options = viewModel.conditionOptions,
                                selectedOption = formState.condition ?: "",
                                onOptionSelected = { onFormChange(formState.copy(condition = it)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FormDropdown(
                                label = "Make *",
                                // Use makeOptions from VehicleViewModel
                                options = vehicleViewModel.makeOptions,
                                selectedOption = formState.makeName ?: "",
                                onOptionSelected = onMakeChange,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Fields only for "General"
                    AnimatedVisibility(visible = formState.adType == "General") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp) // Add padding when this section appears
                        ) {
                            FormDropdown(
                                label = "Model *",
                                options = modelState.models.map { it.name },
                                selectedOption = formState.modelName ?: "",
                                onOptionSelected = onModelChange,
                                enabled = !modelState.isLoading && formState.makeId != null,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = formState.year?.toString() ?: "",
                                onValueChange = { onFormChange(formState.copy(year = it.toIntOrNull())) },
                                label = { Text("Year") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // --- *** END CONDITIONAL UI *** ---

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Advertisement Date")
                    OutlinedTextField(
                        value = formState.startDate,
                        onValueChange = { onFormChange(formState.copy(startDate = it)) },
                        label = { Text("Start Date *") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarToday, "Calendar") }
                        // TODO: Replace with a real DatePickerDialog
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = formState.noEndDate,
                            onCheckedChange = { onFormChange(formState.copy(noEndDate = it)) },
                            colors = CheckboxDefaults.colors(checkedColor = themeColor)
                        )
                        Text("No End Date")
                    }

                    // --- Conditional Location Section ---
                    AnimatedVisibility(visible = formState.showLocation) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionTitle(title = "Advertisement Location")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text("80012 Wheat Ridge Colorado", modifier = Modifier.padding(16.dp)) // Hardcoded
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = true,
                                    onClick = { /*TODO: Add logic to state*/ },
                                    colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                                )
                                Text("Within a radius around my business address")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = false,
                                    onClick = { /*TODO: Add logic to state*/ },
                                    colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                                )
                                Text("Within a specific location")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // *** FIX 2: Use formState.locationRadius ***
                            Text("Radius: ${formState.locationRadius.toInt()} miles")
                            Slider(
                                value = formState.locationRadius,
                                onValueChange = { onFormChange(formState.copy(locationRadius = it)) },
                                valueRange = 1f..100f,
                                steps = 98,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColor,
                                    activeTrackColor = themeColor,
                                    inactiveTrackColor = themeColor.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                onClick = { viewModel.saveAdvertisement() }, // Call save
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp), // Padding for the button
                enabled = !formState.isSaving // Disable when saving
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (formState.isEditing) "Update Ad" else "Save Ad",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
        }
    }
}

// --- Reusable Composables ---

@Composable
fun GradientButton(
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
                else Modifier
                    .background(Color.Gray.copy(alpha = 0.5f))
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color.Black.copy(alpha = 0.8f),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun GoalSelectionCard(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    selectedGoal: String,
    onSelect: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect(title) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selectedGoal == title) themeColor else Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
            RadioButton(
                selected = selectedGoal == title,
                onClick = { onSelect(title) },
                colors = RadioButtonDefaults.colors(selectedColor = themeColor)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // Added enabled flag
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = enabled && expanded, // Only expand if enabled
        onExpandedChange = { if (enabled) expanded = !expanded }, // Only change if enabled
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled, // Pass enabled state
            // *** FIX 3: Use OutlinedTextFieldDefaults.colors ***
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColor,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                disabledTextColor = Color.Black.copy(alpha = 0.6f),
                disabledLabelColor = Color.Gray,
                disabledBorderColor = Color.LightGray.copy(alpha = 0.7f)
            ),
        )
        ExposedDropdownMenu(
            expanded = enabled && expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddAdsScreenPreview() {
    MaterialTheme {
        // This preview will show a blank form.
        // To preview an 'edit' state, you'd need to mock the ViewModel.
        AddAdsScreen(navController = rememberNavController())
    }
}

