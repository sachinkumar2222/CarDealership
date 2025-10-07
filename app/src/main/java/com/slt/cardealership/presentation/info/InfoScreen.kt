package com.slt.cardealership.presentation.info

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.DealerCategory
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.domain.model.HourDetails // <-- IMPORT THE NESTED DATA CLASS
import com.slt.cardealership.ui.theme.CarDealershipTheme

class EditFieldState(val label: String, initialValue: String) {
    var value by mutableStateOf(initialValue)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    navController: NavController, // <-- 1. Add NavController as a parameter
    viewModel: InfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 2. WRAP THE SCREEN IN A SCAFFOLD ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dealership Info", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F2F5))
            )
        }
    ) { paddingValues ->
        // The rest of your screen content goes inside the Scaffold's content lambda
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Use padding from Scaffold
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
                }
                is InfoUiState.Success -> {
                    // Pass both the viewModel and data down
                    DealerInfoContent(
                        dealerInfo = state.dealerInfo,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DealerInfoContent(dealerInfo: DealerInfo = previewDealerInfo,viewModel: InfoViewModel = hiltViewModel()) {
    val businessHours = dealerInfo.dealerHours?.find { it.hoursType == "general" }
    val partsHours = dealerInfo.dealerHours?.find { it.hoursType == "parts" }
    val serviceHours = dealerInfo.dealerHours?.find { it.hoursType == "service" }
    var isVirtual by remember { mutableStateOf(dealerInfo.isVirtual ?: false) }
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showTestDriveDialog by remember { mutableStateOf(false) }


    if (showDeliveryDialog && dealerInfo.homeDelivery != null) {
        HomeDeliveryDialog(
            initialState = dealerInfo.homeDelivery,
            onDismiss = { showDeliveryDialog = false },
            onSave = { isAvailable, isNationWide, radius ->
                // TODO: Call ViewModel to save these new values
                showDeliveryDialog = false
            }
        )
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color(0xFFF0F8FF)
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp) // Only bottom padding needed
    ) {
        item { HeaderImage(dealerInfo) }
        // Wrap the rest of the content in a Column with padding
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DealerDetailsCard(dealerInfo)
                DeliveryAndTestDriveCard(
                    dealerInfo = dealerInfo,
                    onEditDelivery = { showDeliveryDialog = true },
                    onEditTestDrive = { showTestDriveDialog = true }
                )
                AdditionalFeaturesCard(
                    isVirtual = isVirtual,
                    onVirtualToggled = { newStatus ->
                        isVirtual = newStatus // Update the temporary UI state
                    }
                )
                BusinessTypeCard(dealerInfo)
                AccessibilityAndAmenitiesCard(dealerInfo.amenities)
                BusinessHoursCard(businessHours, partsHours, serviceHours)
                AboutCard(dealerInfo)
            }
        }
    }
}

@Composable
fun HeaderImage(dealerInfo: DealerInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp)
            .padding(12.dp)
            .shadow(4.dp, shape = RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = dealerInfo.headerImageUrl,
            contentDescription = "Dealership exterior",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.toyota)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Content on top of the image
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = dealerInfo.name ?: "Dealership Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dealerInfo.city}, ${dealerInfo.state}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (dealerInfo.isClaimed == true) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painterResource(R.drawable.verified),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Claimed & Verified",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

        }
    }
}



@Composable
fun BusinessTypeCard(dealerInfo: DealerInfo) {
    InfoCard(
        title = "Business Type",
        icon = Icons.Default.Business,
        onEditClick = { /* TODO */ }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            DetailInfoRow(label = "Dealership Type", value = dealerInfo.dealerType ?: "N/A")
            DetailInfoRow(label = "Business Segment", value = dealerInfo.dealerCategory?.businessSegment ?: "N/A")
            DetailInfoRow(label = "Business Category", value = dealerInfo.dealerCategory?.name ?: "N/A")
        }
    }
}

@Composable
fun BusinessHoursCard(general: DealerHours?, parts: DealerHours?, service: DealerHours?) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Business", "Parts", "Service")

    InfoCard(
        title = "Business Hours",
        icon = Icons.Default.Schedule,
        onEditClick = { /* TODO */ }
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (selectedTabIndex) {
                0 -> HoursColumn(hoursData = general)
                1 -> HoursColumn(hoursData = parts)
                2 -> HoursColumn(hoursData = service)
            }
        }
    }
}

@Composable
fun HoursColumn(hoursData: DealerHours?) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val hourDetails = hoursData?.hourDetails

        if (hourDetails.isNullOrEmpty()) {
            Text(text = "Not available", color = Color.Gray, fontSize = 12.sp)
        } else {
            hourDetails.forEach { day ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Day of the week (e.g., "Monday")
                    Text(
                        text = day.day ?: "N/A",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        modifier = Modifier.width(90.dp)
                    )

                    // --- THIS IS THE NEW CUSTOMIZED LOGIC ---
                    if (day.isClose == true) {
                        // Display 'Closed' icon and text
                        Image(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )

                    } else {
                        // Display 'Open' icon and timings
                        Image(
                            painter = painterResource(R.drawable.open),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(32.dp))
                        Text(
                            text = "${day.openTime ?: "--"} - ${day.closeTime ?: "--"}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityAndAmenitiesCard(amenities: Amenities?) {
    InfoCard(
        title = "Amenities",
        icon = Icons.Default.Deck,
        onEditClick = { /* TODO */ }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            CheckmarkRow("Wi-Fi", amenities?.isWifi)
            CheckmarkRow("Parking", amenities?.isParking)
            CheckmarkRow("Kids Play Area", amenities?.isKidsPlayArea)
            CheckmarkRow("Wheelchair Accessible Entrance", amenities?.isEntrance)
            CheckmarkRow("Wheelchair Accessible Seating", amenities?.isSeating)
            CheckmarkRow("Wheelchair Accessible Restroom", amenities?.isRestroom)
        }
    }
}
@Composable
fun AboutCard(dealerInfo: DealerInfo) {
    InfoCard(
        title = "About",
        icon = Icons.Default.Info,
        onEditClick = { /* TODO */ }
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = dealerInfo.aboutText ?: "No description provided.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}


private val previewDealerInfo = DealerInfo(
    id = 1L,
    name = "Preview Dealership Name",
    phone = "(123) 456-7890",
    address = "123 Main St",
    city = "Anytown",
    state = "CA",
    zipCode = "90210",
    websiteUrl = "www.example.com",
    aboutText = "This is a sample description about the dealership for the preview.",
    isClaimed = true,
    dealerType = "Independent",
    dealerCategory = DealerCategory(name = "Car Dealership", businessSegment = "Automobile"),
    amenities = Amenities(
        isEntrance = true,
        isRestroom = false,
        isSeating = true,
        isParking = true,
        isKidsPlayArea = false,
        isWifi = true
    ), // An empty string will make AsyncImage use the error/placeholder drawable
    dealerHours = listOf(
        DealerHours(
            hoursType = "general",
            hourDetails = listOf(
                HourDetails("Monday", "9:00 AM", "6:00 PM", false),
                HourDetails("Tuesday", "9:00 AM", "6:00 PM", false),
                HourDetails("Wednesday", "9:00 AM", "6:00 PM", false),
                HourDetails("Thursday", "9:00 AM", "6:00 PM", false),
                HourDetails("Friday", "9:00 AM", "6:00 PM", false),
                HourDetails("Saturday", "10:00 AM", "4:00 PM", false),
                HourDetails("Sunday", null, null, true) // Represents "Closed"
            )
        ),
        DealerHours( // Add sample data for parts hours
            hoursType = "parts",
            hourDetails = listOf(
                HourDetails("Monday", "8:00 AM", "5:00 PM", false),
                HourDetails("Sunday", null, null, true)
            )
        ),
        DealerHours( // Add sample data for service hours
            hoursType = "service",
            hourDetails = listOf(HourDetails("Monday", "7:30 AM", "5:30 PM", false))
        )
    ),
    isVirtual = false,
    homeDelivery = HomeDelivery(
        isAvailable = true,
        isNationWide = false,
        radius = 50
    ),homeTestDrive = HomeTestDrive(
        isAvailable = false,
        radius = 0
    ),
    headerImageUrl = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFieldEditDialog(
    title: String,
    fields: List<EditFieldState>,
    onDismiss: () -> Unit,
    onSave: (List<EditFieldState>) -> Unit
) {
    // Use the base Dialog for a custom layout
    Dialog(onDismissRequest = onDismiss) {
        // The main container with rounded corners and elevation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp), // Set min/max height
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 1. A more stylish header with an icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painterResource(R.drawable.surmi),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // 2. The scrollable content area for the fields
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(fields) { field ->
                        OutlinedTextField(
                            value = field.value,
                            onValueChange = { field.value = it },
                            label = { Text(field.label) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                //Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the bottom

                // 3. A footer with clearly styled action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(fields) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFC0657FA))) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun SmallIconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.05f))
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
    }
}

@Composable
fun DealerDetailsCard(dealerInfo: DealerInfo) {
    var showEditDialog by remember { mutableStateOf(false) }
    var fieldsToEdit by remember { mutableStateOf<List<EditFieldState>>(emptyList()) }

    if (showEditDialog) {
        MultiFieldEditDialog(
            title = "Edit Details",
            fields = fieldsToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { updatedFields ->
                // TODO: Call ViewModel to save data
                showEditDialog = false
            }
        )
    }

    // Main Card container with title and subtle styling from the image
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFF)), // A very light, almost white color
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Dealer Details",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Dealer Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // List of details using the new reusable row composable
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DetailInfoRow(
                    icon = painterResource(R.drawable.personn),
                    label = "Name",
                    value = dealerInfo.name ?: "N/A",
                    onEditClick = {
                        fieldsToEdit = listOf(EditFieldState("Name", dealerInfo.name ?: ""))
                        showEditDialog = true
                    }
                )
                DetailInfoRow(
                    icon = painterResource(R.drawable.linkk),
                    label = "Website",
                    value = dealerInfo.websiteUrl ?: "Not Available", // Placeholder from image
                    onEditClick = { /* Show dialog for email */ }
                )
                DetailInfoRow(
                    icon = painterResource(R.drawable.calll),
                    label = "Phone Number",
                    value = dealerInfo.phone ?: "N/A",
                    onEditClick = { /* Show dialog for phone */ }
                )
                DetailInfoRow(
                    icon = painterResource(R.drawable.loca),
                    label = "Address",
                    value = "${dealerInfo.address}, ${dealerInfo.city}",
                    onEditClick = {
                        fieldsToEdit = listOf(
                            EditFieldState("Address", dealerInfo.address ?: ""),
                            EditFieldState("City", dealerInfo.city ?: "")
                        )
                        showEditDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: Painter,
    label: String,
    value: String,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with a styled background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Column for Label and Value
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }

        // Edit Icon Button
        SmallIconButton(icon = Icons.Default.Edit, onClick = onEditClick)
    }
}

@Composable
fun CheckmarkRow(text: String, isAvailable: Boolean?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        val icon = if (isAvailable == true) Icons.Default.CheckCircle else Icons.Default.Cancel
        val tint = if (isAvailable == true) Color(0xFF34A853) else Color.Red.copy(alpha = 0.7f)
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFF)),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                SmallIconButton(icon = Icons.Default.Edit, onClick = onEditClick)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Unique content for each card goes here
            content()
        }
    }
}

@Composable
fun AdditionalFeaturesCard(
    isVirtual: Boolean,
    onVirtualToggled: (Boolean) -> Unit,
    // Add other parameters for the other toggles as needed
) {
    // This card uses the same stunning design as your other InfoCards
    InfoCard(
        title = "Additional Features",
        icon = Icons.Default.AutoAwesome, // A fitting icon for new features
        onEditClick = { /* This can be an overall edit button if needed */ }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // A row for the "Virtual Dealership" toggle
            ToggleRow(
                label = "Virtual Dealership",
                description = "Toggle if you are / are not a virtual dealership",
                isChecked = isVirtual,
                onCheckedChange = onVirtualToggled
            )

            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

            // A placeholder for the "Virtual Appointment" toggle
            ToggleRow(
                label = "Virtual Appointment",
                description = "Enable or disable virtual appointments",
                isChecked = false, // Replace with real data later
                onCheckedChange = { /* Will be connected to ViewModel later */ }
            )
            // Add other ToggleRow composables for Home Delivery, etc. here
        }
    }
}

/**
 * A reusable row for a label, description, and a toggle switch.
 */
@Composable
fun ToggleRow(
    label: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DeliveryAndTestDriveCard(
    dealerInfo: DealerInfo,
    onEditDelivery: () -> Unit,
    onEditTestDrive: () -> Unit
) {
    InfoCard(
        title = "At-Home Services",
        icon = Icons.Default.HomeWork,
        onEditClick = { /* Can be a general edit button */ }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
            // Row for Home Delivery
            FeatureRow(
                icon = Icons.Default.LocalShipping,
                title = "Home Delivery",
                status = if (dealerInfo.homeDelivery?.isAvailable == true) "Available" else "Not Available",
                onEditClick = onEditDelivery
            )

            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

            // Row for Home Test Drive
            FeatureRow(
                icon = Icons.Default.DriveEta,
                title = "Home Test Drive",
                status = if (dealerInfo.homeTestDrive?.isAvailable == true) "Available" else "Not Available",
                onEditClick = onEditTestDrive
            )
        }
    }
}

// --- 2. A REUSABLE ROW FOR EACH FEATURE ---
@Composable
fun FeatureRow(icon: ImageVector, title: String, status: String, onEditClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = status, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        SmallIconButton(icon = Icons.Default.Edit, onClick = onEditClick)
    }
}


// --- 3. THE DIALOG FOR HOME DELIVERY ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDeliveryDialog(
    initialState: HomeDelivery,
    onDismiss: () -> Unit,
    onSave: (isAvailable: Boolean, isNationWide: Boolean, radius: String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState.isAvailable) }
    var isNationWide by remember { mutableStateOf(initialState.isNationWide) }
    var radius by remember { mutableStateOf(initialState.radius.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Home Delivery", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ToggleRow(label = "Do you have the facility of Home Delivery?", isChecked = isAvailable, onCheckedChange = { isAvailable = it }, description = "")
                ToggleRow(label = "Deliver Nation Wide?", isChecked = isNationWide, onCheckedChange = { isNationWide = it }, description = "")

                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Please Enter The Distance (radius)") },
                    trailingIcon = { Text("Miles", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(isAvailable, isNationWide, radius) }, shape = RoundedCornerShape(8.dp)) { Text("Save") }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    CarDealershipTheme(darkTheme = false) {
        // Preview will show basic layout as it has no data
        //InfoScreen()
        DealerInfoContent()
    }
}




