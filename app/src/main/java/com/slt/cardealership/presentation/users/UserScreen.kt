package com.slt.cardealership.presentation.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.domain.model.ManageUsers
import com.slt.cardealership.presentation.common.FullScreenError
import com.slt.cardealership.presentation.home.shimmer
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.slt.cardealership.presentation.home.businessTextDark

// --- REMOVED: UserItem and dummyUserList ---

// --- Defined in HomeScreen.kt, just re-using here ---
val businessDarkBlue = Color(0xFF233E66)
val backgroundColor = Color(0xFFF5F7FA)
val surfaceColor = Color.White
// ---

val avatarBlue = Color(0xFFCBEAFB)
val textBlue = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    onBackClick: () -> Unit,
    onAddUserClick: () -> Unit,
    onManageUserClick: (ManageUsers) -> Unit, // <-- Use ManageUsers
    onEditUserClick: (ManageUsers) -> Unit,   // <-- Use ManageUsers
    viewModel: UserViewModel = hiltViewModel(), // <-- Inject ViewModel
) {
    // --- Collect state from ViewModel ---
    val uiState by viewModel.userListState.collectAsState()
    val currentUserUsername by viewModel.currentUserUsername.collectAsState()


    // --- Local state for the screen ---
    val state = uiState // To make access easier

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("Users", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // <-- Use lambda
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor // Use theme color
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUserClick,
                containerColor = Color(0xFF2196F3), // Solid Blue
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add new user",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = backgroundColor // Use theme color
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // --- Handle Loading, Error, Success States ---
            when {
                // --- 1. Loading State ---
                state.isLoading -> {
                    UserListShimmer() // Show shimmer placeholders
                }

                // --- 2. Error State ---
                state.error != null -> {
                    FullScreenError(
                        message = state.error,
                        onRetry = { viewModel.loadUsers() }
                    )
                }

                // --- 3. Success (and Empty) State ---
                else -> {
                    UserListContent(
                        users = state.users,
                        totalUsers = state.totalUsers,
                        currentUserUsername = currentUserUsername,
                        canLoadMore = state.canLoadMore,
                        isLoadingMore = state.isLoadingMore,
                        onLoadMore = { viewModel.loadMoreUsers() },
                        onManageClick = onManageUserClick,
                        onEditClick = onEditUserClick
                    )
                }
            }
        }
    }
}

@Composable
fun UserListContent(
    users: List<ManageUsers>,
    totalUsers: Int,
    currentUserUsername: String?,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onManageClick: (ManageUsers) -> Unit,
    onEditClick: (ManageUsers) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // --- Top Header Info ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                // Use totalUsers from state for pagination
                text = "Users ( $totalUsers )",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = businessTextDark
            )
            Text(
                text = "Manage users from here",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- User List ---
        if (users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No users found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    val isCurrentUser = (currentUserUsername != null) &&
                            (user.username == currentUserUsername)

                    // Simple slide-in animation wrapper could go here,
                    // but for now we focus on the Premium Card design
                    UserCard(
                        user = user,
                        isCurrentUser = isCurrentUser,
                        onManageClick = { onManageClick(user) },
                        onEditClick = { onEditClick(user) }
                    )
                }

                // --- Pagination Item ---
                item {
                    if (isLoadingMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2196F3))
                        }
                    } else if (canLoadMore) {
                        OutlinedButton(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Load More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: ManageUsers,
    isCurrentUser: Boolean,
    onManageClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // More rounded
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Higher elevation
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min) // For the vertical strip
        ) {
            // --- Blue Accent Strip ---
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(Color(0xFF2196F3))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // --- User Info ---
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(50.dp) // Slightly larger
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE3F2FD)), // Lighter blue bg
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.image_url.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.image_url,
                                    contentDescription = "${user.first_name} Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    // Use first letter of first_name
                                    text = user.first_name?.firstOrNull()?.toString()?.uppercase() ?: "U",
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                // Combine first_name and last_name
                                text = "${user.first_name ?: ""} ${user.last_name ?: ""}".trim(),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), // Larger
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.username, // username is the email
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // --- Role and Actions ---
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = user.role_name ?: "N/A",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isCurrentUser) {
                            OutlinedButton(
                                onClick = onManageClick,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF2196F3)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Manage")
                            }
                        } else {
                            // Replaced simple icon with a small styled IconButton
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F7FA))
                                    .clickable { onEditClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit User",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Shimmer Composables for Loading State ---

@Composable
fun UserListShimmer() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        userScrollEnabled = false
    ) {
        // Shimmer for header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Shimmer for list items
        items(5) {
            UserCardShimmer()
        }
    }
}

@Composable
fun UserCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
        }
    }
}