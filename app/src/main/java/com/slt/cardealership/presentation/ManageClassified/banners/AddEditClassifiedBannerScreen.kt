package com.slt.cardealership.presentation.ManageClassified.banners

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage

import com.slt.cardealership.presentation.articles.FeaturedImageUploader
import com.slt.cardealership.presentation.articles.FormCard
import com.slt.cardealership.presentation.common.LabeledTextField
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBannerBottomSheet(
    siteId: String,
    bannerId: String? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddEditClassifiedBannerViewModel = hiltViewModel()
) {
    // Manually initialize ViewModel with params
    LaunchedEffect(siteId, bannerId) {
        viewModel.initializeViewModel(siteId, bannerId)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Handle Success
    LaunchedEffect(uiState) {
        if (uiState is AddEditBannerUiState.Content && (uiState as AddEditBannerUiState.Content).isSuccess) {
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight() // Take up 90% of height for the sheet
    ) {
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
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
            is AddEditBannerUiState.Content -> {
                AddEditBannerSheetForm(
                    state = state,
                    onTitleChange = viewModel::onTitleChange,
                    onUrlChange = viewModel::onUrlChange,
                    onStartDateChange = viewModel::onStartDateChange,
                    onEndDateChange = viewModel::onEndDateChange,
                    onImageSelected = viewModel::onImageSelected,
                    onSave = viewModel::saveBanner,
                    onCancel = onDismiss,
                    viewModel = viewModel // Pass VM for image removal if needed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBannerSheetForm(
    state: AddEditBannerUiState.Content,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddEditClassifiedBannerViewModel
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Date Picker States
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDate)
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
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.endDate)
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
        ) { DatePicker(state = datePickerState) }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startDisplay = state.startDate?.let { dateFormat.format(Date(it)) } ?: ""
    val endDisplay = state.endDate?.let { dateFormat.format(Date(it)) } ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp), // Increased padding
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = if (state.isEditMode) "Edit Banner" else "Add Banner",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
            color = Color.Black
        )

        // Banner Image
        BannerImageUploader(
            imageUrl = if (state.imageUri != null) state.imageUri.toString() else state.imageUrl,
            onImageClick = { launcher.launch("image/*") }
        )

        // Title
        LabeledTextField(
            label = "Title *",
            value = state.title,
            onValueChange = onTitleChange
        )

        // URL
        LabeledTextField(
            label = "Destination URL (Optional)",
            value = state.url,
            onValueChange = onUrlChange
        )

        // Start Date
        Box(modifier = Modifier.fillMaxWidth()) {
            LabeledTextField(
                label = "Start Date",
                value = startDisplay,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = Color.Black
                    )
                }
            )
            // Overlay for click
            Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
        }

        // End Date
        Box(modifier = Modifier.fillMaxWidth()) {
            LabeledTextField(
                label = "End Date (Optional)",
                value = endDisplay,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = Color.Black
                    )
                }
            )
            // Overlay for click
            Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
        }

        // Error
        if (state.error != null) {
            Text(
                text = state.error,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Save Button
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(8.dp),
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Banner", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BannerImageUploader(
    imageUrl: String?,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Large height as in screenshot
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEEEE)) // Light grey background
            .clickable(onClick = onImageClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Banner Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image, // Or similar gallery icon
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to select image",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
