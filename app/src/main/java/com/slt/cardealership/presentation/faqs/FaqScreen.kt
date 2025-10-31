package com.slt.cardealership.presentation.faq // Your package name

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SearchOff // <-- Import for empty state
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.slt.cardealership.domain.model.FaqItem // <-- Import REAL model
import com.slt.cardealership.presentation.home.HomeRoutes

// --- DEFINE COLORS ---
private val lightBlueColor = Color(0xFF2196F3)
private val darkBlueColor = Color(0xFF1565C0)

private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        lightBlueColor,
        darkBlueColor
    )
)
// --- END DEFINE COLORS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    navController: NavController, // <-- Changed
    viewModel: FaqViewModel = hiltViewModel() // <-- Added ViewModel
) {
    val listState by viewModel.listState.collectAsState() // <-- Added
    val snackbarHostState = remember { SnackbarHostState() } // <-- Added
    val context = LocalContext.current // <-- Added

    // --- Handle Events (Snackbars & Loading) ---
    LaunchedEffect(Unit) {
        viewModel.getFaqs() // <-- Load data on start

        viewModel.events.collect { event ->
            when (event) {
                is FaqEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FaqEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                FaqEvent.NavigateBack -> {
                    // This event is for the Add/Edit screen, not this one
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // <-- Added
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FAQ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // <-- Changed
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        floatingActionButton = {
            CustomGradientFloatingActionButton(
                onClick = {
                    // --- CONNECTED ---
                    viewModel.prepareNewFaqForm()
                    navController.navigate(HomeRoutes.AddFaqScreen) // <-- Assumed route name
                    // -----------------
                },
                gradient = blueGradient,
                content = {
                    Icon(Icons.Default.Add, contentDescription = "Add New FAQ", tint = Color.White)
                }
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage your FAQ's content from here",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // --- CONNECTED TO VIEWMODEL STATE ---
            when {
                listState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                listState.faqs.isEmpty() -> {
                    EmptyFaqContent(message = "No FAQ found")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(listState.faqs, key = { it.id }) { faqItem ->
                            FaqCard(
                                item = faqItem,
                                onEditClick = {
                                    // --- CONNECTED ---
                                    viewModel.loadFaqForEdit(faqItem.id)
                                    navController.navigate("add_faq_screen") // Assumed route name
                                    // -----------------
                                },
                                onDeleteClick = {
                                    // --- CONNECTED ---
                                    // The 'type' param is required by your API.
                                    // We'll hardcode 'default' as a guess.
                                    viewModel.deleteFaq(faqItem.id, type = "default")
                                    // -----------------
                                }
                            )
                        }
                    }
                }
            }
            // --- END OF CONNECTION ---
        }
    }
}

@Composable
fun CustomGradientFloatingActionButton(
    onClick: () -> Unit,
    gradient: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        contentColor = Color.White,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun FaqCard(
    item: FaqItem, // <-- Now uses the REAL domain model
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "expansion_arrow")

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    Spacer(modifier = Modifier.size(16.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = isExpanded) {
                // NOTE: The 'answer' contains HTML ("<pre>...").
                // This Text composable will show the raw HTML.
                // You may need to use a different composable to render it correctly.
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Show Less" else "Show Answer",
                    style = MaterialTheme.typography.bodySmall,
                    color = lightBlueColor,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand/Collapse",
                    tint = lightBlueColor,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }
    }
}

@Composable
fun EmptyFaqContent(
    message: String
) {
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
            // Use a default icon if R.drawable.file_searching_rafiki is not found
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "Empty illustration",
                modifier = Modifier.size(200.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            /* Image(
                painter = painterResource(id = R.drawable.file_searching_rafiki), // Make sure this drawable exists
                contentDescription = "Empty folder illustration",
                modifier = Modifier.size(200.dp)
            )
            */
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}


// --- Preview Updated ---

// Dummy data for preview using the REAL model
private val dummyFaqPreviewList = listOf(
    FaqItem(id = 1, question = "What is Jetpack Compose?", answer = "Jetpack Compose is Android's modern toolkit...", domain_name = null, created_by = "user", updated_by = "user", created_on = 0, updated_on = 0),
    FaqItem(id = 2, question = "How do I use a Scaffold?", answer = "Scaffold is a layout component...", domain_name = null, created_by = "user", updated_by = "user", created_on = 0, updated_on = 0)
)

@Preview(showBackground = true)
@Composable
fun FaqScreenPreview() {
    MaterialTheme {
        // This is tricky because the composable now relies on a ViewModel.
        // We'll just show the empty state preview for simplicity.
        FaqScreen(navController = rememberNavController())
    }
}