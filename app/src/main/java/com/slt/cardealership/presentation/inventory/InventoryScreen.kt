package com.slt.cardealership.presentation.inventory

import com.slt.cardealership.presentation.navigation.HomeRoutes

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
// Removed invalid import
import com.slt.cardealership.presentation.vehicle.InventoryEvent // Ensure this import is correct
import com.slt.cardealership.presentation.vehicle.VehicleViewModel
import kotlinx.coroutines.flow.collectLatest

// Constants for styling
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
                else->{}
            }
        }
    }


    val sortOption by viewModel.sortOption.collectAsState()

    if (showAddOptionsDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptionsDialog = false },
            containerColor = Color.White,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddVehicleOptionsSheetContent(
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
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar( // Standard TopAppBar
                title = { Text("Inventory", fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.shadow(elevation = 8.dp) // Subtle shadow
            )
        },
        floatingActionButton = {
            // Styled FAB
            Box(
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(themeColor)
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
                vehicleCount = listState.vehicles.size,
                currentSort = sortOption,
                onSortChange = { newSort -> viewModel.updateSortOption(newSort) }
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
            VehicleListItem(
                vehicle = vehicle,
                onClick = { onVehicleClick(vehicle.id) }, // Pass VIN back on click
                onEditClick = { vehicle.id?.let { onEditClick(it) } },
                onGalleryClick = { vehicle.id?.let { id -> onGalleryClick(id, vehicle.vin) } }
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
fun VehicleListItem(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // For Menu
    var isDetailsVisible by remember { mutableStateOf(false) } // For Accordion

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isDetailsVisible = !isDetailsVisible }, // Toggle expansion
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Header Row (Always Visible) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Name and Price Summary
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Icon(
                        imageVector = if (isDetailsVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Info
                    Column {
                        Text(
                            text = "${vehicle.year} ${vehicle.brandName} ${vehicle.modelName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${String.format("%,.0f", vehicle.dealerPrice ?: 0.0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColor,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " • ${vehicle.condition ?: "Used"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Actions Menu (Prevent ripple from card click)
                Box(modifier = Modifier.wrapContentSize()) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(160.dp),
                        containerColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = themeColor
                                )
                            },
                            onClick = {
                                expanded = false
                                onEditClick()
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        DropdownMenuItem(
                            text = { Text("Gallery", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            onClick = {
                                expanded = false
                                onGalleryClick()
                            }
                        )
                    }
                }
            }

            // --- Expandable Details ---
            androidx.compose.animation.AnimatedVisibility(visible = isDetailsVisible) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))

                    val rowColor1 = Color(0xFFF9FAFB)
                    val rowColor2 = Color.White

                    DetailRow("Trim", vehicle.trimName ?: "N/A", rowColor1)
                    DetailRow("VIN", vehicle.vin, rowColor2)
                    DetailRow("Stock #", vehicle.stockNo ?: "N/A", rowColor1)
                    DetailRow("Mileage", "${String.format("%,d", vehicle.mileage?.toInt() ?: 0)} mi", rowColor2)
                    DetailRow("Status", vehicle.status ?: "Available", rowColor1)
                    DetailRow("Start Date", "N/A", rowColor2) // Placeholder as per request "starting data"
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, backgroundColor: Color, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.weight(0.4f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.DarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}


// --- Other Composables (InventoryHeader, Chip, VehicleInfoChip, Dialogs) ---
@Composable
fun InventoryHeader(
    vehicleCount: Int,
    currentSort: VehicleViewModel.InventorySortOption,
    onSortChange: (VehicleViewModel.InventorySortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vehicle Count with styled text
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E293B), // Dark Slate
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = themeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$vehicleCount",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Advanced Sort Button
        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sort",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // Enhanced Dropdown
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .width(240.dp)
                        .heightIn(max = 400.dp),
                    offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 8.dp)
                ) {
                    Text(
                        text = "Sort Vehicles By",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    
                    VehicleViewModel.InventorySortOption.values().forEach { option ->
                        val isSelected = option == currentSort
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = option.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) themeColor else Color(0xFF334155)
                                    )
                                }
                            },
                            onClick = {
                                onSortChange(option)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when {
                                        option.name.contains("ASC") -> Icons.Default.ArrowUpward
                                        option.name.contains("DESC") -> Icons.Default.ArrowDownward
                                        else -> Icons.Default.SortByAlpha
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) themeColor else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = themeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier.background(
                                if (isSelected) themeColor.copy(alpha = 0.08f) else Color.Transparent
                            )
                        )
                    }
                }
            }
        }
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
fun AddVehicleOptionsSheetContent(
    onAddManually: () -> Unit,
    onVinDecode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Add a New Vehicle",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Choose how you want to add the vehicle",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

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

@Composable
fun AddOptionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)), // Very subtle border
        colors = CardDefaults.cardColors(containerColor = Color.White), // White background
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColor.copy(alpha = 0.1f)), // Light blue background for icon
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = themeColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // Slate 800
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B) // Slate 500
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1) // Light gray chevron
            )
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

