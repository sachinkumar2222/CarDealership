package com.slt.cardealership.presentation.profile

// --- NEW: Imports for Material 3 Date Picker ---
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.slt.cardealership.presentation.common.LabeledTextField
import com.slt.cardealership.presentation.common.AnimatedDropdown
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// --- End New Imports ---

import android.widget.Toast // Kept original import
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.ui.theme.CarDealershipTheme
// Removed java.util.Calendar import as it's no longer needed here

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(), // Inject ProfileViewModel
    onSaveSuccess: () -> Unit // Callback for successful save (e.g., navigate back)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val editableProfile by viewModel.editableProfile.collectAsState() // Observe the editable draft

    // --- TextField Style ---
    val brandBlue = Color(0xFF2196F3)

    // --- Gradient for the Save Button (Moved to Form) ---

    // --- Gradient for the Save Button (Moved to Form) ---
    // val blueGradient = ... // This is now defined in the Form

    // Handle UI side effects based on ViewModel state
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.NavigateBack -> {
                    onSaveSuccess() // This will call navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        // --- MODIFIED: Removed the bottomBar ---
        containerColor = Color(0xFFF5F7FA) // Light grey background
    ) { paddingValues ->
        when (uiState) {
            ProfileUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Error -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (uiState as ProfileUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { viewModel.fetchFullUserProfile() }) {
                        Text("Try Again")
                    }
                }
            }
            // Display form when data is successfully loaded (or during saving states)
            is ProfileUiState.Success,
            is ProfileUiState.Saving,
            is ProfileUiState.SaveSuccess,
            is ProfileUiState.SaveError -> {
                val profile = editableProfile // Get the current draft profile from ViewModel
                if (profile != null) {
                    // --- MODIFIED: Pass paddingValues and new props to the Form ---
                    EditProfileForm(
                        modifier = Modifier.padding(paddingValues), // Pass padding
                        profile = profile,
                        onUpdateProfileField = viewModel::updateProfileField,

                        onSaveProfile = { viewModel.onSaveProfile() }, // Pass save action
                        isSaving = uiState is ProfileUiState.Saving // Pass saving state
                    )
                } else {
                    // Fallback in case profile is null despite Success state (should ideally not happen)
                    Text(
                        "Profile data not found.",
                        modifier = Modifier.padding(paddingValues).padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // --- NEW: Added OptIn for rememberDatePickerState ---
@Composable
fun EditProfileForm(
    modifier: Modifier = Modifier, // --- NEW: Accept a modifier ---
    profile: DetailedUserProfile,
    onUpdateProfileField: ((DetailedUserProfile) -> DetailedUserProfile) -> Unit,

    onSaveProfile: () -> Unit, // --- NEW: Callback for save ---
    isSaving: Boolean          // --- NEW: State for save button ---
) {


    val genders = listOf("Male", "Female", "Other")
    val languages = listOf("English", "Spanish", "French", "German") // Example languages
    val statuses = listOf("Active", "Inactive")

    // --- NEW: Material 3 Date Picker States ---
    var showDobDialog by remember { mutableStateOf(false) }
    var showDojDialog by remember { mutableStateOf(false) }

    // Formatter to convert Long millis from picker to a String
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    // --- NEW: Gradient for the Save Button (copied from Scaffold) ---
    val brandBlue = Color(0xFF2196F3)
    // --- Theme Wrapper for Date Picker to be Blue ---
    val datePickerTheme = MaterialTheme.colorScheme.copy(
        primary = brandBlue,
        onPrimary = Color.White,
        surface = Color.White, // Dialog background
        onSurface = Color.Black
    )

    // --- MODIFIED: Removed old DatePickerDialog code ---

    // --- M3 Date Picker Dialog for DOB ---
    if (showDobDialog) {
        val datePickerState = rememberDatePickerState()
        MaterialTheme(colorScheme = datePickerTheme) {
            DatePickerDialog(
                onDismissRequest = { showDobDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val dateString = dateFormat.format(Date(millis))
                            onUpdateProfileField { it.copy(dob = dateString) }
                        }
                        showDobDialog = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDobDialog = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    // --- M3 Date Picker Dialog for DOJ ---
    if (showDojDialog) {
        val datePickerState = rememberDatePickerState()
        MaterialTheme(colorScheme = datePickerTheme) {
            DatePickerDialog(
                onDismissRequest = { showDojDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val dateString = dateFormat.format(Date(millis))
                            onUpdateProfileField { it.copy(doj = dateString) }
                        }
                        showDojDialog = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDojDialog = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }


    Column(
        // --- MODIFIED: Applied modifier from Scaffold ---
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Profile Image with Edit Icon ---
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = profile.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.ad_goal1,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .padding(8.dp)
                    .clickable { /* TODO: Handle image edit/upload */ }, // Placeholder for image upload
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit Image",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- User Name and Handle (non-editable, for display) ---
        Text(
            text = "${profile.firstName} ${profile.lastName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = profile.username, // Username is often the email and read-only
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "User Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        // --- Row 1: First Name / Last Name ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabeledTextField(
                value = profile.firstName,
                onValueChange = { newValue -> onUpdateProfileField { it.copy(firstName = newValue) } },
                label = "First Name",
                modifier = Modifier.weight(1f)
            )
            LabeledTextField(
                value = profile.lastName,
                onValueChange = { newValue -> onUpdateProfileField { it.copy(lastName = newValue) } },
                label = "Last Name",
                modifier = Modifier.weight(1f)
            )
        }

        // --- Username (email) - Often read-only or with specific update flow
        LabeledTextField(
            value = profile.username,
            onValueChange = { /* Username/email usually has a separate update process */ },
            label = "Username (email)",
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // --- Role (Display Only) ---
        LabeledTextField(
            value = profile.roleName,
            onValueChange = { /* Role is usually not directly editable */ },
            label = "Role",
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // --- Organization (Display Only) ---
        LabeledTextField(
            value = profile.organizationName.orEmpty(),
            onValueChange = { /* Organization is usually not directly editable */ },
            label = "Organization",
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // --- Department (Editable?) -> User requested Read-only (Grey) ---
        LabeledTextField(
            value = profile.departmentName.orEmpty(),
            // onValueChange = { newValue -> onUpdateProfileField { it.copy(departmentName = newValue) } },
            onValueChange = {}, // Disabled
            label = "Department",
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // --- Designation (Editable?) -> User requested Read-only (Grey) ---
        LabeledTextField(
            value = profile.designationName.orEmpty(),
            // onValueChange = { newValue -> onUpdateProfileField { it.copy(designationName = newValue) } },
            onValueChange = {}, // Disabled
            label = "Designation",
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // --- Dealer (Display Only) ---
        LabeledTextField(
            value = profile.dealerName.orEmpty(),
            onValueChange = { /* Dealer is usually not directly editable */ },
            label = "Dealer",
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // --- Status (Read Only) ---
        LabeledTextField(
            value = if (profile.isActive) "Active" else "Inactive",
            onValueChange = {},
            label = "Status",
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown for Status", tint = Color.Gray)
            }
        )

        AnimatedDropdown(
            label = "Gender",
            options = genders,
            selectedOption = profile.gender.orEmpty(),
            onOptionSelected = { option -> onUpdateProfileField { it.copy(gender = option) } },
            modifier =Modifier.fillMaxWidth(),
            enabled = true
        )

        // Language Dropdown
        AnimatedDropdown(
            label = "Language",
            options = languages,
            selectedOption = profile.language.orEmpty(),
            onOptionSelected = { option -> onUpdateProfileField { it.copy(language = option) } },
            modifier =Modifier.fillMaxWidth(),
            enabled = true
        )

        // DOB Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            LabeledTextField(
                value = profile.dob.orEmpty(),
                onValueChange = { },
                label = "Date of Birth",
                placeholder = "Select date",
                modifier = Modifier.fillMaxWidth(),
                enabled = true, // Keep White
                readOnly = true, // No typing
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date Picker for DOB")
                }
            )
            // Overlay to capture clicks
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 24.dp) // Adjust for label
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDobDialog = true }
            )
        }

        // DOJ Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            LabeledTextField(
                value = profile.doj.orEmpty(),
                onValueChange = { },
                label = "Date of Joining",
                placeholder = "Select date",
                modifier = Modifier.fillMaxWidth(),
                enabled = true, // Keep White
                readOnly = true, // No typing
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date Picker for DOJ")
                }
            )
            // Overlay to capture clicks
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 24.dp) // Adjust for label
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDojDialog = true }
            )
        }

        // --- Phone (Editable) ---
        LabeledTextField(
            value = profile.phone.orEmpty(),
            onValueChange = { newValue -> onUpdateProfileField { it.copy(phone = newValue) } },
            label = "Phone",
            modifier = Modifier.fillMaxWidth()
        )

        // --- Address (Editable) ---
        LabeledTextField(
            value = profile.address.orEmpty(),
            onValueChange = { newValue -> onUpdateProfileField { it.copy(address = newValue) } },
            label = "Address",
            modifier = Modifier.fillMaxWidth()
        )

        // --- NEW: Save Button (Moved from Scaffold) ---
        Button(
            onClick = onSaveProfile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(54.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color(0xFF2196F3).copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ), // Removed blueGradient from here as we use solid color
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Save", modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // --- NEW: Added spacer at the bottom for scroll padding ---
        Spacer(modifier = Modifier.height(32.dp))
    }
}