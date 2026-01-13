package com.slt.cardealership.presentation.info

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import coil3.request.ImageRequest
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.SignalWifiOff // <-- IMPORT FOR ERROR ICON
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush // <-- IMPORT ADDED
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // <-- IMPORT ADDED
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.error
import coil3.request.placeholder
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.domain.model.HourDetails
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.slt.cardealership.utils.HtmlText
import com.slt.cardealership.R
import java.util.Locale

// --- ADDED: Gradient for the new error button ---
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3),
        Color(0xFF1565C0)
    )
)
// ----------------------------------------------

class EditFieldState(val label: String, initialValue: String) {
    var value by mutableStateOf(initialValue)
}

data class VirtualAppointmentState(
    val isAvailable: Boolean,
    val link: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    navController: NavController,
    viewModel: InfoViewModel = hiltViewModel()
) {

    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            // Pass the selected Uri to the ViewModel
            uri?.let { viewModel.onHeaderImageSelected(it) }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InfoEvent.ShowSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is InfoEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Dealership Info", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F8FF))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F8FF)),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is InfoUiState.Loading -> LoadingAnimation()
                // --- THIS IS THE FIX ---
                is InfoUiState.Error -> {
                    FullScreenError(
                        errorMessage = state.message,
                        onTryAgain = {
                            viewModel.fetchDealerInfo() // Call the retry function
                        }
                    )
                }
                // --- END FIX ---
                is InfoUiState.Success -> {
                    DealerInfoContent(
                        dealerInfo = state.dealerInfo,
                        viewModel = viewModel,
                        isSaving = state.isSaving,
                        onHeaderImageClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        selectedImageUri = selectedImageUri
                    )
                }
            }
        }
    }
}

@Composable
fun DealerInfoContent(
    dealerInfo: DealerInfo,
    viewModel: InfoViewModel = hiltViewModel(),
    isSaving: Boolean = false,
    onHeaderImageClick: () -> Unit,
    selectedImageUri: Uri?
) {
    val businessHours = dealerInfo.dealerHours?.find { it.hoursType == "general" }
    val partsHours = dealerInfo.dealerHours?.find { it.hoursType == "parts" }
    val serviceHours = dealerInfo.dealerHours?.find { it.hoursType == "service" }

    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showTestDriveDialog by remember { mutableStateOf(false) }
    var showAmenitiesDialog by remember { mutableStateOf(false) }
    var showHoursDialog by remember { mutableStateOf(false) }
    var showDealerTypeDialog by remember { mutableStateOf(false) }
    var showVirtualAppointmentDialog by remember { mutableStateOf(false) }

    if (showDealerTypeDialog) {
        DealerTypeEditDialog(
            initialType = dealerInfo.dealerType ?: "Independent",
            isSaving = isSaving,
            onDismiss = { showDealerTypeDialog = false },
            onSave = { newType ->
                viewModel.requestDealerTypeChange(newType)
                showDealerTypeDialog = false
            }
        )
    }

    if (showDeliveryDialog && dealerInfo.homeDelivery != null) {
        HomeDeliveryDialog(
            initialState = dealerInfo.homeDelivery,
            isSaving = isSaving,
            onDismiss = { showDeliveryDialog = false },
            onSave = { isAvailable, isNationWide, radius ->
                viewModel.updateHomeDelivery(isAvailable, isNationWide, radius)
                showDeliveryDialog = false
            }
        )
    }

    if (showTestDriveDialog && dealerInfo.homeTestDrive != null) {
        HomeTestDriveDialog(
            initialState = dealerInfo.homeTestDrive,
            isSaving = isSaving,
            onDismiss = { showTestDriveDialog = false },
            onSave = { isAvailable, radius ->
                viewModel.updateHomeTestDrive(isAvailable, radius)
                showTestDriveDialog = false
            }
        )
    }

    if (showAmenitiesDialog && dealerInfo.amenities != null) {
        AmenitiesEditDialog(
            initialState = dealerInfo.amenities,
            isSaving = isSaving,
            onDismiss = { showAmenitiesDialog = false },
            // --- FIX: Pass all 6 values to the ViewModel ---
            onSave = { wifi, parking, kidsArea, isEntrance, isSeating, isRestroom ->
                viewModel.updateAmenities(
                    wifi = wifi,
                    parking = parking,
                    kidsArea = kidsArea,
                    isEntrance = isEntrance,
                    isSeating = isSeating,
                    isRestroom = isRestroom
                )
                showAmenitiesDialog = false
            }
        )
    }

    if (showHoursDialog) {
        BusinessHoursEditDialog(
            isSaving = isSaving,
            initialGeneral = businessHours,
            initialParts = partsHours,
            initialService = serviceHours,
            onDismiss = { showHoursDialog = false },
            onSave = { general, parts, service ->
                viewModel.updateBusinessHours(general, parts, service)
                Log.d("InfoScreen", "Save Hours clicked")
                showHoursDialog = false
            }
        )
    }

    if (showVirtualAppointmentDialog) {
        // Use the 'dealerInfo' and 'isSaving' parameters passed into DealerInfoContent
        val currentVirtualAppointmentState = dealerInfo.let {
            VirtualAppointmentState(
                isAvailable = it.isVirtualAppointment == true,
                link = it.virtualAppointmentLink ?: ""
            )
        }
        VirtualAppointmentDialog(
            initialState = currentVirtualAppointmentState,
            isSaving = isSaving, // <-- Use the 'isSaving' parameter
            onDismiss = { showVirtualAppointmentDialog = false },
            onSave = { isAvailable, link ->
                viewModel.updateVirtualAppointment(isAvailable, link)
                showVirtualAppointmentDialog = false
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
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            HeaderImage(
                dealerInfo,
                selectedImageUri = selectedImageUri,
                onClick = onHeaderImageClick
            )
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DealerDetailsCard(dealerInfo, viewModel, isSaving)
                DeliveryAndTestDriveCard(
                    dealerInfo = dealerInfo,
                    onEditDelivery = { showDeliveryDialog = true },
                    onEditTestDrive = { showTestDriveDialog = true }
                )
                AdditionalFeaturesCard(
                    dealerInfo = dealerInfo, // <-- THIS IS THE CHANGE
                    onVirtualToggled = { viewModel.updateIsVirtual(it) },
                    onEditVirtualAppointmentClick = { showVirtualAppointmentDialog = true }
                )
                BusinessTypeCard(
                    dealerInfo = dealerInfo,
                    onEditDealerType = { showDealerTypeDialog = true } // <-- Pass the lambda
                )
                AccessibilityAndAmenitiesCard(
                    amenities = dealerInfo.amenities,
                    onEditClick = { showAmenitiesDialog = true }
                )
                BusinessHoursCard(
                    businessHours,
                    partsHours,
                    serviceHours,
                    onEditClick = { showHoursDialog = true } // <-- Connect edit click
                )
                AboutCard(dealerInfo, viewModel, isSaving)
            }
        }
    }
}

@Composable
fun HeaderImage(
    dealerInfo: DealerInfo,
    selectedImageUri: Uri?, // <-- 8. ADD
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp)
            .padding(12.dp)
            .shadow(4.dp, shape = RoundedCornerShape(24.dp))
            .clickable { onClick()}
    ) {
        AsyncImage(
            // --- 10. THE CACHE-BUSTING FIX ---
            model = ImageRequest.Builder(LocalContext.current)
                // Show the temporary picked image, or the server URL
                .data(selectedImageUri ?: dealerInfo.headerImageUrl)
                // Use the 'updatedOn' timestamp as a unique key
                .memoryCacheKey(dealerInfo.updatedOn.toString())
                .diskCacheKey(dealerInfo.updatedOn.toString())
                .placeholder(R.drawable.toyota) // Use placeholder from original
                .error(R.drawable.toyota) // Fallback to placeholder
                .build(),
            contentDescription = "Dealership exterior",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.7f))
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Image",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

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
fun BusinessTypeCard(
    dealerInfo: DealerInfo,
    onEditDealerType: () -> Unit // <-- Changed parameter
) {
    InfoCard(
        title = "Business Type",
        icon = Icons.Default.Business,
        onEditClick = null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // These two rows are not editable
            DetailInfoRow(
                label = "Business Segment",
                value = dealerInfo.dealerCategory?.businessSegment ?: "N/A"
            )
            DetailInfoRow(
                label = "Business Category",
                value = dealerInfo.dealerCategory?.name ?: "N/A"
            )

            // --- THIS IS THE FIX ---
            // A special row for Dealership Type that has the edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use a Column to hold the label/value, weighted to take up space
                Column(modifier = Modifier.weight(1f)) {
                    DetailInfoRow(
                        label = "Dealership Type",
                        value = dealerInfo.dealerType ?: "N/A"
                    )
                }
                // The edit button
                SmallIconButton(
                    icon = Icons.Default.Edit,
                    onClick = onEditDealerType // <-- Connected
                )
            }
            // --- END FIX ---
        }
    }
}

@Composable
fun BusinessHoursCard(
    general: DealerHours?, parts: DealerHours?, service: DealerHours?,
    onEditClick: () -> Unit // <-- ADD THIS
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Business", "Parts", "Service")

    InfoCard(
        title = "Business Hours",
        icon = Icons.Default.Schedule,
        onEditClick = onEditClick // <-- CONNECT THIS
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val hourDetails = hoursData?.hourDetails

        if (hourDetails.isNullOrEmpty()) {
            Text(text = "Not available", color = Color.Gray, fontSize = 14.sp)
        } else {
            hourDetails.forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day.day?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (day.isClose == "yes") {
                        Text(
                            text = "Closed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )

                    } else {
                        Text(
                            text = "${day.openTime ?: "--"} - ${day.closeTime ?: "--"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityAndAmenitiesCard(
    amenities: Amenities?,
    onEditClick: () -> Unit
) {
    InfoCard(
        title = "Amenities",
        icon = Icons.Default.Deck,
        onEditClick = onEditClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
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
fun AboutCard(
    dealerInfo: DealerInfo,
    viewModel: InfoViewModel,
    isSaving: Boolean
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var fieldsToEdit by remember { mutableStateOf<List<EditFieldState>>(emptyList()) }

    if (showEditDialog) {
        MultiFieldEditDialog(
            title = "Edit About",
            fields = fieldsToEdit,
            isSaving = isSaving,
            onDismiss = { showEditDialog = false },
            onSave = { updatedFields ->
                val aboutText = updatedFields.firstOrNull()?.value ?: ""
                viewModel.saveDealerUpdates(
                    mapOf("description" to aboutText),
                    "About section updated"
                )
                showEditDialog = false
            }
        )
    }

    InfoCard(
        title = "About",
        icon = Icons.Default.Info,
        onEditClick = {
            fieldsToEdit = listOf(EditFieldState("About", dealerInfo.aboutText ?: ""))
            showEditDialog = true
        }
    ) {
        Text(
            text = "Description:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (!dealerInfo.description.isNullOrEmpty()) {
            HtmlText(
                htmlString = dealerInfo.description,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFieldEditDialog(
    title: String,
    fields: List<EditFieldState>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<EditFieldState>) -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
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
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(fields) { field ->
                        OutlinedTextField(
                            value = field.value,
                            onValueChange = { field.value = it },
                            label = { Text(field.label) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !isSaving
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(fields) },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFC0657FA))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.Gray
        )
    }
}

@Composable
fun DealerDetailsCard(
    dealerInfo: DealerInfo,
    viewModel: InfoViewModel,
    isSaving: Boolean
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var fieldsToEdit by remember { mutableStateOf<List<EditFieldState>>(emptyList()) }

    if (showEditDialog) {
        MultiFieldEditDialog(
            title = "Edit Details",
            fields = fieldsToEdit,
            isSaving = isSaving,
            onDismiss = { showEditDialog = false },
            onSave = { updatedFields ->
                val updateMap = updatedFields.associate {
                    val apiKey = when (it.label) {
                        "Name" -> "name"
                        "Website" -> "website_url" // TODO: Verify API key
                        "Phone Number" -> "phone"
                        "Address" -> "address"
                        "City" -> "city_name" // TODO: Verify API key
                        else -> ""
                    }
                    apiKey to it.value
                }.filter { it.key.isNotBlank() }

                Log.d("InfoScreen", "Saving Details: $updateMap")
                viewModel.saveDealerUpdates(updateMap, "Details updated")
                showEditDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
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
                    value = dealerInfo.websiteUrl ?: "Not Available",
                    onEditClick = {
                        fieldsToEdit =
                            listOf(EditFieldState("Website", dealerInfo.websiteUrl ?: ""))
                        showEditDialog = true
                    }
                )
                DetailInfoRow(
                    icon = painterResource(R.drawable.calll),
                    label = "Phone Number",
                    value = dealerInfo.phone ?: "N/A",
                    onEditClick = {
                        fieldsToEdit =
                            listOf(EditFieldState("Phone Number", dealerInfo.phone ?: ""))
                        showEditDialog = true
                    }
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
    onEditClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (onEditClick != null) {
            SmallIconButton(icon = Icons.Default.Edit, onClick = onEditClick)
        }
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
    onEditClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFF)),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
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
                if (onEditClick != null) {
                    SmallIconButton(icon = Icons.Default.Edit, onClick = onEditClick)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
fun AdditionalFeaturesCard(
    dealerInfo: DealerInfo, // <-- CHANGE: Pass the whole dealerInfo object
    onVirtualToggled: (Boolean) -> Unit,
    onEditVirtualAppointmentClick: () -> Unit
) {
    InfoCard(
        title = "Additional Features",
        icon = Icons.Default.AutoAwesome,
        onEditClick = null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // This ToggleRow for Virtual Dealership is correct
            ToggleRow(
                label = "Virtual Dealership",
                description = "Toggle if you are / are not a virtual dealership",
                isChecked = dealerInfo.isVirtual == true, // <-- Read from dealerInfo
                onCheckedChange = onVirtualToggled
            )

            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

            // --- THIS IS THE FIX ---
            // Use a FeatureRow to display status and an edit button
            FeatureRow(
                icon = Icons.Default.Videocam, // Use a relevant icon
                title = "Virtual Appointment",
                // Read the status from dealerInfo
                status = if (dealerInfo.isVirtualAppointment == true) "Available" else "Not Available",
                // Call the function when clicked
                onEditClick = onEditVirtualAppointmentClick
            )
        }
    }
}

@Composable
fun ToggleRow(
    label: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
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
        onEditClick = null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            FeatureRow(
                icon = Icons.Default.LocalShipping,
                title = "Home Delivery",
                status = if (dealerInfo.homeDelivery?.isAvailable == true) "Available" else "Not Available",
                onEditClick = onEditDelivery
            )

            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

            FeatureRow(
                icon = Icons.Default.DriveEta,
                title = "Home Test Drive",
                status = if (dealerInfo.homeTestDrive?.isAvailable == true) "Available" else "Not Available",
                onEditClick = onEditTestDrive // <-- CONNECTED
            )
        }
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, status: String, onEditClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = status, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (onEditClick != null) {
            SmallIconButton(icon = Icons.Default.Edit, onClick = onEditClick)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDeliveryDialog(
    initialState: HomeDelivery,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (isAvailable: Boolean, isNationWide: Boolean, radius: String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState.isAvailable) }
    var isNationWide by remember { mutableStateOf(initialState.isNationWide ?: false) }
    var radius by remember { mutableStateOf(initialState.radius.toString()) }

    LaunchedEffect(initialState) {
        isAvailable = initialState.isAvailable
        isNationWide = initialState.isNationWide ?: false
        radius = initialState.radius.toString()
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Home Delivery",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(
                            Icons.Default.Close,
                            null
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ToggleRow(
                    label = "Do you have the facility of Home Delivery?",
                    isChecked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    description = "",
                    enabled = !isSaving
                )
                ToggleRow(
                    label = "Deliver Nation Wide?",
                    isChecked = isNationWide,
                    onCheckedChange = { isNationWide = it },
                    description = "",
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Please Enter The Distance (radius)") },
                    trailingIcon = { Text("Miles", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(isAvailable, isNationWide, radius) },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// --- NEW DIALOG FOR HOME TEST DRIVE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTestDriveDialog(
    initialState: HomeTestDrive,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (isAvailable: Boolean, radius: String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState.isAvailable) }
    var radius by remember { mutableStateOf(initialState.radius.toString()) }

    LaunchedEffect(initialState) {
        isAvailable = initialState.isAvailable
        radius = initialState.radius.toString()
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Home Test Drive",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(
                            Icons.Default.Close,
                            null
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ToggleRow(
                    label = "Do you have the facility of Home Test Drive?",
                    isChecked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    description = "",
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Please Enter The Distance (radius)") },
                    trailingIcon = { Text("Miles", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(isAvailable, radius) },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// --- NEW DIALOG FOR AMENITIES ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmenitiesEditDialog(
    initialState: Amenities,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    // --- FIX: Update onSave to include all 6 values ---
    onSave: (
        wifi: Boolean,
        parking: Boolean,
        kidsArea: Boolean,
        isEntrance: Boolean,
        isSeating: Boolean,
        isRestroom: Boolean
    ) -> Unit
) {
    // --- FIX: Add state for all 6 toggles ---
    var isWifi by remember { mutableStateOf(initialState.isWifi ?: false) }
    var isParking by remember { mutableStateOf(initialState.isParking ?: false) }
    var isKidsPlayArea by remember { mutableStateOf(initialState.isKidsPlayArea ?: false) }
    var isEntrance by remember { mutableStateOf(initialState.isEntrance ?: false) }
    var isSeating by remember { mutableStateOf(initialState.isSeating ?: false) }
    var isRestroom by remember { mutableStateOf(initialState.isRestroom ?: false) }
    // ----------------------------------------

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.heightIn(max = 600.dp) // Make dialog scrollable
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Edit Amenities",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(
                            Icons.Default.Close,
                            null
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- FIX: Use a LazyColumn for scrolling ---
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        ToggleRow(
                            label = "Wi-Fi",
                            description = "Is Wi-Fi available?",
                            isChecked = isWifi,
                            onCheckedChange = { isWifi = it },
                            enabled = !isSaving
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Parking",
                            description = "Is parking available?",
                            isChecked = isParking,
                            onCheckedChange = { isParking = it },
                            enabled = !isSaving
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Kids Play Area",
                            description = "Is a kids play area available?",
                            isChecked = isKidsPlayArea,
                            onCheckedChange = { isKidsPlayArea = it },
                            enabled = !isSaving
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Wheelchair Accessible Entrance",
                            description = "Is the entrance accessible?",
                            isChecked = isEntrance,
                            onCheckedChange = { isEntrance = it },
                            enabled = !isSaving
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Wheelchair Accessible Seating",
                            description = "Is seating accessible?",
                            isChecked = isSeating,
                            onCheckedChange = { isSeating = it },
                            enabled = !isSaving
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Wheelchair Accessible Restroom",
                            description = "Is the restroom accessible?",
                            isChecked = isRestroom,
                            onCheckedChange = { isRestroom = it },
                            enabled = !isSaving
                        )
                    }
                }
                // --- END FIX ---

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        // --- FIX: Pass all 6 values back ---
                        onClick = {
                            onSave(
                                isWifi,
                                isParking,
                                isKidsPlayArea,
                                isEntrance,
                                isSeating,
                                isRestroom
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LoadingAnimation() {
    // 1. Load the Lottie animation composition from your assets folder
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lott.json")) // <-- Replace with your JSON file name

    // 2. Display the Lottie animation
    Box(
        modifier = Modifier.fillMaxSize(), // Center the animation if desired
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever, // Loop the animation indefinitely
            modifier = Modifier.size(200.dp) // Adjust size as needed
        )
    }
}

@Composable
fun FullScreenError(
    errorMessage: String,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Define colors
    val primaryBlue = Color(0xFF1B7DD8)
    val textGrey = Color(0xFF6A7C93)
    val backgroundBlue = Color(0xFFF0F8FF)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBlue)
            .padding(horizontal = 40.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // <-- Center everything
    ) {

        // --- Simple Icon ---
        Icon(
            imageVector = Icons.Rounded.SignalWifiOff,
            contentDescription = "Error",
            modifier = Modifier.size(120.dp),
            tint = primaryBlue.copy(alpha = 0.8f) // Use the theme color
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- "Oops!" Title ---
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Main Message ---
        Text(
            text = "Internal Server Error!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Description Text ---
        Text(
            text = "Something went wrong. Try refreshing the page or checking your internet connection. We'll see you in a moment!",
            style = MaterialTheme.typography.bodyMedium,
            color = textGrey,
            textAlign = TextAlign.Center // Center the text
        )

        // --- Technical Error Message ---
        Text(
            text = "Error: $errorMessage",
            style = MaterialTheme.typography.bodySmall,
            color = textGrey.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp)) // Space before button

        // --- "Try again" Button ---
        Button(
            onClick = onTryAgain,
            shape = RoundedCornerShape(50), // Fully rounded
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlue, // Solid blue background
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Try again",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * A data class to hold the mutable state for a single day in the dialog.
 */
class DayHourState(
    val day: String,
    initialOpen: String,
    initialClose: String,
    initialIsClosed: Boolean
) {
    var openTime by mutableStateOf(initialOpen)
    var closeTime by mutableStateOf(initialClose)
    var isClosed by mutableStateOf(initialIsClosed)

    // Helper to convert back to the domain model
    fun toHourDetails(): HourDetails {
        // --- THIS IS THE FIX ---
        // Converts the dialog's Boolean state (isClosed) back to the
        // String ("yes" or "no") that the API and HourDetails model expect.
        return HourDetails(
            day = this.day.lowercase(Locale.ROOT), // Send as lowercase
            openTime = if (isClosed) null else this.openTime,
            closeTime = if (isClosed) null else this.closeTime,
            isClose = if (isClosed) "yes" else "no", // Send as "yes" or "no"
        )
        // -------------------------------------------------
    }
}

/**
 * Creates and remembers a list of mutable DayHourState objects.
 */
@Composable
fun rememberHoursState(hours: DealerHours?): List<DayHourState> {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    return remember(hours) {
        days.map { dayName ->
            // --- FIX: Compare with lowercase day from API ---
            val details = hours?.hourDetails?.find { it.day == dayName.lowercase(Locale.ROOT) }
            DayHourState(
                day = dayName, // Keep capitalized for dialog display
                initialOpen = details?.openTime ?: "09:00 AM",
                initialClose = details?.closeTime ?: "05:00 PM",
                // --- FIX: Check for the string "yes" ---
                initialIsClosed = details?.isClose == "yes"
                // ---------------------------------------
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHoursEditDialog(
    isSaving: Boolean,
    initialGeneral: DealerHours?,
    initialParts: DealerHours?,
    initialService: DealerHours?,
    onDismiss: () -> Unit,
    onSave: (general: List<HourDetails>, parts: List<HourDetails>, service: List<HourDetails>) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Business", "Parts", "Service")

    // Create mutable states for all 21 fields (7 days x 3 tabs)
    val generalHoursState = rememberHoursState(hours = initialGeneral)
    val partsHoursState = rememberHoursState(hours = initialParts)
    val serviceHoursState = rememberHoursState(hours = initialService)

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.heightIn(max = 650.dp)
        ) {
            Column {
                // --- Header ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Edit Business Hours",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    // --- Tabs ---
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                enabled = !isSaving,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Content switches based on tab ---
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        val currentHoursState = when (selectedTabIndex) {
                            0 -> generalHoursState
                            1 -> partsHoursState
                            else -> serviceHoursState
                        }

                        items(currentHoursState) { dayState ->
                            HourEditRow(dayState = dayState, enabled = !isSaving)
                            if (dayState != currentHoursState.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Save Button ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onSave(
                                    generalHoursState.map { it.toHourDetails() },
                                    partsHoursState.map { it.toHourDetails() },
                                    serviceHoursState.map { it.toHourDetails() }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Changes", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourEditRow(dayState: DayHourState, enabled: Boolean) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayState.day,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Closed",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = dayState.isClosed,
                onCheckedChange = { dayState.isClosed = it },
                enabled = enabled
            )
        }

        AnimatedVisibility(visible = !dayState.isClosed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dayState.openTime,
                    onValueChange = { dayState.openTime = it },
                    label = { Text("Open") },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dayState.closeTime,
                    onValueChange = { dayState.closeTime = it },
                    label = { Text("Close") },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerTypeEditDialog(
    initialType: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (newType: String) -> Unit
) {
    val options = listOf("Independent", "Franchise")
    var isExpanded by remember { mutableStateOf(false) }
    // Set the initial selected option, defaulting to "Independent"
    var selectedOption by remember { mutableStateOf(if (initialType in options) initialType else "Independent") }

    // --- FIX: Use AlertDialog instead of Dialog + Card ---
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        // --- 1. Title Slot ---
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Request for Dealership Type change",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, enabled = !isSaving) {
                    Icon(Icons.Default.Close, null)
                }
            }
        },
        // --- 2. Text/Content Slot ---
        text = {
            Column {
                HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp)) // Divider after title

                Text(
                    "Select dealership type",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // The Dropdown now lives in the 'text' slot
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { if (!isSaving) isExpanded = it },
                ) {
                    OutlinedTextField(
                        value = if (selectedOption == "Franchise") "Franchise (Request for change)" else selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isSaving
                    )
                    // This menu will now render in the correct popup window
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Independent") },
                            onClick = {
                                selectedOption = "Independent"
                                isExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Franchise (Request for change)") },
                            onClick = {
                                selectedOption = "Franchise"
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        },
        // --- 3. Confirm Button Slot ---
        confirmButton = {
            Button(
                onClick = { onSave(selectedOption) },
                shape = RoundedCornerShape(8.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D60FE)) // Match blue
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        },
        // --- 4. Dismiss Button Slot ---
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualAppointmentDialog(
    initialState: VirtualAppointmentState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (isAvailable: Boolean, link: String) -> Unit
) {
    var isAvailable by remember { mutableStateOf(initialState.isAvailable) }
    var link by remember { mutableStateOf(initialState.link) }

    // Use LaunchedEffect to update state if initialState changes (e.g., after a refresh)
    LaunchedEffect(initialState) {
        isAvailable = initialState.isAvailable
        link = initialState.link
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Virtual Appointment",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(Icons.Default.Close, null)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ToggleRow(
                    label = "Do you have the facility of Virtual Appointment?",
                    isChecked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    description = if (isAvailable) "If \"YES\" enter URL" else "", // Dynamic description
                    enabled = !isSaving
                )

                // Only show the URL input if isAvailable is true
                if (isAvailable) {
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Virtual Appointment Link") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        singleLine = true // Ensures it doesn't expand
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(isAvailable, link) },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
