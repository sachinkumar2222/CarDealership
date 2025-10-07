package com.slt.cardealership.presentation.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.presentation.articles.AddEditArticleScreen
import com.slt.cardealership.presentation.articles.ArticleScreen
import com.slt.cardealership.presentation.info.InfoScreen
import com.slt.cardealership.presentation.navigation.Routes
import com.slt.cardealership.presentation.photos.PhotoScreen
import com.slt.cardealership.presentation.settings.SettingsScreen
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
    object Photos : HomeRoutes()

    @Serializable
    data class AddEditArticle(val articleId: String? = null) : HomeRoutes()
}

data class DashboardItem(val title: String, val icon: Painter, val route: HomeRoutes)
data class NavDrawerItem(val title: String, val icon: ImageVector)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: HomeRoutes)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val homeNavController = rememberNavController() // For nested navigation
    val homeViewModel: HomeViewModel = hiltViewModel()
    //val articleViewModel: ArticleViewModel = hiltViewModel()

    val navDrawerItems = listOf(
        NavDrawerItem("Home", Icons.Default.Home),
        NavDrawerItem("Info", Icons.Default.Info),
        NavDrawerItem("Articles", Icons.Default.Article)
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
            bottomBar = { AnimatedBottomBar(navController = homeNavController) },
            modifier = Modifier.statusBarsPadding()
        ) { paddingValues ->
            NavHost(
                navController = homeNavController,
                startDestination = HomeRoutes.Dashboard,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<HomeRoutes.Dashboard> {
                    DashboardContent(
                        navController = homeNavController,
                        //articleViewModel = articleViewModel
                    )
                }
                composable<HomeRoutes.Info> { InfoScreen(navController = homeNavController) }
                composable<HomeRoutes.Articles> {
                    ArticleScreen(navController = homeNavController)
                }
                composable<HomeRoutes.AddEditArticle> {
                    AddEditArticleScreen(onNavigateBack = { homeNavController.popBackStack() })
                }
                composable<HomeRoutes.Profile> {
                    CenteredText("Profile Screen")
                }
                composable<HomeRoutes.Settings> { SettingsScreen() }
                composable<HomeRoutes.Photos> { PhotoScreen(navController = homeNavController) }
            }
        }
    }
}


@Composable
fun DashboardContent(
    navController: NavController, homeViewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val dashboardItems = listOf(
        DashboardItem("Info", painterResource(R.drawable.info), HomeRoutes.Info),
        DashboardItem("Articles", painterResource(R.drawable.newspaper), HomeRoutes.Articles),
        DashboardItem("Photos", painterResource(R.drawable.picture), HomeRoutes.Photos),
        DashboardItem("Services", painterResource(R.drawable.user_headset), HomeRoutes.Dashboard),
        DashboardItem(
            "Manage Classifieds",
            painterResource(R.drawable.user_gear),
            HomeRoutes.Dashboard
        ),
        DashboardItem("Analytics", painterResource(R.drawable.chart_histogram), HomeRoutes.Dashboard)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F8FF))
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { WelcomeCard() }
        items(dashboardItems) { item ->
            InfoCardGridItem(item.title, item.icon) { navController.navigate(item.route) }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // --- THIS IS THE CHANGE ---
            // Show the latest post based on the state from the ViewModel
            when (val state = homeState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                is HomeUiState.Error -> {
                    Text(state.message, modifier = Modifier.padding(16.dp))
                }

                is HomeUiState.Success -> {
                    // If a post exists, show the card for it
                    state.latestPost?.let { post ->
                        LatestPostCard(post = post)
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { GoogleBusinessProfileCard() }
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
fun InfoCardGridItem(title: String, icon: Painter, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. A Card with a RoundedCornerShape
        Card(
            shape = RoundedCornerShape(20.dp), // Adjust the corner radius as you like
            modifier = Modifier.size(72.dp),
            colors = CardDefaults.cardColors(
                // 2. Use a solid white background
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = icon,
                    contentDescription = title,
                    // 3. Adjust the icon size for better padding
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            // 4. Use a softer color for the text
            color = Color.Black
        )
    }
}

@Composable
fun LatestPostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        shape = RoundedCornerShape(24.dp), // More pronounced rounded corners
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp) // Internal padding for all content
        ) {
            // 1. Inset Image with its own rounded corners
            AsyncImage(
                model = post.image,
                contentDescription = post.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp)), // Corners for the image itself
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Styled Title and Subtitle
            Text(
                text = post.name ?: "No Title",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.content ?: "No content",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Bottom row mimicking the price and add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // We use the creation date instead of a price
                Text(
                    text = "Published on Oct 5, 2025", // Replace with a real date formatter for post.createdOn
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

            }
        }
    }
}

@SuppressLint("RestrictedApi")
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
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, item.title) },
                label = { Text(item.title) },
                // --- THIS IS THE FIX ---
                // We compare the string name of the item's route with the string names
                // in the current navigation hierarchy.
                selected = currentDestination?.hierarchy?.any {
                    it.route == item.route::class.qualifiedName
                } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
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
            .height(180.dp)
            .padding(8.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
        ) {
            Image(
                painter = painterResource(R.drawable.earth),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 16.dp, horizontal = 16.dp)
                    .size(150.dp),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
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

@Composable
fun AnimatedBottomBar(navController: NavController) {
    // Re-use the same items from your old BottomNavigationBar
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, HomeRoutes.Dashboard),
        BottomNavItem("Info", Icons.Default.Info, HomeRoutes.Info),
        BottomNavItem("Profile", Icons.Default.Person, HomeRoutes.Profile),
        BottomNavItem("Settings", Icons.Default.Settings, HomeRoutes.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // This is the main container, styled like the HTML version
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Padding around the bar
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(40.dp))
            .background(color = Color.White, shape = RoundedCornerShape(40.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.route::class.qualifiedName
            } == true

            // Each icon is an item in the bar
            AnimatedBottomBarItem(
                item = item,
                isSelected = isSelected,
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

@Composable
fun AnimatedBottomBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Define the gradient for the active state
    val activeGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D6EFD), Color(0xFF0A58CA))
    )

    // Animate the background based on selection
    val background by animateDpAsState(
        targetValue = if (isSelected) 48.dp else 0.dp,
        animationSpec = tween(300), label = ""
    )

    // Animate the icon color based on selection
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Black.copy(alpha = 0.7f),
        animationSpec = tween(300), label = ""
    )

    Box(
        modifier = Modifier
            .size(48.dp) // Wrapper to hold the animated background and icon
            .clickable(
                onClick = onClick,
                indication = null, // Disable ripple effect
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // This is the animated background circle
        Box(
            modifier = Modifier
                .size(background)
                .clip(CircleShape)
                .background(
                    if (isSelected) activeGradient else Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
        )

        // The icon itself
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLayoutPreview() {
    Scaffold(
        topBar = { TopBar(onMenuClick = {}, onLogoutClick = {}) },
        bottomBar = { BottomNavigationBar(navController = rememberNavController()) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DashboardContent(navController = rememberNavController())
        }
    }
}



