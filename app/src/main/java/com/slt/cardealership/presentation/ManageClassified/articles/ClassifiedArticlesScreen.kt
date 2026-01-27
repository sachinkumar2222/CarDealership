package com.slt.cardealership.presentation.ManageClassified.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.slt.cardealership.presentation.home.HomeRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifiedArticlesScreen(
    siteId: String,
    navController: NavController,
    viewModel: ClassifiedArticlesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- Automatic Refresh Logic ---
    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    val shouldRefresh by savedStateHandle?.getLiveData<Boolean>("should_refresh")?.observeAsState() ?: mutableStateOf(false)

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh == true) {
            viewModel.fetchData() // Refresh data
            savedStateHandle?.remove<Boolean>("should_refresh") // Reset flag
        }
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortLabel by remember { mutableStateOf("Created On (desc)") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Articles", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Sort Dropdown
                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(currentSortLabel, color = Color.Black)
                            Icon(
                                if (showSortMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            val options = listOf(
                                Triple("Created On (desc)", "CreatedOn", "desc"),
                                Triple("Created On (asc)", "CreatedOn", "asc"),
                                Triple("Updated On (desc)", "UpdatedOn", "desc"),
                                Triple("Updated On (asc)", "UpdatedOn", "asc")
                            )

                            options.forEach { (label, sortBy, sortOrder) ->
                                val isSelected = currentSortLabel == label
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = if (isSelected) Color.White else Color.Black
                                        )
                                    },
                                    onClick = {
                                        currentSortLabel = label
                                        viewModel.onSortChange(sortBy, sortOrder)
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.background(
                                        if (isSelected) Color(0xFF2196F3) else Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(HomeRoutes.AddEditClassifiedArticle(siteId = siteId)) },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Article")
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5)),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ArticlesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ArticlesUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = Color.Red)
                        Button(onClick = { viewModel.fetchData() }) {
                            Text("Retry")
                        }
                    }
                }
                is ArticlesUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        EmptyArticleState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.posts) { post ->
                                ArticleItemCard(
                                    post = post,
                                    onEditClick = {
                                        navController.navigate(
                                            HomeRoutes.AddEditClassifiedArticle(
                                                siteId = siteId,
                                                articleId = post.id
                                            )
                                        )
                                    },
                                    onDeleteClick = {
                                        postToDelete = post
                                        showDeleteDialog = true
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                postToDelete = null
            },
            title = { Text("Delete Article") },
            text = { Text("Are you sure you want to delete \"${postToDelete?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        postToDelete?.id?.let { viewModel.deletePost(it) }
                        showDeleteDialog = false
                        postToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    postToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ArticleItemCard(
    post: Post,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = post.image,
                    contentDescription = post.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Status Chip (Overlay)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (post.status == "published") Color(0xFFE8F5E9) else Color(0xFFFFF3E0).copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = post.status?.replaceFirstChar { it.uppercase() } ?: "Draft",
                        color = if (post.status == "published") Color(0xFF2E7D32) else Color(0xFFE65100),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // More Options Menu (Overlay) - High Visibility
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .size(36.dp), // Fixed size for consistency
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options", modifier = Modifier.size(20.dp))
                    }

                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(160.dp), // Consistent width
                            containerColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3)) },
                                onClick = {
                                    onEditClick()
                                    menuExpanded = false
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
                            DropdownMenuItem(
                                text = { Text("Delete", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onDeleteClick()
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = post.name ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Meta info row (Date)
                if (post.createdOn != null && post.createdOn > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = try {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(Date(post.createdOn))
                            } catch (e: Exception) {
                                "-"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content Preview with HTML stripped
                val contentPreview = remember(post.content) {
                    post.content?.replace(Regex("<.*?>"), "")?.trim() ?: "No content"
                }

                Text(
                    text = contentPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray, // Slightly darker for readability
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun EmptyArticleState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Article,
            contentDescription = "No Articles",
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No articles yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Click the '+' button to add your first article.",
            color = Color.Gray
        )
    }
}
