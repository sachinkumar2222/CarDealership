package com.slt.cardealership.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.presentation.articles.AddEditArticleScreen
import com.slt.cardealership.presentation.articles.ArticleScreen
import com.slt.cardealership.presentation.articles.ArticleUiState
import com.slt.cardealership.presentation.articles.ArticleViewModel
import com.slt.cardealership.presentation.info.InfoScreen
import com.slt.cardealership.presentation.navigation.Routes
import com.slt.cardealership.ui.theme.CarDealershipTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// --- Nested Navigation Routes for the Bottom Bar ---
@Serializable
sealed class HomeRoutes {
    @Serializable
    object Dashboard : HomeRoutes()
    @Serializable
    object Info : HomeRoutes()
    @Serializable
    object Profile : HomeRoutes()
    @Serializable
    object Settings : HomeRoutes()
    @Serializable
    object Articles : HomeRoutes()
    @Serializable
    data class AddEditArticle(val articleId: String? = null) : HomeRoutes()
}

// --- Data Models for UI ---
// CORRECTED: Added the 'route' property to fix the "Unresolved reference" error.
data class DashboardItem(val title: String, val icon: ImageVector, val route: HomeRoutes)
data class NavDrawerItem(val title: String, val icon: ImageVector)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: HomeRoutes)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val homeNavController = rememberNavController() // For nested navigation
    val homeViewModel: HomeViewModel = hiltViewModel()
    val articleViewModel: ArticleViewModel = hiltViewModel()

    val navDrawerItems = listOf(
        NavDrawerItem("Home", Icons.Default.Home),
        NavDrawerItem("Info", Icons.Default.Info)
        // ... add other items here
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                items = navDrawerItems,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onLogoutClick = {
                        homeViewModel.onLogoutClicked()
                        mainNavController.navigate(Routes.LoginScreen) {
                            popUpTo(Routes.HomeScreen) { inclusive = true }
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController = homeNavController) }
        ) { paddingValues ->
            NavHost(
                navController = homeNavController,
                startDestination = HomeRoutes.Dashboard,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<HomeRoutes.Dashboard> {
                    DashboardContent(
                        navController = homeNavController,
                        articleViewModel = articleViewModel
                    )
                }
                composable<HomeRoutes.Info> { InfoScreen() }
                composable<HomeRoutes.Articles> { ArticleScreen(navController = homeNavController) }
                composable<HomeRoutes.AddEditArticle> { backStackEntry ->
                    // Extract the arguments from the navigation back stack
                    val args = backStackEntry.toRoute<HomeRoutes.AddEditArticle>()
                    // Pass the arguments to the screen
                    AddEditArticleScreen(
                        articleId = args.articleId,
                        onNavigateBack = { homeNavController.popBackStack() }
                    )
                }
                composable<HomeRoutes.Profile> { CenteredText("Profile Screen") }
                composable<HomeRoutes.Settings> { CenteredText("Settings Screen") }

            }
        }
    }
}

@Composable
fun DashboardContent(navController: NavController, articleViewModel: ArticleViewModel) {
    val articleState by articleViewModel.uiState.collectAsState()
    // Each dashboard item now has a route associated with it for navigation
    val dashboardItems = listOf(
        DashboardItem("Info", Icons.Default.Business, HomeRoutes.Info),
        DashboardItem("Articles", Icons.Default.Article, HomeRoutes.Articles),
        DashboardItem("My Websites", Icons.Default.Language, HomeRoutes.Dashboard),
        DashboardItem("Services", Icons.Default.MiscellaneousServices, HomeRoutes.Dashboard),
        DashboardItem("Manage Classifieds", Icons.Default.ListAlt, HomeRoutes.Dashboard),
        DashboardItem("Analytics", Icons.Default.ShowChart, HomeRoutes.Dashboard)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { WelcomeCard() }
        items(dashboardItems) { item ->
            InfoCardGridItem(item.title, item.icon) { navController.navigate(item.route) }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { GoogleBusinessProfileCard() }
        item(span = { GridItemSpan(maxLineSpan) }) {
            when (val state = articleState) {
                is ArticleUiState.Success -> {
                    // If the article list is not empty, get the first one and show the card
                    state.articles.firstOrNull()?.let { article ->
                        LatestArticleCard(article = article)
                    }
                }
                // You can add Loading or Error states here if you wish
                else -> { /* Do nothing for loading/error on the dashboard for now */ }
            }
        }
    }
}

@Composable
fun LatestArticleCard(article: Article) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            "Latest Article",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp) // Align with grid padding
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = article.title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = article.createdOn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onMenuClick: () -> Unit, onLogoutClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        title = { Icon(Icons.Default.Business, "Logo", tint = MaterialTheme.colorScheme.primary) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu") }
        },
        actions = {
            Box {
                Button(
                    onClick = { menuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "S",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            "Toggle Menu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Account Settings") },
                        onClick = { menuExpanded = false })
                    DropdownMenuItem(
                        text = { Text("Log Out") },
                        onClick = {
                            menuExpanded = false
                            onLogoutClick()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCardGridItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                title,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, HomeRoutes.Dashboard),
        BottomNavItem("Info", Icons.Default.Info, HomeRoutes.Info),
        BottomNavItem("Profile", Icons.Default.Person, HomeRoutes.Profile),
        BottomNavItem("Settings", Icons.Default.Settings, HomeRoutes.Settings)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route::class.qualifiedName,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(items: List<NavDrawerItem>, onCloseDrawer: () -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Default.Close, contentDescription = "Close Drawer")
                }
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Divider()
            LazyColumn {
                items(items) { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = item.title == "Home",
                        onClick = { /* Handle item click */ },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF29B6F6))
        ) {
            Image(
                painter = painterResource(R.drawable.earth),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Testing Dealership For Developers",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GoogleBusinessProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(38.dp)
                )
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF4285F4)
                )
            }

            Text(
                text = "Do you want to import Your Google Business Profile Information?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Button(onClick = { /* Handle Import */ }) {
                Text("Import Now")
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.gmb),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CarDealershipTheme {
        HomeScreen(rememberNavController())
    }
}

