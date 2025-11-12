package com.slt.cardealership.presentation.home


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
// --- REMOVED: FlowRow ---
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// --- REMOVED: filled.Add ---
import androidx.compose.material.icons.filled.Business
// --- REMOVED: filled.DirectionsCar ---
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.GridView // <-- NEW IMPORT
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
// --- REMOVED: outlined.PhotoCamera ---
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Search // <-- NEW IMPORT
import androidx.compose.material.icons.outlined.Warehouse // <-- NEW IMPORT
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.presentation.ads.AddAdsScreen
import com.slt.cardealership.presentation.ads.AdsScreen
import com.slt.cardealership.presentation.articles.AddEditArticleScreen
import com.slt.cardealership.presentation.articles.ArticleScreen
import com.slt.cardealership.presentation.auth.AuthViewModel
import com.slt.cardealership.presentation.faq.AddFaqScreen
import com.slt.cardealership.presentation.faq.FaqScreen
import com.slt.cardealership.presentation.info.FullScreenError
import com.slt.cardealership.presentation.info.InfoScreen
import com.slt.cardealership.presentation.info.InfoUiState
import com.slt.cardealership.presentation.info.InfoViewModel
import com.slt.cardealership.presentation.inventory.AddVehicleScreen
import com.slt.cardealership.presentation.inventory.InventoryScreen
import com.slt.cardealership.presentation.photos.PhotoScreen
import com.slt.cardealership.presentation.profile.EditProfileScreen
import com.slt.cardealership.presentation.profile.ProfileScreen
import com.slt.cardealership.presentation.profile.ProfileViewModel
import com.slt.cardealership.presentation.seo.AddSeoScreen
import com.slt.cardealership.presentation.seo.SeoScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.compose.ui.geometry.Offset
import androidx.navigation.toRoute
import com.slt.cardealership.presentation.users.AddUserScreen
import com.slt.cardealership.presentation.users.ChangePasswordScreen
import com.slt.cardealership.presentation.users.EditUserScreen
import com.slt.cardealership.presentation.users.UserScreen


@Serializable
sealed class HomeRoutes {
    @Serializable
    object Dashboard : HomeRoutes()

    @Serializable
    object Info : HomeRoutes()

    @Serializable
    object Profile : HomeRoutes()

    @Serializable
    data class EditProfileScreen(val userId: Long) : HomeRoutes()

    @Serializable
    object Inventory : HomeRoutes()

    @Serializable
    object Ads : HomeRoutes()

    @Serializable
    object Articles : HomeRoutes()

    @Serializable
    object Photos : HomeRoutes()

    @Serializable
    object AddVehicleScreen : HomeRoutes()

    @Serializable
    object AddAdsScreen : HomeRoutes()

    @Serializable
    object AddUserScreen : HomeRoutes()

    @Serializable
    object SeoScreen : HomeRoutes()

    @Serializable
    object AddSeoScreen : HomeRoutes()

    @Serializable
    object FaqScreen : HomeRoutes()

    @Serializable
    object AddFaqScreen : HomeRoutes()

    @Serializable
    object AddPhoto : HomeRoutes() // <-- This was missing

    @Serializable
    object UserScreen : HomeRoutes()


    @Serializable
    data class ChangePasswordScreen(val userId: Long) : HomeRoutes()

    @Serializable
    data class EditUserScreen(val userId: Long) : HomeRoutes()


    @Serializable
    data class AddEditArticle(val articleId: String? = null) : HomeRoutes()
}

// --- NEW Data class for the stats grid ---
data class DashboardStats(
    val inventoryCount: Int,
    val activeAdsCount: Int,
    val articlesCount: Int,
    val topKeywordsCount: Int
)

data class DashboardItem(val title: String, val icon: ImageVector, val route: HomeRoutes)
data class NavDrawerItem(val title: String, val icon: ImageVector)
data class DrawerItem(val title: String, val icon: ImageVector, val route: HomeRoutes)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: HomeRoutes)
// --- REMOVED QuickActionItem ---

// --- REVERTED TO LIGHT THEME ---
val businessDarkBlue = Color(0xFF233E66)
val businessLightBlue = Color(0xFF253A63)
val businessTextLight = Color(0xFFCCD6F6)
val businessTextDark = Color(0xFF233E66)
val surfaceColor = Color.White
val backgroundColor = Color(0xFFF5F7FA)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
    val homeNavController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val infoviewModel: InfoViewModel = hiltViewModel()
    val uiState by infoviewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                navController = homeNavController,
                drawerState = drawerState,
                scope = scope,
                currentRoute = currentRoute
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                if (currentRoute == HomeRoutes.Dashboard::class.qualifiedName) {
                    TopBar(
                        navController = homeNavController,
                        onMenuClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        },
                        onLogoutClick = { authViewModel.signOut() })
                }
            },
            // --- BOTTOM BAR IS BACK ---
            bottomBar = { ProfessionalBottomBar(navController = homeNavController) },
            modifier = Modifier.statusBarsPadding(),
            containerColor = backgroundColor
        ) { paddingValues ->
            NavHost(
                navController = homeNavController,
                startDestination = HomeRoutes.Dashboard,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<HomeRoutes.Dashboard> {
                    when (val state = uiState) {
                        is InfoUiState.Loading -> {
                            DashboardLoadingShimmer()
                        }

                        is InfoUiState.Error -> {
                            FullScreenError(
                                errorMessage = state.message,
                                onTryAgain = {
                                    infoviewModel.fetchDealerInfo()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        is InfoUiState.Success -> {
                            // --- MOCKED STATS ---
                            // You will fetch this from your ViewModel
                            val stats = DashboardStats(
                                inventoryCount = 74,
                                activeAdsCount = 3,
                                articlesCount = 12,
                                topKeywordsCount = 5
                            )
                            DashboardContent(
                                dealerInfo = state.dealerInfo,
                                stats = stats, // Pass stats
                                navController = homeNavController,
                            )
                        }
                    }
                }
                composable<HomeRoutes.Info> { InfoScreen(navController = homeNavController) }
                composable<HomeRoutes.Articles> {
                    ArticleScreen(navController = homeNavController)
                }
                composable<HomeRoutes.AddEditArticle> {
                    AddEditArticleScreen(
                        navController = homeNavController,
                        onNavigateBack = { homeNavController.popBackStack() })
                }
                composable<HomeRoutes.Profile> {
                    val profileViewModel: ProfileViewModel = hiltViewModel()

                    ProfileScreen(
                        navController = mainNavController,
                        viewModel = profileViewModel,
                        onSignOutClick = { authViewModel.signOut() },
                        onEditProfileClick = { userId ->
                            homeNavController.navigate(HomeRoutes.EditProfileScreen(userId))
                        },
                        onBackClick = { homeNavController.popBackStack() }
                    )
                }

                composable<HomeRoutes.EditProfileScreen> { backStackEntry ->
                    val profileViewModel: ProfileViewModel = hiltViewModel()
                    EditProfileScreen(
                        onBackClick = { homeNavController.popBackStack() },
                        viewModel = profileViewModel,
                        onSaveSuccess = { homeNavController.popBackStack() }
                    )
                }

                composable<HomeRoutes.Photos> { PhotoScreen(navController = homeNavController) }
                composable<HomeRoutes.AddPhoto> { // Added route
                    PhotoScreen(navController = homeNavController)
                }
                composable<HomeRoutes.Inventory> { InventoryScreen(navController = homeNavController) }
                composable<HomeRoutes.AddVehicleScreen> {
                    AddVehicleScreen(navController = homeNavController)
                }
                composable<HomeRoutes.Ads> { AdsScreen(navController = homeNavController) }
                composable<HomeRoutes.AddAdsScreen> {
                    AddAdsScreen(navController = homeNavController)
                }
                composable<HomeRoutes.SeoScreen> {
                    SeoScreen(navController = homeNavController)
                }
                composable<HomeRoutes.AddSeoScreen> {
                    AddSeoScreen(navController = homeNavController)
                }
                composable<HomeRoutes.FaqScreen> {
                    FaqScreen(navController = homeNavController)
                }
                composable<HomeRoutes.AddFaqScreen> {
                    AddFaqScreen(navController = homeNavController)
                }
                composable<HomeRoutes.UserScreen> {
                    UserScreen(
                        onBackClick = { homeNavController.popBackStack() },
                        onAddUserClick = {
                            // Navigate to the screen for adding a new user
                            homeNavController.navigate(HomeRoutes.AddUserScreen)
                        },
                        onManageUserClick = { user ->
                            // "Manage" is for the current user, so go to their main Profile screen
                            homeNavController.navigate(HomeRoutes.Profile)
                        },
                        onEditUserClick = { user ->
                            // "Edit" is for other users, so go to the EditUser screen with their ID
                            homeNavController.navigate(HomeRoutes.EditUserScreen(user.id))
                        }
                    )
                }

                composable<HomeRoutes.AddUserScreen> {
                    AddUserScreen(
                        onBackClick = { homeNavController.popBackStack() },
                       onAddUserSuccess = { homeNavController.popBackStack() }
                    )
                }

                composable<HomeRoutes.EditUserScreen> { backStackEntry ->
                    // 1. Get the userId that was passed in the route
                    val userToEdit = backStackEntry.toRoute<HomeRoutes.EditUserScreen>()

                    // 2. Call your EditUserScreen composable
                    EditUserScreen(
                        userId = userToEdit.userId, // <-- Pass the ID
                        onBackClick = { homeNavController.popBackStack() },
                        onChangePasswordClick = { userId ->
                             homeNavController.navigate(HomeRoutes.ChangePasswordScreen(userId))
                        }
                        // The ViewModel is automatically provided by Hilt
                    )
                }
                composable<HomeRoutes.ChangePasswordScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.ChangePasswordScreen>()

                    ChangePasswordScreen(
                        userId = args.userId,
                        onCloseClick = { homeNavController.popBackStack() },
                        onPasswordChangedSuccess = {
                            // On success, close the screen
                            homeNavController.popBackStack()
                            // You could also show a Snackbar here
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentRoute: String?
) {
    // Main navigation is in the bottom bar,
    // so this holds secondary items
    val items = listOf(
        DrawerItem("SEO", Icons.Outlined.BarChart, HomeRoutes.SeoScreen),
        DrawerItem("FAQ", Icons.Outlined.Quiz, HomeRoutes.FaqScreen)
    )

    ModalDrawerSheet(
        drawerContainerColor = surfaceColor,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(businessDarkBlue)
                .padding(vertical = 32.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Dealer Panel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(items) { item ->
                val selected = currentRoute == item.route::class.qualifiedName
                NavigationDrawerItem(
                    label = {
                        Text(
                            item.title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    selected = selected,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (currentRoute != item.route::class.qualifiedName) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = businessDarkBlue.copy(alpha = 0.1f),
                        selectedIconColor = businessDarkBlue,
                        selectedTextColor = businessTextDark,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = businessTextDark
                    )
                )
            }
        }
    }
}


// --- NEW: A helper composable for section titles ---
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = businessTextDark.copy(alpha = 0.8f),
        modifier = modifier
            .padding(start = 4.dp, bottom = 8.dp)
    )
}

// --- NEW DASHBOARD CONTENT DESIGN ---
@Composable
fun DashboardContent(
    dealerInfo: DealerInfo,
    stats: DashboardStats, // <-- Accept the stats
    navController: NavController
) {

    // --- List of "Manage" cards from your image ---
    val manageItems = listOf(
        DashboardItem("Inventory", Icons.Outlined.GridView, HomeRoutes.Inventory),
        DashboardItem("Articles", Icons.Outlined.Article, HomeRoutes.Articles),
        DashboardItem("Ads Manager", Icons.Outlined.Campaign, HomeRoutes.Ads),
        DashboardItem("SEO", Icons.Outlined.Search, HomeRoutes.SeoScreen),
        DashboardItem(
            "User", Icons.Outlined.Person, HomeRoutes.UserScreen
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- 1. Header ---
        item {
            DashboardHeader(dealerInfo = dealerInfo)
        }

        // --- 2. Stats Grid ---
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(259.dp), // Fixed height for 2 rows
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false // Correct way to disable nested scroll
            ) {
                item {
                    StatCard(
                        title = "Total Inventory",
                        count = stats.inventoryCount.toString(),
                        icon = Icons.Outlined.Warehouse,
                        onClick = { navController.navigate(HomeRoutes.Inventory) }
                    )
123                }
                item {
                    StatCard(
                        title = "Active Ads",
                        count = stats.activeAdsCount.toString(),
                        icon = Icons.Outlined.Campaign,
                        onClick = { navController.navigate(HomeRoutes.Ads) }
                    )
                }
                item {
                    StatCard(
                        title = "Published Articles",
                        count = stats.articlesCount.toString(),
                        icon = Icons.Outlined.Article,
                        onClick = { navController.navigate(HomeRoutes.Articles) }
                    )
                }
                item {
                    StatCard(
                        title = "Top Keywords",
                        count = stats.topKeywordsCount.toString(),
                        icon = Icons.Outlined.Search,
                        onClick = { navController.navigate(HomeRoutes.SeoScreen) }
                    )
                }
            }
        }

        // --- 3. Manage Section ---
        item {
            SectionTitle(text = "Manage", modifier = Modifier)
        }

        items(manageItems) { item ->
            ManageActionCard(
                title = item.title,
                subtitle = when (item.route) {
                    is HomeRoutes.Inventory -> "Manage all vehicles"
                    is HomeRoutes.Articles -> "Create & edit posts"
                    is HomeRoutes.Ads -> "Track campaign performance"
                    is HomeRoutes.SeoScreen -> "Improve search visibility"
                    is HomeRoutes.UserScreen -> "Manage users"
                    else -> ""
                },
                icon = item.icon,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

// --- NEW HEADER ---
@Composable
fun DashboardHeader(dealerInfo: DealerInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome Back,",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Text(
            text = dealerInfo.name ?: "Dealer Admin",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = businessTextDark
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, onMenuClick: () -> Unit, onLogoutClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Business,
                    "Logo",
                    tint = businessTextDark
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Dealer Panel",
                    fontWeight = FontWeight.Bold,
                    color = businessTextDark
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    "Menu",
                    tint = businessTextDark
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(businessDarkBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "S", // User Initial
                            color = businessDarkBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(surfaceColor)
                ) {
                    DropdownMenuItem(
                        text = { Text("Account Settings", color = businessTextDark) },
                        onClick = {
                            navController.navigate(HomeRoutes.Profile)
                            menuExpanded = false
                        })
                    DropdownMenuItem(
                        text = { Text("Log Out", color = businessTextDark) },
                        onClick = {
                            menuExpanded = false
                            onLogoutClick()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
        // --- Make TopBar transparent ---
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = surfaceColor
        )
    )
}

// --- NEW: Card for the 2x2 Stats Grid ---
@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1.5f) // Rectangular shape
            .clickable(onClick = onClick), // Make it clickable
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = businessDarkBlue
            )
            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = businessTextDark
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- NEW: Card for the "Manage" List ---
@Composable
fun ManageActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, hoveredElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(businessDarkBlue.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = businessDarkBlue
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = businessTextDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}


// --- SHIMMER PLACEHOLDERS ---

@Composable
fun StatCardShimmer() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1.5f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
            Column {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun ManageActionCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun DashboardHeaderShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
    }
}

@Composable
fun DashboardLoadingShimmer() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        item {
            DashboardHeaderShimmer()
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(4) { StatCardShimmer() }
            }
        }
        item {
            // Shimmer for "Manage" title
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 24.dp, bottom = 8.dp)
                    .width(100.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
        items(4) {
            ManageActionCardShimmer()
        }
    }
}

@Composable
fun FullScreenError(
    errorMessage: String,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onTryAgain,
                colors = ButtonDefaults.buttonColors(containerColor = businessDarkBlue)
            ) {
                Text("Try Again", color = Color.White)
            }
        }
    }
}

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "Shimmer Transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Shimmer Animation"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.9f),
            Color.LightGray.copy(alpha = 0.4f),
            Color.LightGray.copy(alpha = 0.9f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}


@Composable
fun ProfessionalBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, HomeRoutes.Dashboard),
        BottomNavItem("Info", Icons.Outlined.Info, HomeRoutes.Info),
        BottomNavItem("Photos", Icons.Outlined.Photo, HomeRoutes.Photos),
        BottomNavItem("Profile", Icons.Outlined.Person, HomeRoutes.Profile)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedIndex = items.indexOfFirst { item ->
        currentDestination?.hierarchy?.any {
            it.route == item.route::class.qualifiedName
        } == true
    }.coerceAtLeast(0)

    val bottomBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = bottomBarShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.3f)
            ),
        color = surfaceColor,
        shape = bottomBarShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val color by animateColorAsState(
                    targetValue = if (isSelected) businessDarkBlue else Color.Gray,
                    label = "itemColor"
                )

                val indicatorSize by animateDpAsState(
                    targetValue = if (isSelected) 6.dp else 0.dp,
                    label = "indicatorSize",
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable(
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(indicatorSize)
                            .clip(CircleShape)
                            .background(businessDarkBlue)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = color,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenLoadingPreview() {
    Scaffold(
        topBar = {
            TopBar(
                navController = rememberNavController(),
                onMenuClick = {},
                onLogoutClick = {})
        },
        bottomBar = { ProfessionalBottomBar(navController = rememberNavController()) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DashboardLoadingShimmer()
        }
    }
}

