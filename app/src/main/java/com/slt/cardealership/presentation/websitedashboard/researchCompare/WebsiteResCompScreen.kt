package com.slt.cardealership.presentation.websitedashboard.researchCompare

import com.slt.cardealership.presentation.navigation.HomeRoutes

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.ui.theme.BrandDarkBlue
import com.slt.cardealership.ui.theme.LightBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteResCompScreen(
    navController: NavController,
    domainId: Int,
    viewModel: WebsiteResCompViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.fetchResearchCompares(domainId)
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Research Compare",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                onClick = { navController.navigate(HomeRoutes.AddResearchCompare(domainId, "")) },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Compare")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Add Category Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Research Compare",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { navController.navigate(HomeRoutes.ManageCategory(domainId, "")) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2196F3)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Manage Category", fontWeight = FontWeight.SemiBold)
                    }
                }

                when (val state = uiState) {
                    is WebsiteResCompUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is WebsiteResCompUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(), // Changed to fillMaxSize for better centering
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchResearchCompares(domainId) }) {
                                Text("Retry")
                            }
                        }
                    }
                    is WebsiteResCompUiState.Success -> {
                        if (state.items.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No research compare items found.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            // Card List Layout
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp), // Add bottom padding
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                itemsIndexed(state.items) { _, item ->
                                    ResearchCompareCard(
                                        item = item,
                                        domainId = domainId,
                                        navController = navController,
                                        onDelete = { itemId -> viewModel.deleteResearchCompare(itemId, domainId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResearchCompareCard(
    item: com.slt.cardealership.domain.model.ResearchCompareItem,
    domainId: Int,
    navController: NavController,
    onDelete: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val trimsCompared = item.trims?.joinToString(" VS ") { "${it.makeName.orEmpty()} ${it.modelName.orEmpty()}" } ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trimsCompared.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chip
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.categoryName ?: "N/A",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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
                    border =BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    DropdownMenuItem(
                        text = { Text("View") },
                        onClick = {
                            menuExpanded = false
                            // TODO: Implement View Action
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            item.id?.let { id ->
                                navController.navigate(
                                    HomeRoutes.EditResearchCompare(
                                        domainId = domainId,
                                        researchCompareId = id,
                                        domainName = ""
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            item.id?.let{ onDelete(it) }
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
