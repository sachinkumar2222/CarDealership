package com.slt.cardealership.presentation.photos

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
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

    if (uiState.showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            title = if (uiState.isDeletingBanner) "Delete Banner" else "Delete Image",
            text = "Are you sure you want to permanently delete this item?",
            onDismiss = viewModel::onDismissDeleteDialog,
            onConfirm = viewModel::confirmDelete
        )
    }

    // Hoist the launcher so it can be called from the FAB
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                viewModel.uploadGalleryImages(context, uris)
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.shadow(8.dp),
                    title = { Text("Photos", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F8FF))
                )
            },
            floatingActionButton = {
                val isBannerTab = uiState.selectedTab == 1
                val isGalleryTab = uiState.selectedTab == 2

                if ((isBannerTab || isGalleryTab) && !uiState.isManageBannerDialogVisible) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (isBannerTab) {
                                viewModel.onAddBannerClicked()
                            } else if (isGalleryTab) {
                                // Launch the gallery picker
                                galleryLauncher.launch("image/*")
                            }
                        },
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBannerTab) "Add New Banner" else "Add Images",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF0F8FF))
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
                        isLoading = uiState.isBannersLoading,
                        banners = uiState.banners,
                        onAddBanner = viewModel::onAddBannerClicked,
                        onEditBanner = viewModel::onEditBannerClicked,
                        onDeleteBanner = viewModel::onDeleteBannerClicked
                    )

                    2 -> GalleryContent(
                        isLoading = uiState.isGalleryLoading,
                        images = uiState.galleryImages,
                        onAddImages = { uris -> viewModel.uploadGalleryImages(context, uris) },
                        onDeleteImage = viewModel::onDeleteGalleryImageClicked
                    )
                }
            }
        }

        // Full Screen Overlay for Manage Banner
        AnimatedVisibility(
            visible = uiState.isManageBannerDialogVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.zIndex(2f) // Ensure it sits on top
        ) {
            ManageBannerScreen(
                uiState = uiState,
                onDismiss = viewModel::onDismissManageBannerDialog,
                onTitleChange = viewModel::onBannerTitleChanged,
                onUrlChange = viewModel::onBannerUrlChanged,
                onImageSelected = viewModel::onBannerImageSelected,
                onSave = { viewModel.onSaveBanner(context) }
            )
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
    val primaryColor = Color(0xFF2196F3)
    val contentColor = if (isSelected) primaryColor else Color.Gray
    val borderColor = if (isSelected) primaryColor else Color(0xFFE0E0E0)

    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun BannerContent(
    isLoading: Boolean,
    banners: List<Banner>,
    onEditBanner: (String) -> Unit,
    onAddBanner: () -> Unit,
    onDeleteBanner: (String) -> Unit
) {
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Old Button Removed


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
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(banners) { banner ->
                    BannerItemCard(
                        banner = banner,
                        onEditClick = { banner.id?.let { onEditBanner(it) } },
                        onDeleteClick = { banner.id?.let { onDeleteBanner(it) } }
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
fun ManageBannerScreen(
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

    BackHandler {
        onDismiss()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (uiState.isEditingBanner) "Edit Banner" else "Add New Banner",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = !uiState.isBannersLoading) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Image Upload Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5)) // Light Gray Background
                    .border(
                        1.dp,
                        Color(0xFFE0E0E0), // Light Border
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToShow = uiState.bannerImageUri ?: uiState.bannerExistingImageUrl

                if (imageToShow == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White, CircleShape)
                                .shadow(2.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = "Upload",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "Tap to upload banner image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = "Selected Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay to indicate changeability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit, // Use Filled since Outlined might not be available or imported
                            contentDescription = "Change Image",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.bannerTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Banner Title") },
                    placeholder = { Text("e.g. Summer Sale 2024") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = uiState.bannerUrl,
                    onValueChange = onUrlChange,
                    label = { Text("Destination URL") },
                    placeholder = { Text("https://example.com/promo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
            }

            if (uiState.isBannersLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GalleryContent(
    isLoading: Boolean,
    images: List<GalleryImage>,
    onAddImages: (List<Uri>) -> Unit,
    onDeleteImage: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
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
                // Old button removed
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
                    IconButton(
                        onClick = { image.id?.let { onDeleteImage(it) } }, // <-- Use the handler
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Image",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun BannerItemCard(
    banner: Banner, onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                IconButton(onClick = onDeleteClick) {
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

@Composable
fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}