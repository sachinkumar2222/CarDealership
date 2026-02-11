package com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings

import com.slt.cardealership.presentation.navigation.HomeRoutes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// Removed invalid import
import com.slt.cardealership.ui.theme.BrandBlue

@Composable
fun ApplicationSettingsScreen(domainId: Int, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppSettingsCard(
            title = "General Settings",
            description = "Manage general application configurations",
            icon = Icons.Default.Settings,
            onClick = { navController.navigate(HomeRoutes.GeneralSettings(domainId)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AppSettingsCard(
            title = "Contact Info",
            description = "Update contact details and address",
            icon = Icons.Default.ContactMail,
            onClick = { navController.navigate(HomeRoutes.ContactInfo(domainId)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AppSettingsCard(
            title = "Social Info",
            description = "Manage social media links",
            icon = Icons.Default.Share,
            onClick = { navController.navigate(HomeRoutes.SocialInfo(domainId)) }
        )
    }
}

@Composable
fun AppSettingsCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BrandBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
