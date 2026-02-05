package com.slt.cardealership.presentation.websitedashboard.researchCompare.manageCategory

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.domain.model.ResearchBlogCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoryScreen(
    domainId: Int,
    domainName: String,
    viewModel: ManageCategoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<ResearchBlogCategory?>(null) }

    // Fetch categories on entry
    LaunchedEffect(Unit) {
        viewModel.fetchCategories(domainId)
    }

    LaunchedEffect(actionState) {
        if (actionState is ManageCategoryViewModel.ActionState.Success) {
            showBottomSheet = false
            categoryToEdit = null
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Category", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoryToEdit = null
                    showBottomSheet = true
                },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F3F5))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sr No.",
                        modifier = Modifier.weight(0.2f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Category Name",
                        modifier = Modifier.weight(0.6f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Actions",
                        modifier = Modifier.weight(0.2f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }

                // Content
                when (val state = uiState) {
                    is ManageCategoryUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF2196F3))
                        }
                    }
                    is ManageCategoryUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color.Red)
                        }
                    }
                    is ManageCategoryUiState.Success -> {
                        if (state.categories.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No categories found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn {
                                itemsIndexed(state.categories) { index, category ->
                                    CategoryRow(
                                        index = index + 1,
                                        category = category,
                                        onEdit = {
                                            categoryToEdit = category
                                            showBottomSheet = true
                                        },
                                        onDelete = {
                                            category.id?.let { id -> viewModel.deleteCategory(id, domainId) }
                                        }
                                    )
                                    if (index < state.categories.size - 1) {
                                        Divider(color = Color(0xFFEEEEEE))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                categoryToEdit = null
            },
            sheetState = sheetState
        ) {
            AddEditCategorySheetContent(
                category = categoryToEdit,
                onSave = { name, desc ->
                    if (categoryToEdit == null) {
                        viewModel.createCategory(domainId, domainName, name, desc)
                    } else {
                        categoryToEdit?.id?.let { id ->
                            viewModel.updateCategory(id, domainId, domainName, name, desc)
                        }
                    }
                },
                onCancel = {
                    showBottomSheet = false
                    categoryToEdit = null
                },
                isLoading = actionState is ManageCategoryViewModel.ActionState.Loading
            )
        }
    }
}

@Composable
fun CategoryRow(
    index: Int,
    category: ResearchBlogCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = category.name ?: "N/A",
            modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.bodyMedium
        )
        Box(
            modifier = Modifier.weight(0.2f),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Actions",
                    tint = Color.Gray
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color.White),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3)) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                )
            }
        }
    }
}

@Composable
fun AddEditCategorySheetContent(
    category: ResearchBlogCategory?,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 24.dp) // Extra padding for safety
    ) {
        Text(
            text = "Category Name",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            enabled = category == null, // Disable editing if category exists (Edit mode)
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledContainerColor = Color(0xFFF5F5F5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Category Description",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(name, description) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(8.dp),
            enabled = name.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save")
        }
    }
}

