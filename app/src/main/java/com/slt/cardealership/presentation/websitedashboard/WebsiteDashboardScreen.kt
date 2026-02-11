package com.slt.cardealership.presentation.websitedashboard

import com.slt.cardealership.presentation.navigation.HomeRoutes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
// Removed wrong import
import com.slt.cardealership.ui.theme.BrandDarkBlue

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
                    // Title only to match reference cleaner look
                    Text(
                        "Website Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = com.slt.cardealership.ui.theme.BrandDarkBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = com.slt.cardealership.ui.theme.BrandDarkBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
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
        DashboardItem("Pages", Icons.Outlined.Article, Color(0xFFF7D9E3), "pages"),
        DashboardItem("Blog", Icons.Outlined.Book, Color(0xFFCBF0F4), "blog"),
        DashboardItem("Sliders", Icons.Outlined.ViewCarousel, Color(0xFFBFF6C3), "sliders"),
        DashboardItem("Menus", Icons.Outlined.Menu, Color(0xFFFFE896), "menus"),
        DashboardItem("Research Compare", Icons.Outlined.CompareArrows, Color(0xFFEAECCC), "research_compare"),
        DashboardItem("Settings", Icons.Outlined.Settings, Color(0xFFCBD4F4), "settings")
    )

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp), // Match reference: 24dp horizontal
        verticalArrangement = Arrangement.spacedBy(12.dp) // Match reference: 12dp spacing
    ) {
        items(dashboardItems.size) { index ->
            val item = dashboardItems[index]
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

@Composable
fun DashboardCard(item: DashboardItem, onCardClick: (String) -> Unit) {
    val contentColor = BrandDarkBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(item.color, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCardClick(item.route) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
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
