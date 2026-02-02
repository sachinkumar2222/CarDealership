package com.slt.cardealership.presentation.services

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.BuildCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel

import com.slt.cardealership.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(
    onBackClick: () -> Unit,
    onServiceClick: (title: String) -> Unit,
    viewModel: ServicesViewModel = hiltViewModel() // <-- 1. INJECT VIEWMODEL
) {
    // --- 2. GET STATE FROM VIEWMODEL ---
    val uiState by viewModel.uiState.collectAsState()

    val customColor = BrandBlue

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Services", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.shadow(8.dp),
                colors =  TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
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
                    // --- 4. SHOW CONTENT ON SUCCESS ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Manage Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // --- 5. USE VIEWMODEL'S LIST, NOT DUMMY DATA ---
                            items(uiState.serviceItems, key = { it.id }) { item ->
                                ServiceMenuItem(
                                    // Use a helper to get the icon
                                    item = ServiceButtonInfo(
                                        title = item.name,
                                        icon = getIconForServiceName(item.name)
                                    ),
                                    color = Color.Black,
                                    onClick = { onServiceClick(item.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A helper function to map the service name string to the correct icon.
 */
@Composable
private fun getIconForServiceName(serviceName: String): ImageVector {
    return when (serviceName) {
        "Sales" -> Icons.Outlined.ShoppingCart
        "Finance" -> Icons.Outlined.MonetizationOn
        "Service" -> Icons.Outlined.BuildCircle
        "Cash For Cars" -> Icons.Outlined.DirectionsCar
        "Build And Price" -> Icons.Outlined.Build
        else -> Icons.Outlined.ListAlt // Default icon
    }
}

// (ServiceMenuItem composable is unchanged and correct)
@Composable
fun ServiceMenuItem(
    item: ServiceButtonInfo,
    color: Color,
    onClick: () -> Unit
) {
    // ... (Your existing UI for this is perfect)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(
                BorderStroke(1.dp, color),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
    }
}

// Data class (unchanged)
data class ServiceButtonInfo(
    val title: String,
    val icon: ImageVector
)