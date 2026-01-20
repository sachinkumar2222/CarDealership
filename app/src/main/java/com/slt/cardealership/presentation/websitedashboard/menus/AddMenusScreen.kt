package com.slt.cardealership.presentation.websitedashboard.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenusScreen(
    navController: NavController,
    domainId: Int,
    viewModel: AddMenusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(domainId) {
        viewModel.fetchData(domainId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Add Menu") },
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
        containerColor = Color(0xFFF5F7FA) // Light gray bg
    ) { paddingValues ->
        when (val state = uiState) {
            is AddMenuUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AddMenuUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is AddMenuUiState.Success -> {
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
                    verticalArrangement = Arrangement.Top
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Menu Name Section
                            Text(
                                "Menu name*",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = formState.menuName,
                                onValueChange = viewModel::onNameChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F0FE).copy(alpha = 0.5f), RoundedCornerShape(4.dp)), // Slight blue tint like screenshot
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFE8F0FE).copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color(0xFFE8F0FE).copy(alpha = 0.5f),
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(4.dp),
                                singleLine = true
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color.LightGray.copy(alpha = 0.5f)
                            )

                            // Menu Settings Section
                            Text(
                                "Menu settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Checkboxes
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = formState.isTopPrimary,
                                    onCheckedChange = viewModel::onTopPrimaryChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                                )
                                Text("Top primary menu")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = formState.isFooter,
                                    onCheckedChange = viewModel::onFooterChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                                )
                                Text("Footer menu")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Add Button
                    Button(
                        onClick = { viewModel.saveMenu(domainId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = formState.menuName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            disabledContainerColor = Color(0xFF2196F3).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}


