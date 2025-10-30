package com.slt.cardealership.presentation.inventory

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
                            } else {
                                Log.e("InventoryScreen", "Clicked vehicle with null ID")
                                // Optionally show an error to the user
                            }
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
    onVehicleClick: (String?) -> Unit // Callback when a vehicle card is clicked
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
fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow( // Custom shadow for depth
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = themeColor.copy(alpha = 0.3f),
                ambientColor = themeColor.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick), // Make card clickable
        shape = RoundedCornerShape(16.dp), // Rounded corners
        colors = CardDefaults.cardColors(containerColor = Color.White) // White background
    ) {
        Column {
            // Image Section with Gradient Overlay
            Box(contentAlignment = Alignment.BottomStart) {
                AsyncImage( // Using Coil3 for image loading
                    model = vehicle.thumbnailImage.takeIf { !it.isNullOrBlank() } // Use thumbnail if not blank
                        ?: "https://placehold.co/600x400/EAEAEA/9E9E9E?text=No+Image", // Placeholder
                    contentDescription = "${vehicle.year} ${vehicle.brandName} ${vehicle.modelName}".trim(),
                    contentScale = ContentScale.Crop, // Crop image to fit bounds
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        // Clip top corners to match card shape
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                // Dark gradient overlay at the bottom of the image
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 200f // Adjust gradient start position
                            )
                        )
                )
                // Text overlaid on the gradient
                Column(modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.BottomStart)) {
                    Text(
                        text = "${vehicle.year} ${vehicle.brandName}".trim(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${vehicle.modelName} ${vehicle.trimName}".trim(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // Details Section below the image
            Column(modifier = Modifier.padding(16.dp)) {
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
                        }", // Format price
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = themeColor
                    )
                    // Show condition chip if condition is available
                    vehicle.condition?.let { condition ->
                        Chip(condition.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) // Capitalize
                    }
                }
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
                // Row for Mileage, Stock #, VIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround // Distribute space evenly
                ) {
                    // Format mileage, handle null
                    VehicleInfoChip(
                        icon = Icons.Outlined.Speed,
                        label = "MILEAGE",
                        value = "${String.format("%,d", vehicle.mileage?.toInt() ?: 0)} mi"
                    )
                    // Show Stock # or N/A
                    VehicleInfoChip(
                        icon = Icons.Outlined.ConfirmationNumber,
                        label = "STOCK #",
                        value = vehicle.stockNo?.takeIf { it.isNotBlank() } ?: "N/A")
                    // Show last 6 digits of VIN
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // White background for the header area
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Display the count of currently loaded vehicles
        Text(
            "Showing $vehicleCount vehicles",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        // Row containing Filter and Sort buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
        ) {
            // Filters Button
            Button(
                onClick = onFiltersClicked,
                modifier = Modifier.weight(1f), // Take up half the width
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9ECEF)), // Light gray background
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filters",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF495057)
                ) // Dark icon
                Spacer(Modifier.width(8.dp))
                Text("Filters", color = Color(0xFF495057)) // Dark text
            }
            // Sort Button
            OutlinedButton(
                onClick = onSortClicked,
                modifier = Modifier.weight(1f), // Take up half the width
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFDEE2E6)) // Light border
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = "Sort",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF495057)
                ) // Dark icon
                Spacer(Modifier.width(8.dp))
                Text("Sort By", color = Color(0xFF495057)) // Dark text
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    // Simple Chip composable for displaying tags like 'New', 'Used'
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp)) // Rounded corners
            .background(themeColor.copy(alpha = 0.1f)) // Light blue background
            .padding(horizontal = 10.dp, vertical = 6.dp) // Padding inside the chip
    ) {
        Text(
            text = text.uppercase(), // Display text in uppercase
            color = themeColor, // Use theme color for text
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun VehicleInfoChip(icon: ImageVector, label: String, value: String) {
    // Composable to display small pieces of info with an icon (Mileage, Stock #, VIN)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Min) // Adjust width to content
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = themeColor,
            modifier = Modifier.size(28.dp)
        ) // Icon
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        ) // Label (e.g., MILEAGE)
        Text( // Value (e.g., 22,150 mi)
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0D1B2A), // Dark text color
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // Prevent long values from wrapping awkwardly
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center // Center content vertically and horizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.offset(y = (-50).dp) // Move content up slightly
        ) {
            Icon(
                Icons.Outlined.DirectionsCar, // Car icon
                contentDescription = "Empty Inventory",
                modifier = Modifier.size(120.dp),
                tint = Color.LightGray.copy(alpha = 0.8f) // Light gray tint
            )
            Text(
                "No Vehicles Found",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
            Text(
                "Tap the '+' button below to add your first vehicle to the inventory.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp) // Add horizontal padding for longer text
            )
        }
    }
}

