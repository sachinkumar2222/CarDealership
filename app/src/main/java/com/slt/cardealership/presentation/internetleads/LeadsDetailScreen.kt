package com.slt.cardealership.presentation.internetleads

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slt.cardealership.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.domain.model.InternetLead
import com.slt.cardealership.presentation.leads.InternetLeadsViewModel
import com.slt.cardealership.ui.theme.BrandDarkBlue

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsDetailScreen(
    title: String,
    onBackClick: () -> Unit,
    // Inject the ViewModel here
    viewModel: InternetLeadsViewModel = hiltViewModel()
) {
    // 1. Trigger data loading when the screen opens (or title changes)
    LaunchedEffect(title) {
        viewModel.loadLeadsByTitle(title)
    }

    // 2. Collect the UI state
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LeadsDetailTopBar(
                title = title,
                onBackClick = onBackClick
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 3. Handle Loading State
            if (uiState.isLoading && uiState.leads.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BrandDarkBlue
                )
            }
            // 4. Handle Error State
            else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // 5. Handle Success/Empty State
            else {
                if (uiState.leads.isEmpty()) {
                    EmptyLeadsView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        items(uiState.leads) { lead ->
                            LeadItemCard(lead = lead)
                        }

                        // Optional: Loading indicator at the bottom for pagination
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }

                    // Optional: Trigger next page load
                    if (uiState.canLoadMore && !uiState.isLoading) {
                        LaunchedEffect(uiState.leads.size) {
                            viewModel.loadNextPage()
                        }
                    }
                }
            }
        }
    }
}

// --- Top App Bar (Unchanged) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsDetailTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = BrandDarkBlue
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// --- UPDATED: Lead Item Card to use InternetLead ---
@Composable
fun LeadItemCard(
    lead: InternetLead
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Row: Name and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${lead.firstName ?: ""} ${lead.lastName ?: ""}".trim().ifEmpty { "Unknown Name" },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandDarkBlue
                )
                Text(
                    text = lead.date ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Middle Section: Details
            // We safely handle nulls with "?:"
            if (!lead.make.isNullOrBlank()) {
                LeadDetailRow(label = "Make", value = lead.make)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (!lead.model.isNullOrBlank()) {
                LeadDetailRow(label = "Model", value = lead.model)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Showing Email as "Domain" or contact info since 'domain' wasn't in the basic model
            LeadDetailRow(label = "Email", value = lead.email ?: "N/A")
        }
    }
}

// --- Helper composable (Unchanged) ---
@Composable
fun LeadDetailRow(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}

// --- Empty Leads View (Unchanged) ---
@Composable
fun EmptyLeadsView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.file_searching_rafiki),
            contentDescription = "No leads",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Leads found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}