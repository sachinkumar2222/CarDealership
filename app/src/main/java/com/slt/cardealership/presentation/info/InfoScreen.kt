package com.slt.cardealership.presentation.info

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.HourDetails // <-- IMPORT THE NESTED DATA CLASS
import com.slt.cardealership.ui.theme.CarDealershipTheme

@Composable
fun InfoScreen(viewModel: InfoViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is InfoUiState.Loading -> CircularProgressIndicator()
            is InfoUiState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Log.d("info","${state.message}")
            }
            is InfoUiState.Success -> {
                DealerInfoContent(dealerInfo = state.dealerInfo)
            }
        }
    }
}

@Composable
fun DealerInfoContent(dealerInfo: DealerInfo) {
    // Find the specific hour details for each section from the API data
    val businessHours = dealerInfo.dealerHours?.find { it.hoursType == "general" }?.hourDetails
    val partsHours = dealerInfo.dealerHours?.find { it.hoursType == "parts" }?.hourDetails
    val serviceHours = dealerInfo.dealerHours?.find { it.hoursType == "service" }?.hourDetails

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DealershipHeader(dealerInfo) }
        item { BasicInfoSection(dealerInfo) }
        // Pass the live data down to the composables
        item { AmenitiesSection(dealerInfo) }
        item { BusinessDetailsSection(dealerInfo) } // This section seems to still use placeholders
        item { TellAboutSection(dealerInfo) }
        // Pass the filtered hour lists to the cards
        item { BusinessHoursCard("Business Hours", businessHours) }
        item { BusinessHoursCard("Parts Hours", partsHours) }
        item { BusinessHoursCard("Service Hours", serviceHours) }
    }
}

@Composable
fun DealershipHeader(dealerInfo: DealerInfo) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            // Use Coil's AsyncImage to load the first showroom image from the URL
            AsyncImage(
                model = dealerInfo.showroomImages?.firstOrNull(),
                contentDescription = "Dealership exterior",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.toyota), // Show this while loading
                error = painterResource(id = R.drawable.toyota),       // Show this if loading fails
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = dealerInfo.name ?: "Dealership Name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, "Verified", tint = Color(0xFF34A853))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Claimed & Verified", color = Color(0xFF34A853), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BasicInfoSection(dealerInfo: DealerInfo) {
    // Construct the full address from multiple API fields
    val fullAddress = listOfNotNull(
        dealerInfo.address,
        dealerInfo.city,
        dealerInfo.state,
        dealerInfo.zipCode
    ).joinToString(", ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            InfoRow(icon = Icons.Default.LocationOn, text = if (fullAddress.isBlank()) "N/A" else fullAddress)
            InfoRow(icon = Icons.Default.Phone, text = dealerInfo.phone ?: "N/A")
            InfoRow(icon = Icons.Default.DateRange, text = "Add business opening date") // Placeholder
            InfoRow(icon = Icons.Default.Link, text = dealerInfo.websiteUrl ?: "N/A")
            InfoRow(icon = Icons.Default.Email, text = dealerInfo.email ?: "N/A")
            // ... other rows can be updated similarly when you add them to the data model
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String, contentDescription: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /* TODO: Implement edit dialogs for these rows */ }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AmenitiesSection(dealerInfo: DealerInfo) {
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    val amenities = dealerInfo.amenities

    if (showAccessibilityDialog) {
        AccessibilityEditDialog(
            onDismiss = { showAccessibilityDialog = false },
            onSave = {
                Log.d("AmenitiesSection", "Save clicked!")
                showAccessibilityDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TitledSectionWithIcon(
                title = "Accessibility",
                icon = Icons.Default.Accessibility,
                onEditClick = { showAccessibilityDialog = true },
                content = {
                    Column {
                        // Use live data, with `?: false` as a safe fallback
                        AmenityRow("Wheelchair accessible entrance", amenities?.isEntrance ?: false)
                        AmenityRow("Wheelchair accessible restroom", amenities?.isRestroom ?: false)
                        AmenityRow("Wheelchair accessible seating", amenities?.isSeating ?: false)
                    }
                }
            )
            Divider()
            TitledSectionWithIcon(
                title = "Amenities",
                icon = Icons.Default.Deck,
                onEditClick = { /* TODO */ },
                content = {
                    Column {
                        AmenityRow("Parking", amenities?.isParking ?: false)
                        AmenityRow("Kids play area", amenities?.isKidsPlayArea ?: false)
                        AmenityRow("Wifi", amenities?.isWifi ?: false)
                    }
                }
            )
        }
    }
}

@Composable
fun AccessibilityEditDialog( onDismiss: () -> Unit, onSave: () -> Unit) {
    // This dialog logic remains the same
    var isEntranceAccessible by remember { mutableStateOf(true) }
    var isRestroomAccessible by remember { mutableStateOf(false) }
    var isSeatingAccessible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Accessibility") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isEntranceAccessible = !isEntranceAccessible }.padding(vertical = 8.dp)) {
                    Checkbox(checked = isEntranceAccessible, onCheckedChange = { isEntranceAccessible = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Wheelchair accessible entrance")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isRestroomAccessible = !isRestroomAccessible }.padding(vertical = 8.dp)) {
                    Checkbox(checked = isRestroomAccessible, onCheckedChange = { isRestroomAccessible = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Wheelchair accessible restroom")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isSeatingAccessible = !isSeatingAccessible }.padding(vertical = 8.dp)) {
                    Checkbox(checked = isSeatingAccessible, onCheckedChange = { isSeatingAccessible = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Wheelchair accessible seating")
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun TitledSectionWithIcon(title: String, icon: ImageVector, onEditClick: () -> Unit, content: @Composable () -> Unit) {
    // This function remains the same
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
        IconButton(onClick = onEditClick) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AmenityRow(text: String, available: Boolean) {
    // This function remains the same
    Row(verticalAlignment = Alignment.CenterVertically) {
        val icon = if (available) Icons.Default.CheckCircle else Icons.Default.Cancel
        val tint = if (available) Color(0xFF34A853) else Color.Red
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BusinessDetailsSection(dealerInfo: DealerInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Adds space between each row
        ) {
            // Use live data from the dealerInfo object with safe fallbacks ("N/A")
            // The `replaceFirstChar` is used to capitalize the first letter (e.g., "automobile" -> "Automobile")
            BusinessDetailRow(
                label = "Business Segment",
                value = dealerInfo.dealerCategory?.businessSegment?.replaceFirstChar { it.uppercase() } ?: "N/A"
            )
            BusinessDetailRow(
                label = "Business Category",
                value = dealerInfo.dealerCategory?.name?.replaceFirstChar { it.uppercase() } ?: "N/A"
            )
            BusinessDetailRow(
                label = "Dealership Type",
                value = dealerInfo.dealerType?.replaceFirstChar { it.uppercase() } ?: "N/A"
            )
        }
    }
}

/**
 * A helper composable for a single row within the Business Details card.
 * It displays a label and its corresponding value.
 */
@Composable
fun BusinessDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        // The IconButton makes the edit icon clickable
        IconButton(onClick = { /* TODO: Implement Edit Dialog for this row */ }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $label",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TellAboutSection(dealerInfo: DealerInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tell About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO */ }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Use the "aboutText" from the API, with a fallback message
            Text(
                text = if (dealerInfo.aboutText.isNullOrBlank()) "No description provided." else dealerInfo.aboutText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun BusinessHoursCard(title: String, hoursList: List<HourDetails>?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Schedule, contentDescription = "Hours", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO */ }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Check if the list from the API is available
            if (hoursList.isNullOrEmpty()) {
                Text("Hours not available for this department.")
            } else {
                // If it is, loop through it and display the hours for each day
                hoursList.forEach { dayDetails ->
                    val hoursText = if (dayDetails.isClose == true) {
                        "Closed"
                    } else {
                        "${dayDetails.openTime} - ${dayDetails.closeTime}"
                    }
                    HoursRow(day = dayDetails.day ?: "Unknown", hours = hoursText)
                }
            }
        }
    }
}

@Composable
fun HoursRow(day: String, hours: String) {
    // This function remains the same
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(text = ":", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            text = hours,
            color = if (hours == "Closed") Color.Red else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    CarDealershipTheme {
        // Preview will show basic layout as it has no data
        InfoScreen()
    }
}

