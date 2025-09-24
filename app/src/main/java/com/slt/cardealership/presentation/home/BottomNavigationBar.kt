//package com.slt.cardealership.presentation.home
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Dashboard
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.graphics.vector.ImageVector
//
//@Composable
//fun BottomNavigationBar() {
//    var selectedItem by remember { mutableStateOf(0) }
//    val items = listOf(
//        BottomNavItem("Dashboard", Icons.Default.Dashboard),
//        BottomNavItem("Profile", Icons.Default.Person),
//        BottomNavItem("Settings", Icons.Default.Settings)
//    )
//
//    NavigationBar {
//        items.forEachIndexed { index, item ->
//            NavigationBarItem(
//                icon = { Icon(item.icon, contentDescription = item.title) },
//                label = { Text(item.title) },
//                selected = selectedItem == index,
//                onClick = { selectedItem = index }
//            )
//        }
//    }
//}
//
////data class BottomNavItem(val title: String, val icon: ImageVector)
