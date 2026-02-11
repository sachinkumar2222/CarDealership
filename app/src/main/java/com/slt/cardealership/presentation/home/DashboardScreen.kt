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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.UserProfile
import com.slt.cardealership.presentation.auth.AuthViewModel
import com.slt.cardealership.presentation.common.FullScreenError
import com.slt.cardealership.presentation.info.InfoUiState
import com.slt.cardealership.presentation.navigation.HomeRoutes
import com.slt.cardealership.ui.theme.BrandBlue
import com.slt.cardealership.ui.theme.BrandDarkBlue
import com.slt.cardealership.ui.theme.LightBackground
import com.slt.cardealership.ui.theme.LightCardBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    authViewModel: AuthViewModel,
    infoUiState: InfoUiState,
    onRetry: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                onLogoutClick = { authViewModel.signOut() }
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = infoUiState) {
                is InfoUiState.Loading -> {
                    DashboardLoadingShimmer()
                }

                is InfoUiState.Error -> {
                    FullScreenError(
                        message = state.message,
                        onRetry = onRetry
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
                        userProfile = state.userProfile,
                        stats = stats,
                        navController = navController,
                        onSeeAllClick = onSeeAllClick
                    )
                }
            }
        }
    }
}

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

@Composable
fun WelcomeHeader(dealerInfo: DealerInfo, userProfile: UserProfile?) {
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
            .border(2.dp, BrandDarkBlue, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = LightBackground)
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
    val secondaryColor = if (isSelected) activeColor.copy(alpha = 0.6f) else Color.Gray
    val containerColor = if (isSelected) Color.White else LightCardBackground

    val cardContent = @Composable {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
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
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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

@Composable
fun DashboardContent(
    dealerInfo: DealerInfo,
    userProfile: UserProfile?,
    stats: DashboardStats,
    navController: NavController,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                WelcomeHeader(dealerInfo = dealerInfo, userProfile = userProfile)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            WelcomeBanner()
        }

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

        items(dummyProjects) { project ->
            ProjectCard(project = project, navController = navController)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(12.dp))
        }
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
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
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
                            "S",
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
fun DashboardLoadingShimmer(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier
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
