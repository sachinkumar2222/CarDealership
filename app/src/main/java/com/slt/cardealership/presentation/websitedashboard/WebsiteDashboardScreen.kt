package com.slt.cardealership.presentation.websitedashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.presentation.home.HomeRoutes

// Brand Colors
val BrandDarkBlue = Color(0xFF233E66)
val ScreenBackground = Color(0xFFF5F7FA)

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteDashboardScreen(
    navController: NavController,
    domainId: Int,
    viewModel: WebsiteDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.fetchDomainDetails(domainId)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Website Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is WebsiteDashboardUiState.Success) {
                            Text(
                                text = (uiState as WebsiteDashboardUiState.Success).domain.domainName,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.DarkGray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier.shadow(8.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is WebsiteDashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WebsiteDashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchDomainDetails(domainId) }) {
                            Text("Retry")
                        }
                    }
                }
                is WebsiteDashboardUiState.Success -> {
                    DashboardContent(domain = state.domain, navController = navController)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(domain: com.slt.cardealership.domain.model.DomainItem, navController: NavController) {
    val dashboardItems = listOf(
        DashboardItem("Pages", Icons.Outlined.Article, Color(0xFFF8D7DA), "pages"),
        DashboardItem("Blog", Icons.Outlined.Book, Color(0xFFD1ECF1), "blog"),
        DashboardItem("Sliders", Icons.Outlined.ViewCarousel, Color(0xFFD4EDDA), "sliders"),
        DashboardItem("Menus", Icons.Outlined.Menu, Color(0xFFFFF3CD), "menus"),
        DashboardItem("Specials", Icons.Outlined.Star, Color(0xFFE2D9F3), "specials"),
        DashboardItem("Research Compare", Icons.Outlined.CompareArrows, Color(0xFFD1F2EB), "research_compare"),
        DashboardItem("Settings", Icons.Outlined.Settings, Color(0xFFE9ECEF), "settings")
    )

    Column(modifier = Modifier.padding(16.dp)) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dashboardItems) { item ->
                DashboardCard(item) { route ->
                    when (route) {
                        "pages" -> navController.navigate(HomeRoutes.WebsitePages(domain.id))
                        "blog" -> navController.navigate(HomeRoutes.WebsiteBlogs(domain.id))
                        "sliders" -> navController.navigate(HomeRoutes.WebsiteSliders(domain.id))
                        "menus" -> navController.navigate(HomeRoutes.WebsiteMenus(domain.id))
                        "research_compare" -> navController.navigate(HomeRoutes.WebsiteResearchCompare(domain.id))
                        "settings" -> navController.navigate(HomeRoutes.WebsiteSettings(domain.id))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(item: DashboardItem, onCardClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp) // Slightly taller for better spacing
            .clickable { onCardClick(item.route) },
        shape = RoundedCornerShape(16.dp), // Softer corners
        colors = CardDefaults.cardColors(containerColor = item.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Added elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)), // Semi-transparent white background
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black.copy(alpha = 0.8f) // Slightly darker tint
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, // Bold text
                color = Color.Black.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}
