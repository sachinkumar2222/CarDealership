package com.slt.cardealership.presentation.websitedashboard.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.ApplicationSettingsScreen
import com.slt.cardealership.presentation.websitedashboard.settings.inventory.InventorySettingsScreen
import com.slt.cardealership.presentation.websitedashboard.settings.buildandprice.BuildAndPriceSettingsScreen
import com.slt.cardealership.presentation.websitedashboard.settings.research.ResearchSettingsScreen
import com.slt.cardealership.presentation.websitedashboard.settings.fonts.ManageFontsScreen
import com.slt.cardealership.ui.theme.BrandBlue
import com.slt.cardealership.ui.theme.LightBackground

@Composable
fun SettingsTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) BrandBlue else Color.White
    val contentColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.5f)
    val elevation = if (isSelected) 6.dp else 2.dp

    Surface(
        onClick = onClick,
        modifier = Modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        shadowElevation = elevation,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SettingsScreen(
    navController: NavController,
    domainId: Int,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val tabs = listOf(
        "Theme Settings",
        "Vehicle Inventory",
        "Build and Price",
        "Research",
        "Manage Fonts",
        "Application Settings"
    )

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBackground)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(tabs) { index, title ->
                    val isSelected = selectedTabIndex == index
                    SettingsTabButton(
                        text = title,
                        isSelected = isSelected,
                        onClick = { viewModel.onTabSelected(index) }
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when(selectedTabIndex) {
                    0 -> ThemeSettingsScreen(domainId = domainId)
                    1 -> InventorySettingsScreen(domainId = domainId)
                    2 -> BuildAndPriceSettingsScreen(domainId = domainId)
                    3 -> ResearchSettingsScreen(domainId = domainId)
                    4 -> ManageFontsScreen(domainId = domainId)
                    5 -> ApplicationSettingsScreen(domainId = domainId, navController = navController)
                }
            }
        }
    }
}
