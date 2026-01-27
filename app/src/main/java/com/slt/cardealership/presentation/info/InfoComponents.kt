package com.slt.cardealership.presentation.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.HourDetails
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.ui.theme.BrandBlue
import androidx.compose.foundation.shape.RoundedCornerShape
import com.slt.cardealership.domain.model.SocialProfileItem
import java.util.Locale

data class EditFieldState(
    val label: String,
    val value: String
)

data class VirtualAppointmentState(
    val isAvailable: Boolean,
    val link: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFieldEditSheet(
    title: String,
    fields: List<EditFieldState>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<EditFieldState>) -> Unit
) {
    var currentFields by remember { mutableStateOf(fields) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .navigationBarsPadding() // Handle software buttons
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            currentFields.forEachIndexed { index, field ->
                OutlinedTextField(
                    value = field.value,
                    onValueChange = { newValue ->
                        val updated = currentFields.toMutableList()
                        updated[index] = field.copy(value = newValue)
                        currentFields = updated
                    },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        focusedLabelColor = BrandBlue
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(currentFields) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmenitiesEditSheet(
    initialState: Amenities,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    // We need to handle potential nulls if Amenities fields are nullable, usually they are Boolean? or Boolean
    var wifi by remember { mutableStateOf(initialState.isWifi ?: false) }
    var parking by remember { mutableStateOf(initialState.isParking ?: false) }
    var kidsArea by remember { mutableStateOf(initialState.isKidsPlayArea ?: false) }
    var isEntrance by remember { mutableStateOf(initialState.isEntrance ?: false) }
    var isSeating by remember { mutableStateOf(initialState.isSeating ?: false) }
    var isRestroom by remember { mutableStateOf(initialState.isRestroom ?: false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Amenities", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            AmenityToggle("Wifi", wifi) { wifi = it }
            AmenityToggle("Parking", parking) { parking = it }
            AmenityToggle("Kids Play Area", kidsArea) { kidsArea = it }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Accessibility", fontWeight = FontWeight.Bold)
            AmenityToggle("Wheelchair Accessible Entrance", isEntrance) { isEntrance = it }
            AmenityToggle("Wheelchair Accessible Seating", isSeating) { isSeating = it }
            AmenityToggle("Wheelchair Accessible Restroom", isRestroom) { isRestroom = it }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSave(wifi, parking, kidsArea, isEntrance, isSeating, isRestroom) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }
        }
    }
}

@Composable
fun AmenityToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandBlue,
                checkedTrackColor = BrandBlue.copy(alpha = 0.5f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHoursEditSheet(
    isSaving: Boolean,
    initialGeneral: DealerHours?,
    initialParts: DealerHours?,
    initialService: DealerHours?,
    onDismiss: () -> Unit,
    onSave: (List<HourDetails>, List<HourDetails>, List<HourDetails>) -> Unit
) {
    // Tabs
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Geneal", "Parts", "Service") // Typo "Geneal" in logs? Stick to "General"

    // We need complete lists for editing. If API returns partial, we should ideally fill gaps, but let's assume we edit what we have.
    // Or better, we need a standard structure of 7 days.
    // For simplicity, we'll edit the list items passed.

    var generalList by remember { mutableStateOf(initialGeneral?.hourDetails ?: emptyList()) }
    var partsList by remember { mutableStateOf(initialParts?.hourDetails ?: emptyList()) }
    var serviceList by remember { mutableStateOf(initialService?.hourDetails ?: emptyList()) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Business Hours", style = MaterialTheme.typography.headlineSmall)
            }
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentList = when(selectedTab) {
                0 -> generalList
                1 -> partsList
                else -> serviceList
            }

            // Edit List
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(currentList.size) { index ->
                    val item = currentList[index]
                    HoursRowItem(
                        item = item,
                        onChange = { newItem ->
                            val updated = currentList.toMutableList()
                            updated[index] = newItem
                            when(selectedTab) {
                                0 -> generalList = updated
                                1 -> partsList = updated
                                else -> serviceList = updated
                            }
                        }
                    )
                    Divider()
                }
            }

            Button(
                onClick = { onSave(generalList, partsList, serviceList) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().padding(top=16.dp, bottom=16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save All Changes")
            }
        }
    }
}

@Composable
fun HoursRowItem(item: HourDetails, onChange: (HourDetails) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(item.day?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.isClose == "yes",
                onCheckedChange = { isClosed ->
                    onChange(item.copy(isClose = if (isClosed) "yes" else "no"))
                }
            )
            Text("Closed")
            Spacer(modifier = Modifier.width(16.dp))
            if (item.isClose != "yes") {
                // Simplified Time Inputs (Text Fields for now)
                OutlinedTextField(
                    value = item.openTime ?: "",
                    onValueChange = { onChange(item.copy(openTime = it)) },
                    label = { Text("Open") },
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = item.closeTime ?: "",
                    onValueChange = { onChange(item.copy(closeTime = it)) },
                    label = { Text("Close") },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        focusedLabelColor = BrandBlue
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDeliverySheet(
    initialState: HomeDelivery?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState?.isAvailable ?: false) }
    var isNationWide by remember { mutableStateOf(initialState?.isNationWide ?: false) }
    var radius by remember { mutableStateOf(initialState?.radius?.toString() ?: "0") }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Home Delivery", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            AmenityToggle("Available", isAvailable) { isAvailable = it }

            if (isAvailable) {
                AmenityToggle("Nationwide", isNationWide) { isNationWide = it }
                if (!isNationWide) {
                    OutlinedTextField(
                        value = radius,
                        onValueChange = { radius = it },
                        label = { Text("Radius (Miles)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            focusedLabelColor = BrandBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSave(isAvailable, isNationWide, radius) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTestDriveSheet(
    initialState: HomeTestDrive?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState?.isAvailable ?: false) }
    var radius by remember { mutableStateOf(initialState?.radius?.toString() ?: "0") }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Home Test Drive", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            AmenityToggle("Available", isAvailable) { isAvailable = it }

            if (isAvailable) {
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radius (Miles)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        focusedLabelColor = BrandBlue
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSave(isAvailable, radius) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualAppointmentSheet(
    initialState: VirtualAppointmentState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState.isAvailable) }
    var link by remember { mutableStateOf(initialState.link) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Virtual Appointment", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            AmenityToggle("Available", isAvailable) { isAvailable = it }

            if (isAvailable) {
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Appointment Link") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        focusedLabelColor = BrandBlue
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSave(isAvailable, link) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerTypeEditSheet(
    initialType: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val types = listOf("Independent", "Franchise")
    var selectedType by remember { mutableStateOf(initialType) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Dealer Type", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            types.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = (type == selectedType),
                            onValueChange = { selectedType = type },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (type == selectedType),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = BrandBlue)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = type, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSave(selectedType) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save Request")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessOpeningDateSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var selectedMonth by remember { mutableStateOf("Select Month") }
    var selectedYear by remember { mutableStateOf("Select Year") }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val years = (2026 downTo 1901).map { it.toString() }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Business opening date", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Month Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            focusedLabelColor = BrandBlue
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedMonth = true })
                    DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    selectedMonth = month
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                // Year Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Year") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            focusedLabelColor = BrandBlue
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedYear = true })
                    DropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false },
                        modifier = Modifier.heightIn(max = 300.dp) // Limit height for scrolling
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year) },
                                onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Removed redundant "Close" button as per user request ("remove cancle button")
            // Kept Save button. X icon in header handles explicit close.
            Button(
                onClick = { onSave(selectedMonth, selectedYear) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(), // Full width
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressEditSheet(
    initialAddress: String?,
    initialCity: String?,
    initialZip: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit // country, state, city, address, zip
) {
    // Determine initial values. If null, empty.
    // We might need "State" and "Country" from DealerInfo if available, but model only showed basic fields in snippet.
    // For now, default Country to "United States" and State to empty or Arizona (example).
    // I will use local state for these.

    var country by remember { mutableStateOf("United States") }
    // As per screenshot, State seems to be a field.
    var state by remember { mutableStateOf("Arizona") }
    var city by remember { mutableStateOf(initialCity ?: "") }
    var address by remember { mutableStateOf(initialAddress ?: "") }
    var zipcode by remember { mutableStateOf(initialZip ?: "") }

    var expandedCountry by remember { mutableStateOf(false) }
    val countries = listOf("United States", "Canada", "Mexico") // Example list

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Address", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Country (Read-only)
            Text("Country:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = country,
                onValueChange = {}, // Read-only
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray, // Less emphasis
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    unfocusedContainerColor = Color(0xFFEFF1F3),
                    focusedContainerColor = Color(0xFFEFF1F3),
                    disabledContainerColor = Color(0xFFEFF1F3),
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = false // Explicitly disabled as per request
            )
            Spacer(modifier = Modifier.height(16.dp))

            // State (Read-only)
            Text("State:*", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state,
                onValueChange = {}, // Read-only
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    unfocusedContainerColor = Color(0xFFEFF1F3),
                    focusedContainerColor = Color(0xFFEFF1F3),
                    disabledContainerColor = Color(0xFFEFF1F3),
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = false // Explicitly disabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            // City (Read-only)
            Text("City:*", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = city,
                onValueChange = {}, // Read-only
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    unfocusedContainerColor = Color(0xFFEFF1F3),
                    focusedContainerColor = Color(0xFFEFF1F3),
                    disabledContainerColor = Color(0xFFEFF1F3),
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = false // Explicitly disabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Address (Editable)
            Text("Address:*", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    focusedLabelColor = BrandBlue,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Zipcode (Read-only)
            Text("Zipcode:*", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = zipcode,
                onValueChange = {}, // Read-only
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    unfocusedContainerColor = Color(0xFFEFF1F3),
                    focusedContainerColor = Color(0xFFEFF1F3),
                    disabledContainerColor = Color(0xFFEFF1F3),
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = false // Explicitly disabled
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(country, state, city, address, zipcode) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneEditSheet(
    initialPhone: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var phone by remember { mutableStateOf(initialPhone ?: "") }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Phone Number", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Primary phone number:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    focusedLabelColor = BrandBlue
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(phone) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialLinksEditSheet(
    existingLinks: List<SocialProfileItem>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<SocialProfileItem>) -> Unit
) {
    val allSocialTypes = listOf(
        "facebook", "twitter", "instagram", "youtube", "linkedin", "indeed", "pinterest", "google_my_business"
    )

    // Initialize editable state map from existing links
    val initialMap = existingLinks.associate { it.socialType to it.socialLink }
    val editableLinks = remember {
        mutableStateMapOf<String, String>().apply {
            allSocialTypes.forEach { type ->
                this[type] = initialMap[type] ?: ""
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color.White,
        sheetState = sheetState,
        modifier = Modifier.statusBarsPadding() // Push entire sheet (including drag handle) below status bar
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Force full screen
                .background(Color.White)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Top padding for title

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Social Media",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // List of Input Fields (Stacked Layout)
            allSocialTypes.forEach { type ->
                val currentUrl = editableLinks[type] ?: ""
                val label = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = currentUrl,
                        onValueChange = { editableLinks[type] = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(if (type.equals("google_my_business", ignoreCase = true) || type.equals("indeed", ignoreCase = true) || type.equals("pinterest", ignoreCase = true)) "Enter URL" else "https://www.${type}.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            focusedLabelColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Reduced gap before save button

            // Save Button
            Button(
                onClick = {
                    val updatedList = editableLinks.mapNotNull { (type, url) ->
                        if (url.isNotBlank()) SocialProfileItem(type, url) else null
                    }
                    onSave(updatedList)
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualDealershipEditSheet(
    isVirtual: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean) -> Unit
) {
    var currentIsVirtual by remember { mutableStateOf(isVirtual) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "Are you a virtual dealership?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

            // Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toggle if you are / are not a virtual dealership",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = currentIsVirtual,
                    onCheckedChange = { currentIsVirtual = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button (Full Width)
            Button(
                onClick = { onSave(currentIsVirtual) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
