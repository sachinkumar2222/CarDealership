package com.slt.cardealership.presentation.home

import com.slt.cardealership.presentation.common.LoadingAnimation
import com.slt.cardealership.presentation.common.FullScreenError
import com.slt.cardealership.presentation.common.NavigationDrawerContent
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.PhotoLibrary
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.presentation.auth.AuthViewModel
import com.slt.cardealership.presentation.info.InfoUiState
import com.slt.cardealership.presentation.info.InfoViewModel
import com.slt.cardealership.ui.theme.BrandBlue
import com.slt.cardealership.ui.theme.BrandDarkBlue
import com.slt.cardealership.ui.theme.LightBackground
import com.slt.cardealership.ui.theme.LightCardBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.slt.cardealership.presentation.navigation.HomeNavGraph
import com.slt.cardealership.presentation.navigation.HomeRoutes



// HomeRoutes moved to com.slt.cardealership.presentation.navigation.HomeRoutes

// --- NEW Data class for the stats grid ---
// Dashboard components moved to DashboardScreen.kt


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
            topBar = { /* TopBar moved to DashboardScreen */ },
            // --- BOTTOM BAR IS BACK ---
            bottomBar = { ProfessionalBottomBar(navController = homeNavController) },
            modifier = Modifier.statusBarsPadding(),
            containerColor = backgroundColor
        ) { paddingValues ->
            HomeNavGraph(
                navController = homeNavController,
                mainNavController = mainNavController,
                paddingValues = paddingValues,
                authViewModel = authViewModel,
                infoviewModel = infoviewModel,
                uiState = uiState,
                drawerState = drawerState,
                scope = scope
            )
        }
    }
}





// NavigationDrawerContent moved to com.slt.cardealership.presentation.common.NavigationDrawerContent



// --- NEW: A helper composable for section titles ---


// --- NEW: Card for the 2x2 Stats Grid ---



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
            topBar = { },
        bottomBar = { ProfessionalBottomBar(navController = rememberNavController()) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // DashboardLoadingShimmer() // Moved to DashboardScreen
        }
    }
}
