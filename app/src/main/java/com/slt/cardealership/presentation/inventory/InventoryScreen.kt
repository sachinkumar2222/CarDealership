package com.slt.cardealership.presentation.inventory

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddOptionsDialog by remember { mutableStateOf(false) }

    if (showAddOptionsDialog) {
        AddVehicleOptionsDialog(
            onDismiss = { showAddOptionsDialog = false },
            onAddManually = {
                showAddOptionsDialog = false
                // navController.navigate("add_vehicle_manual") // TODO: UNCOMMENT TO NAVIGATE
            },
            onVinDecode = {
                showAddOptionsDialog = false
                // TODO: Handle VIN decode navigation/logic
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Light grey background
        topBar = {
            TopAppBar(
                title = { Text("All Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0D1B2A)
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddOptionsDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            InventoryHeader(
                vehicleCount = uiState.vehicles.size,
                onFiltersClicked = { /* TODO */ },
                onSortClicked = { /* TODO */ }
            )
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.vehicles.isEmpty()) {
                EmptyInventory()
            } else {
                InventoryList(uiState.vehicles) { vehicleId ->
                    // navController.navigate("vehicle_details/$vehicleId") // TODO: UNCOMMENT FOR DETAILS
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryList(vehicles: List<Vehicle>, onVehicleClick: (String?) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(vehicles, key = { it.id!! }) { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                onClick = { onVehicleClick(vehicle.id) },
                modifier = Modifier.animateItem(
                    placementSpec = tween(durationMillis = 300)
                )
            )
        }
    }
}

@Composable
fun InventoryHeader(vehicleCount: Int, onFiltersClicked: () -> Unit, onSortClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "Showing $vehicleCount vehicles",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onFiltersClicked,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9ECEF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters", modifier = Modifier.size(20.dp), tint = Color(0xFF495057))
                Spacer(Modifier.width(8.dp))
                Text("Filters", color = Color(0xFF495057))
            }
            OutlinedButton(
                onClick = onSortClicked,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFDEE2E6))
            ) {
                Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(20.dp), tint = Color(0xFF495057))
                Spacer(Modifier.width(8.dp))
                Text("Sort By", color = Color(0xFF495057))
            }
        }
    }
}

@Composable
fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(contentAlignment = Alignment.BottomStart) {
                AsyncImage(
                    model = vehicle.previewImageUrl ?: "https://placehold.co/600x400/E0E0E0/757575?text=Vehicle",
                    contentDescription = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = vehicle.trim ?: "N/A",
                        fontSize = 16.sp,
                        color = Color.LightGray
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%,.2f", vehicle.price ?: 0.0)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    vehicle.condition?.let { condition ->
                        Chip(condition)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    VehicleInfoChip(icon = Icons.Outlined.Speed, label = "MILEAGE", value = "${String.format("%,d", vehicle.mileage ?: 0)} mi")
                    VehicleInfoChip(icon = Icons.Outlined.ConfirmationNumber, label = "STOCK #", value = vehicle.stockNumber ?: "N/A")
                    VehicleInfoChip(icon = Icons.Outlined.CalendarToday, label = "DII", value = vehicle.daysInInventory.toString())
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun VehicleInfoChip(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D1B2A))
    }
}

@Composable
fun AddVehicleOptionsDialog(
    onDismiss: () -> Unit,
    onAddManually: () -> Unit,
    onVinDecode: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add a New Vehicle", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                AddOptionCard(
                    title = "Add Manually",
                    subtitle = "Enter all vehicle details by hand.",
                    icon = Icons.Outlined.DriveFileRenameOutline,
                    onClick = onAddManually
                )
                Spacer(modifier = Modifier.height(16.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyInventory() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.offset(y = (-50).dp) // Move up slightly
        ) {
            Icon(
                Icons.Outlined.CarCrash,
                contentDescription = "Empty Inventory",
                modifier = Modifier.size(120.dp),
                tint = Color.LightGray
            )
            Text(
                "Your Inventory is Empty",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Get started by clicking the '+' button to add your first vehicle.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

