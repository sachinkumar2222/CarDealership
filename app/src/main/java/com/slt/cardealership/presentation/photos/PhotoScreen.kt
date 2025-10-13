package com.slt.cardealership.presentation.photos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    val context = LocalContext.current

    if (uiState.isManageBannerDialogVisible) {
        ManageBannerDialog(
            uiState = uiState,
            onDismiss = viewModel::onDismissManageBannerDialog,
            onTitleChange = viewModel::onBannerTitleChanged,
            onUrlChange = viewModel::onBannerUrlChanged,
            onImageSelected = viewModel::onBannerImageSelected,
            onSave = { viewModel.onSaveBanner(context) }
        )
    }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F8FF)) // Match background
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
            when (uiState.selectedTab) {
                0 -> Text("Google Photos content goes here.")
                1 -> BannerContent(
                    isLoading = uiState.isBannersLoading, // <-- PASS THE LOADING STATE
                    banners = uiState.banners,
                    onAddBanner = viewModel::onAddBannerClicked,
                    onEditBanner = viewModel::onEditBannerClicked,
                )

                2 -> GalleryContent(
                    isLoading = uiState.isGalleryLoading,
                    images = uiState.galleryImages,
                    onAddImages = { uris -> viewModel.uploadGalleryImages(context, uris) },
                )
            }
        }
    }
}

@Composable
fun GalleryItemShimmer() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .shimmer()
    )
}

@Composable
fun PhotoTabCard(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = contentColor)
            Text(text, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}


@Composable
fun BannerContent(
    isLoading: Boolean,
    banners: List<Banner>,
    onEditBanner: (String) -> Unit,
    onAddBanner: () -> Unit
) {
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onAddBanner,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(blueGradient,RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Banner")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Banner")
        }

        if (isLoading) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(3) { // Show 3 shimmer placeholders while loading
                    BannerItemShimmerCard()
                }
            }
        } else if (banners.isEmpty()) {
            EmptyContent(title = "No banner added yet", message = "Manage your banner here")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(banners) { banner ->
                    BannerItemCard(
                        banner = banner,
                        onEditClick = { banner.id?.let { onEditBanner(it) } }
                    )
                }
            }
        }
    }
}

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.9f),
            Color.LightGray.copy(alpha = 0.4f),
            Color.LightGray.copy(alpha = 0.9f)
        ),
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBannerDialog(
    uiState: PhotosUiState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onSave: () -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Manage Banner",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Form Fields
                OutlinedTextField(
                    value = uiState.bannerTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.bannerUrl,
                    onValueChange = onUrlChange,
                    label = { Text("URL *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Image Uploader
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val imageToShow = uiState.bannerImageUri ?: uiState.bannerExistingImageUrl
                    if (uiState.bannerImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = "Upload Image",
                                modifier = Modifier.size(40.dp)
                            )
                            Text("Upload Image")
                        }
                    } else {
                        AsyncImage(
                            model = uiState.bannerImageUri,
                            contentDescription = "Selected Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (imageToShow == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Text("Upload Image")
                        }
                    } else {
                        AsyncImage(
                            model = imageToShow, // <-- Shows new or existing image
                            contentDescription = "Banner Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                // ... (Add validation text if needed)

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message
                if (uiState.error != null) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (uiState.isBannersLoading) {
                        CircularProgressIndicator()
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onSave, shape = RoundedCornerShape(8.dp)) { Text("Save") }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryContent(
    isLoading: Boolean,
    images: List<GalleryImage>,
    onAddImages: (List<Uri>) -> Unit,
) {

    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
    )

    val galleryImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                onAddImages(uris)
            }
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize() // Fill all available space
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Dealership Gallery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { galleryImagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, // Sets the background color of the button
                        contentColor = Color.White    // Sets the color of the Icon and Text
                    ),
                    modifier = Modifier
                        .background(blueGradient,RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.Add, contentDescription = "Add Images")
                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Add Images")
                }
            }
        }

        if (isLoading) {
            items(6) {
                GalleryItemShimmer()
            }
        } else if (images.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyContent(title = "No gallery images yet", message = "Add your first photo")
            }
        } else {
            // Show the actual image grid when loading is complete
            items(images) { image ->
                Box(contentAlignment = Alignment.TopEnd) {
                    AsyncImage(
                        model = image.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

    }
}

@Composable
fun BannerItemCard(banner: Banner, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Banner Image
            AsyncImage(
                // IMPORTANT: This assumes 'imagePath' is a full URL.
                // If it's a partial path, you must add your base URL:
                // model = "https://backend-api-stg.sba.net${banner.imagePath}",
                model = banner.imageUrl,
                contentDescription = banner.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f), // A common aspect ratio for banners
                contentScale = ContentScale.Fit
            )

            // Banner Details and Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = banner.title ?: "No Title",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = banner.url ?: "No URL",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Action Buttons
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { /* TODO: Handle Delete */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun BannerItemShimmerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shimmer() // Apply shimmer
            )

            // Details Placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .shimmer()
                    ) // Title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                            .shimmer()
                    ) // URL
                }
                // Action Icon Placeholders
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .shimmer()
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
        Icon(
            Icons.Default.Folder,
            contentDescription = "",
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, color = Color.Gray)
    }
}