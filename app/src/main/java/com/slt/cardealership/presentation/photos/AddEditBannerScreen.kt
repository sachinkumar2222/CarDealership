package com.slt.cardealership.presentation.photos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.slt.cardealership.presentation.common.LabeledTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday

import androidx.compose.material.icons.filled.Image
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
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import com.slt.cardealership.ui.theme.BrandBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBannerScreen(
    uiState: PhotosUiState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Direct content for BottomSheet
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row (acting as simplified TopAppBar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = if (uiState.isEditingBanner) "Edit Global Banner" else "Add Global Banner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Banner Image Picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable { imageLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.bannerImageUri != null) {
                AsyncImage(
                    model = uiState.bannerImageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!uiState.bannerExistingImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = uiState.bannerExistingImageUrl,
                    contentDescription = "Existing Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Text("Tap to select image", color = Color.Gray)
                }
            }
        }

        // Title
        LabeledTextField(
            label = "Title *",
            value = uiState.bannerTitle,
            onValueChange = onTitleChange,
            placeholder = "Enter title"
        )

        // URL
        LabeledTextField(
            label = "Destination URL (Optional)",
            value = uiState.bannerUrl,
            onValueChange = onUrlChange,
            placeholder = "Enter destination URL"
        )

        // Start Date Picker
        DatePickerField(
            label = "Start Date",
            selectedDate = uiState.bannerStartDate,
            onDateSelected = onStartDateChange
        )

        // End Date Picker
        DatePickerField(
            label = "End Date (Optional)",
            selectedDate = uiState.bannerEndDate,
            onDateSelected = onEndDateChange
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button (Full Width for Bottom Sheet)
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Save Banner", modifier = Modifier.padding(8.dp))
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateText = remember(selectedDate) {
        if (selectedDate != null) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate))
        } else {
            ""
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        LabeledTextField(
            label = label,
            value = dateText,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = "Select Date",
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = Color.Black)
            }
        )
        // Overlay for click
        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK", color = BrandBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = BrandBlue)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = BrandBlue,
                    todayDateBorderColor = BrandBlue,
                    todayContentColor = BrandBlue
                )
            )
        }
    }
}
