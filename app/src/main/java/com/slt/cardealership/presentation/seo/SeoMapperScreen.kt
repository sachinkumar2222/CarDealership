package com.slt.cardealership.presentation.seo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.SeoDomain
import com.slt.cardealership.presentation.common.FullScreenError
import com.slt.cardealership.presentation.common.LoadingAnimation
import com.slt.cardealership.presentation.common.PrimaryButton
import com.slt.cardealership.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoMapperScreen(
    navController: NavController,
    viewModel: SeoMapperViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- Bottom Sheet Logic ---
    if (uiState.showManageSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeManageSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            ManageSeoSheet(
                uiState = uiState,
                onSearch = viewModel::onTagSearch,
                onToggleTag = viewModel::toggleTagSelection,
                onSave = viewModel::saveDomainTags,
                onCancel = viewModel::closeManageSheet
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SEO Tag Mapper", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B),
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Header Section with Search
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Domain List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Search and manage SEO tags for your websites",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by domain name...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // List Content
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else if (uiState.error != null) {
                FullScreenError(
                    message = uiState.error!!,
                    onRetry = viewModel::loadDomains
                )
            } else if (uiState.filteredDomains.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No domains found", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredDomains) { domain ->
                        SeoDomainCard(
                            domain = domain,
                            onManageClick = { 
                                viewModel.openManageSheet(domain)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSeoSheet(
    uiState: SeoMapperUiState,
    onSearch: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    // Reset search when sheet opens/re-renders with new content
    LaunchedEffect(uiState.showManageSheet) {
        searchText = ""
        onSearch("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f) // Take up significant height
            .padding(20.dp)
    ) {
        Text(
            text = "Manage Tags for ${uiState.selectedDomain?.domainName ?: "Domain"}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar for Tags
        OutlinedTextField(
            value = searchText,
            onValueChange = { 
                searchText = it
                onSearch(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search tags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isSheetLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(uiState.filteredTags) { _, tag ->
                    val isChecked = uiState.assignedTagIds.contains(tag.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tag.id?.let { onToggleTag(it) } }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { tag.id?.let { id -> onToggleTag(id) } },
                            colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = tag.tagName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Medium
                            )
                             Text(
                                text = tag.tagUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SeoDomainCard(
    domain: SeoDomain,
    onManageClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Domain Name
                Text(
                    text = domain.domainName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Website Type Chip
                Surface(
                    color = if (domain.productTypeName.equals("Classifieds", ignoreCase = true)) 
                        Color(0xFFEFF6FF) else Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = domain.productTypeName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (domain.productTypeName.equals("Classifieds", ignoreCase = true)) 
                            BrandBlue else Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Action Button
                OutlinedButton(
                    onClick = onManageClick,
                    border = BorderStroke(1.dp, BrandBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "Manage",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = BrandBlue
                    )
                }
            }
        }
    }
}
