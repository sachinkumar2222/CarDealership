package com.slt.cardealership.presentation.home

// Add these imports at the top of your file
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
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
import coil3.compose.AsyncImage
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.presentation.ads.AddAdsScreen
import com.slt.cardealership.presentation.ads.AdsScreen
import com.slt.cardealership.presentation.articles.AddEditArticleScreen
import com.slt.cardealership.presentation.articles.ArticleScreen
import com.slt.cardealership.presentation.auth.AuthViewModel
import com.slt.cardealership.presentation.faq.AddFaqScreen
import com.slt.cardealership.presentation.faq.FaqScreen
import com.slt.cardealership.presentation.info.InfoScreen
import com.slt.cardealership.presentation.inventory.AddVehicleScreen
import com.slt.cardealership.presentation.inventory.InventoryScreen
import com.slt.cardealership.presentation.photos.PhotoScreen
import com.slt.cardealership.presentation.profile.ProfileScreen
import com.slt.cardealership.presentation.seo.AddSeoScreen
import com.slt.cardealership.presentation.seo.SeoScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
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
    object SeoScreen : HomeRoutes()

    @Serializable
    object AddSeoScreen : HomeRoutes()

    @Serializable
    object FaqScreen : HomeRoutes()

    @Serializable
    object AddFaqScreen : HomeRoutes()

    @Serializable
    data class AddEditArticle(val articleId: String? = null) : HomeRoutes()
}

data class DashboardItem(val title: String, val icon: Painter, val route: HomeRoutes)
data class NavDrawerItem(val title: String, val icon: ImageVector)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: HomeRoutes)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
    val scope = rememberCoroutineScope()
    val homeNavController = rememberNavController() // For nested navigation
    val homeViewModel: HomeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    //val articleViewModel: ArticleViewModel = hiltViewModel()


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
                AddEditArticleScreen(
                    navController = homeNavController,
                    onNavigateBack = { homeNavController.popBackStack() })
            }
            composable<HomeRoutes.Profile> {
                ProfileScreen(
                    navController = mainNavController,
                    onSignOutClick = { authViewModel.signOut() }
                )
            }

            composable<HomeRoutes.Photos> { PhotoScreen(navController = homeNavController) }

            composable<HomeRoutes.Inventory> { InventoryScreen(navController = homeNavController) }

            composable<HomeRoutes.AddVehicleScreen> {
                AddVehicleScreen(navController = homeNavController)
            }

            composable<HomeRoutes.Ads> {AdsScreen(navController = homeNavController)}
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
        }
    }
}


@Composable
fun DashboardContent(
    navController: NavController,
    homeViewModel: HomeViewModel? = if (!LocalInspectionMode.current) hiltViewModel() else null
) {
    val homeState by homeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(
            HomeUiState.Success(emptyList())
        )
    }
    val dashboardItems = listOf(
        DashboardItem("Info", painterResource(R.drawable.info), HomeRoutes.Info),
        DashboardItem("Articles", painterResource(R.drawable.newspaper), HomeRoutes.Articles),
        DashboardItem("Photos", painterResource(R.drawable.picture), HomeRoutes.Photos),
        DashboardItem("Inventory", painterResource(R.drawable.inventory), HomeRoutes.Inventory),
        DashboardItem(
            "Ad Manager",
            painterResource(R.drawable.ads_manage),
            HomeRoutes.Ads
        ),
        DashboardItem(
            "Seo Manager",
            painterResource(R.drawable.chart_histogram),
            HomeRoutes.SeoScreen
        ),
        DashboardItem(
            "Faq",
            painterResource(R.drawable.site_browser),
            HomeRoutes.FaqScreen
        )
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
                    LatestPostShimmerCard()
                }

                is HomeUiState.Error -> {
                    Text(state.message, modifier = Modifier.padding(16.dp))
                }

                is HomeUiState.Success -> {
                    // If a post exists, show the card for it
//                    state.latestPost?.let { post ->
//                        LatestPostCard(post = post)
//                    }
                    if (state.latestPosts.isNotEmpty()) {
                        // Call the new slideshow composable
                        LatestArticlesSlideshow(posts = state.latestPosts)
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
fun LatestPostCard(post: Post, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
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

@Composable
fun LatestPostShimmerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer() // <-- APPLY SHIMMER
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Title Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer() // <-- APPLY SHIMMER
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Placeholders
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer() // <-- APPLY SHIMMER
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer() // <-- APPLY SHIMMER
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Text Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer() // <-- APPLY SHIMMER
            )
        }
    }
}


@Composable
fun WelcomeCard() {

    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
    )

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
                .background(
                    // Color(0xFF000000)
                    blueGradient
                )
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
fun AnimatedBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, HomeRoutes.Dashboard),
        BottomNavItem("Info", Icons.Outlined.Info, HomeRoutes.Info),
        BottomNavItem("Photos", Icons.Outlined.Photo, HomeRoutes.Photos),
        BottomNavItem(
            "Profile", Icons.Outlined.Person, HomeRoutes.Profile
        )
    )
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
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
            .shadow(elevation = 0.dp, shape = bottomBarShape)
            .background(blueGradient, shape = bottomBarShape),
        color = Color.Transparent,
        shape = bottomBarShape
    ) {
        BoxWithConstraints(
            modifier = Modifier.height(80.dp)
        ) {
            val itemWidth = maxWidth / items.size
            val indicatorOffset by animateDpAsState(
                targetValue = (itemWidth * selectedIndex) + (itemWidth / 2) - 18.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "indicatorOffset"
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorOffset)
                    .width(36.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF042A2B) else Color(0xFFDADADA),
                        label = "iconColor"
                    )

                    val iconSize by animateDpAsState(
                        targetValue = if (isSelected) 34.dp else 26.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "iconSize"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
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
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
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
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LatestArticlesSlideshow(posts: List<Post>) {
    // Return early if the list is empty to prevent crashes
    if (posts.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState(pageCount = { posts.size })

    // This effect creates a timer that runs as long as the slideshow is on screen
    LaunchedEffect(Unit) {
        while (true) {
            // Wait for 5 seconds before scrolling
            delay(3000)

            // This is a good practice for smooth animations
            yield()

            // Calculate the next page index, looping back to the start
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount

            // Trigger the smooth scroll to the next page
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            LatestPostCard(
                post = posts[page],
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The indicator dots remain the same
        Row(
            Modifier.height(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(posts.size) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLayoutPreview() {
    Scaffold(
        topBar = { TopBar(onMenuClick = {}, onLogoutClick = {}) },
        bottomBar = { AnimatedBottomBar(navController = rememberNavController()) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DashboardContent(navController = rememberNavController())
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimatedBottomBarPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedBottomBar(navController = rememberNavController())
    }
}



