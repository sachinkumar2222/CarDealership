package com.slt.cardealership.presentation.seo // Your package name


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.domain.model.SeoTag // Import your REAL SeoTag model
import com.slt.cardealership.presentation.home.HomeRoutes
import com.slt.cardealership.presentation.seo.SeoEvent
import com.slt.cardealership.presentation.seo.SeoViewModel

// --- DEFINE COLORS ---
private val lightBlueColor = Color(0xFF2196F3)
private val darkBlueColor = Color(0xFF1565C0)

// Define the gradient
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3),
        Color(0xFF2196F3)
    )
)
// --- END DEFINE COLORS ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoScreen(
    navController: NavController,
    viewModel: SeoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Handle Events (Snackbars & Dialog) ---
    LaunchedEffect(Unit) {
        // Load data when screen appears.
        viewModel.loadSeoScreenData(domainId = 0)

        viewModel.events.collect { event ->
            when (event) {
                is SeoEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SeoEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                // FIX: Removed CloseAddTagDialog, as this screen no longer opens it
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 6.dp),
                title = { Text("SEO Tags", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Add padding for FAB
                ) {
                    item {
                        Text(
                            text = "Manage your SEO tags from here",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(horizontal = 16.dp,vertical = 16.dp)
                        )
                    }

                    if (uiState.allTags.isEmpty()) {
                        item {
                            EmptySeoContent()
                        }
                    } else {


                        // --- List Items ---
                        itemsIndexed(uiState.allTags, key = { _, item -> item.id }) { index, item ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                SeoTagItemRow(
                                    index = index,
                                    item = item,
                                    onDeleteClick = { viewModel.deleteTag(item.id) }
                                )
                            }
                        }
                    }
                }
            }

            // --- Custom Floating Action Button ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(blueGradient, RoundedCornerShape(12.dp))
                        .clickable(onClick = {
                            viewModel.clearAddTagState()
                            navController.navigate(HomeRoutes.AddSeoScreen)
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add New SEO Tag",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SeoTagItemRow(
    index: Int,
    item: SeoTag,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.tagName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 8.dp))

            DetailRow("Tag URL:", item.tagUrl)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(80.dp),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = lightBlueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun EmptySeoContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff, // Changed from custom drawable
                contentDescription = "Empty illustration",
                modifier = Modifier.size(150.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Text(
                text = "No seo tag found",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}


