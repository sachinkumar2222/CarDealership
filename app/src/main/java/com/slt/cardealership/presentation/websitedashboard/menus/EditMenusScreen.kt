package com.slt.cardealership.presentation.websitedashboard.menus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.DomainMenuItem
import com.slt.cardealership.domain.model.DomainPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenusScreen(
    navController: NavController,
    domainId: Int,
    menuId: String,
    viewModel: EditMenusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(domainId, menuId) {
        viewModel.fetchData(domainId, menuId)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Edit Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.initiateAddMenuItem(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Menu Item") },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        when (val state = uiState) {
            is EditMenuUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is EditMenuUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is EditMenuUiState.Success -> {
                if (state.isSaved) {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
                val formState by viewModel.formState.collectAsState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Menu Name
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Menu name*", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = formState.menuName,
                                onValueChange = viewModel::onNameChange,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color(0xFF2196F3)
                                )
                            )
                        }
                    }

                    // Add Menu Items Section (List Only)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header removed as requested

                            if (formState.menuItems.isEmpty()) {
                                Text("No items added yet.", color = Color.Gray)
                            } else {
                                formState.menuItems.forEachIndexed { index, item ->
                                    RecursiveMenuItem(
                                        item = item,
                                        onUpdate = { updatedItem ->
                                            viewModel.updateMenuItem(index, updatedItem)
                                        },
                                        onRemove = {
                                            viewModel.removeMenuItem(index)
                                        },
                                        onAddSubMenu = { target ->
                                            viewModel.initiateAddMenuItem(target)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    // Menu Settings
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Menu settings", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = formState.isTopPrimary,
                                    onCheckedChange = viewModel::onTopPrimaryChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                                )
                                Text("Top primary menu")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = formState.isFooter,
                                    onCheckedChange = viewModel::onFooterChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                                )
                                Text("Footer menu")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = formState.isFooterBottom,
                                    onCheckedChange = viewModel::onFooterBottomChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                                )
                                Text("Footer bottom menu")
                            }
                        }
                    }

                    // Update Button
                    Button(
                        onClick = { viewModel.saveMenu(domainId, menuId) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = formState.menuName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Update", color = Color.White)
                    }
                }

                if (formState.isAddDialogVisible) {
                    AddMenuItemBottomSheet(
                        sheetState = sheetState,
                        pages = state.pages,
                        selectedPageIds = formState.selectedPageIds,
                        onDismiss = viewModel::onDismissAddDialog,
                        onPageSelectionChange = viewModel::onPageSelectionChange,
                        onAddPages = { viewModel.onAddPagesToMenu(state.pages) },
                        onAddCustomLink = viewModel::onAddCustomLinkToMenu
                    )
                }
            }
        }
    }
}
