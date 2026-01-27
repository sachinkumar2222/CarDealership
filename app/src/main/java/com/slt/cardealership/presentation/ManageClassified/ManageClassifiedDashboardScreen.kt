package com.slt.cardealership.presentation.ManageClassified

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.slt.cardealership.presentation.home.HomeRoutes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClassifiedDashboardScreen(
    navController: NavController,
    siteId: String,
    siteTitle: String,
    viewModel: ManageClassifiedDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(siteId) {
        siteId.toIntOrNull()?.let {
            viewModel.fetchDomainDetails(it)
        }
    }


    val subtitle = (uiState as? DashboardUiState.Success)?.domainItem?.domainName ?: siteTitle

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Website Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray
                        )
                    }


                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(8.dp)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = {
                            siteId.toIntOrNull()?.let { viewModel.fetchDomainDetails(it) }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    // We have the domain details in state.domainItem.
                    // For now, the UI buttons are static, so we just render them.
                    // If the user wants to use data from domainItem later, it's available.

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardButton(
                            title = "Articles",
                            icon = Icons.Outlined.Article,
                            color = Color(0xFFFFD8E4), // Pinkish
                            onClick = { navController.navigate(HomeRoutes.ClassifiedArticles(siteId = siteId)) }
                        )
                        DashboardButton(
                            title = "Banners",
                            icon = Icons.Outlined.ViewCarousel, // Using carousel icon for banners
                            color = Color(0xFFD0F0FD), // Light Blue
                            onClick = { navController.navigate(HomeRoutes.ClassifiedBanners(siteId = siteId)) }
                        )
                        DashboardButton(
                            title = "FAQs",
                            icon = Icons.Outlined.Quiz, // Quiz/FAQ icon
                            color = Color(0xFFE6E6FA), // Lavender/Purple
                            onClick = { navController.navigate(HomeRoutes.ClassifiedFaqs(siteId = siteId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}
