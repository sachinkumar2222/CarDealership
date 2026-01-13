package com.slt.cardealership.presentation.websitedashboard.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.DomainPage
import com.slt.cardealership.domain.model.DomainPageDetails
import com.slt.cardealership.presentation.home.HomeRoutes
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsitePagesScreen(
    navController: NavController,
    domainId: Int,
    viewModel: WebsitePagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.fetchPages(domainId)
    }

    // Listen for refresh requests from other screens (e.g., AddPageScreen)
    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    val refreshPages by savedStateHandle?.getLiveData<Boolean>("refresh_pages")?.observeAsState() ?: mutableStateOf(false)

    LaunchedEffect(refreshPages) {
        if (refreshPages == true) {
            viewModel.fetchPages(domainId)
            savedStateHandle?.remove<Boolean>("refresh_pages")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var pageToEdit by remember { mutableStateOf<DomainPage?>(null) }

    val sliders by viewModel.sliders.collectAsState()

    // Separate effect for details to handle the suspend call and state update
    val fetchedPageDetailsState = remember { mutableStateOf<DomainPageDetails?>(null) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    LaunchedEffect(showEditDialog, pageToEdit) {
        if (showEditDialog && pageToEdit != null) {
            isLoadingDetails = true
            viewModel.fetchSliders(domainId.toString())
            fetchedPageDetailsState.value = viewModel.getPageDetails(pageToEdit!!.id)
            isLoadingDetails = false
        }
    }

    if (showEditDialog && pageToEdit != null && fetchedPageDetailsState.value != null) {
        ComplexEditPageDialog(
            page = fetchedPageDetailsState.value!!,
            domainId = domainId,
            sliders = sliders,
            onDismiss = { showEditDialog = false },
            onSave = { request ->
                viewModel.updatePage(request)
                showEditDialog = false
            }
        )
    }

    if (isLoadingDetails) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pages") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier.shadow(8.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(HomeRoutes.AddPageScreen(domainId)) },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Page")
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        when (val state = uiState) {
            is WebsitePagesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is WebsitePagesUiState.Error -> {
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
            is WebsitePagesUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    PagesList(
                        pages = state.pages,
                        modifier = Modifier.weight(1f),
                        onEditClick = { page ->
                            pageToEdit = page
                            showEditDialog = true
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
fun PagesList(
    pages: List<DomainPage>,
    modifier: Modifier = Modifier,
    onEditClick: (DomainPage) -> Unit,
    paginationContent: @Composable () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pages) { page ->
            PageCard(
                page = page,
                onEditClick = { onEditClick(page) }
            )
        }
        item {
            paginationContent()
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
                text = "Showing ${(currentPage - 1) * itemsPerPage + 1}-${minOf(currentPage * itemsPerPage, totalItems)} of $totalItems pages",
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

@Composable
fun PageCard(
    page: DomainPage,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Page Name and Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = page.pageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (page.status.lowercase()) {
                        "active" -> Color(0xFFE7F5E9)
                        "inactive" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE0E0E0)
                    }
                ) {
                    Text(
                        text = page.status.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                            else it.toString()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (page.status.lowercase()) {
                            "active" -> Color(0xFF2E7D32)
                            "inactive" -> Color(0xFFE65100)
                            else -> Color(0xFF616161)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Page Slug
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Slug: ",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = page.pageSlug,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Page Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Type: ",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = page.pageTypeName,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row with Metadata and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Updated: ${formatTimestamp(page.updatedOn)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "By: ${page.updatedBy}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2196F3)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        // Assuming timestamp is in seconds based on "1761833692" (year 2025)
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = Date(timestamp * 1000)
        sdf.format(date)
    } catch (e: Exception) {
        "-"
    }
}




