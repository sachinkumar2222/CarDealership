package com.slt.cardealership.presentation.ads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.domain.model.Advertisement // Import the correct model
import com.slt.cardealership.presentation.home.HomeRoutes
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Local data class is no longer needed

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
                title = { Text("All Advertisements", fontWeight = FontWeight.Bold) },
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
            val blueGradient = Brush.horizontalGradient(
                colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
            )
            Box(
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(blueGradient)
                    .clickable {
                        viewModel.prepareNewAdForm() // Prepare ViewModel for a new ad
                        navController.navigate(HomeRoutes.AddAdsScreen)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Advertisement",
                    tint = Color.White
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
                viewModel.loadAdForEdit(ad.id!!) // Load data for editing
                navController.navigate(HomeRoutes.AddAdsScreen)
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
        Text(
            text = "Manage your advertisements here",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when {
            listState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            listState.ads.isEmpty() -> {
                EmptyState()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = advertisement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f) // Allow text to take space
                )
                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 8.dp))

            DetailRow("Adv Type:", advertisement.type ?: "N/A")
            DetailRow("Start Date:", formatTimestamp(advertisement.startDate))
            DetailRow("End Date:", if(advertisement.endDate == null) "No End Date" else formatTimestamp(advertisement.endDate))
            DetailRow("Make:", advertisement.makeNames ?: "N/A")
            DetailRow("Model:", advertisement.modelName ?: "N/A")
            DetailRow("Keywords:", advertisement.keywords ?: "N/A")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(100.dp),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.DarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOff,
            contentDescription = "No advertisement found",
            modifier = Modifier.size(120.dp),
            tint = Color.LightGray
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

