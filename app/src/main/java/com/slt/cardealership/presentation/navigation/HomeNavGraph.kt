//package com.slt.cardealership.presentation.navigation
//
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.DrawerState
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.toRoute
//import com.slt.cardealership.presentation.home.HomeRoutes
//import com.slt.cardealership.presentation.home.DashboardContent
//import com.slt.cardealership.presentation.home.DashboardLoadingShimmer
//import com.slt.cardealership.presentation.home.DashboardStats
//import com.slt.cardealership.presentation.home.InfoUiState
//import com.slt.cardealership.presentation.home.InfoViewModel
//import com.slt.cardealership.presentation.auth.AuthViewModel
//import com.slt.cardealership.presentation.common.FullScreenError
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//// Screen Imports
//import com.slt.cardealership.presentation.home.InfoScreen
//import com.slt.cardealership.presentation.article.ArticleScreen
//import com.slt.cardealership.presentation.article.AddEditArticleScreen
//import com.slt.cardealership.presentation.article.ArticleLinksScreen
//import com.slt.cardealership.presentation.profile.ProfileScreen
//import com.slt.cardealership.presentation.profile.EditProfileScreen
//import com.slt.cardealership.presentation.profile.ProfileViewModel
//import com.slt.cardealership.presentation.photo.PhotoScreen
//import com.slt.cardealership.presentation.inventory.InventoryScreen
//import com.slt.cardealership.presentation.inventory.AddVehicleScreen
//import com.slt.cardealership.presentation.inventory.VehicleGalleryScreen
//import com.slt.cardealership.presentation.ads.AdsScreen
//import com.slt.cardealership.presentation.ads.AddAdsScreen
//import com.slt.cardealership.presentation.seo.SeoScreen
//import com.slt.cardealership.presentation.seo.AddSeoScreen
//import com.slt.cardealership.presentation.faq.FaqScreen
//import com.slt.cardealership.presentation.faq.AddFaqScreen
//import com.slt.cardealership.presentation.user.UserScreen
//import com.slt.cardealership.presentation.user.AddUserScreen
//import com.slt.cardealership.presentation.user.EditUserScreen
//import com.slt.cardealership.presentation.profile.ChangePasswordScreen
//import com.slt.cardealership.presentation.seomenu.SeoMenuScreen
//import com.slt.cardealership.presentation.seomenu.AddSeoMenuScreen
//import com.slt.cardealership.presentation.service.ServiceScreen
//import com.slt.cardealership.presentation.service.ServiceDetailScreen
//import com.slt.cardealership.presentation.leads.LeadsListScreen
//import com.slt.cardealership.presentation.leads.LeadsDetailScreen
//import androidx.navigation.NavController
//
//@Composable
//fun HomeNavGraph(
//    navController: NavHostController,
//    mainNavController: NavController,
//    paddingValues: PaddingValues,
//    authViewModel: AuthViewModel,
//    infoviewModel: InfoViewModel,
//    uiState: InfoUiState,
//    drawerState: DrawerState,
//    scope: CoroutineScope
//) {
//    NavHost(
//        navController = navController,
//        startDestination = HomeRoutes.Dashboard,
//        modifier = Modifier.padding(paddingValues)
//    ) {
//        composable<HomeRoutes.Dashboard> {
//            when (val state = uiState) {
//                is InfoUiState.Loading -> {
//                    DashboardLoadingShimmer()
//                }
//
//                is InfoUiState.Error -> {
//                    FullScreenError(
//                        message = state.message,
//                        onRetry = {
//                            infoviewModel.fetchDealerInfo()
//                        }
//                    )
//                }
//
//                is InfoUiState.Success -> {
//                    val stats = DashboardStats(
//                        inventoryCount = 74,
//                        activeAdsCount = 3,
//                        articlesCount = 12,
//                        topKeywordsCount = 5
//                    )
//                    DashboardContent(
//                        dealerInfo = state.dealerInfo,
//                        userProfile = state.userProfile,
//                        stats = stats,
//                        navController = navController,
//                        onSeeAllClick = {
//                            scope.launch {
//                                drawerState.open()
//                            }
//                        }
//                    )
//                }
//            }
//        }
//        composable<HomeRoutes.Info> { InfoScreen(navController = navController) }
//        composable<HomeRoutes.Articles> {
//            ArticleScreen(navController = navController)
//        }
//        composable<HomeRoutes.AddEditArticle>(
//            enterTransition = {
//                slideInVertically(
//                    initialOffsetY = { it },
//                    animationSpec = tween(500)
//                )
//            },
//            popExitTransition = {
//                slideOutVertically(
//                    targetOffsetY = { it },
//                    animationSpec = tween(500)
//                )
//            }
//        ) {
//            AddEditArticleScreen(
//                navController = navController,
//                onNavigateBack = { navController.popBackStack() })
//        }
//        composable<HomeRoutes.ArticleLinks> {
//            ArticleLinksScreen(navController = navController)
//        }
//        composable<HomeRoutes.Profile> {
//            val profileViewModel: ProfileViewModel = hiltViewModel()
//
//            ProfileScreen(
//                navController = mainNavController,
//                viewModel = profileViewModel,
//                onSignOutClick = { authViewModel.signOut() },
//                onEditProfileClick = { userId ->
//                    navController.navigate(HomeRoutes.EditProfileScreen(userId))
//                },
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable<HomeRoutes.EditProfileScreen> { backStackEntry ->
//            val profileViewModel: ProfileViewModel = hiltViewModel()
//            EditProfileScreen(
//                onBackClick = { navController.popBackStack() },
//                viewModel = profileViewModel,
//                onSaveSuccess = { navController.popBackStack() }
//            )
//        }
//
//        composable<HomeRoutes.Photos> { PhotoScreen(navController = navController) }
//        composable<HomeRoutes.AddPhoto> {
//            PhotoScreen(navController = navController)
//        }
//        composable<HomeRoutes.Inventory> { InventoryScreen(navController = navController) }
//        composable<HomeRoutes.AddVehicleScreen> {
//            AddVehicleScreen(navController = navController)
//        }
//        composable<HomeRoutes.VehicleGalleryScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.VehicleGalleryScreen>()
//            VehicleGalleryScreen(
//                navController = navController,
//                vehicleId = args.vehicleId,
//                vin = args.vin
//            )
//        }
//        composable<HomeRoutes.Ads> { AdsScreen(navController = navController) }
//        composable<HomeRoutes.AddAdsScreen> {
//            AddAdsScreen(navController = navController)
//        }
//        composable<HomeRoutes.SeoScreen> {
//            SeoScreen(navController = navController)
//        }
//        composable<HomeRoutes.AddSeoScreen> {
//            AddSeoScreen(navController = navController)
//        }
//        composable<HomeRoutes.FaqScreen> {
//            FaqScreen(navController = navController)
//        }
//        composable<HomeRoutes.AddFaqScreen> {
//            AddFaqScreen(navController = navController)
//        }
//        composable<HomeRoutes.UserScreen> {
//            UserScreen(
//                onBackClick = { navController.popBackStack() },
//                onAddUserClick = {
//                    navController.navigate(HomeRoutes.AddUserScreen)
//                },
//                onManageUserClick = {
//                    navController.navigate(HomeRoutes.Profile)
//                },
//                onEditUserClick = { user ->
//                    navController.navigate(HomeRoutes.EditUserScreen(user.id))
//                }
//            )
//        }
//
//        composable<HomeRoutes.AddUserScreen> {
//            AddUserScreen(
//                onBackClick = { navController.popBackStack() },
//                onAddUserSuccess = { navController.popBackStack() }
//            )
//        }
//
//        composable<HomeRoutes.EditUserScreen> { backStackEntry ->
//            val userToEdit = backStackEntry.toRoute<HomeRoutes.EditUserScreen>()
//            EditUserScreen(
//                userId = userToEdit.userId,
//                onBackClick = { navController.popBackStack() },
//                onChangePasswordClick = { userId ->
//                    navController.navigate(HomeRoutes.ChangePasswordScreen(userId))
//                }
//            )
//        }
//        composable<HomeRoutes.ChangePasswordScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ChangePasswordScreen>()
//
//            ChangePasswordScreen(
//                userId = args.userId,
//                onCloseClick = { navController.popBackStack() },
//                onPasswordChangedSuccess = {
//                    navController.popBackStack()
//                }
//            )
//        }
//        composable<HomeRoutes.SeoMenuScreen> {
//            SeoMenuScreen(
//                onBackClick = { navController.popBackStack() },
//                navController = navController
//            )
//        }
//
//        composable<HomeRoutes.AddSeoMenuScreen> {
//            AddSeoMenuScreen(
//                onBackClick = { navController.popBackStack() })
//        }
//
//        composable<HomeRoutes.ServiceScreen> {
//            ServiceScreen(
//                onBackClick = { navController.popBackStack() },
//                onServiceClick = { title ->
//                    navController.navigate(HomeRoutes.ServiceDetailScreen(title))
//                }
//            )
//        }
//        composable<HomeRoutes.ServiceDetailScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ServiceDetailScreen>()
//            ServiceDetailScreen(
//                serviceTitle = args.title,
//                onBackClick = { navController.popBackStack() },
//                viewModel = hiltViewModel()
//            )
//        }
//
//        composable<HomeRoutes.LeadsListScreen> {
//            LeadsListScreen(
//                onBackClick = { navController.popBackStack() },
//                onCategoryClick = { title ->
//                    navController.navigate(HomeRoutes.LeadsDetailScreen(title = title))
//                }
//            )
//        }
//
//        composable<HomeRoutes.LeadsDetailScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.LeadsDetailScreen>()
//            LeadsDetailScreen(
//                title = args.title,
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable<HomeRoutes.MyWebsites> {
//            com.slt.cardealership.presentation.mywebsites.MyWebsitesScreen(
//                navController = navController
//            )
//        }
//
//        composable<HomeRoutes.WebsiteDashboard> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsiteDashboard>()
//            com.slt.cardealership.presentation.websitedashboard.WebsiteDashboardScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.WebsitePages> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsitePages>()
//            com.slt.cardealership.presentation.websitedashboard.pages.WebsitePagesScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.AddPageScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.AddPageScreen>()
//            com.slt.cardealership.presentation.websitedashboard.pages.AddPageScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.EditPageScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.EditPageScreen>()
//            com.slt.cardealership.presentation.websitedashboard.pages.EditPageScreen(
//                navController = navController,
//                domainId = args.domainId,
//                pageId = args.pageId
//            )
//        }
//
//        composable<HomeRoutes.WebsiteBlogs> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsiteBlogs>()
//            com.slt.cardealership.presentation.websitedashboard.blogs.WebsiteBlogsScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.AddBlogScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.AddBlogScreen>()
//            com.slt.cardealership.presentation.websitedashboard.blogs.AddBlogScreen(
//                navController = navController,
//                domainId = args.domainId,
//                blogId = args.blogId
//            )
//        }
//
//        composable<HomeRoutes.WebsiteSliders> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsiteSliders>()
//            com.slt.cardealership.presentation.websitedashboard.sliders.WebsiteSlidersScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.AddSlider> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.AddSlider>()
//            com.slt.cardealership.presentation.websitedashboard.sliders.AddSliderScreen(
//                navController = navController,
//                domainId = args.domainId,
//                sliderId = args.sliderId
//            )
//        }
//
//        composable<HomeRoutes.WebsiteMenus> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsiteMenus>()
//            com.slt.cardealership.presentation.websitedashboard.menus.WebsiteMenusScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.WebsiteResearchCompare> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsiteResearchCompare>()
//            com.slt.cardealership.presentation.websitedashboard.researchCompare.WebsiteResCompScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.AddResearchCompare> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.AddResearchCompare>()
//            com.slt.cardealership.presentation.websitedashboard.researchCompare.AddResCompScreenWrapper(
//                navController = navController,
//                domainId = args.domainId,
//                domainName = args.domainName
//            )
//        }
//
//        composable<HomeRoutes.EditResearchCompare> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.EditResearchCompare>()
//            com.slt.cardealership.presentation.websitedashboard.researchCompare.AddResCompScreenWrapper(
//                navController = navController,
//                domainId = args.domainId,
//                domainName = args.domainName,
//                researchCompareId = args.researchCompareId
//            )
//        }
//
//        composable<HomeRoutes.ManageCategory> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ManageCategory>()
//            com.slt.cardealership.presentation.websitedashboard.researchCompare.manageCategory.ManageCategoryScreen(
//                domainId = args.domainId,
//                domainName = args.domainName,
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        composable<HomeRoutes.WebsiteSettings> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.WebsiteSettings>()
//            com.slt.cardealership.presentation.websitedashboard.settings.SettingsScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.GeneralSettings> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.GeneralSettings>()
//            com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.general.GeneralSettingsScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.ContactInfo> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ContactInfo>()
//            com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.contactinfo.ContactInfoScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.SocialInfo> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.SocialInfo>()
//            com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.socialinfo.SocialInfoScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.AddMenusScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.AddMenusScreen>()
//            com.slt.cardealership.presentation.websitedashboard.menus.AddMenusScreen(
//                navController = navController,
//                domainId = args.domainId
//            )
//        }
//
//        composable<HomeRoutes.EditMenusScreen> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.EditMenusScreen>()
//            com.slt.cardealership.presentation.websitedashboard.menus.EditMenusScreen(
//                navController = navController,
//                domainId = args.domainId,
//                menuId = args.menuId
//            )
//        }
//
//        composable<HomeRoutes.ManageClassifiedTabs> {
//            com.slt.cardealership.presentation.ManageClassified.ManageClassifiedTabsScreen(
//                navController = navController
//            )
//        }
//
//        composable<HomeRoutes.ManageClassifiedDashboard> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ManageClassifiedDashboard>()
//            com.slt.cardealership.presentation.ManageClassified.ManageClassifiedDashboardScreen(
//                navController = navController,
//                siteId = args.id,
//                siteTitle = args.title
//            )
//        }
//
//        composable<HomeRoutes.ClassifiedArticles> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ClassifiedArticles>()
//            com.slt.cardealership.presentation.ManageClassified.articles.ClassifiedArticlesScreen(
//                siteId = args.siteId,
//                navController = navController
//            )
//        }
//
//        composable<HomeRoutes.AddEditClassifiedArticle> {
//            com.slt.cardealership.presentation.ManageClassified.articles.AddEditClassifiedArticleScreen(
//                navController = navController,
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        composable<HomeRoutes.ClassifiedBanners> { backStackEntry ->
//            val route: HomeRoutes.ClassifiedBanners = backStackEntry.toRoute()
//            com.slt.cardealership.presentation.ManageClassified.banners.ClassifiedBannersScreen(
//                siteId = route.siteId,
//                navController = navController
//            )
//        }
//
//        composable<HomeRoutes.ClassifiedFaqs> { backStackEntry ->
//            val args = backStackEntry.toRoute<HomeRoutes.ClassifiedFaqs>()
//            com.slt.cardealership.presentation.ManageClassified.faqs.ClassifiedFaqsScreen(
//                navController = navController,
//                siteId = args.siteId
//            )
//        }
//
//        composable<HomeRoutes.AddEditClassifiedFaq> { backStackEntry ->
//            com.slt.cardealership.presentation.ManageClassified.faqs.AddEditClassifiedFaqScreen(
//                navController = navController,
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//    }
//}
