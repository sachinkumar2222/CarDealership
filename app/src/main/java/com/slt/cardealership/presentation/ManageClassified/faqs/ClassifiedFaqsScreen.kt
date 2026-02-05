package com.slt.cardealership.presentation.ManageClassified.faqs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.presentation.home.HomeRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifiedFaqsScreen(
    navController: NavController,
    siteId: String,
    viewModel: ClassifiedFaqsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Bottom Sheet States
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedFaqId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Classified FAQs",
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
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(8.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedFaqId = null // Add mode
                    showBottomSheet = true
                },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add FAQ")
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ClassifiedFaqsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ClassifiedFaqsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
                is ClassifiedFaqsUiState.Content -> {
                    if (state.faqs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No FAQs found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        FaqTable(
                            faqs = state.faqs,
                            onEditClick = { faq ->
                                selectedFaqId = faq.id.toString()
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for Add/Edit
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            modifier = Modifier.statusBarsPadding(),
            content = {
                AddEditFaqBottomSheet(
                    siteId = siteId,
                    faqId = selectedFaqId,
                    onDismiss = { showBottomSheet = false },
                    onSuccess = {
                        showBottomSheet = false
                        viewModel.fetchData() // Refresh list
                    }
                )
            }
        )
    }
}

@Composable
fun FaqTable(faqs: List<FaqItem>, onEditClick: (FaqItem) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .background(Color(0xFFF0F2F5), shape = MaterialTheme.shapes.small)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sr No.",
                modifier = Modifier.weight(0.15f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Question",
                modifier = Modifier.weight(0.65f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Actions",
                modifier = Modifier.weight(0.2f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Divider(color = Color.LightGray, thickness = 0.5.dp)

        // List Content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(faqs) { index, faq ->
                FaqRow(
                    index = index + 1,
                    faq = faq,
                    onEditClick = { onEditClick(faq) }
                )
                Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun FaqRow(index: Int, faq: FaqItem, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = faq.question,
            modifier = Modifier.weight(0.65f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .weight(0.2f)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color(0xFF2196F3)
            )
        }
    }
}
