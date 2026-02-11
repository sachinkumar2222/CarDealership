package com.slt.cardealership.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.presentation.navigation.HomeRoutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class DrawerItem(val title: String, val icon: ImageVector, val route: HomeRoutes)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentRoute: String?,
    dealerInfo: DealerInfo?
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
    val businessTextDark = Color(0xFF233E66) // Define locally or import if common

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
