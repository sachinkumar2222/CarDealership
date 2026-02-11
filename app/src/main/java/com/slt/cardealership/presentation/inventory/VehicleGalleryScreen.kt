package com.slt.cardealership.presentation.inventory

import com.slt.cardealership.presentation.navigation.HomeRoutes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
// Removed invalid import
import com.slt.cardealership.presentation.vehicle.VehicleViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleGalleryScreen(
    navController: NavController,
    vehicleId: String,
    vin: String
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(HomeRoutes.Dashboard::class)
    }
    val viewModel: VehicleViewModel = hiltViewModel(parentEntry)
    val galleryState by viewModel.galleryState.collectAsState()
    val context = LocalContext.current

    // Selection State
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedImages = remember { mutableStateListOf<String>() }

    // Initialize gallery loading
    LaunchedEffect(vehicleId) {
        viewModel.getVehicleGallery(vehicleId)
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val files = uris.mapNotNull { uri ->
                uriToFile(context, uri)
            }
            if (files.isNotEmpty()) {
                viewModel.uploadVehicleGalleryImages(vehicleId, files)
            }
        }
    }

    // Handle Upload Success/Error
    LaunchedEffect(galleryState) {
        if (galleryState.multiUploadSuccess) {
            Toast.makeText(context, "Images uploaded successfully", Toast.LENGTH_SHORT).show()
            viewModel.onGalleryEventConsumed() // Reset state to avoid duplicate toasts
        }
        galleryState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.onGalleryEventConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedImages.size} Selected" else "Vehicle Gallery",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedImages.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Selection")
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        if (selectedImages.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.deleteGalleryImages(vehicleId, selectedImages.toList())
                                isSelectionMode = false
                                selectedImages.clear()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Red)
                            }
                        }
                    } else {
                        TextButton(onClick = { isSelectionMode = true }) {
                            Text("Select", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Bottom Actions (Hide in Selection Mode) - Moved to bottomBar for stability
            if (!isSelectionMode) {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.loadVehicleForEdit(vehicleId)
                                navController.navigate(HomeRoutes.AddVehicleScreen)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF2196F3)),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Edit Vehicle", color = Color(0xFF2196F3))
                        }

                        Button(
                            onClick = { navController.popBackStack() }, // Save acts as Back/Confirm here
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section with Counts (Refactored)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${galleryState.gallery?.images?.size ?: 0} images",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            // Removed Old Button Row as requested

            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            // VIN Display
            if (!isSelectionMode) { // Optional: Hide VIN in selection mode to focus on images
                Column {
                    Text("VIN", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFE9ECEF),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = vin,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Upload Area (Visual Trigger)
                Surface(
                    color = Color(0xFFFFF8E1), // Light yellow background
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFFFFA000))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Images.", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Image Grid
            if (galleryState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val images = galleryState.gallery?.images ?: emptyList()
                    items(images) { imageUrl ->
                        val isSelected = selectedImages.contains(imageUrl)

                        Box(
                            modifier = Modifier
                                .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isSelectionMode) {
                                        if (isSelected) {
                                            selectedImages.remove(imageUrl)
                                        } else {
                                            selectedImages.add(imageUrl)
                                        }
                                    } else {
                                        // Standard click (maybe view full screen in future)
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = "$imageUrl?t=${System.currentTimeMillis()}",
                                contentDescription = "Gallery Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.LightGray)
                            )

                            // Selection Overlay / Indicator
                            if (isSelectionMode) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            if (isSelected) Color.Black.copy(alpha = 0.4f) else Color.Transparent
                                        )
                                )

                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "Selected" else "Unselected",
                                    tint = if (isSelected) Color(0xFF2196F3) else Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(if (isSelected) Color.White else Color.Transparent, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to convert URI to File
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
