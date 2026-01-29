package com.slt.cardealership.presentation.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.material3.HorizontalDivider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.presentation.home.HomeRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                onClick = { navController.navigate(HomeRoutes.AddEditArticle()) },
                containerColor = Color(0xFF2196F3)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Article", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5)),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ArticleUiState.Loading -> CircularProgressIndicator()
                is ArticleUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ArticleUiState.Success -> {
                    if (state.articles.isEmpty()) {
                        EmptyArticleState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.articles) { article ->
                                ArticleItemCard(
                                    post = article,
                                    onDeleteClick = {
                                        article.id?.let { viewModel.deleteArticle(it) }
                                    },
                                    onEditClick = {
                                        navController.navigate(HomeRoutes.AddEditArticle(articleId = article.id))
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
                            text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(post.createdOn)),
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
            imageVector = Icons.Default.Article,
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