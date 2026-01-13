//package com.slt.cardealership.presentation.websitedashboard.menus
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddEditMenuScreen(
//    navController: NavController,
//    domainId: Int,
//    menuId: String? = null,
//    viewModel: AddEditMenuViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsState()
//
//    LaunchedEffect(domainId, menuId) {
//        viewModel.fetchData(domainId, menuId)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (menuId == null) "Add Menu" else "Edit Menu") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White,
//                    titleContentColor = Color.Black,
//                    navigationIconContentColor = Color.Black
//                )
//            )
//        },
//        containerColor = Color(0xFFF5F7FA)
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            when (val state = uiState) {
//                is AddEditMenuUiState.Loading -> {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                }
//                is AddEditMenuUiState.Error -> {
//                    Text(
//                        text = state.message,
//                        color = Color.Red,
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//                is AddEditMenuUiState.Success -> {
//                    val formState by viewModel.formState.collectAsState()
//
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp)
//                            .verticalScroll(rememberScrollState())
//                    ) {
//                        // Menu Name Section
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = CardDefaults.cardColors(containerColor = Color.White),
//                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                Text(
//                                    text = "Menu name*",
//                                    style = MaterialTheme.typography.labelMedium,
//                                    color = Color.Gray
//                                )
//                                Spacer(modifier = Modifier.height(8.dp))
//                                OutlinedTextField(
//                                    value = formState.menuName,
//                                    onValueChange = viewModel::onNameChange,
//                                    modifier = Modifier.fillMaxWidth(),
//                                    singleLine = true
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Menu Settings Section
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = CardDefaults.cardColors(containerColor = Color.White),
//                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                Text(
//                                    text = "Menu settings",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Checkbox(
//                                        checked = formState.isTopPrimary,
//                                        onCheckedChange = viewModel::onTopPrimaryChange
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text("Top primary menu")
//                                }
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Checkbox(
//                                        checked = formState.isFooter,
//                                        onCheckedChange = viewModel::onFooterChange
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text("Footer menu")
//                                }
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Checkbox(
//                                        checked = formState.isFooterBottom,
//                                        onCheckedChange = ""
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text("Footer bottom menu")
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Menu Items Section
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = CardDefaults.cardColors(containerColor = Color.White),
//                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                Column(
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    Text(
//                                        text = "Add menu items",
//                                        style = MaterialTheme.typography.titleMedium,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                    Text(
//                                        text = "(Note: Click & Drag to rearrange the menu items!)",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = Color.Gray
//                                    )
//                                    Spacer(modifier = Modifier.height(12.dp))
//                                    Button(
//                                        onClick = viewModel::onAddMenuItemClick,
//                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
//                                        modifier = Modifier.fillMaxWidth() // Full width for mobile
//                                    ) {
//                                        Text("Add Menu Item", color = MaterialTheme.colorScheme.primary)
//                                    }
//                                }
//                                Spacer(modifier = Modifier.height(16.dp))
//
//                                formState.menuItems.forEachIndexed { index, item ->
//                                    RecursiveMenuItem(item = item, onUpdate = { updatedItem ->
//                                        // TODO: Handle update
//                                    })
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        // Update Button
//                        Button(
//                            onClick = { /* TODO: Update */ },
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
//                        ) {
//                            Text("Update", color = Color.White)
//                        }
//                    }
//
//                    if (formState.isAddDialogVisible) {
//                        AddMenuItemDialog(
//                            pages = state.pages,
//                            selectedPageIds = formState.selectedPageIds,
//                            onDismiss = viewModel::onDismissAddDialog,
//                            onPageSelectionChange = viewModel::onPageSelectionChange,
//                            onAddPages = { viewModel.onAddPagesToMenu(state.pages) },
//                            onAddCustomLink = viewModel::onAddCustomLinkToMenu
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RecursiveMenuItem(
//    item: com.slt.cardealership.domain.model.DomainMenuItem,
//    onUpdate: (com.slt.cardealership.domain.model.DomainMenuItem) -> Unit,
//    depth: Int = 0
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(start = (depth * 16).dp), // Indentation
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
//        shape = RoundedCornerShape(4.dp),
//        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
//    ) {
//        Column {
//            // Header Row
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { expanded = !expanded }
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = item.menuLabel,
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Medium
//                )
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = if (!item.pageSlug.isNullOrEmpty()) "Page" else "Custom Link",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Icon(
//                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                        contentDescription = if (expanded) "Collapse" else "Expand",
//                        tint = Color.Gray
//                    )
//                }
//            }
//
//            // Expanded Content
//            if (expanded) {
//                HorizontalDivider(color = Color(0xFFE0E0E0))
//                Column(modifier = Modifier.padding(16.dp)) {
//                    // Label
//                    Text("Navigation Label", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                    OutlinedTextField(
//                        value = item.menuLabel,
//                        onValueChange = { onUpdate(item.copy(menuLabel = it)) },
//                        modifier = Modifier.fillMaxWidth(),
//                        singleLine = true
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Type Specific Fields
//                    if (!item.pageSlug.isNullOrEmpty()) {
//                        Text("Page Slug", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                        OutlinedTextField(
//                            value = item.pageSlug ?: "",
//                            onValueChange = { onUpdate(item.copy(pageSlug = it)) },
//                            modifier = Modifier.fillMaxWidth(),
//                            singleLine = true,
//                            enabled = false // Usually selected via dropdown, keeping read-only for now
//                        )
//                    } else {
//                        Text("URL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                        OutlinedTextField(
//                            value = item.customUrl ?: "",
//                            onValueChange = { onUpdate(item.copy(customUrl = it)) },
//                            modifier = Modifier.fillMaxWidth(),
//                            singleLine = true
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Target
//                    Text("Target", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                    OutlinedTextField(
//                        value = item.target ?: "",
//                        onValueChange = { onUpdate(item.copy(target = it)) },
//                        modifier = Modifier.fillMaxWidth(),
//                        singleLine = true
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Params
//                    Text("Params", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                    OutlinedTextField(
//                        value = item.prms ?: "",
//                        onValueChange = { onUpdate(item.copy(prms = it)) },
//                        modifier = Modifier.fillMaxWidth(),
//                        singleLine = true
//                    )
//
//                    // Remove Button
//                    Spacer(modifier = Modifier.height(16.dp))
//                    TextButton(
//                        onClick = { /* TODO: Remove */ },
//                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
//                    ) {
//                        Text("Remove")
//                    }
//                }
//            }
//        }
//    }
//
//    // Render Children
//    if (item.childItems.isNotEmpty()) {
//        Spacer(modifier = Modifier.height(8.dp))
//        item.childItems.forEach { child ->
//            RecursiveMenuItem(
//                item = child,
//                onUpdate = { updatedChild ->
//                    // TODO: Update child in parent list
//                },
//                depth = depth + 1
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//    }
//}
//
//@Composable
//fun AddMenuItemDialog(
//    pages: List<com.slt.cardealership.domain.model.DomainPage>,
//    selectedPageIds: Set<String>,
//    onDismiss: () -> Unit,
//    onPageSelectionChange: (String, Boolean) -> Unit,
//    onAddPages: () -> Unit,
//    onAddCustomLink: (String, String, String) -> Unit
//) {
//    var selectedTab by remember { mutableIntStateOf(0) }
//    val tabs = listOf("Page", "Custom link")
//
//    // Custom Link State
//    var linkLabel by remember { mutableStateOf("") }
//    var linkUrl by remember { mutableStateOf("") }
//    var linkTarget by remember { mutableStateOf("_self") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Add menu item") },
//        text = {
//            Column {
//                TabRow(selectedTabIndex = selectedTab) {
//                    tabs.forEachIndexed { index, title ->
//                        Tab(
//                            selected = selectedTab == index,
//                            onClick = { selectedTab = index },
//                            text = { Text(title) }
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//
//                when (selectedTab) {
//                    0 -> { // Page Tab
//                        Column(
//                            modifier = Modifier
//                                .height(300.dp)
//                                .verticalScroll(rememberScrollState())
//                        ) {
//                            pages.forEach { page ->
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .clickable {
//                                            onPageSelectionChange(
//                                                page.id,
//                                                !selectedPageIds.contains(page.id)
//                                            )
//                                        }
//                                        .padding(vertical = 8.dp)
//                                ) {
//                                    Checkbox(
//                                        checked = selectedPageIds.contains(page.id),
//                                        onCheckedChange = { isChecked ->
//                                            onPageSelectionChange(page.id, isChecked)
//                                        }
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text("page.title")
//                                }
//                            }
//                        }
//                    }
//                    1 -> { // Custom Link Tab
//                        Column {
//                            OutlinedTextField(
//                                value = linkLabel,
//                                onValueChange = { linkLabel = it },
//                                label = { Text("Navigation Label") },
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = linkUrl,
//                                onValueChange = { linkUrl = it },
//                                label = { Text("URL") },
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = linkTarget,
//                                onValueChange = { linkTarget = it },
//                                label = { Text("Target") },
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    if (selectedTab == 0) {
//                        onAddPages()
//                    } else {
//                        onAddCustomLink(linkLabel, linkUrl, linkTarget)
//                    }
//                }
//            ) {
//                Text("Add To Menu")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
//}
