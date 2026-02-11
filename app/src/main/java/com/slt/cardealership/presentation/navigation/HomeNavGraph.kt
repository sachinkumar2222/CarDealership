package com.slt.cardealership.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.slt.cardealership.presentation.home.DashboardScreen
import com.slt.cardealership.presentation.info.InfoScreen
import com.slt.cardealership.presentation.articles.ArticleScreen
import com.slt.cardealership.presentation.articles.AddEditArticleScreen
import com.slt.cardealership.presentation.articles.ArticleLinksScreen
import com.slt.cardealership.presentation.profile.ProfileScreen
import com.slt.cardealership.presentation.profile.EditProfileScreen
import com.slt.cardealership.presentation.profile.ProfileViewModel
import com.slt.cardealership.presentation.photos.PhotoScreen
import com.slt.cardealership.presentation.inventory.InventoryScreen
import com.slt.cardealership.presentation.inventory.AddVehicleScreen
import com.slt.cardealership.presentation.inventory.VehicleGalleryScreen
import com.slt.cardealership.presentation.ads.AdsScreen
import com.slt.cardealership.presentation.ads.AddAdsScreen
import com.slt.cardealership.presentation.ads.EditAdsScreen
import com.slt.cardealership.presentation.seo.SeoScreen
import com.slt.cardealership.presentation.faq.FaqScreen
import com.slt.cardealership.presentation.users.UserScreen
import com.slt.cardealership.presentation.users.AddUserScreen
import com.slt.cardealership.presentation.users.EditUserScreen
import com.slt.cardealership.presentation.users.ChangePasswordScreen
import com.slt.cardealership.presentation.seomenu.SeoMenuScreen
import com.slt.cardealership.presentation.services.ServiceScreen
import com.slt.cardealership.presentation.services.ServiceDetailScreen
import com.slt.cardealership.presentation.internetleads.LeadsListScreen
import com.slt.cardealership.presentation.internetleads.LeadsDetailScreen
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.slt.cardealership.presentation.info.InfoUiState
import com.slt.cardealership.presentation.info.InfoViewModel
import com.slt.cardealership.presentation.auth.AuthViewModel
import com.slt.cardealership.presentation.common.FullScreenError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    mainNavController: NavController,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel,
    infoviewModel: InfoViewModel,
    uiState: InfoUiState,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.Dashboard,
        modifier = Modifier.padding(paddingValues)
    ) {

        composable<HomeRoutes.Dashboard> {
            com.slt.cardealership.presentation.home.DashboardScreen(
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                authViewModel = authViewModel,
                infoUiState = uiState,
                onRetry = { infoviewModel.fetchDealerInfo() },
                onSeeAllClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }

        composable<HomeRoutes.Info> { InfoScreen(navController = navController) }
        composable<HomeRoutes.Articles> {
            ArticleScreen(navController = navController)
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
                navController = navController,
                onNavigateBack = { navController.popBackStack() })
        }
        composable<HomeRoutes.ArticleLinks> {
            ArticleLinksScreen(navController = navController)
        }
        composable<HomeRoutes.Profile> {
            val profileViewModel: ProfileViewModel = hiltViewModel()

            ProfileScreen(
                navController = mainNavController,
                viewModel = profileViewModel,
                onSignOutClick = { authViewModel.signOut() },
                onEditProfileClick = { userId ->
                    navController.navigate(HomeRoutes.EditProfileScreen(userId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<HomeRoutes.EditProfileScreen> { backStackEntry ->
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = profileViewModel,
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable<HomeRoutes.Photos> { PhotoScreen(navController = navController) }
        composable<HomeRoutes.AddPhoto> {
            PhotoScreen(navController = navController)
        }
        composable<HomeRoutes.Inventory> { InventoryScreen(navController = navController) }
        composable<HomeRoutes.AddVehicleScreen> {
            AddVehicleScreen(navController = navController)
        }
        composable<HomeRoutes.VehicleGalleryScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.VehicleGalleryScreen>()
            VehicleGalleryScreen(
                navController = navController,
                vehicleId = args.vehicleId,
                vin = args.vin
            )
        }
        composable<HomeRoutes.Ads> { AdsScreen(navController = navController) }
        composable<HomeRoutes.AddAdsScreen> {
            AddAdsScreen(navController = navController)
        }
        composable<HomeRoutes.EditAdsScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.EditAdsScreen>()
            EditAdsScreen(navController = navController, adId = args.adId)
        }
        composable<HomeRoutes.SeoScreen> {
            SeoScreen(navController = navController)
        }
        composable<HomeRoutes.SeoMapperScreen> {
             com.slt.cardealership.presentation.seo.SeoMapperScreen(navController = navController)
        }
        composable<HomeRoutes.FaqScreen> {
            FaqScreen(navController = navController)
        }
        composable<HomeRoutes.UserScreen> {
            UserScreen(
                onBackClick = { navController.popBackStack() },
                onAddUserClick = {
                    navController.navigate(HomeRoutes.AddUserScreen)
                },
                onManageUserClick = {
                    navController.navigate(HomeRoutes.Profile)
                },
                onEditUserClick = { user ->
                    navController.navigate(HomeRoutes.EditUserScreen(user.id))
                }
            )
        }

        composable<HomeRoutes.AddUserScreen> {
            AddUserScreen(
                onBackClick = { navController.popBackStack() },
                onAddUserSuccess = { navController.popBackStack() }
            )
        }

        composable<HomeRoutes.EditUserScreen> { backStackEntry ->
            val userToEdit = backStackEntry.toRoute<HomeRoutes.EditUserScreen>()
            EditUserScreen(
                userId = userToEdit.userId,
                onBackClick = { navController.popBackStack() },
                onChangePasswordClick = { userId ->
                    navController.navigate(HomeRoutes.ChangePasswordScreen(userId))
                }
            )
        }
        composable<HomeRoutes.ChangePasswordScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ChangePasswordScreen>()

            ChangePasswordScreen(
                userId = args.userId,
                onCloseClick = { navController.popBackStack() },
                onPasswordChangedSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable<HomeRoutes.SeoMenuScreen> {
            SeoMenuScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }

        composable<HomeRoutes.ServiceScreen> {
            ServiceScreen(
                onBackClick = { navController.popBackStack() },
                onServiceClick = { title ->
                    navController.navigate(HomeRoutes.ServiceDetailScreen(title))
                }
            )
        }
        composable<HomeRoutes.ServiceDetailScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ServiceDetailScreen>()
            ServiceDetailScreen(
                serviceTitle = args.title,
                onBackClick = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        composable<HomeRoutes.LeadsListScreen> {
            LeadsListScreen(
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { title ->
                    navController.navigate(HomeRoutes.LeadsDetailScreen(title = title))
                }
            )
        }

        composable<HomeRoutes.LeadsDetailScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.LeadsDetailScreen>()
            LeadsDetailScreen(
                title = args.title,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<HomeRoutes.MyWebsites> {
            com.slt.cardealership.presentation.mywebsites.MyWebsitesScreen(
                navController = navController
            )
        }

        composable<HomeRoutes.WebsiteDashboard> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsiteDashboard>()
            com.slt.cardealership.presentation.websitedashboard.WebsiteDashboardScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.WebsitePages> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsitePages>()
            com.slt.cardealership.presentation.websitedashboard.pages.WebsitePagesScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.AddPageScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.AddPageScreen>()
            com.slt.cardealership.presentation.websitedashboard.pages.AddPageScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.EditPageScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.EditPageScreen>()
            com.slt.cardealership.presentation.websitedashboard.pages.EditPageScreen(
                navController = navController,
                domainId = args.domainId,
                pageId = args.pageId
            )
        }

        composable<HomeRoutes.WebsiteBlogs> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsiteBlogs>()
            com.slt.cardealership.presentation.websitedashboard.blogs.WebsiteBlogsScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.AddBlogScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.AddBlogScreen>()
            com.slt.cardealership.presentation.websitedashboard.blogs.AddBlogScreen(
                navController = navController,
                domainId = args.domainId,
                blogId = args.blogId
            )
        }

        composable<HomeRoutes.WebsiteSliders> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsiteSliders>()
            com.slt.cardealership.presentation.websitedashboard.sliders.WebsiteSlidersScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.AddSlider> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.AddSlider>()
            com.slt.cardealership.presentation.websitedashboard.sliders.AddSliderScreen(
                navController = navController,
                domainId = args.domainId,
                sliderId = args.sliderId
            )
        }

        composable<HomeRoutes.WebsiteMenus> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsiteMenus>()
            com.slt.cardealership.presentation.websitedashboard.menus.WebsiteMenusScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.WebsiteResearchCompare> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsiteResearchCompare>()
            com.slt.cardealership.presentation.websitedashboard.researchCompare.WebsiteResCompScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.AddResearchCompare> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.AddResearchCompare>()
            com.slt.cardealership.presentation.websitedashboard.researchCompare.AddResCompScreenWrapper(
                navController = navController,
                domainId = args.domainId,
                domainName = args.domainName
            )
        }

        composable<HomeRoutes.EditResearchCompare> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.EditResearchCompare>()
            com.slt.cardealership.presentation.websitedashboard.researchCompare.AddResCompScreenWrapper(
                navController = navController,
                domainId = args.domainId,
                domainName = args.domainName,
                researchCompareId = args.researchCompareId
            )
        }

        composable<HomeRoutes.ManageCategory> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ManageCategory>()
            com.slt.cardealership.presentation.websitedashboard.researchCompare.manageCategory.ManageCategoryScreen(
                domainId = args.domainId,
                domainName = args.domainName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HomeRoutes.WebsiteSettings> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.WebsiteSettings>()
            com.slt.cardealership.presentation.websitedashboard.settings.SettingsScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.GeneralSettings> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.GeneralSettings>()
            com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.general.GeneralSettingsScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.ContactInfo> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ContactInfo>()
            com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.contactinfo.ContactInfoScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.SocialInfo> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.SocialInfo>()
            com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.socialinfo.SocialInfoScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.AddMenusScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.AddMenusScreen>()
            com.slt.cardealership.presentation.websitedashboard.menus.AddMenusScreen(
                navController = navController,
                domainId = args.domainId
            )
        }

        composable<HomeRoutes.EditMenusScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.EditMenusScreen>()
            com.slt.cardealership.presentation.websitedashboard.menus.EditMenusScreen(
                navController = navController,
                domainId = args.domainId,
                menuId = args.menuId
            )
        }

        composable<HomeRoutes.ManageClassifiedTabs> {
            com.slt.cardealership.presentation.ManageClassified.ManageClassifiedTabsScreen(
                navController = navController
            )
        }

        composable<HomeRoutes.ManageClassifiedDashboard> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ManageClassifiedDashboard>()
            com.slt.cardealership.presentation.ManageClassified.ManageClassifiedDashboardScreen(
                navController = navController,
                siteId = args.id,
                siteTitle = args.title
            )
        }

        composable<HomeRoutes.ClassifiedArticles> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ClassifiedArticles>()
            com.slt.cardealership.presentation.ManageClassified.articles.ClassifiedArticlesScreen(
                siteId = args.siteId,
                navController = navController
            )
        }

        composable<HomeRoutes.AddEditClassifiedArticle> {
            com.slt.cardealership.presentation.ManageClassified.articles.AddEditClassifiedArticleScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HomeRoutes.ClassifiedBanners> { backStackEntry ->
            val route: HomeRoutes.ClassifiedBanners = backStackEntry.toRoute()
            com.slt.cardealership.presentation.ManageClassified.banners.ClassifiedBannersScreen(
                siteId = route.siteId,
                navController = navController
            )
        }

        composable<HomeRoutes.ClassifiedFaqs> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.ClassifiedFaqs>()
            com.slt.cardealership.presentation.ManageClassified.faqs.ClassifiedFaqsScreen(
                navController = navController,
                siteId = args.siteId
            )
        }

        composable<HomeRoutes.AddEditClassifiedFaq> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeRoutes.AddEditClassifiedFaq>()
            com.slt.cardealership.presentation.ManageClassified.faqs.AddEditFaqBottomSheet(
                siteId = args.siteId,
                faqId = args.faqId,
                onDismiss = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}


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
    object AddPhoto : HomeRoutes()

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
    data class EditPageScreen(val domainId: Int, val pageId: String) : HomeRoutes()

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
    data class ManageCategory(val domainId: Int, val domainName: String = "") : HomeRoutes()

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
