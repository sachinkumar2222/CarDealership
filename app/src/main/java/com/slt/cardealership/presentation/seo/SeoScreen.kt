package com.slt.cardealership.presentation.seo // Your package name

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        lightBlueColor,
        darkBlueColor
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
    val context = LocalContext.current
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // --- Header Row with Title and ONLY Filters Button ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Seo Tags",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Filters not implemented", Toast.LENGTH_SHORT).show() },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filters",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filters")
                    }
                }

                // --- Subtitle Text (unchanged) ---
                Text(
                    text = "Manage your SEO tags from here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- List or Empty State (Connected) ---
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.allTags.isEmpty()) {
                    EmptySeoContent()
                } else {
                    SeoTagList(
                        list = uiState.allTags,
                        onEditClick = { tagToEdit ->
                            viewModel.loadTagForEdit(tagToEdit)
                            navController.navigate(HomeRoutes.AddSeoScreen)
                        },
                        onDeleteClick = { tagToDelete ->
                            viewModel.deleteTag(tagToDelete.id)
                        }
                    )
                }
            }

            // --- Custom Floating Action Button (Connected) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(blueGradient, RoundedCornerShape(12.dp))
                        // --- FIX: Navigate to AddSeoScreen ---
                        .clickable(onClick = { viewModel.clearAddTagState()
                            navController.navigate(HomeRoutes.AddSeoScreen) }), // <-- ASSUMING THIS IS YOUR ROUTE
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

        // --- FIX: Removed the AddSeoTagDialog composable ---
    }
}

@Composable
fun SeoTagList(
    list: List<SeoTag>,
    onEditClick: (SeoTag) -> Unit,
    onDeleteClick: (SeoTag) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // --- Table Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sr No.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.15f)
            )
            Text(
                "Tag Name",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.3f)
            )
            Text(
                "Tag URL", // <-- FIX: Changed from Description
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.35f)
            )
            Text(
                "Actions",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.End
            )
        }

        // --- Table Rows ---
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(list, key = { _, item -> item.id }) { index, item ->
                SeoTagItemRow(
                    index = index,
                    item = item,
                    onEditClick = { onEditClick(item) },
                    onDeleteClick = { onDeleteClick(item) }
                )
            }
        }
    }
}

@Composable
fun SeoTagItemRow(
    index: Int,
    item: SeoTag, // <-- FIX: Use correct SeoTag model
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            (index + 1).toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.15f)
        )
        Text(
            item.tagName, // <-- FIX: Use API field tagName
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = item.tagUrl, // <-- FIX: Use API field tagUrl
            style = MaterialTheme.typography.bodyMedium,
            color = lightBlueColor,
            modifier = Modifier.weight(0.35f)
        )
        Row(
            modifier = Modifier.weight(0.2f),
            horizontalArrangement = Arrangement.End
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onDeleteClick)
            )
        }
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


