package com.slt.cardealership.presentation.photos

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
// import androidx.compose.ui.res.stringResource // Not strictly needed unless using string resources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.GalleryImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    navController: NavController,
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
                    FloatingActionButton(
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add")
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

        // Bottom Sheet for Manage Banner
        if (uiState.isManageBannerDialogVisible) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { viewModel.onDismissManageBannerDialog() },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                AddEditBannerScreen(
                    uiState = uiState,
                    onDismiss = {
                        viewModel.onDismissManageBannerDialog()
                    },
                    onTitleChange = viewModel::onBannerTitleChanged,
                    onUrlChange = viewModel::onBannerUrlChanged,
                    onImageSelected = viewModel::onBannerImageSelected,
                    onSave = { viewModel.onSaveBanner(context) },
                    onStartDateChange = viewModel::onBannerStartDateChanged,
                    onEndDateChange = viewModel::onBannerEndDateChanged
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
    val primaryColor = Color(0xFF2196F3)
    val contentColor = if (isSelected) primaryColor else Color.Gray
    val borderColor = if (isSelected) primaryColor else Color(0xFFE0E0E0)

    Card(
        modifier = modifier.height(52.dp),
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
        if (isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(6) { // Show 6 shimmer placeholders
                    BannerItemShimmerCard()
                }
            }
        } else if (banners.isEmpty()) {
            EmptyContent(title = "No banner added yet", message = "Manage your banner here")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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

@Composable
fun GalleryContent(
    isLoading: Boolean,
    images: List<GalleryImage>,
    onAddImages: (List<Uri>) -> Unit,
    onDeleteImage: (GalleryImage) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
        }

        if (isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) {
                    GalleryItemShimmer()
                }
            }
        } else if (images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyContent(title = "No gallery images yet", message = "Add your first photo")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(images) { image ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = image.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Delete button with wavy border positioned at top-right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(0.dp)
                        ) {
                            // Wavy decorative background
                            Canvas(
                                modifier = Modifier
                                    .size(44.dp)
                            ) {
                                val wavePath = Path().apply {
                                    // Start from top-right corner
                                    moveTo(size.width, 0f)

                                    // Top edge (straight to match corner)
                                    lineTo(size.width * 0.3f, 0f)

                                    // Left wavy edge
                                    val waveHeight = 4.dp.toPx()
                                    val waveCount = 3
                                    val segmentHeight = size.height / waveCount

                                    for (i in 0 until waveCount) {
                                        val y1 = i * segmentHeight
                                        val y2 = (i + 0.5f) * segmentHeight
                                        val y3 = (i + 1) * segmentHeight

                                        cubicTo(
                                            0f, y1,
                                            -waveHeight, y2,
                                            0f, y3
                                        )
                                    }

                                    // Bottom wavy edge
                                    val bottomWaveWidth = 4.dp.toPx()
                                    val bottomWaveCount = 3
                                    val segmentWidth = size.width / bottomWaveCount

                                    for (i in 0 until bottomWaveCount) {
                                        val x1 = i * segmentWidth
                                        val x2 = (i + 0.5f) * segmentWidth
                                        val x3 = (i + 1) * segmentWidth

                                        cubicTo(
                                            x1, size.height,
                                            x2, size.height + bottomWaveWidth,
                                            x3, size.height
                                        )
                                    }

                                    // Right edge (straight)
                                    lineTo(size.width, size.height)
                                    lineTo(size.width, 0f)
                                    close()
                                }

                                drawPath(
                                    path = wavePath,
                                    color = Color.White,
                                    style = Fill
                                )
                            }

                            // Delete icon centered in the wavy shape
                            IconButton(
                                onClick = { onDeleteImage(image) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Image",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
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
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Optional: Navigate to detail? */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow
    ) {
        Column {
            // Image Container with Menu Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f) // Landscape Aspect Ratio for shorter cards
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient Overlay for Menu Visibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp) // Gradient height
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Menu Button Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp) // Reduced padding slightly to push it closer to edge if needed
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                        // Background removed for professional look
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Reduced padding
                verticalArrangement = Arrangement.spacedBy(2.dp) // Reduced spacing
            ) {
                // Title
                Text(
                    text = banner.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // URL
                if (!banner.url.isNullOrBlank()) {
                    Text(
                        text = banner.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575), // Grey 600
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Date
                val dateText = remember(banner.createdOn) {
                    val millis = (banner.createdOn ?: 0L) * 1000L
                    if (millis > 0) {
                        "Created On: " + java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))
                    } else {
                        "No Date"
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BannerItemShimmerCard() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(12.dp))
                .shimmer()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp)
                .shimmer()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .shimmer()
        )
    }
}


@Composable
fun EmptyContent(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.file_searching_rafiki),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
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