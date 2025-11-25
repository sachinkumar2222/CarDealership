package com.slt.cardealership.presentation.seomenu// Make sure this package matches

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// --- 1. IMPORT YOUR VIEWMODEL AND EVENTS ---
import com.slt.cardealership.presentation.seomenu.SeoMenuEvent
import com.slt.cardealership.presentation.seomenu.SeoMenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeoMenuScreen(
    onBackClick: () -> Unit,
    viewModel: SeoMenuViewModel = hiltViewModel() // <-- 2. Inject the main ViewModel
) {
    // --- 3. Observe the NEW UI State ---
    val uiState by viewModel.addMenuUiState.collectAsState()
    val context = LocalContext.current

    var isMenuTargetExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    // --- 4. Load categories when the screen starts ---
    LaunchedEffect(key1 = true) {
        viewModel.loadAddScreenData()
    }

    // --- 5. Listen for events from the ViewModel ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SeoMenuEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SeoMenuEvent.SaveAddSuccessAndNavBack -> {
                    onBackClick() // Navigate back on success
                }
                // Ignore events for the list screen
                is SeoMenuEvent.SaveListSuccessAndNavBack -> {}
                else -> {}
            }
        }
    }

    val customColor = Color(0xFF11233c)
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedIndicatorColor = customColor,
        unfocusedIndicatorColor = customColor.copy(alpha = 0.7f),
        focusedLabelColor = customColor,
        unfocusedLabelColor = customColor,
        focusedTextColor = customColor,
        unfocusedTextColor = customColor,
        focusedTrailingIconColor = customColor,
        unfocusedTrailingIconColor = customColor
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add SEO Menu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = customColor,
                    navigationIconContentColor = customColor
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.saveNewMenuItem() }, // <-- 6. Connect Save button
                enabled = !uiState.isSaving, // Disable when saving
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColor,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Save",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // --- 7. Menu Category Dropdown (Connected) ---
                    ExposedDropdownMenuBox(
                        expanded = isCategoryExpanded,
                        onExpandedChange = { isCategoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Menu Category") },
                            placeholder = { Text("Select Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            colors = textFieldColors
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryExpanded,
                            onDismissRequest = { isCategoryExpanded = false }
                        ) {
                            uiState.allCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name, color = customColor) },
                                    onClick = {
                                        viewModel.updateAddFormCategory(category)
                                        isCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // --- 8. Menu Label (Connected) ---
                    OutlinedTextField(
                        value = uiState.menuLabel,
                        onValueChange = { viewModel.updateAddFormLabel(it) },
                        label = { Text("Menu Label") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    // --- 9. Menu URL (Connected) ---
                    OutlinedTextField(
                        value = uiState.menuUrl,
                        onValueChange = { viewModel.updateAddFormUrl(it) },
                        label = { Text("Menu Url") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    // --- 10. Menu Target Dropdown (Connected) ---
                    ExposedDropdownMenuBox(
                        expanded = isMenuTargetExpanded,
                        onExpandedChange = { isMenuTargetExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedTarget,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Menu Target") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuTargetExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            colors = textFieldColors
                        )
                        ExposedDropdownMenu(
                            expanded = isMenuTargetExpanded,
                            onDismissRequest = { isMenuTargetExpanded = false }
                        ) {
                            viewModel.menuTargets.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = customColor) },
                                    onClick = {
                                        viewModel.updateAddFormTarget(option)
                                        isMenuTargetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}