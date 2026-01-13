package com.slt.cardealership.presentation.inventory

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Correct import for index
import androidx.compose.foundation.lazy.rememberLazyListState // Needed for scroll state
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.presentation.home.HomeRoutes
import com.slt.cardealership.presentation.vehicle.InventoryEvent // Ensure this import is correct
import com.slt.cardealership.presentation.vehicle.VehicleViewModel
import kotlinx.coroutines.flow.collectLatest

// Constants for styling
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3), // Light Blue
        Color(0xFF1565C0)  // Dark Blue
    )
)
private val themeColor = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    // viewModel: VehicleViewModel = hiltViewModel() // Use the correct ViewModel
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(HomeRoutes.Dashboard::class)
    }
    val viewModel: VehicleViewModel = hiltViewModel(parentEntry)
    val listState by viewModel.vehicleListState.collectAsState() // Use correct state property
    var showAddOptionsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observe error state for Snackbar
    LaunchedEffect(listState.error) {
        listState.error?.let {
            snackbarHostState.showSnackbar(
                message = "Error: $it",
                duration = SnackbarDuration.Short
            )
            // Optional: viewModel.clearListError() // Add if you want to clear the error after showing
        }
    }

    // --- Load initial vehicles ---
    LaunchedEffect(Unit) {
        viewModel.getResearchVehicles() // Call the updated function
    }

    // --- Observe events (like errors from saving) ---
    LaunchedEffect(key1 = Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is InventoryEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }

                InventoryEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }


    if (showAddOptionsDialog) {
        AddVehicleOptionsDialog(
            onDismiss = { showAddOptionsDialog = false },
            onAddManually = {
                showAddOptionsDialog = false
                viewModel.prepareNewVehicleForm() // Reset form state before navigating
                navController.navigate(HomeRoutes.AddVehicleScreen)
            },
            onVinDecode = {
                showAddOptionsDialog = false
                // TODO: Implement VIN decode logic/navigation
                viewModel.clearVinDecodeState() // Reset VIN state before navigating
                // navController.navigate(HomeRoutes.VinDecoderScreen)
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar( // Standard TopAppBar
                title = { Text("All Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Show back arrow only if there's a previous screen in the stack
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0D1B2A) // Dark title color
                ),
                modifier = Modifier.shadow(elevation = 2.dp) // Subtle shadow
            )
        },
        floatingActionButton = {
            // Styled FAB
            Box(
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(blueGradient)
                    .clickable { showAddOptionsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Vehicle",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            InventoryHeader(
                vehicleCount = listState.vehicles.size, // Show current loaded count
                onFiltersClicked = { /* TODO: Implement filter logic */ },
                onSortClicked = { /* TODO: Implement sort logic */ }
            )

            // Conditional content based on loading and data state
            when {
                // Initial loading state (no items yet)
                listState.isLoading && listState.vehicles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeColor)
                    }
                }
                // Empty state (after initial load, still no items)
                !listState.isLoading && listState.vehicles.isEmpty() && listState.currentPage == 1 -> {
                    EmptyInventory()
                }
                // Show the list (potentially while loading more pages)
                else -> {
                    InventoryList(
                        vehicles = listState.vehicles,
                        isLoadingMore = listState.isLoading, // Indicate if loading next page
                        canLoadMore = listState.canLoadMore, // Pass flag from state
                        onLoadMore = { viewModel.getResearchVehicles(loadNextPage = true) }, // Action to load next page
                        onVehicleClick = { vehicleId ->
                            if (vehicleId != null) { // Add null check for safety
                                viewModel.loadVehicleForEdit(vehicleId) // Pre-load data using ID
                                navController.navigate(HomeRoutes.AddVehicleScreen) // Navigate to edit/view screen
                            }
                        },
                        onEditClick = { vehicleId ->
                            viewModel.loadVehicleForEdit(vehicleId)
                            navController.navigate(HomeRoutes.AddVehicleScreen)
                        },
                        onGalleryClick = { vehicleId, vin ->
                            navController.navigate(HomeRoutes.VehicleGalleryScreen(vehicleId, vin))
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryList(
    vehicles: List<Vehicle>,
    isLoadingMore: Boolean, // To show loading indicator at the bottom
    canLoadMore: Boolean,   // To decide whether to trigger load more
    onLoadMore: () -> Unit, // Callback to load the next page
    onVehicleClick: (String?) -> Unit, // Callback when a vehicle card is clicked
    onEditClick: (String) -> Unit,
    onGalleryClick: (String, String) -> Unit
) {
    val listState = rememberLazyListState() // State for the LazyColumn

    LazyColumn(
        state = listState, // Attach the scroll state
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 24.dp,
            bottom = 80.dp
        ), // Padding around the list
        verticalArrangement = Arrangement.spacedBy(20.dp) // Space between items
    ) {
        itemsIndexed(
            items = vehicles,
            // Key MUST be unique for each item for performance and animations
            key = { index, vehicle -> vehicle.id ?: "${vehicle.vin}-$index" }
        ) { index, vehicle ->
            VehicleCard(
                vehicle = vehicle,
                onClick = { onVehicleClick(vehicle.id) }, // Pass VIN back on click
                onEditClick = { vehicle.id?.let { onEditClick(it) } },
                onGalleryClick = { vehicle.id?.let { id -> onGalleryClick(id, vehicle.vin) } },
                modifier = Modifier.animateItem( // Basic fade-in animation
                    fadeInSpec = tween(300),
                    fadeOutSpec = tween(300)
                )
            )
        }

        // Show loading indicator at the bottom when loading the next page
        if (isLoadingMore) {
            item { // Add a single item slot for the indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = themeColor)
                }
            }
        }
    }

    // --- Logic to automatically trigger loading the next page ---
    val shouldLoadMore by remember {
        derivedStateOf { // Calculate based on scroll state
            val layoutInfo = listState.layoutInfo
            // Get index of the last visible item
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItemsCount = layoutInfo.totalItemsCount

            // Trigger when near the end (e.g., 3 items from the end),
            // but only if not already loading and if more pages might exist
            !isLoadingMore && canLoadMore && lastVisibleItemIndex >= totalItemsCount - 3 && totalItemsCount > 0
        }
    }

    // Effect that triggers onLoadMore when shouldLoadMore becomes true
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            Log.d("InventoryList", "Requesting next page...")
            onLoadMore()
        }
    }
}


@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x0D000000)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image Section with Gradient Overlay
            Box(contentAlignment = Alignment.BottomStart) {
                AsyncImage(
                    model = (vehicle.thumbnailImage.takeIf { !it.isNullOrBlank() }
                        ?: "https://placehold.co/600x400/EAEAEA/9E9E9E?text=No+Image") + "?t=${System.currentTimeMillis()}",
                    contentDescription = "${vehicle.year} ${vehicle.brandName} ${vehicle.modelName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Slightly taller image
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )

                // Action Buttons (Edit / Gallery) - Top Right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onEditClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    // Gallery Button
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onGalleryClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Outlined.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Title Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${vehicle.year} ${vehicle.brandName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${vehicle.modelName} ${vehicle.trimName}",
                        style = MaterialTheme.typography.headlineSmall, // Larger title
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Info Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Price and Condition Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${
                            String.format(
                                "%,.0f",
                                vehicle.dealerPrice ?: 0.0
                            )
                        }",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor // Blue price
                    )

                    // Condition Chip
                    vehicle.condition?.let { condition ->
                        Surface(
                            color = themeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = condition.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Key Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VehicleInfoChip(
                        icon = Icons.Outlined.Speed,
                        label = "MILEAGE",
                        value = "${String.format("%,d", vehicle.mileage?.toInt() ?: 0)} mi"
                    )
                    VehicleInfoChip(
                        icon = Icons.Outlined.ConfirmationNumber,
                        label = "STOCK #",
                        value = vehicle.stockNo?.takeIf { it.isNotBlank() } ?: "N/A"
                    )
                    VehicleInfoChip(
                        icon = Icons.Outlined.Key,
                        label = "VIN",
                        value = vehicle.vin.takeLast(6) ?: "N/A"
                    )
                }
            }
        }
    }
}


// --- Other Composables (InventoryHeader, Chip, VehicleInfoChip, Dialogs) ---
@Composable
fun InventoryHeader(vehicleCount: Int, onFiltersClicked: () -> Unit, onSortClicked: () -> Unit) {
    // Simplified Header: Only shows count as requested
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "Showing $vehicleCount vehicles",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF64748B), // Slate gray
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun Chip(text: String) {
    // Deprecated in favor of inline Surface in VehicleCard for custom styling
    // Kept to avoid breaking other references if any, but implemented as no-op or simple
    Surface(
        color = themeColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = themeColor
        )
    }
}

@Composable
fun VehicleInfoChip(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(100.dp) // Fixed width for alignment
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8) // Muted gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B), // Dark slate
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AddVehicleOptionsDialog(
    onDismiss: () -> Unit,
    onAddManually: () -> Unit,
    onVinDecode: () -> Unit
) {
    // Dialog shown when the FAB is clicked
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp), // More rounded corners for dialog
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Add a New Vehicle",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Option Card for adding manually
                AddOptionCard(
                    title = "Add Manually",
                    subtitle = "Enter all vehicle details by hand.",
                    icon = Icons.Outlined.DriveFileRenameOutline,
                    onClick = onAddManually
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Option Card for using VIN decoder
                AddOptionCard(
                    title = "Use VIN Decoder",
                    subtitle = "Scan or enter a VIN to auto-fill.",
                    icon = Icons.Outlined.QrCodeScanner,
                    onClick = onVinDecode
                )
            }
        }
    }
}

@Composable
fun AddOptionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    // Reusable card for options within the AddVehicleOptionsDialog
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Make the card clickable
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)), // Light border
        colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.1f)) // Very light blue background
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon( // Icon on the left
                icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = themeColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column { // Title and subtitle text
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyInventory() {
    // Composable shown when the inventory list is empty
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = com.slt.cardealership.R.drawable.file_searching_rafiki),
            contentDescription = "No Vehicles Found",
            modifier = Modifier
                .size(280.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            "No Vehicles Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B), // Darker slate for better readability
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Your inventory is currently empty. Tap the '+' button below to add your first vehicle.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B), // Slate gray
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

