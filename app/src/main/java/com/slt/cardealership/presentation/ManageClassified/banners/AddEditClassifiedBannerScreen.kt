package com.slt.cardealership.presentation.ManageClassified.banners

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassifiedBannerScreen(
    siteId: String,
    bannerId: String? = null,
    navController: NavController,
    viewModel: AddEditClassifiedBannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Success/Navigation Effect
    LaunchedEffect(uiState) {
        if (uiState is AddEditBannerUiState.Content && (uiState as AddEditBannerUiState.Content).isSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("should_refresh", true)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = {
                    Text(
                        if (bannerId != null) "Manage Banner" else "Add Banner",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is AddEditBannerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AddEditBannerUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.message, color = Color.Red)
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                is AddEditBannerUiState.Content -> {
                    AddEditBannerContent(
                        state = state,
                        onTitleChange = viewModel::onTitleChange,
                        onUrlChange = viewModel::onUrlChange,
                        onStartDateChange = viewModel::onStartDateChange,
                        onEndDateChange = viewModel::onEndDateChange,
                        onImageSelected = viewModel::onImageSelected,
                        onSave = viewModel::saveBanner,
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBannerContent(
    state: AddEditBannerUiState.Content,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Date Picker States
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.startDate
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onStartDateChange(datePickerState.selectedDateMillis)
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.endDate
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onEndDateChange(datePickerState.selectedDateMillis)
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Formatting Dates for Display
    // Using UTC or default timezone as appropriate - typically UI uses local time for selection
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDisplay = state.startDate?.let { dateFormat.format(Date(it)) } ?: ""
    val endDisplay = state.endDate?.let { dateFormat.format(Date(it)) } ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Validation Error
        if (state.error != null) {
            Text(
                text = state.error,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Title Field
        Column {
            Text(
                text = "Title *",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                )
            )
        }

        // URL Field
        Column {
            Text(
                text = "URL *",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                )
            )
        }

        // Dates Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Start Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start Date",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = startDisplay,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select start date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFEEEEEE),
                        focusedContainerColor = Color(0xFFEEEEEE),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // End Date (Normally visible but disabled first? Requirements say it shows AFTER start date selected)
            // But UI screenshot shows it alongside. I'll make it enabled only if start date exists.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "End Date",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = endDisplay,
                    onValueChange = {},
                    readOnly = true,
                    enabled = state.startDate != null,
                    placeholder = { Text("Select end date") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showEndDatePicker = true },
                            enabled = state.startDate != null
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = if (state.startDate != null) Color.Black else Color.LightGray
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFEEEEEE),
                        focusedContainerColor = Color(0xFFEEEEEE),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        // Note Warning
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFFFC107),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "Note",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "If no dates are selected, Start Date will be set to today's date and there will be no Expiration Date!",
                fontSize = 12.sp,
                color = Color.Black
            )
        }

        // Image Upload
        Column {
            Text(
                text = "Image *",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { launcher.launch("image/*") }
            ) {
                // Dashed Border Background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
                    drawRect(color = Color(0xFF2196F3), style = stroke)
                }

                if (state.imageUri != null) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (state.imageUrl != null) {
                    AsyncImage(
                        model = state.imageUrl,
                        contentDescription = "Existing Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = Color(0xFF2196F3),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Upload",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Upload Image", color = Color.Gray)
                    }
                }
            }
        }

        // Validations Text
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "• (PNG, JPEG, JPG, AVIF and WebP supported)",
                color = Color(0xFFF44336),
                fontSize = 12.sp
            )
            Text(
                text = "• Resolution should be 1250 x 500",
                color = Color(0xFFF44336),
                fontSize = 12.sp
            )
            Text(
                text = "• Maximum file size 500KB",
                color = Color(0xFFF44336),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        // Action Buttons
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(4.dp),
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Save")
            }
        }
    }
}
