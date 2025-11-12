package com.slt.cardealership.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage // For loading profile images from URL
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DetailedUserProfile // Import the DetailedUserProfile
import com.slt.cardealership.presentation.info.FullScreenError // Re-use
import com.slt.cardealership.presentation.info.LoadingAnimation // Re-use
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.CameraAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(), // Inject ViewModel
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditProfileClick: (userId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            // --- 4. Call the ViewModel's new function ---
            viewModel.onImageSelected(uri)
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        // Handle loading, error, and success states
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingAnimation() // Your existing loading animation
                }
            }
            is ProfileUiState.Error -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FullScreenError(
                        errorMessage = state.message,
                        onTryAgain = { viewModel.fetchFullUserProfile() }
                    )
                }
            }
            is ProfileUiState.Success -> {
                UserProfileContent(
                    userProfile = state.userProfile,
                    onEditProfileClick = onEditProfileClick,
                    onSignOutClick = onSignOutClick, // Pass the sign out click handler
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    onImageClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            else -> {}
        }
    }
}

@Composable
fun UserProfileContent(
    userProfile: DetailedUserProfile,
    onEditProfileClick: (userId: Long) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    onImageClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { onImageClick() } // <-- 3. APPLY CLICKABLE
        ) {
            AsyncImage(
                model = userProfile.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.ad_goal1,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray) // Add a placeholder background
            )
            // --- 4. ADD THE CAMERA ICON OVERLAY ---
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt, // <-- Make sure to import this
                    contentDescription = "Edit Profile Image",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- User Name ---
        Text(
            text = "${userProfile.firstName} ${userProfile.lastName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // --- User Handle ---
        Text(
            text = userProfile.username,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Gradient Definition ---
        val blueGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF2196F3),
                Color(0xFF1565C0)
            )
        )

        // --- Edit Profile Button ---
        Button(
            onClick = { onEditProfileClick(userProfile.id) },
            modifier = Modifier
                .fillMaxWidth()
                .background(blueGradient, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // Makes the gradient visible
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Edit Profile",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Info Items (Designation, Department) ---
        userProfile.designationName?.takeIf { it.isNotBlank() }?.let {
            ProfileInfoItem(
                icon = Icons.Outlined.Work,
                text = "Designation: $it"
            )
        }
        userProfile.departmentName?.takeIf { it.isNotBlank() }?.let {
            ProfileInfoItem(
                icon = Icons.Outlined.Work,
                text = "Department: $it"
            )
        }


        // --- Change Password ---
        ProfileMenuItem(
            icon = Icons.Outlined.Key,
            text = "Change Password",
            onClick = { /* TODO: Handle Password Click, e.g., navigate to ChangePasswordScreen */ }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

        ProfileMenuItem(
            icon = Icons.Default.HelpOutline,
            text = "Help & Support",
            onClick = { /* TODO: Handle Help Click, e.g., navigate to HelpScreen */ }
        )
        ProfileMenuItem(
            icon = Icons.Default.ExitToApp,
            text = "Log out",
            isLogout = true,
            onClick = onSignOutClick // Use the provided signOutClick handler
        )
    }
}

// --- ProfileInfoItem (unchanged) ---
@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    text: String
) {
    val contentColor = Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

// --- ProfileMenuItem (unchanged) ---
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isLogout) Color.Red else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
        if (!isLogout) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go to $text",
                tint = Color.Gray
            )
        }
    }
}


