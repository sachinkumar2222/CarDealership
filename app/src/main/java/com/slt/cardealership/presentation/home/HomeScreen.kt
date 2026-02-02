package com.slt.cardealership.presentation.home

import com.slt.cardealership.presentation.common.LoadingAnimation
import com.slt.cardealership.presentation.common.FullScreenError
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.presentation.articles.ArticleLinksScreen
import com.slt.cardealership.presentation.ads.AddAdsScreen
import com.slt.cardealership.presentation.ads.EditAdsScreen // Import logic
import com.slt.cardealership.presentation.ads.AdsScreen
import com.slt.cardealership.presentation.articles.AddEditArticleScreen
import com.slt.cardealership.presentation.articles.ArticleScreen
import com.slt.cardealership.presentation.auth.AuthViewModel

import com.slt.cardealership.presentation.faq.FaqScreen
import com.slt.cardealership.presentation.info.InfoScreen
import com.slt.cardealership.presentation.info.InfoUiState
import com.slt.cardealership.presentation.info.InfoViewModel
import com.slt.cardealership.presentation.internetleads.LeadsDetailScreen
import com.slt.cardealership.presentation.internetleads.LeadsListScreen
import com.slt.cardealership.presentation.inventory.AddVehicleScreen
import com.slt.cardealership.presentation.inventory.InventoryScreen
import com.slt.cardealership.presentation.photos.PhotoScreen
import com.slt.cardealership.presentation.profile.EditProfileScreen
import com.slt.cardealership.presentation.profile.ProfileScreen
import com.slt.cardealership.presentation.profile.ProfileViewModel
import com.slt.cardealership.presentation.seo.SeoScreen

import com.slt.cardealership.presentation.seomenu.SeoMenuScreen
import com.slt.cardealership.presentation.services.ServiceDetailScreen
import com.slt.cardealership.presentation.services.ServiceScreen
import com.slt.cardealership.presentation.users.AddUserScreen
import com.slt.cardealership.presentation.users.ChangePasswordScreen
import com.slt.cardealership.presentation.users.EditUserScreen
import com.slt.cardealership.presentation.users.UserScreen
import com.slt.cardealership.presentation.inventory.VehicleGalleryScreen
import com.slt.cardealership.ui.theme.BrandBlue
import com.slt.cardealership.ui.theme.BrandDarkBlue
import com.slt.cardealership.ui.theme.LightBackground
import com.slt.cardealership.ui.theme.LightCardBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


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
    data class EditAdsScreen(val adId: String) : HomeRoutes()

    @Serializable
    object AddUserScreen : HomeRoutes()

    @Serializable
    object SeoScreen : HomeRoutes()

    @Serializable
    object SeoMapperScreen : HomeRoutes()

    @Serializable
    object FaqScreen : HomeRoutes()

    @Serializable
    object AddPhoto : HomeRoutes() // <-- This was missing

    @Serializable
    object UserScreen : HomeRoutes()

    @Serializable
    object SeoMenuScreen : HomeRoutes()

    @Serializable
    data class ChangePasswordScreen(val userId: Long) : HomeRoutes()

    @Serializable
    data class EditUserScreen(val userId: Long) : HomeRoutes()

    @Serializable
    object ServiceScreen : HomeRoutes()

    @Serializable
    data class ServiceDetailScreen(val title: String) : HomeRoutes()

    @Serializable
    data class AddEditArticle(val articleId: String? = null) : HomeRoutes()

    @Serializable
    object ArticleLinks : HomeRoutes()

    @Serializable
    object LeadsListScreen : HomeRoutes()

    @Serializable
    data class LeadsDetailScreen(val title: String) : HomeRoutes()

    @Serializable
    object MyWebsites : HomeRoutes()

    @Serializable
    data class WebsiteDashboard(val domainId: Int) : HomeRoutes()

    @Serializable
    data class WebsitePages(val domainId: Int) : HomeRoutes()

    @Serializable
    data class WebsiteBlogs(val domainId: Int) : HomeRoutes()

    @Serializable
    data class AddPageScreen(val domainId: Int) : HomeRoutes()

    @Serializable
    data class AddBlogScreen(val domainId: Int, val blogId: String? = null) : HomeRoutes()

    @Serializable
    data class WebsiteSliders(val domainId: Int) : HomeRoutes()

    @Serializable
    data class AddSlider(val domainId: Int, val sliderId: String? = null) : HomeRoutes()

    @Serializable
    data class WebsiteMenus(val domainId: Int) : HomeRoutes()

    @Serializable
    data class AddMenusScreen(val domainId: Int) : HomeRoutes()

    @Serializable
    data class EditMenusScreen(val domainId: Int, val menuId: String) : HomeRoutes()

    @Serializable
    data class VehicleGalleryScreen(val vehicleId: String, val vin: String) : HomeRoutes()

    @Serializable
    data class WebsiteResearchCompare(val domainId: Int) : HomeRoutes()

    @Serializable
    data class AddResearchCompare(val domainId: Int, val domainName: String = "") : HomeRoutes()

    @Serializable
    data class EditResearchCompare(
        val domainId: Int,
        val researchCompareId: String,
        val domainName: String = ""
    ) : HomeRoutes()

    @Serializable
    data class WebsiteSettings(val domainId: Int) : HomeRoutes()

    @Serializable
    data class GeneralSettings(val domainId: Int) : HomeRoutes()

    @Serializable
    data class ContactInfo(val domainId: Int) : HomeRoutes()

    @Serializable
    data class SocialInfo(val domainId: Int) : HomeRoutes()

    @Serializable
    object ManageClassifiedTabs : HomeRoutes()

    @Serializable
    data class ManageClassifiedDashboard(val id: String, val title: String) : HomeRoutes()

    @Serializable
    data class ClassifiedArticles(val siteId: String) : HomeRoutes()

    @Serializable
    data class AddEditClassifiedArticle(val siteId: String, val articleId: String? = null) : HomeRoutes()

    @Serializable
    data class ClassifiedBanners(val siteId: String) : HomeRoutes()

    @Serializable
    data class AddEditClassifiedBanner(val siteId: String, val bannerId: String? = null) : HomeRoutes()

    @Serializable
    data class ClassifiedFaqs(val siteId: String) : HomeRoutes()

    @Serializable
    data class AddEditClassifiedFaq(val siteId: String, val faqId: String? = null) : HomeRoutes()
}

// --- NEW Data class for the stats grid ---
data class DashboardStats(
    val inventoryCount: Int,
    val activeAdsCount: Int,
    val articlesCount: Int,
    val topKeywordsCount: Int
)

data class Project(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val isDark: Boolean = false,
    val routes: HomeRoutes
)

data class DashboardItem(val title: String, val icon: ImageVector, val route: HomeRoutes)

val dummyProjects = listOf(
    Project(
        "1", "Articles", Icons.Outlined.Article,
        "Latest industry news & blogs",
        isDark = true,
        HomeRoutes.Articles
    ),
    Project(
        "2", "Photos",
        Icons.Outlined.PhotoLibrary,
        "Browse your media gallery",
        routes = HomeRoutes.Photos
    ),
    Project(
        "3", "Ad Manager",
        Icons.Outlined.AdsClick,
        "Manage campaigns & ads",
        routes = HomeRoutes.Ads
    ),
    Project(
        "4", "Inventory",
        Icons.Outlined.Inventory,
        "Track stock & products",
        routes = HomeRoutes.Inventory
    )
)

data class DrawerItem(val title: String, val icon: ImageVector, val route: HomeRoutes)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: HomeRoutes)


val businessDarkBlue = Color(0xFF233E66)
val businessLightBlue = Color(0xFF253A63)
val businessTextLight = Color(0xFFCCD6F6)
val businessTextDark = Color(0xFF233E66)
val surfaceColor = Color.White
val backgroundColor = Color(0xFFF5F7FA)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController, authViewModel: AuthViewModel) {
    val homeNavController = rememberNavController()
    // val authViewModel: AuthViewModel = hiltViewModel() // <-- REMOVED
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
                currentRoute = currentRoute,
                dealerInfo = (uiState as? InfoUiState.Success)?.dealerInfo
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
                                message = state.message,
                                onRetry = {
                                    infoviewModel.fetchDealerInfo()
                                }
                            )
                        }

                        is InfoUiState.Success -> {
                            val stats = DashboardStats(
                                inventoryCount = 74,
                                activeAdsCount = 3,
                                articlesCount = 12,
                                topKeywordsCount = 5
                            )
                            DashboardContent(
                                dealerInfo = state.dealerInfo,
                                userProfile = state.userProfile, // Pass UserProfile
                                stats = stats, // Pass stats
                                navController = homeNavController,
                                onSeeAllClick = {
                                    // Open the drawer
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        }
                    }
                }
                composable<HomeRoutes.Info> { InfoScreen(navController = homeNavController) }
                composable<HomeRoutes.Articles> {
                    ArticleScreen(navController = homeNavController)
                }
                composable<HomeRoutes.AddEditArticle>(
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(500)
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(500)
                        )
                    }
                ) {
                    AddEditArticleScreen(
                        navController = homeNavController,
                        onNavigateBack = { homeNavController.popBackStack() })
                }

                composable<HomeRoutes.ArticleLinks> {
                    ArticleLinksScreen(navController = homeNavController)
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
                composable<HomeRoutes.VehicleGalleryScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.VehicleGalleryScreen>()
                    // Use FQN if import is missing or just ensure import is added
                    VehicleGalleryScreen(
                        navController = homeNavController,
                        vehicleId = args.vehicleId,
                        vin = args.vin
                    )
                }
                composable<HomeRoutes.Ads> { AdsScreen(navController = homeNavController) }
                composable<HomeRoutes.AddAdsScreen> {
                    AddAdsScreen(navController = homeNavController)
                }
                composable<HomeRoutes.EditAdsScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.EditAdsScreen>()
                    EditAdsScreen(navController = homeNavController, adId = args.adId) 
                }
                composable<HomeRoutes.SeoScreen> {
                    SeoScreen(navController = homeNavController)
                }
                composable<HomeRoutes.SeoMapperScreen> {
                    com.slt.cardealership.presentation.seo.SeoMapperScreen(navController = homeNavController)
                }

                composable<HomeRoutes.FaqScreen> {
                    FaqScreen(navController = homeNavController)
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
                composable<HomeRoutes.SeoMenuScreen> {
                    SeoMenuScreen(
                        onBackClick = { homeNavController.popBackStack() },
                        navController = homeNavController
                    )
                }

                composable<HomeRoutes.ServiceScreen> {
                    ServiceScreen(
                        onBackClick = { homeNavController.popBackStack() },
                        onServiceClick = { title ->
                            homeNavController.navigate(HomeRoutes.ServiceDetailScreen(title))
                        }
                    )
                }
                composable<HomeRoutes.ServiceDetailScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.ServiceDetailScreen>()
                    ServiceDetailScreen(
                        serviceTitle = args.title,
                        onBackClick = { homeNavController.popBackStack() },
                        viewModel = hiltViewModel()
                    )
                }


                composable<HomeRoutes.LeadsListScreen> {
                    LeadsListScreen(
                        onBackClick = { homeNavController.popBackStack() },
                        onCategoryClick = { title ->
                            // Since you are using Type-Safe Navigation (Serializable),
                            // the library automatically handles special characters like '&'
                            // in "Build & Price". You don't need manual Uri.encode here.
                            homeNavController.navigate(HomeRoutes.LeadsDetailScreen(title = title))
                        }
                    )
                }

                // 2. The Detail Screen
                composable<HomeRoutes.LeadsDetailScreen> { backStackEntry ->
                    // Extract the arguments using .toRoute()
                    val args = backStackEntry.toRoute<HomeRoutes.LeadsDetailScreen>()

                    LeadsDetailScreen(
                        title = args.title,
                        onBackClick = { homeNavController.popBackStack() }
                    )
                }

                composable<HomeRoutes.MyWebsites> {
                    com.slt.cardealership.presentation.mywebsites.MyWebsitesScreen(
                        navController = homeNavController
                    )
                }

                composable<HomeRoutes.WebsiteDashboard> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsiteDashboard>()
                    com.slt.cardealership.presentation.websitedashboard.WebsiteDashboardScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.WebsitePages> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsitePages>()
                    com.slt.cardealership.presentation.websitedashboard.pages.WebsitePagesScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.AddPageScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.AddPageScreen>()
                    com.slt.cardealership.presentation.websitedashboard.pages.AddPageScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.WebsiteBlogs> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsiteBlogs>()
                    com.slt.cardealership.presentation.websitedashboard.blogs.WebsiteBlogsScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.AddBlogScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.AddBlogScreen>()
                    com.slt.cardealership.presentation.websitedashboard.blogs.AddBlogScreen(
                        navController = homeNavController,
                        domainId = args.domainId,
                        blogId = args.blogId
                    )
                }

                composable<HomeRoutes.WebsiteSliders> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsiteSliders>()
                    com.slt.cardealership.presentation.websitedashboard.sliders.WebsiteSlidersScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.AddSlider> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.AddSlider>()
                    com.slt.cardealership.presentation.websitedashboard.sliders.AddSliderScreen(
                        navController = homeNavController,
                        domainId = args.domainId,
                        sliderId = args.sliderId
                    )
                }

                composable<HomeRoutes.WebsiteMenus> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsiteMenus>()
                    com.slt.cardealership.presentation.websitedashboard.menus.WebsiteMenusScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.WebsiteResearchCompare> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsiteResearchCompare>()
                    com.slt.cardealership.presentation.websitedashboard.researchCompare.WebsiteResCompScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.AddResearchCompare> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.AddResearchCompare>()
                    com.slt.cardealership.presentation.websitedashboard.researchCompare.AddResCompScreenWrapper(
                        navController = homeNavController,
                        domainId = args.domainId,
                        domainName = args.domainName
                    )
                }

                composable<HomeRoutes.EditResearchCompare> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.EditResearchCompare>()
                    com.slt.cardealership.presentation.websitedashboard.researchCompare.AddResCompScreenWrapper(
                        navController = homeNavController,
                        domainId = args.domainId,
                        domainName = args.domainName,
                        researchCompareId = args.researchCompareId
                    )
                }

                composable<HomeRoutes.WebsiteSettings> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.WebsiteSettings>()
                    com.slt.cardealership.presentation.websitedashboard.settings.SettingsScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.GeneralSettings> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.GeneralSettings>()
                    com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.general.GeneralSettingsScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.ContactInfo> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.ContactInfo>()
                    com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.contactinfo.ContactInfoScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.SocialInfo> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.SocialInfo>()
                    com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.socialinfo.SocialInfoScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.AddMenusScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.AddMenusScreen>()
                    com.slt.cardealership.presentation.websitedashboard.menus.AddMenusScreen(
                        navController = homeNavController,
                        domainId = args.domainId
                    )
                }

                composable<HomeRoutes.EditMenusScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.EditMenusScreen>()
                    com.slt.cardealership.presentation.websitedashboard.menus.EditMenusScreen(
                        navController = homeNavController,
                        domainId = args.domainId,
                        menuId = args.menuId
                    )
                }

                composable<HomeRoutes.ManageClassifiedTabs> {
                    com.slt.cardealership.presentation.ManageClassified.ManageClassifiedTabsScreen(
                        navController = homeNavController
                    )
                }

                composable<HomeRoutes.ManageClassifiedDashboard> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.ManageClassifiedDashboard>()
                    com.slt.cardealership.presentation.ManageClassified.ManageClassifiedDashboardScreen(
                        navController = homeNavController,
                        siteId = args.id,
                        siteTitle = args.title
                    )
                }

                composable<HomeRoutes.ClassifiedArticles> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.ClassifiedArticles>()
                    com.slt.cardealership.presentation.ManageClassified.articles.ClassifiedArticlesScreen(
                        siteId = args.siteId,
                        navController = homeNavController
                    )
                }

                composable<HomeRoutes.AddEditClassifiedArticle> {
                    com.slt.cardealership.presentation.ManageClassified.articles.AddEditClassifiedArticleScreen(
                        navController = homeNavController,
                        onNavigateBack = { homeNavController.popBackStack() }
                    )
                }

                composable<HomeRoutes.ClassifiedBanners> { backStackEntry ->
                    val route: HomeRoutes.ClassifiedBanners = backStackEntry.toRoute()
                    com.slt.cardealership.presentation.ManageClassified.banners.ClassifiedBannersScreen(
                        siteId = route.siteId,
                        navController = homeNavController
                    )
                }

                composable<HomeRoutes.AddEditClassifiedBanner> { backStackEntry ->
                    val route: HomeRoutes.AddEditClassifiedBanner = backStackEntry.toRoute()
                    com.slt.cardealership.presentation.ManageClassified.banners.AddEditClassifiedBannerScreen(
                        siteId = route.siteId,
                        bannerId = route.bannerId,
                        navController = homeNavController
                    )
                }


                composable<HomeRoutes.ClassifiedFaqs> { backStackEntry ->
                    val args = backStackEntry.toRoute<HomeRoutes.ClassifiedFaqs>()
                    com.slt.cardealership.presentation.ManageClassified.faqs.ClassifiedFaqsScreen(
                        navController = homeNavController,
                        siteId = args.siteId
                    )
                }

                composable<HomeRoutes.AddEditClassifiedFaq> { backStackEntry ->
                    // val args = backStackEntry.toRoute<HomeRoutes.AddEditClassifiedFaq>()
                    // The ViewModel gets args from SavedStateHandle directly
                    com.slt.cardealership.presentation.ManageClassified.faqs.AddEditClassifiedFaqScreen(
                        navController = homeNavController,
                        onNavigateBack = { homeNavController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(dealerInfo: DealerInfo, userProfile: com.slt.cardealership.domain.model.UserProfile?) {
    Column {
        Text(
            text = "Hi! " + (dealerInfo.name ?: "Dealer Admin"),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = BrandDarkBlue,
            fontSize = 22.sp
        )
        Text(
            text = "have a good day",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun WelcomeBanner() {

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrandDarkBlue, RoundedCornerShape(20.dp)), // <-- UPDATED
        colors = CardDefaults.cardColors(containerColor = LightBackground) // <-- UPDATED
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 10.dp, top = 1.dp, bottom = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lets schedule your projects",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Image(
                painter = painterResource(id = R.drawable.vehicle_sale_cuate),
                contentDescription = "Schedule Projects with Car",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}



@Composable
fun ProjectCard(project: Project, navController: NavController) {
    val activeColor = Color(0xFF2196F3)
    val isSelected = project.isDark

    val textColor = if (isSelected) activeColor else Color.Black
    // Make description slightly lighter/gray but distinct
    val secondaryColor = if (isSelected) activeColor.copy(alpha = 0.6f) else Color.Gray
    val containerColor = if (isSelected) Color.White else LightCardBackground

    // Common content logic
    val cardContent = @Composable {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Main Icon and the small + icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = project.icon,
                    contentDescription = project.title,
                    tint = if (isSelected) activeColor else secondaryColor,
                    modifier = Modifier.size(36.dp)
                )
                // Plus icon with circle border
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isSelected) activeColor else secondaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Options",
                        tint = if (isSelected) activeColor else secondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Content Column: Title and Description
            Column {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (isSelected) {
        OutlinedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, activeColor),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Blue.copy(alpha = 0.1f),
                    ambientColor = Color.Blue.copy(alpha = 0.05f)
                )
                .clickable { project.routes.let { navController.navigate(it) } }
        ) {
            cardContent()
        }
    } else {
        OutlinedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)), // Light gray border for unselected
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .clickable { project.routes.let { navController.navigate(it) } }
        ) {
            cardContent()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentRoute: String?,
    dealerInfo: DealerInfo? // Added parameter
) {

    val items = listOf(
        DrawerItem("Dashboard", Icons.Outlined.Home, HomeRoutes.Dashboard),
        DrawerItem("Inventory", Icons.Outlined.Inventory, HomeRoutes.Inventory),
        DrawerItem("Ads Manager", Icons.Outlined.Campaign, HomeRoutes.Ads),
        DrawerItem("Articles", Icons.Outlined.Article, HomeRoutes.Articles),
        DrawerItem("Photos", Icons.Outlined.PhotoLibrary, HomeRoutes.Photos),
        DrawerItem("Services", Icons.Outlined.Security, HomeRoutes.ServiceScreen),
        DrawerItem("Internet Leads", Icons.Outlined.Leaderboard, HomeRoutes.LeadsListScreen),
        DrawerItem("SEO", Icons.Outlined.BarChart, HomeRoutes.SeoScreen),
        DrawerItem("SEO Menu", Icons.Outlined.Warehouse, HomeRoutes.SeoMenuScreen),
        DrawerItem("FAQ", Icons.Outlined.Quiz, HomeRoutes.FaqScreen),
        DrawerItem("Users", Icons.Outlined.Person, HomeRoutes.UserScreen),
        DrawerItem("Manage Classifieds", Icons.Outlined.Public, HomeRoutes.ManageClassifiedTabs),
        DrawerItem("Profile", Icons.Outlined.Person, HomeRoutes.Profile),
        DrawerItem("My Websites", Icons.Default.Language, HomeRoutes.MyWebsites)
    )

    // The requested active color
    val activeColor = Color(0xFF2196F3)

    ModalDrawerSheet(
        drawerContainerColor = Color.White, // Light Theme
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        // Header with the active blue color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(activeColor)
                .padding(vertical = 40.dp, horizontal = 24.dp)
        ) {
            Column {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dealer Panel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Show Dealer Plan
                dealerInfo?.dealerType?.let { plan ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Plan: $plan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "Management Console",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp, start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items) { item ->
                val selected = currentRoute == item.route::class.qualifiedName

                // Custom Item Row for "indicator" style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) activeColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable {
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
                        }
                        .padding(start = 12.dp, end = 12.dp), // Padding inside the clickable area
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Indicator Bar (only visible when selected)
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(activeColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        // Spacer to keep alignment consistent if desired,
                        // or just rely on the padding.
                        // To perfectly mimic the image where unselected items don't have the gap:
                        // We will just add the icon directly.
                        // If you want rigid alignment, add a spacer here too.
                        // For now, let's keep it fluid like standard drawers.
                        // Actually, let's add a small spacer to offset the icon slightly from the edge
                        // if we want to match the selected state's text position exactly.
                        // But usually, the icon just shifts. Let's stick to standard behavior (icon shifts).
                    }

                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (selected) activeColor else Color.Gray
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) activeColor else Color.DarkGray
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.LightGray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
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
    userProfile: com.slt.cardealership.domain.model.UserProfile?, // Added UserProfile
    stats: DashboardStats,
    navController: NavController,
    onSeeAllClick: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                WelcomeHeader(dealerInfo = dealerInfo, userProfile = userProfile) // Pass UserProfile
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            WelcomeBanner()
        }

        // Section Title
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onSeeAllClick) {
                        Text("view all", color = BrandBlue)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Grid Items
        items(dummyProjects) { project ->
            ProjectCard(project = project, navController = navController)
        }

        // Bottom Spacer
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(12.dp))
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
        modifier = Modifier.shadow(8.dp),
        title = {
            Text("Home", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    // Replace 'ic_custom_menu' with the actual name of your PNG file
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp), // Standard icon size
                    tint = Color.Black // Tints the PNG black. Change to Color.Unspecified to keep original PNG colors.
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
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
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF2196F3))
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
fun WelcomeHeaderShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
    }
}

@Composable
fun WelcomeBannerShimmer() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = LightBackground)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 10.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer()
            )
        }
    }
}

@Composable
fun ProjectCardShimmer() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
            }

            // Content Column
            Column {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun DashboardLoadingShimmer() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            WelcomeHeaderShimmer()
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            WelcomeBannerShimmer()
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // "Manage" Section Title Shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }

        items(4) {
            ProjectCardShimmer()
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

