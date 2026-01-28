package com.slt.cardealership.presentation.info

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.placeholder // Explicitly import placeholder
import coil3.request.error       // Explicitly import error
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.*
import com.slt.cardealership.utils.HtmlText
import com.slt.cardealership.ui.theme.BrandBlue
import java.util.Locale
// Explicit imports to help compiler
import com.slt.cardealership.presentation.info.AddressEditSheet
import com.slt.cardealership.presentation.info.PhoneEditSheet
import com.slt.cardealership.presentation.info.MakesEditSheet

// Custom Colors
val GreenVerified = Color(0xFF2E7D32)
val LightGreenBackground = Color(0xFFE8F5E9)
// val BlueLink = Color(0xFF2196F3) // Use BrandBlue
val TextGray = Color(0xFF757575)
val BackgroundGray = Color(0xFFF5F7F8)

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
        onResult = { uri: Uri? -> uri?.let { viewModel.onHeaderImageSelected(it) } }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InfoEvent.ShowSuccess -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is InfoEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Dealership Info", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGray),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is InfoUiState.Loading -> CircularProgressIndicator()
                is InfoUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Red)
                        Button(onClick = { viewModel.fetchDealerInfo() }) { Text("Retry") }
                    }
                }
                is InfoUiState.Success -> {
                    DealerInfoContent(
                        dealerInfo = state.dealerInfo,
                        socialProfiles = state.socialProfiles,
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
    socialProfiles: List<SocialProfileItem>,
    viewModel: InfoViewModel,
    isSaving: Boolean,
    onHeaderImageClick: () -> Unit,
    selectedImageUri: Uri?
) {
    // Dialog States
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showTestDriveDialog by remember { mutableStateOf(false) }
    var showAmenitiesDialog by remember { mutableStateOf(false) }
    var showHoursDialog by remember { mutableStateOf(false) }
    var showDealerTypeDialog by remember { mutableStateOf(false) }
    var showVirtualAppointmentDialog by remember { mutableStateOf(false) }
    var showSocialLinksDialog by remember { mutableStateOf(false) }
    var showBusinessDateDialog by remember { mutableStateOf(false) }
    var showMakesDialog by remember { mutableStateOf(false) }
    var showVirtualDealershipDialog by remember { mutableStateOf(false) }


    // Address & Phone Edit State
    var showAddressDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }

    // Details Edit State
    var showDetailsEditDialog by remember { mutableStateOf(false) }
    var detailsFieldsToEdit by remember { mutableStateOf<List<EditFieldState>>(emptyList()) }

    // About Edit State
    var showAboutEditDialog by remember { mutableStateOf(false) }
    var aboutFieldsToEdit by remember { mutableStateOf<List<EditFieldState>>(emptyList()) }

    // --- Dialogs Implementations --- (Same as before, just reused)

    if (showSocialLinksDialog) {
        SocialLinksEditSheet(
            existingLinks = socialProfiles,
            isSaving = isSaving,
            onDismiss = { showSocialLinksDialog = false },
            onSave = { updatedLinks ->
                viewModel.saveSocialProfiles(updatedLinks)
                showSocialLinksDialog = false
            }
        )
    }

    if (showAddressDialog) {
        AddressEditSheet(
            initialAddress = dealerInfo.address,
            initialCity = dealerInfo.city,
            initialZip = dealerInfo.zipCode,
            isSaving = isSaving,
            onDismiss = { showAddressDialog = false },
            onSave = { country, state, city, address, zip ->
                viewModel.saveDealerUpdates(
                    mapOf(
                        "address" to address,
                        "city_name" to city,
                        "zip_code" to zip,
                        // Assuming backend can handle state/country updates if needed, or mapped elsewhere.
                        // based on current generic map, state might not be there or might be differently named.
                        // Using typical keys. If backend doesn't support them, they might be ignored or need adjustment.
                        // "state" to state,
                        // "country" to country
                    ),
                    "Address updated"
                )
                showAddressDialog = false
            }
        )
    }

    if (showPhoneDialog) {
        PhoneEditSheet(
            initialPhone = dealerInfo.phone,
            isSaving = isSaving,
            onDismiss = { showPhoneDialog = false },
            onSave = { newPhone ->
                viewModel.saveDealerUpdates(
                    mapOf("phone" to newPhone),
                    "Phone number updated"
                )
                showPhoneDialog = false
            }
        )
    }

    if (showDetailsEditDialog) {
        MultiFieldEditSheet(
            title = "Edit Details",
            fields = detailsFieldsToEdit,
            isSaving = isSaving,
            onDismiss = { showDetailsEditDialog = false },
            onSave = { updatedFields ->
                val updateMap = updatedFields.associate {
                    val apiKey = when (it.label) {
                        "Name" -> "name"
                        "Website" -> "website_url"
                        "Phone Number" -> "phone"
                        "Address" -> "address"
                        "City" -> "city_name"
                        "Zip Code" -> "zip_code"
                        else -> ""
                    }
                    apiKey to it.value
                }.filter { it.key.isNotBlank() }
                viewModel.saveDealerUpdates(updateMap, "Details updated")
                showDetailsEditDialog = false
            }
        )
    }

    if (showAboutEditDialog) {
        MultiFieldEditSheet(
            title = "Edit About",
            fields = aboutFieldsToEdit,
            isSaving = isSaving,
            onDismiss = { showAboutEditDialog = false },
            onSave = { updatedFields ->
                val aboutText = updatedFields.firstOrNull()?.value ?: ""
                viewModel.saveDealerUpdates(
                    mapOf("description" to aboutText),
                    "About section updated"
                )
                showAboutEditDialog = false
            }
        )
    }

    if (showDealerTypeDialog) {
        DealerTypeEditSheet(
            initialType = dealerInfo.dealerType ?: "Independent",
            isSaving = isSaving,
            onDismiss = { showDealerTypeDialog = false },
            onSave = { newType ->
                viewModel.requestDealerTypeChange(newType)
                showDealerTypeDialog = false
            }
        )
    }

    // --- Makes Dialog ---
    // We need access to allMakes and selectedMakes from the state.
    // Ensure we are in Success state before showing (which we are, inside DealerInfoContent)
    val successState = viewModel.uiState.collectAsState().value as? InfoUiState.Success
    // Safely cast or assume passed params?
    // DealerInfoContent is called only on Success, but let's be safe or pass these as params to DealerInfoContent?
    // Passing as params would be cleaner but requires changing signature heavily.
    // Let's use the ViewModel state since we are already coupled.

    if (showMakesDialog && successState != null) {
        val currentSelectedIds = successState.selectedMakes.map { it.makeId }
        MakesEditSheet(
            allMakes = successState.allMakes,
            selectedMakeIds = currentSelectedIds,
            isSaving = isSaving,
            onDismiss = { showMakesDialog = false },
            onSave = { newIds ->
                viewModel.saveMakes(newIds)
                showMakesDialog = false
            }
        )
    }


    if (showDeliveryDialog && dealerInfo.homeDelivery != null) {

        HomeDeliverySheet(
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
        HomeTestDriveSheet(
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
        AmenitiesEditSheet(
            initialState = dealerInfo.amenities,
            isSaving = isSaving,
            onDismiss = { showAmenitiesDialog = false },
            onSave = { wifi, parking, kidsArea, isEntrance, isSeating, isRestroom ->
                viewModel.updateAmenities(wifi, parking, kidsArea, isEntrance, isSeating, isRestroom)
                showAmenitiesDialog = false
            }
        )
    }

    if (showHoursDialog) {
        BusinessHoursEditSheet(
            isSaving = isSaving,
            initialGeneral = dealerInfo.dealerHours?.find { it.hoursType == "general" },
            initialParts = dealerInfo.dealerHours?.find { it.hoursType == "parts" },
            initialService = dealerInfo.dealerHours?.find { it.hoursType == "service" },
            onDismiss = { showHoursDialog = false },
            onSave = { general, parts, service ->
                viewModel.updateBusinessHours(general, parts, service)
                showHoursDialog = false
            }
        )
    }

    if (showVirtualAppointmentDialog) {
        val currentVirtualAppointmentState = dealerInfo.let {
            VirtualAppointmentState(
                isAvailable = it.isVirtualAppointment == true,
                link = it.virtualAppointmentLink ?: ""
            )
        }
        VirtualAppointmentSheet(
            initialState = currentVirtualAppointmentState,
            isSaving = isSaving,
            onDismiss = { showVirtualAppointmentDialog = false },
            onSave = { isAvailable, link ->
                viewModel.updateVirtualAppointment(isAvailable, link)
                showVirtualAppointmentDialog = false
            }
        )
    }

    if (showBusinessDateDialog) {
        BusinessOpeningDateSheet(
            isSaving = isSaving,
            onDismiss = { showBusinessDateDialog = false },
            onSave = { month, year ->
                viewModel.saveOpeningDate(month, year)
                showBusinessDateDialog = false
            }
        )
    }

    if (showVirtualDealershipDialog) {
        VirtualDealershipEditSheet(
            isVirtual = dealerInfo.isVirtual ?: false,
            isSaving = isSaving,
            onDismiss = { showVirtualDealershipDialog = false },
            onSave = { isVirtual ->
                viewModel.updateIsVirtual(isVirtual)
                showVirtualDealershipDialog = false
            }
        )
    }



    // --- Content ---
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Basic Info
        item {
            HeaderAndBasicInfoCard(
                dealerInfo = dealerInfo,
                selectedImageUri = selectedImageUri,
                onHeaderImageClick = onHeaderImageClick,
                onEditDetails = {
                    detailsFieldsToEdit = listOf(
                        EditFieldState("Name", dealerInfo.name ?: ""),
                        EditFieldState("Website", dealerInfo.websiteUrl ?: "")
                    )
                    showDetailsEditDialog = true
                },
                onEditAddress = { showAddressDialog = true },
                onEditPhone = { showPhoneDialog = true },
                onEditVirtualDealership = {
                    showVirtualDealershipDialog = true
                },
                onEditBusinessDate = { showBusinessDateDialog = true }
            )
        }

        // 2. Virtual Appointment, Delivery, Test Drive
        item {
            ServicesCard(
                dealerInfo = dealerInfo,
                onEditVirtualAppointment = { showVirtualAppointmentDialog = true },
                onEditHomeDelivery = { showDeliveryDialog = true },
                onEditHomeTestDrive = { showTestDriveDialog = true }
            )
        }

        // 3. Accessibility & Amenities
        item {
            AmenitiesCard(
                amenities = dealerInfo.amenities,
                onEdit = { showAmenitiesDialog = true }
            )
        }

        // 4. Social Links
        item {
            SocialLinksCard(
                socialProfiles = socialProfiles,
                onEdit = { showSocialLinksDialog = true }
            )
        }

        // 5. Business Details
        item {
            // Prepare Makes String
            val successState = viewModel.uiState.collectAsState().value as? InfoUiState.Success
            val makesText = if (successState != null && successState.selectedMakes.isNotEmpty()) {
                // Use makeName from setting if available, else look up in allMakes
                successState.selectedMakes.joinToString(", ") { setting ->
                    setting.makeName ?: successState.allMakes.find { it.id == setting.makeId }?.name ?: "Unknown"
                }
            } else {
                "Select makes"
            }

            BusinessDetailsCard(
                dealerInfo = dealerInfo,
                makesText = makesText,
                onEditType = { showDealerTypeDialog = true },
                onEditMakes = { showMakesDialog = true }
            )
        }

        // 6. About
        item {
            AboutCard(
                description = dealerInfo.description,
                dealerName = dealerInfo.name,
                onEdit = {
                    aboutFieldsToEdit = listOf(EditFieldState("About", dealerInfo.description ?: ""))
                    showAboutEditDialog = true
                }
            )
        }

        // 7. Hours
        item {
            HoursCard(
                hours = dealerInfo.dealerHours,
                onEdit = { showHoursDialog = true }
            )
        }
    }
}

// --- Cards ---

@Composable
fun InfoSectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    onEdit: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)), // Very light border
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (onEdit != null) {
                        // Use a real button or just an icon? Screenshot shows icon button separate or inline.
                        // For section headers, usually an Update button or Icon.
                        if (title == "Social Links") {
                            Button(
                                onClick = onEdit,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Add / Update", fontSize = 12.sp)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = BrandBlue,
                                modifier = Modifier.size(20.dp).clickable { onEdit() }
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun HeaderAndBasicInfoCard(
    dealerInfo: DealerInfo,
    selectedImageUri: Uri?,
    onHeaderImageClick: () -> Unit,
    onEditDetails: () -> Unit,
    onEditVirtualDealership: () -> Unit,
    onEditBusinessDate: () -> Unit,
    onEditAddress: () -> Unit,
    onEditPhone: () -> Unit
) {
    InfoSectionCard(onEdit = onEditDetails) { // Edit applies to the text fields primarily
        // Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .clickable { onHeaderImageClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedImageUri ?: dealerInfo.headerImageUrl)
                    .memoryCacheKey(dealerInfo.updatedOn.toString())
                    .diskCacheKey(dealerInfo.updatedOn.toString())
                    .placeholder(R.drawable.toyota)
                    .error(R.drawable.toyota)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            )
            // Edit Icon for ImageD
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .size(28.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription=null, modifier=Modifier.size(16.dp), tint=BrandBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = dealerInfo.name ?: "Dealership Name",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Verified Badge
        if (dealerInfo.isClaimed == true) {
            Container(
                color = LightGreenBackground,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, GreenVerified.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Verified, null, tint = GreenVerified, modifier=Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Claimed & Verified", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Address
        InfoRowItem(icon = Icons.Outlined.LocationOn, text = "${dealerInfo.address}, ${dealerInfo.city}, ${dealerInfo.zipCode}", showEdit = true, onEdit = onEditAddress)

        // Phone
        InfoRowItem(icon = Icons.Outlined.Phone, text = dealerInfo.phone ?: "N/A", showEdit = true, onEdit = onEditPhone)

        // Add Business Opening Date (Placeholder as not in model)
        InfoRowItem(icon = Icons.Outlined.DateRange, text = "Add business opening date", showEdit = true, onEdit = { onEditBusinessDate() })

        // Website
        InfoRowItem(icon = Icons.Outlined.Language, text = dealerInfo.websiteUrl ?: "Add website", isLink = true, showEdit = false, onEdit = onEditDetails)

        // Email (Using slug or generic handle for now)
        InfoRowItem(icon = Icons.Outlined.AlternateEmail, text = dealerInfo.slug ?: "email-handle", showEdit = false)

        // Virtual Dealership
        // "Are you a Virtual Dealership? No"
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Outlined.Link, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Are you a Virtual Dealership?", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(if (dealerInfo.isVirtual == true) "Yes" else "No", color = TextGray, fontSize = 14.sp)
            }
            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEditVirtualDealership() })
        }
    }
}

@Composable
fun ServicesCard(
    dealerInfo: DealerInfo,
    onEditVirtualAppointment: () -> Unit,
    onEditHomeDelivery: () -> Unit,
    onEditHomeTestDrive: () -> Unit
) {
    InfoSectionCard {
        // Virtual Appointment
        InfoRowItemWithTitle(
            icon = Icons.Outlined.Link,
            title = "Virtual Appointment",
            onEdit = onEditVirtualAppointment
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color=Color(0xFFF0F0F0))

        // Home Delivery
        InfoRowItemWithTitle(
            icon = Icons.Outlined.Business, // Icon closest to "Building"
            title = "Home Delivery",
            onEdit = onEditHomeDelivery
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color=Color(0xFFF0F0F0))

        // Home Test Drive
        InfoRowItemWithTitle(
            icon = Icons.Outlined.Business, // Icon closest to "Building"
            title = "Home Test Drive",
            onEdit = onEditHomeTestDrive
        )
    }
}

@Composable
fun AmenitiesCard(
    amenities: Amenities?,
    onEdit: () -> Unit
) {
    InfoSectionCard(title = "Accessibility", onEdit = onEdit) { // Screenshot says "Accessibility", but has amenities too.
        // Grouping logic: in screenshot Accessibility has entrance, restroom, seating.
        // Amenities has Parking, Kids, Wifi.
        // For simplicity, I will put them in one list for now or separate sections.

        // 1. Accessibility
        AmenityRow("Wheelchair accessible entrance", amenities?.isEntrance)
        AmenityRow("Wheelchair accessible restroom", amenities?.isRestroom)
        AmenityRow("Wheelchair accessible seating", amenities?.isSeating)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Amenities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        AmenityRow("Parking", amenities?.isParking)
        AmenityRow("Kids play area", amenities?.isKidsPlayArea)
        AmenityRow("Wifi", amenities?.isWifi)
    }
}


@Composable
fun SocialLinksCard(
    socialProfiles: List<SocialProfileItem>,
    onEdit: () -> Unit
) {
    // Filter out profiles with empty URLs
    val visibleProfiles = socialProfiles.filter { it.socialLink.isNotBlank() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = if (visibleProfiles.isNotEmpty()) 16.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Social Links",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Add / Update", fontSize = 12.sp)
                }
            }

            if (visibleProfiles.isNotEmpty()) {
                visibleProfiles.forEachIndexed { index, profile ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = profile.socialType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.socialLink,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (index < visibleProfiles.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessDetailsCard(
    dealerInfo: DealerInfo,
    makesText: String,
    onEditType: () -> Unit,
    onEditMakes: () -> Unit
) {
    InfoSectionCard {
        DetailRow("Business Segment", dealerInfo.dealerCategory?.businessSegment ?: "Automobile")
        DetailRow("Business Category", dealerInfo.dealerCategory?.name ?: "Car Dealership")
        Divider(color = Color.Transparent, modifier = Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Dealership Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(dealerInfo.dealerType ?: "N/A", color = TextGray, fontSize = 14.sp)
            }
            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEditType() })
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Make(s)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(makesText, color = TextGray, fontSize = 14.sp)
            }
            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEditMakes() })
        }
    }
}

@Composable
fun AboutCard(
    description: String?,
    dealerName: String?,
    onEdit: () -> Unit
) {
    InfoSectionCard(title = "About", onEdit = onEdit) {
        Text(dealerName ?: "Dealership", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (description.isNullOrEmpty()) {
            Text("No description available.", style = MaterialTheme.typography.bodyMedium, color = TextGray)
        } else {
            // Truncate or Show All? Screenshot shows text.
            HtmlText(htmlString = description)
        }
    }
}

@Composable
fun HoursCard(
    hours: List<DealerHours>?,
    onEdit: () -> Unit
) {
    InfoSectionCard(title = "Business Hours", onEdit = onEdit) {
        val general = hours?.find { it.hoursType == "general" }
        HoursList(general)

        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Parts Hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEdit() })
        }
        HoursList(hours?.find { it.hoursType == "parts" })

        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Service Hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEdit() })
        }
        HoursList(hours?.find { it.hoursType == "service" })
    }
}

@Composable
fun HoursList(hours: DealerHours?) {
    if (hours?.hourDetails.isNullOrEmpty()) {
        Text("Not available", color = TextGray, fontSize = 12.sp)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            hours?.hourDetails?.forEach { day ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = day.day?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "N/A",
                        color = TextGray, fontSize = 14.sp
                    )
                    if (day.isClose == "yes") {
                        Text("Closed", color = Color.Red, fontSize = 14.sp)
                    } else {
                        Text("${day.openTime ?: "--"} - ${day.closeTime ?: "--"}", color = Color.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


// --- Helper Composables ---

@Composable
fun InfoRowItem(
    icon: ImageVector,
    text: String,
    showEdit: Boolean = false,
    isLink: Boolean = false,
    onEdit: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLink) BrandBlue else Color.Black,
            modifier = Modifier.weight(1f)
        )
        if (showEdit) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEdit() })
        }
    }
}

@Composable
fun InfoRowItemWithTitle(
    icon: ImageVector,
    title: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp).clickable { onEdit() })
    }
}

@Composable
fun AmenityRow(text: String, isAvailable: Boolean?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAvailable == true) {
            Icon(Icons.Default.CheckCircle, null, tint = GreenVerified, modifier = Modifier.size(20.dp))
        } else {
            Icon(Icons.Default.Cancel, null, tint = Color.Red, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = TextGray, fontSize = 14.sp)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(value, color = TextGray, fontSize = 14.sp)
    }
}


@Composable
fun Container(
    modifier: Modifier = Modifier,
    color: Color = Color.Transparent,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.ui.graphics.RectangleShape,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
    ) {
        content()
    }
}


