package com.slt.cardealership.presentation.internetleads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.domain.model.InternetLead
import com.slt.cardealership.domain.model.LeadType // Make sure this Enum exists
import com.slt.cardealership.presentation.leads.InternetLeadsViewModel
import com.slt.cardealership.ui.theme.BrandDarkBlue

// 1. Data model (unchanged)
data class LeadCategory(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

// 2. Dummy data (Corrected from your previous input)
val leadCategories = listOf(
    LeadCategory("General Leads", Icons.Outlined.Folder, Color(0xFFF7D9E3)), // Pink
    LeadCategory("Finance Leads", Icons.Outlined.MonetizationOn, Color(0xFFCBF0F4)), // Light Blue
    LeadCategory("Service Leads", Icons.Outlined.Settings, Color(0xFFCBD4F4)), // Indigo Light
    LeadCategory("Contact Leads", Icons.Outlined.PersonOutline, Color(0xFFFFE896)), // Yellow
    LeadCategory("Inventory Leads", Icons.Outlined.Inventory2, Color(0xFFDED0B6)), // Brown Light
    LeadCategory("Cash for Car Leads", Icons.Outlined.DirectionsCar, Color(0xFFBFF6C3)), // Green Light
    LeadCategory("Build & Price Leads", Icons.Outlined.Calculate, Color(0xFFEAECCC)) // Lime Light
)

// 3. The Main Screen Composable (UPDATED)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsListScreen(
    onCategoryClick: (title: String) -> Unit = {},
    viewModel: InternetLeadsViewModel  = hiltViewModel()
) {
    Scaffold(
        topBar = {
            LeadsTopBar()
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(leadCategories) { category ->
                LeadCategoryItem(
                    category = category,
                    onClick = {
                        // --- UPDATED: Pass the category title ---
                        onCategoryClick(category.title)
                    }
                )
            }
        }
    }
}

// 4. The Top App Bar (unchanged)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsTopBar() {
    TopAppBar(
        title = {
            Text(
                "Internet Leads",
                fontWeight = FontWeight.Bold,
                color = BrandDarkBlue
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

// 5. The Category Card Composable (unchanged)
@Composable
fun LeadCategoryItem(
    category: LeadCategory,
    onClick: () -> Unit
) {
    val contentColor = BrandDarkBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(category.backgroundColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = category.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
    }
}
