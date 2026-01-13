package com.slt.cardealership.presentation.seomenu

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // <-- 1. IMPORT itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.slt.cardealership.domain.model.SeoCategory
import com.slt.cardealership.presentation.home.HomeRoutes
import com.slt.cardealership.presentation.seomenu.SeoMenuEvent
import com.slt.cardealership.presentation.seomenu.SeoMenuUiItem
import com.slt.cardealership.presentation.seomenu.SeoMenuViewModel

// --- This is your main screen, now connected to the ViewModel ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoMenuScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    viewModel: SeoMenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- Listen for one-time events (like toast messages or navigation) ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SeoMenuEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SeoMenuEvent.SaveSuccessAndNavBack -> {
                    // Save was successful, now navigate back
                    onBackClick()
                }

                else -> {}
            }
        }
    }

    // --- Refresh data when screen resumes (e.g. coming back from Add Screen) ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSeoMenus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val customColor = Color(0xFF2196F3)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = { Text("SEO Menus", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                onClick = { navController.navigate(HomeRoutes.AddSeoMenuScreen) }, // <-- 2. CONNECTED to ViewModel
                containerColor = customColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add New SEO Menu Row",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        bottomBar = {
            // --- 3. This is now a "Save" button, replacing "Bulk Delete" ---
            Button(
                onClick = { viewModel.saveSeoMenus() }, // <-- 4. CONNECTED to ViewModel
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColor, // Use your brand color
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Changes", // <-- 5. TEXT CHANGED
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {
            when {
                // --- 6. Handle VM Loading/Error States ---
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    // --- 7. This is your UI, but connected to the ViewModel ---
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {


                        // --- 8. REMOVED "Select All" header ---
                        // (It is not needed for an editor screen)

                        // --- 9. CONNECTED LazyColumn to ViewModel ---
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 150.dp)    ) {
                            item {
                                Text(
                                    text = "Manage your SEO menu from here",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            itemsIndexed(uiState.menuItems, key = { _, item -> item.localId }) { index, item ->
                                // --- 10. USING THE NEW EDITABLE CARD ---
                                EditableSeoMenuCard(
                                    item = item,
                                    allCategories = uiState.allCategories,
                                    allTargets = viewModel.menuTargets,
                                    onItemChange = { updatedItem ->
                                        // This is how the ViewModel updates the item
                                        viewModel.updateMenuItem(index) { updatedItem }
                                    },
                                    onDelete = {
                                        // This is how the ViewModel deletes an item
                                        viewModel.removeMenuItem(item.localId)
                                    },
                                    customColor = customColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- THIS IS THE NEW EDITABLE CARD ---
// It uses your card's UI, but I have replaced the read-only
// Text fields with editable OutlinedTextFields and Dropdowns.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableSeoMenuCard(
    item: SeoMenuUiItem,
    allCategories: List<SeoCategory>,
    allTargets: List<String>,
    onItemChange: (SeoMenuUiItem) -> Unit,
    onDelete: () -> Unit,
    customColor: Color
) {


    // Define colors for the TextFields
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = customColor,
        unfocusedIndicatorColor = Color.LightGray,
        focusedLabelColor = customColor,
        unfocusedLabelColor = Color.Gray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedTrailingIconColor = customColor,
        unfocusedTrailingIconColor = Color.Gray
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Row 1: Category and Target Dropdowns ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Category Dropdown
                SeoDropdown(
                    label = "Category",
                    selectedOption = item.categoryName,
                    options = allCategories,
                    onOptionSelected = { category ->
                        onItemChange(item.copy(categoryId = category.id, categoryName = category.name))
                    },
                    optionLabel = { it.name },
                    modifier = Modifier.weight(1f)
                )

                // Target Dropdown
                SeoDropdown(
                    label = "Target",
                    selectedOption = item.target,
                    options = allTargets,
                    onOptionSelected = { target ->
                        onItemChange(item.copy(target = target))
                    },
                    optionLabel = { it },
                    modifier = Modifier.weight(1f)
                )
            }

            // --- Row 2: Menu Label (Now Editable) ---
            OutlinedTextField(
                value = item.label,
                onValueChange = { newItemLabel -> onItemChange(item.copy(label = newItemLabel)) },
                label = { Text("Menu Label") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true
            )

            // --- Row 3: Menu URL (Now Editable) ---
            OutlinedTextField(
                value = item.url,
                onValueChange = { newItemUrl -> onItemChange(item.copy(url = newItemUrl)) },
                label = { Text("Menu URL (e.g., https://... )") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true
            )

            // --- Row 4: Delete Button ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete Menu Item",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


