package com.slt.cardealership.presentation.websitedashboard.blogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.DomainBlog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteBlogsScreen(
    navController: NavController,
    domainId: Int,
    viewModel: WebsiteBlogsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.fetchBlogs(domainId)
    }

    // Observe refresh trigger from savedStateHandle
    LaunchedEffect(Unit) {
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle

        savedStateHandle?.let { handle ->
            handle.getStateFlow("refresh_blogs", false).collect { shouldRefresh ->
                if (shouldRefresh) {
                    viewModel.fetchBlogs(domainId)
                    handle["refresh_blogs"] = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blogs") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                onClick = { navController.navigate(com.slt.cardealership.presentation.home.HomeRoutes.AddBlogScreen(domainId)) },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Blog")
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        when (val state = uiState) {
            is WebsiteBlogsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is WebsiteBlogsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is WebsiteBlogsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    BlogsList(
                        blogs = state.blogs,
                        modifier = Modifier.weight(1f),
                        onEditClick = { blog ->
                            navController.navigate(
                                com.slt.cardealership.presentation.home.HomeRoutes.AddBlogScreen(
                                    domainId = domainId,
                                    blogId = blog.id
                                )
                            )
                        },
                        paginationContent = {
                            PaginationControls(
                                currentPage = state.currentPage,
                                totalPages = state.totalPages,
                                totalItems = state.totalItems,
                                itemsPerPage = state.itemsPerPage,
                                onPreviousClick = { viewModel.goToPreviousPage() },
                                onNextClick = { viewModel.goToNextPage() },
                                onPageClick = { page -> viewModel.goToPage(page) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BlogsList(
    blogs: List<DomainBlog>,
    modifier: Modifier = Modifier,
    onEditClick: (DomainBlog) -> Unit,
    paginationContent: @Composable () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(blogs) { blog ->
            ExpandableBlogCard(
                blog = blog,
                onEditClick = { onEditClick(blog) }
            )
        }
        item {
            paginationContent()
        }
    }
}

@Composable
fun ExpandableBlogCard(
    blog: DomainBlog,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Critical Info + Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Dropdown Icon (Chevron)
                val rotationState by animateFloatAsState(
                    targetValue = if (expanded) 90f else 0f,
                    label = "chevron_rotation"
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp) // Align slightly with text
                        .rotate(rotationState),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Critical Info Column (Title + Type/Date)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = blog.title ?: "Untitled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${blog.blogType} • ${formatTimestamp(blog.createdOn)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Three-dot Action Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
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
                            text = {
                                Text(
                                    "Edit",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF1E88E5), // Blue
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Slug
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Slug: ",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = blog.slug ?: "-",
                            fontSize = 13.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories
                    if (!blog.categories.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Categories: ",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = blog.categories,
                                fontSize = 13.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Status: ",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when ((blog.status ?: "unknown").lowercase()) {
                                "published" -> Color(0xFFE7F5E9)
                                "draft" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE0E0E0)
                            }
                        ) {
                            Text(
                                text = (blog.status ?: "Unknown").replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                    else it.toString()
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = when ((blog.status ?: "unknown").lowercase()) {
                                    "published" -> Color(0xFF2E7D32)
                                    "draft" -> Color(0xFFE65100)
                                    else -> Color(0xFF616161)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Updated By
                    Text(
                        text = "Updated By: ${blog.updatedBy}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    itemsPerPage: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPageClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Info text
            Text(
                text = "Showing ${(currentPage - 1) * itemsPerPage + 1}-${minOf(currentPage * itemsPerPage, totalItems)} of $totalItems blogs",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                OutlinedButton(
                    onClick = onPreviousClick,
                    enabled = currentPage > 1,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Prev", fontSize = 13.sp)
                }

                // Page numbers (Scrollable)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(totalPages) { index ->
                        val page = index + 1
                        if (page == currentPage) {
                            Button(
                                onClick = { },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = page.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onPageClick(page) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = page.toString(),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Next button
                OutlinedButton(
                    onClick = onNextClick,
                    enabled = currentPage < totalPages,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Next", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = Date(timestamp * 1000)
        sdf.format(date)
    } catch (e: Exception) {
        "-"
    }
}
