//package com.example.intern.screen.dash
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.outlined.AdsClick // Or Campaign
//import androidx.compose.material.icons.outlined.Article
//import androidx.compose.material.icons.outlined.Build // Used for Services
//import androidx.compose.material.icons.outlined.Group // Used for Users
//import androidx.compose.material.icons.outlined.Home
//import androidx.compose.material.icons.outlined.Info
//import androidx.compose.material.icons.outlined.Inventory
//import androidx.compose.material.icons.outlined.PhotoLibrary
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.NavigationBarItemDefaults
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.intern.R // Ensure you have R.drawable.img_dashboard_welcome_car
//import com.example.intern.ui.theme.BrandBlue // <-- IMPORTED
//import com.example.intern.ui.theme.BrandDarkBlue // <-- IMPORTED
//import com.example.intern.ui.theme.LightBackground // <-- IMPORTED
//import com.example.intern.ui.theme.LightCardBackground // <-- IMPORTED
//
//// --- Data Models (unchanged) ---
//data class Project(
//    val id: String,
//    val title: String,
//    val icon: ImageVector,
//    val description: String, // Added this line
//    val isDark: Boolean = false
//)
//
//data class BottomNavItem(
//    val label: String,
//    val icon: ImageVector,
//    val screen: String
//)
//
//// --- Dummy Data (unchanged) ---
//val dummyProjects = listOf(
//    Project("1", "Articles", Icons.Outlined.Article, "Latest industry news & blogs", isDark = true),
//    Project("2", "Photos", Icons.Outlined.PhotoLibrary, "Browse your media gallery"),
//    Project("3", "Ad Manager", Icons.Outlined.AdsClick, "Manage campaigns & ads"),
//    Project("4", "Inventory", Icons.Outlined.Inventory, "Track stock & products")
//)
//
//val bottomNavItems = listOf(
//    BottomNavItem("Home", Icons.Outlined.Home, "Home"),
//    BottomNavItem("Info", Icons.Outlined.Info, "InfoScreen"),
//    BottomNavItem("Services", Icons.Outlined.Build, "ServicesScreen"), // This is the button
//    BottomNavItem("Users", Icons.Outlined.Group, "UsersScreen")
//)
//
//// --- Main Screen Composable ---
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    onNavigate: (screen: String) -> Unit
//) {
//    var currentScreen by remember { mutableStateOf("Home") }
//    // val brandColor = Color(0xFF0A2540) // A dark blue from the image  <-- REMOVED
//
//    Scaffold(
//        topBar = { HomeTopBar() },
//        bottomBar = {
//            HomeBottomBar(
//                currentScreen = currentScreen,
//                onNavigate = { screen -> // 'screen' will be "Home", "InfoScreen", "ServicesScreen", etc.
//                    currentScreen = screen // This updates the local 'selected' state
//
//                    // --- THIS IS THE FIX ---
//                    // Now, we map the button's screen name to the name MainActivity expects
//                    // and call the main navigation function.
//                    val mainActivityScreen = when (screen) {
//                        "ServicesScreen" -> "ServiceScreen" // Map to "ServiceScreen"
//                        "UsersScreen"    -> "UserScreen"    // (I fixed this one for you too)
//                        else             -> screen          // "Home", "InfoScreen" pass through
//                    }
//
//                    onNavigate(mainActivityScreen) // This tells MainActivity to change the screen
//                }
//            )
//        },
//        // --- FLOATING ACTION BUTTON REMOVED ---
//        containerColor = Color.White
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(horizontal = 24.dp)
//        ) {
//            Spacer(modifier = Modifier.height(16.dp))
//            WelcomeHeader()
//            Spacer(modifier = Modifier.height(24.dp))
//            // --- SEARCH BAR REMOVED ---
//            WelcomeBanner() // Now takes brandColor for border
//            Spacer(modifier = Modifier.height(24.dp))
//            ProjectSection()
//            Spacer(modifier = Modifier.height(24.dp))
//        }
//    }
//}
//
//// --- Screen Components ---
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeTopBar() {
//    CenterAlignedTopAppBar(
//        title = {
//            Text("Home", fontWeight = FontWeight.Bold)
//        },
//        navigationIcon = {
//            IconButton(onClick = { /* TODO */ }) {
//                // --- CHANGED TO CUSTOM PNG ---
//                Icon(
//                    // Replace 'ic_custom_menu' with the actual name of your PNG file
//                    painter = painterResource(id = R.drawable.menu),
//                    contentDescription = "Menu",
//                    modifier = Modifier.size(24.dp), // Standard icon size
//                    tint = Color.Black // Tints the PNG black. Change to Color.Unspecified to keep original PNG colors.
//                )
//            }
//        },
//        actions = {
//            // --- PROFILE IMAGE (Kept from previous step) ---
//            Image(
//                painter = painterResource(id = R.drawable.file_searching_rafiki), // Your profile image
//                contentDescription = "Profile Image",
//                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
//                modifier = Modifier
//                    .padding(end = 16.dp)
//                    .size(40.dp)
//                    .clip(CircleShape)
//                    .clickable { /* TODO */ }
//            )
//        },
//        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//            containerColor = Color.White
//        )
//    )
//}
//@Composable
//fun WelcomeHeader() {
//    Column {
//        Text(
//            text = "Hi Jenifer!",
//            style = MaterialTheme.typography.headlineLarge,
//            fontWeight = FontWeight.Bold,
//            color = BrandDarkBlue // <-- UPDATED
//        )
//        Text(
//            text = "Good Morning",
//            style = MaterialTheme.typography.bodyLarge,
//            color = Color.Gray
//        )
//    }
//}
//
//// --- SearchBar composable removed from here ---
//
//@Composable
//fun WelcomeBanner() {
//    // val brandColor = Color(0xFF0A2540) // <-- REMOVED (no longer needed here)
//
//    Card(
//        shape = RoundedCornerShape(20.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(2.dp, BrandDarkBlue, RoundedCornerShape(20.dp)), // <-- UPDATED
//        colors = CardDefaults.cardColors(containerColor = LightBackground) // <-- UPDATED
//    ) {
//        Row(
//            modifier = Modifier.padding(start = 20.dp, end = 10.dp, top = 1.dp, bottom = 1.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Welcome!",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = "Lets schedule your projects",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//            // --- UPDATED IMAGE TO A CAR PLACEHOLDER ---
//            // Make sure you have a drawable named 'img_dashboard_welcome_car' in res/drawable
//            Image(
//                painter = painterResource(id = R.drawable.vehicle_sale_cuate), // Placeholder
//                contentDescription = "Schedule Projects with Car",
//                modifier = Modifier.size(150.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun ProjectSection() {
//    Column {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Manage",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold
//            )
//            TextButton(onClick = { /* TODO */ }) {
//                Text("view all", color = BrandBlue) // <-- UPDATED
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(2),
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            // Adjust height if needed, or make it not fixed by removing .height()
//            modifier = Modifier.height(400.dp)
//        ) {
//            items(dummyProjects) { project ->
//                ProjectCard(project = project)
//            }
//        }
//    }
//}
//
//@Composable
//fun ProjectCard(project: Project) {
//    val textColor = if (project.isDark) Color.White else Color.Black
//    val secondaryColor = if (project.isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
//    val cardColor = if (project.isDark) BrandDarkBlue else LightCardBackground // <-- UPDATED
//
//    Card(
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(containerColor = cardColor),
//        // --- ADDED ELEVATION FOR SHADOW ---
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Adjust dp for shadow intensity
//        modifier = Modifier.size(160.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(20.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Top Row: Main Icon and the small + icon
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Icon(
//                    imageVector = project.icon,
//                    contentDescription = project.title,
//                    tint = secondaryColor,
//                    modifier = Modifier.size(28.dp)
//                )
//                // Plus icon with circle border
//                Box(
//                    modifier = Modifier
//                        .size(24.dp)
//                        .clip(CircleShape)
//                        .border(1.dp, secondaryColor, CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Options",
//                        tint = secondaryColor,
//                        modifier = Modifier.size(16.dp)
//                    )
//                }
//            }
//
//            // Content Column: Title and Description
//            Column {
//                Text(
//                    text = project.title,
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.SemiBold,
//                    color = textColor
//                )
//                Text(
//                    text = project.description,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = secondaryColor,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun HomeBottomBar(
//    currentScreen: String,
//    onNavigate: (String) -> Unit
//) {
//    NavigationBar(
//        containerColor = Color.White,
//        tonalElevation = 8.dp
//    ) {
//        bottomNavItems.forEach { item ->
//            NavigationBarItem(
//                selected = (currentScreen == item.screen),
//                onClick = { onNavigate(item.screen) },
//                icon = {
//                    Icon(
//                        imageVector = item.icon,
//                        contentDescription = item.label
//                    )
//                },
//                label = { Text(item.label) },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = BrandDarkBlue, // <-- UPDATED
//                    selectedTextColor = BrandDarkBlue, // <-- UPDATED
//                    unselectedIconColor = Color.Gray,
//                    unselectedTextColor = Color.Gray
//                )
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, widthDp = 360)
//@Composable
//fun HomeScreenPreview() {
//    MaterialTheme {
//        HomeScreen(onNavigate = {})
//    }
//}