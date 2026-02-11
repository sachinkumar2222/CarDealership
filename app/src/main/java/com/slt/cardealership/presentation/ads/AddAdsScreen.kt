package com.slt.cardealership.presentation.ads

import com.slt.cardealership.presentation.navigation.HomeRoutes

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.R
// Removed invalid import
import com.slt.cardealership.presentation.vehicle.VehicleViewModel
import com.slt.cardealership.presentation.common.LabeledTextField
import com.slt.cardealership.presentation.common.AnimatedDropdown
import com.slt.cardealership.presentation.common.GradientButton
import com.slt.cardealership.presentation.common.SectionTitle
import com.slt.cardealership.presentation.common.GoalSelectionCard
import com.slt.cardealership.ui.theme.BrandBlue
import kotlinx.coroutines.flow.collectLatest

// --- Constants ---
private val themeColor = BrandBlue

// Mock Goal Icons
private val goalIcons = mapOf(
    "Sales" to R.drawable.ad_goal1,
    "Website Traffic" to R.drawable.ad_goal2,
    "Leads" to R.drawable.ad_goal3,
    "Product offers" to R.drawable.ad_goal4,
    "Create a custom goal" to R.drawable.ad_goal5
)

// Moved makeMap from ViewModel to here to solve privacy error
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
    val onFormChange = remember<(AdFormState) -> Unit> {
        { newState -> viewModel.onFormStateChange(newState) }
    }

    val onAdTypeChange = remember<(String) -> Unit> {
        { newType ->
            val showLocation = newType in listOf("General", "Co-op")
            onFormChange(
                formState.copy(
                    adType = newType,
                    showLocation = showLocation,
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
                        selectedGoalTypeName = "",
                        selectedGoalTypeId = 0
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

    val onMakeChange = remember<(String) -> Unit> {
        { makeName ->
            val makeId = makeMap[makeName.lowercase()] ?: 0
            onFormChange(formState.copy(makeName = makeName, makeId = makeId, modelName = null, modelId = null))
            if (formState.adType == "General") {
                viewModel.fetchModelsForMake(makeId)
            }
        }
    }

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
                modifier = Modifier.shadow(8.dp),
                title = { Text(
                    "Add Advertisement",
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
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (formState.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                    }

                    formState.formError?.let {
                        Log.d("AddAdsScreen", "Form error: $it")
                    }

                    SectionTitle(title = "Advertisement Details")
                    
                    LabeledTextField(
                        label = "Advertisement name *",
                        value = formState.adName,
                        onValueChange = { onFormChange(formState.copy(adName = it)) },
                        placeholder = "Advertisement name",
                        isError = formState.formError != null && formState.adName.isBlank()
                    )

                    LabeledTextField(
                        label = "Advertisement Description",
                        value = formState.adDescription,
                        onValueChange = { onFormChange(formState.copy(adDescription = it)) },
                        placeholder = "Advertisement Description",
                        minLines = 3,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Select Advertisement Goal *")

                    if (goalState.isLoading) {
                        CircularProgressIndicator(color = BrandBlue)
                    } else {
                        val goalError = goalState.error
                        if (goalError != null) {
                            Text(goalError, color = MaterialTheme.colorScheme.error)
                        } else {
                            goalState.goals.forEach { goal ->
                                GoalSelectionCard(
                                    title = goal.name,
                                    subtitle = goal.description ?: "Select this goal",
                                    icon = goalIcons.getOrDefault(goal.name, R.drawable.ad_goal5),
                                    selectedGoal = formState.selectedGoalName,
                                    onSelect = onGoalChange
                                )
                            }
                        }
                    }

                    LabeledTextField(
                        label = "Goal Url *",
                        value = formState.goalUrl,
                        onValueChange = { onFormChange(formState.copy(goalUrl = it)) },
                        placeholder = "Goal Url",
                        isError = formState.formError != null && formState.goalUrl.isBlank()
                    )

                    AnimatedVisibility(visible = goalTypeState.types.isNotEmpty() && !goalTypeState.isLoading) {
                        AnimatedDropdown(
                            label = "Goal Category Type",
                            options = goalTypeState.types.map { it.name },
                            selectedOption = formState.selectedGoalTypeName,
                            onOptionSelected = onGoalTypeChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Advertisement Type *")
                    AnimatedDropdown(
                        label = "Select Type *",
                        options = viewModel.adTypeOptions,
                        selectedOption = formState.adType,
                        onOptionSelected = onAdTypeChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- Conditional UI ---
                    AnimatedVisibility(visible = formState.adType in listOf("General", "Co-op")) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            AnimatedDropdown(
                                label = "Condition *",
                                options = viewModel.conditionOptions,
                                selectedOption = formState.condition ?: "",
                                onOptionSelected = { onFormChange(formState.copy(condition = it)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            AnimatedDropdown(
                                label = "Make *",
                                options = vehicleViewModel.makeOptions,
                                selectedOption = formState.makeName ?: "",
                                onOptionSelected = onMakeChange,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    AnimatedVisibility(visible = formState.adType == "General") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            AnimatedDropdown(
                                label = "Model *",
                                options = modelState.models.map { it.name },
                                selectedOption = formState.modelName ?: "",
                                onOptionSelected = onModelChange,
                                enabled = !modelState.isLoading && formState.makeId != null,
                                modifier = Modifier.fillMaxWidth()
                            )

                            LabeledTextField(
                                label = "Year",
                                value = formState.year?.toString() ?: "",
                                onValueChange = { onFormChange(formState.copy(year = it.toIntOrNull())) },
                                placeholder = "Year",
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Advertisement Date")
                    
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = formState.startDateMillis ?: System.currentTimeMillis()
                    )
                    var showDatePicker by remember { mutableStateOf(false) }

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val selectedDateMillis = datePickerState.selectedDateMillis
                                        if (selectedDateMillis != null) {
                                            val formattedDate = java.text.SimpleDateFormat(
                                                "MMM dd, yyyy",
                                                java.util.Locale.getDefault()
                                            ).format(java.util.Date(selectedDateMillis))

                                            onFormChange(
                                                formState.copy(
                                                    startDate = formattedDate,
                                                    startDateMillis = selectedDateMillis
                                                )
                                            )
                                        }
                                        showDatePicker = false
                                    }
                                ) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel", color = themeColor)
                                }
                            },
                            colors = DatePickerDefaults.colors(containerColor = Color.White)
                        ) {
                            DatePicker(
                                state = datePickerState,
                                colors = DatePickerDefaults.colors(
                                    selectedDayContainerColor = themeColor,
                                    todayDateBorderColor = themeColor,
                                    todayContentColor = themeColor,
                                    selectedYearContainerColor = themeColor,
                                    currentYearContentColor = themeColor,
                                    weekdayContentColor = themeColor,
                                    headlineContentColor = themeColor
                                )
                            )
                        }
                    }

                    Box {
                        LabeledTextField(
                            label = "Start Date *",
                            value = formState.startDate,
                            onValueChange = { },
                            placeholder = "Start Date",
                            readOnly = true,
                            enabled = false, // Visual only
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    "Calendar",
                                    tint = themeColor
                                )
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(top = 28.dp) // Cover the field
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatePicker = true }
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = formState.noEndDate,
                            onCheckedChange = { onFormChange(formState.copy(noEndDate = it)) },
                            colors = CheckboxDefaults.colors(checkedColor = themeColor)
                        )
                        Text("No End Date")
                    }

                    AnimatedVisibility(visible = formState.showLocation) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionTitle(title = "Advertisement Location")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text("80012 Wheat Ridge Colorado", modifier = Modifier.padding(16.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = true,
                                    onClick = { },
                                    colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                                )
                                Text("Within a radius around my business address")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = false,
                                    onClick = { },
                                    colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                                )
                                Text("Within a specific location")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
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
                onClick = { viewModel.saveAdvertisement() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
                enabled = !formState.isSaving
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        "Save Ad",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
