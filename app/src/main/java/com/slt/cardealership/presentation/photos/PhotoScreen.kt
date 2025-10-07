package com.slt.cardealership.presentation.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.GalleryImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    navController: NavController, // <-- 1. Add NavController as a parameter
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 2. WRAP THE ENTIRE SCREEN IN A SCAFFOLD ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photos", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F2F5)) // Match background
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Use padding from Scaffold
                .background(Color(0xFFF0F8FF))
                .padding(horizontal = 16.dp), // Add horizontal padding for content
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Tab selection cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoTabCard(
                    text = "Google Photos",
                    icon = Icons.Default.Cloud,
                    isSelected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    modifier = Modifier.weight(1f)
                )
                PhotoTabCard(
                    text = "Banner",
                    icon = Icons.Default.ViewCarousel,
                    isSelected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                )
                PhotoTabCard(
                    text = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    isSelected = uiState.selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Content based on selected tab
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (uiState.selectedTab) {
                    0 -> Text("Google Photos content goes here.")
                    1 -> BannerContent(banners = uiState.banners)
                    2 -> GalleryContent(images = uiState.galleryImages)
                }
            }
        }
    }
}

@Composable
fun PhotoTabCard(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = contentColor)
            Text(text, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Composable
fun BannerContent(banners: List<Banner>) {
    if (banners.isEmpty()) {
        EmptyContent(title = "No banner added yet", message = "Manage your banner here")
    } else {
        // Display list of banners
    }
}

@Composable
fun GalleryContent(images: List<GalleryImage>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Dealership Gallery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Button(onClick = { /* TODO: Launch Image Picker */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Images")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Images")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(images) { image ->
                AsyncImage(
                    model = image.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun EmptyContent(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Folder, contentDescription = "", modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, color = Color.Gray)
    }
}