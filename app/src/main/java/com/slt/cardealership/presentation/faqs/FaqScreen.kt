package com.slt.cardealership.presentation.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.slt.cardealership.presentation.faqs.AddFaqSheet
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.presentation.home.HomeRoutes
import com.slt.cardealership.ui.theme.BrandBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    navController: NavController,
    viewModel: FaqViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    // Bottom Sheet State
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    // Local state for search
    var searchQuery by remember { mutableStateOf("") }

    // Filter FAQs based on search query
    val filteredFaqs = remember(listState.faqs, searchQuery) {
        if (searchQuery.isBlank()) {
            listState.faqs
        } else {
            listState.faqs.filter {
                it.question.contains(searchQuery, ignoreCase = true) ||
                        it.answer.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // --- Handle Events (Snackbars) ---
    LaunchedEffect(Unit) {
        viewModel.getFaqs()

        viewModel.events.collect { event ->
            when (event) {
                is FaqEvent.ShowSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                is FaqEvent.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                FaqEvent.NavigateBack -> {
                    showSheet = false
                }
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = { Text("FAQ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.prepareNewFaqForm()
                    showSheet = true
                },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New FAQ")
            }
        },
        containerColor = Color(0xFFF5F7FA) // Slightly lighter background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { focusManager.clearFocus() } // Clear focus on background click
        ) {

            // --- Header & Search Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Manage your FAQ content",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search questions...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- List Content ---
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    listState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandBlue)
                    }
                    filteredFaqs.isEmpty() -> {
                        EmptyFaqContent(
                            message = if (searchQuery.isNotEmpty()) "No results found for \"$searchQuery\""
                            else "No FAQs added yet"
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = androidx.compose.foundation.lazy.rememberLazyListState(),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp)
                        ) {
                            items(filteredFaqs, key = { it.id }) { faqItem ->
                                FaqCard(
                                    item = faqItem,
                                    onEditClick = {
                                        viewModel.loadFaqForEdit(faqItem.id)
                                        showSheet = true
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteFaq(faqItem.id, type = "default")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // --- Bottom Sheet ---
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.5f)
            ) {
               AddFaqSheet(
                   viewModel = viewModel,
                   onClose = { showSheet = false }
               )
            }
        }
    }
}

@Composable
fun FaqCard(
    item: FaqItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "expansion_arrow")

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Question + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top, // Align top to handle multi-line titles
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Expand Icon on Left
                 Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(top = 2.dp, end = 12.dp) // Align visually with text top
                        .size(24.dp),
                    tint = Color(0xFF94A3B8)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         // Domain/Type Tag
                        if (!item.domain_name.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = item.domain_name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        // Date
                        Text(
                            text = formatDate(item.created_on),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Action Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp) // Slightly smaller touch target visual, but keep usable
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        )
                    }
                }
            }

            // Expanded Content (Answer)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Answer:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF334155),
                        lineHeight = 22.sp
                    )
                }
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
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "Empty",
                modifier = Modifier.size(100.dp),
                tint = Color.LightGray
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper to format date
private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}