package com.slt.cardealership.presentation.ads

import com.slt.cardealership.presentation.navigation.HomeRoutes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Advertisement // Import the correct model
// Removed invalid import
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Local data class is no longer needed
val BrandBlue = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsScreen(
    navController: NavController,
    // Scope the ViewModel to the parent graph to share with AddAdsScreen
    parentEntry: androidx.navigation.NavBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(HomeRoutes.Dashboard::class)
    },
    viewModel: AdsViewModel = hiltViewModel(parentEntry)
) {
    val listState by viewModel.adsListState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf<Advertisement?>(null) }

    // --- Load ads when the screen is first composed ---
    LaunchedEffect(Unit) {
        viewModel.getAdvertisements()
    }

    // --- Handle events from ViewModel ---
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AdsEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
                is AdsEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
                AdsEvent.NavigateBack -> {
                    // This event is handled by AddAdsScreen
                }
            }
        }
    }

    // --- Delete Confirmation Dialog ---
    showDeleteDialog?.let { adToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Advertisement?") },
            text = { Text("Are you sure you want to delete '${adToDelete.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAdvertisement(adToDelete.id!!) // Use non-null ID
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Advertisement", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                   viewModel.prepareNewAdForm()
                   navController.navigate(HomeRoutes.AddAdsScreen)
                },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Advertisement"
                )
            }
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        AdvertisementList(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            listState = listState, // Pass the state
            onEditClick = { ad ->
                // viewModel.loadAdForEdit(ad.id!!) // Handled in EditAdsScreen
                navController.navigate(HomeRoutes.EditAdsScreen(ad.id!!))
            },
            onDeleteClick = { ad ->
                showDeleteDialog = ad // Show confirmation dialog
            }
        )
    }
}

@Composable
fun AdvertisementList(
    modifier: Modifier = Modifier,
    listState: AdsListState,
    onEditClick: (Advertisement) -> Unit,
    onDeleteClick: (Advertisement) -> Unit
) {
    Column(modifier = modifier) {
        // Removed fixed Text header from here

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Add padding for FAB
        ) {
            // Header item - Always visible and scrollable
            item {
                Text(
                    text = "Manage your advertisements here",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            when {
                listState.isLoading -> {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                listState.ads.isEmpty() -> {
                    item {
                        EmptyState()
                    }
                }
                else -> {
                    items(listState.ads, key = { it.id ?: it.title }) { ad ->
                        AdvertisementListItem(
                            advertisement = ad,
                            onEditClick = { onEditClick(ad) },
                            onDeleteClick = { onDeleteClick(ad) }
                        )
                    }
                }
            }
        }

        listState.error?.let {
            // Show error inline if list is empty, otherwise Snackbar handles it
            if (listState.ads.isEmpty() && !listState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Function to format timestamp
private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "N/A"
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = Date(timestamp * 1000) // Convert seconds to milliseconds
        sdf.format(date)
    } catch (e: Exception) {
        "Invalid Date"
    }
}


@Composable
fun AdvertisementListItem(
    advertisement: Advertisement,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // For Menu
    var isDetailsVisible by remember { mutableStateOf(false) } // For Accordion
    val isDeleted = advertisement.isDeleted == true


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
                 // Left Side: Name and Details Summary
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
                            val titleColor = if (isDeleted) Color.Gray else Color.Black
                            Text(
                                text = advertisement.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = if (isDeleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isDeleted) "Deleted" else (advertisement.type ?: "General"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDeleted) Color.Red else BrandBlue,
                                    fontWeight = FontWeight.Medium
                                )
                                val dateDisplay = if (isDeleted) {
                                    val updatedOn = advertisement.updatedOn
                                    if (updatedOn != null) formatTimestamp(updatedOn) else "N/A"
                                } else {
                                    formatTimestamp(advertisement.startDate)
                                }
                                
                                Text(
                                    text = " • $dateDisplay",
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
                                        tint = if (isDeleted) Color.LightGray else BrandBlue
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    if (!isDeleted) onEditClick()
                                },
                                enabled = !isDeleted,
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Black,
                                    leadingIconColor = BrandBlue,
                                    disabledTextColor = Color.LightGray,
                                    disabledLeadingIconColor = Color.LightGray
                                )
                            )
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                            DropdownMenuItem(
                                text = { Text("Delete", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = if (isDeleted) Color.LightGray else Color.Red
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    if (!isDeleted) onDeleteClick()
                                },
                                enabled = !isDeleted,
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Red,
                                    leadingIconColor = Color.Red,
                                    disabledTextColor = Color.LightGray,
                                    disabledLeadingIconColor = Color.LightGray
                                )
                            )
                        }
                 }
            }
            
            // --- Expandable Details ---
            AnimatedVisibility(visible = isDetailsVisible) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))
                    
                    val rowColor1 = Color(0xFFF9FAFB)
                    val rowColor2 = Color.White
                    
                    // DetailRow("Adv Type", advertisement.type ?: "N/A", rowColor1) // Already in header
                    DetailRow("Is Deleted", if (isDeleted) "Yes" else "No", rowColor1)
                    DetailRow("Make", advertisement.makeNames ?: "N/A", rowColor2)
                    DetailRow("Model", advertisement.modelName ?: "N/A", rowColor1)
                    DetailRow("Keywords", advertisement.keywords ?: "N/A", rowColor2)
                    DetailRow("End Date", if(advertisement.endDate == null) "No End Date" else formatTimestamp(advertisement.endDate), rowColor1)
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

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.file_searching_rafiki),
            contentDescription = "No advertisement found",
            modifier = Modifier.size(200.dp) // Adjusted size for illustration
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No advertisements found", // Updated text
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AdsScreenPreview() {
    MaterialTheme {
        AdsScreen(navController = rememberNavController())
    }
}

