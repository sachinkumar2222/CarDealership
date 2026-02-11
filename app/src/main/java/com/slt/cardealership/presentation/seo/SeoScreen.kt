package com.slt.cardealership.presentation.seo

import com.slt.cardealership.presentation.navigation.HomeRoutes // Your package name


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
// import com.slt.cardealership.presentation.home.HomeRoutes.SeoMapperScreen // Removed
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.ui.theme.BrandBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoScreen(
    navController: NavController,
    viewModel: SeoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- State for Bottom Sheet ---
    var showAddTagSheet by remember { mutableStateOf(false) }

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
                SeoEvent.CloseAddTagDialog -> {
                    showAddTagSheet = false // Close sheet on success
                }
            }
        }
    }

    if (showAddTagSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTagSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            AddSeoSheetContent(
                viewModel = viewModel,
                onCancel = { showAddTagSheet = false }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 8.dp),
                title = { Text("SEO Tags", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                )
            )
        },

        floatingActionButton = {
            // Styled FAB
            Box(
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandBlue)
                    .clickable(onClick = {
                        viewModel.clearAddTagState()
                        showAddTagSheet = true // Open Sheet
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Vehicle",
                    tint = Color.White
                )
            }
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manage SEO Tags",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            OutlinedButton(
                                onClick = { navController.navigate(HomeRoutes.SeoMapperScreen) },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BrandBlue),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = BrandBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    "SEO Tag Mapper",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (uiState.allTags.isEmpty()) {
                        item {
                            EmptySeoContent()
                        }
                    } else {

                        // --- List Items ---
                        itemsIndexed(uiState.allTags, key = { _, item -> item.id ?: item.tagName }) { index, item ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                SeoTagItemRow(
                                    index = index,
                                    item = item,
                                    onDeleteClick = { item.id?.let { viewModel.deleteTag(it) } }
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
fun EmptySeoContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = "Empty illustration",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF94A3B8)
                )
            }
           
            Text(
                text = "No SEO Tags Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center
            )
             Text(
                text = "Tap the + button to add your first SEO tag.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SeoTagItemRow(
    index: Int,
    item: SeoTag,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dropdown Icon (Left)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown, // Or ArrowDropDown
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState),
                    tint = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 2. Title & Subtitle (Middle)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.tagName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (expanded) "Hide details" else "Tap to view details",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (expanded) BrandBlue else Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                // 3. Three Dot Menu (Right)
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = Color(0xFF64748B)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {

                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Black) },
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
            
            // Expandable Content
            androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Tag URL Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Text(
                            text = "URL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.width(40.dp)
                        )
                        Text(
                            text = item.tagUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandBlue,
                            modifier = Modifier.weight(1f),
                            // Selectable text or just text
                        )
                    }
                }
            }
        }
    }
}




// --- 5. BOTTOM SHEET CONTENT ---

@Composable
fun AddSeoSheetContent(
    viewModel: SeoViewModel,
    onCancel: () -> Unit
) {
    val addState by viewModel.addTagState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp), // Extra padding for bottom sheet
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Add New SEO Tag",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Create a new tag to improve search ranking.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Inputs using Common Components
        com.slt.cardealership.presentation.common.LabeledTextField(
            label = "Tag Name *",
            value = addState.tagName,
            onValueChange = { viewModel.onAddTagStateChange(addState.copy(tagName = it)) },
            placeholder = "e.g. Best SUV Deals",
            isError = addState.error != null && addState.tagName.isBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        com.slt.cardealership.presentation.common.LabeledTextField(
            label = "Tag URL *",
            value = addState.tagUrl,
            onValueChange = { viewModel.onAddTagStateChange(addState.copy(tagUrl = it)) },
            placeholder = "e.g. /best-suv-deals",
            isError = addState.error != null && addState.tagUrl.isBlank()
        )

        if (addState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = addState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button using Common Component
        com.slt.cardealership.presentation.common.PrimaryButton(
            onClick = { viewModel.addNewTag() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = addState.tagName.isNotBlank() && addState.tagUrl.isNotBlank() && !addState.isSaving
        ) {
             if (addState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Save Tag",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}



