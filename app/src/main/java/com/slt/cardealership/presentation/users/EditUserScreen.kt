package com.slt.cardealership.presentation.users

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.presentation.home.FullScreenError
import com.slt.cardealership.presentation.home.backgroundColor
import com.slt.cardealership.presentation.home.businessDarkBlue
import com.slt.cardealership.presentation.home.surfaceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    // --- This screen MUST receive the userId from NavHost ---
    userId: Long,
    onBackClick: () -> Unit,
    onChangePasswordClick: (Long) -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    // --- Collect the DETAIL state ---
    val uiState by viewModel.userDetailState.collectAsState()
    val user = uiState.user

    // --- Tell the ViewModel to load the data for this user ID ---
    LaunchedEffect(key1 = userId) {
        viewModel.fetchUserDetails(userId)
    }

    // --- Clean up the state when leaving the screen ---
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearUserDetails()
        }
    }

    // --- States for all fields ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }
    var designation by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageCacheKey by remember { mutableStateOf<String?>(null) }

    // --- Populate states when user data loads ---
    LaunchedEffect(user) {
        if (user != null) {
            // --- FIX 2: Use camelCase to match your data model ---
            firstName = user.firstName ?: ""
            lastName = user.lastName ?: ""
            phone = user.phone ?: ""
            role = user.roleName ?: "N/A"
            status = if (user.isActive) "Active" else "Inactive"
            designation = user.designationName ?: "N/A"
            department = user.departmentName ?: "N/A"
            username = user.username ?: "N/A"
            imageUrl = user.imageUrl ?: ""
            selectedImageUri = null
            imageCacheKey = user.updatedOn.toString()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri // Set the new URI state
        }
    )

    // --- Full-screen state handling ---
    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.detailError != null -> {
                FullScreenError(
                    errorMessage = uiState.detailError!!,
                    // --- FIX 3: Pass the userId to the retry function ---
                    onTryAgain = { viewModel.fetchUserDetails(userId) }
                )
            }
            user != null -> {
                // --- On success, show the content ---
                EditUserScreenContent(
                    user = user,
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    role = role,
                    onRoleChange = { role = it },
                    status = status,
                    onStatusChange = { status = it },
                    designation = designation,
                    department = department,
                    username = username,
                    imageUrl = imageUrl,
                    imageUri = selectedImageUri,
                    isSaving = uiState.isSaving, // Pass saving state
                    onBackClick = onBackClick,
                    onUpdateClick = {
                        viewModel.saveUserChanges(
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            status = status,
                            imageUri = selectedImageUri
                        )
                    },
                    onChangePasswordClick = { onChangePasswordClick(user.id) },
                    onImageClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    imageCacheKey = imageCacheKey
                )
            }
        }
    }
}

// --- This is your UI, now as a separate composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreenContent(
    user: DetailedUserProfile,
    firstName: String, onFirstNameChange: (String) -> Unit,
    lastName: String, onLastNameChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    role: String, onRoleChange: (String) -> Unit,
    status: String, onStatusChange: (String) -> Unit,
    designation: String,
    department: String,
    username: String,
    imageUri: Uri?,
    imageUrl: String,
    isSaving: Boolean, // Added isSaving
    onBackClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onImageClick: () -> Unit,
    imageCacheKey: String?
) {

    var isStatusMenuExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Active", "Inactive")
    // --- TextField Style (Unchanged) ---
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = surfaceColor,
        unfocusedContainerColor = surfaceColor,
        disabledContainerColor = surfaceColor,
        focusedIndicatorColor = businessDarkBlue,
        unfocusedIndicatorColor = Color.LightGray,
        disabledIndicatorColor = Color.LightGray,
        focusedLabelColor = businessDarkBlue,
        unfocusedLabelColor = Color.Gray,
        disabledLabelColor = Color.Gray,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit User", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        bottomBar = {
            Button(
                onClick = onUpdateClick,
                enabled = !isSaving, // Disable button when saving
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor) // Match scaffold bg
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Update",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        containerColor = surfaceColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Profile Image Section ---
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { onImageClick() }
            ) {
                Log.d("AsyncImage", "Image URL: $imageUrl")
                Log.d("AsyncImage", "Image URI: $imageUri")
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        // --- 9. SHOW NEW URI OR FALL BACK TO SERVER URL ---
                        .data(imageUri ?: imageUrl)
                        .error(R.drawable.file_searching_rafiki)
                        .crossfade(true)
                        .diskCacheKey(imageCacheKey)
                        .memoryCacheKey(imageCacheKey)
                        .build(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit Profile Image",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- User Name and Handle ---
            Text(
                text = "$firstName $lastName".trim(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // --- Info Rows ---
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoItem(
                icon = Icons.Outlined.Work,
                text = "Designation: $designation"
            )
            ProfileInfoItem(
                icon = Icons.Outlined.Work,
                text = "Department: $department"
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            ProfileMenuItem(
                icon = Icons.Outlined.Key,
                text = "Change Password",
                onClick = onChangePasswordClick
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "User Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Row 1: First Name / Last Name (Half Width) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Row 2: Phone / Role (Half Width) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Business Phone") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = onRoleChange,
                    label = { Text("Role") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    enabled = false // Role is usually not editable
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Status (Full Width) ---
            ExposedDropdownMenuBox(
                expanded = isStatusMenuExpanded,
                onExpandedChange = { isStatusMenuExpanded = !isStatusMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {}, // ReadOnly, so no action here
                    label = { Text("Status") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(), // This is the anchor for the menu
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    readOnly = true, // Still readOnly, click is handled by the box
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = isStatusMenuExpanded
                        )
                    }
                )
                // This is the actual menu that pops up
                ExposedDropdownMenu(
                    expanded = isStatusMenuExpanded,
                    onDismissRequest = { isStatusMenuExpanded = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onStatusChange(option) // Update the state
                                isStatusMenuExpanded = false // Close the menu
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- This composable is for non-clickable info items ---
@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    text: String
) {
    val contentColor = Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // <-- REDUCED PADDING
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
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

// --- This composable is for clickable menu items ---
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    val contentColor = Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp), // <-- REDUCED PADDING
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
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go to $text",
            tint = Color.Gray
        )
    }
}