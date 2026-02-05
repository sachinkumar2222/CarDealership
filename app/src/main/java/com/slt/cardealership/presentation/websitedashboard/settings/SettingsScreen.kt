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

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

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
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF2196F3)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = {
                            Text(
                                title,
                                color = if (selectedTabIndex == index) Color(0xFF2196F3) else Color.Gray,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
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
