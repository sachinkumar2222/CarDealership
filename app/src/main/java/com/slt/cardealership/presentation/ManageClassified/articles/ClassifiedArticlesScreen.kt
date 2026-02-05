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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(HomeRoutes.AddEditClassifiedArticle(siteId = siteId)) },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp) // Matches standard FAB shape usually, or Circle
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Sort
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Articles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Text("Sort by", color = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(Color.White),

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
                                            color = if (isSelected) Color(0xFF2196F3) else Color.Black
                                        )
                                    },
                                    onClick = {
                                        currentSortLabel = label
                                        viewModel.onSortChange(sortBy, sortOrder)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                when (val state = uiState) {
                    is ArticlesUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ArticlesUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = Color.Red)
                                Button(onClick = { viewModel.fetchData() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is ArticlesUiState.Success -> {
                        if (state.posts.isEmpty()) {
                            EmptyArticleState()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.name ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.status?.replaceFirstChar { it.uppercase() } ?: "Draft",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (post.status == "published") Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                    if (post.createdOn != null && post.createdOn > 0) {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(post.createdOn)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Gray
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .width(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    )
                }
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
