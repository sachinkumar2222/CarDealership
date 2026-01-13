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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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

    // --- Success Listener ---
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onBackClick()
        }
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

    // --- Password Change State ---
    var showPasswordSheet by remember { mutableStateOf(false) }
    val passwordState by viewModel.changePasswordState.collectAsState()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- Success Listener for Password ---
    LaunchedEffect(passwordState.isSuccess) {
        if (passwordState.isSuccess) {
            showPasswordSheet = false
            viewModel.clearChangePasswordState()
            // Optional: Show a snackbar or toast here
        }
    }

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
                    isSaving = uiState.isSaving,
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
                    // --- Change: Open Sheet instead of Navigate ---
                    onChangePasswordClick = { showPasswordSheet = true },
                    onImageClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    imageCacheKey = imageCacheKey
                )

                // --- Password Change Bottom Sheet ---
                if (showPasswordSheet) {
                    androidx.compose.material3.ModalBottomSheet(
                        onDismissRequest = {
                            showPasswordSheet = false
                            viewModel.clearChangePasswordState()
                        },
                        sheetState = sheetState,
                        containerColor = Color.White
                    ) {
                        ChangePasswordSheetContent(
                            isLoading = passwordState.isLoading,
                            error = passwordState.error,
                            onSave = { p1, p2 -> viewModel.changePassword(user.id, p1, p2) },
                            onCancel = { showPasswordSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordSheetContent(
    isLoading: Boolean,
    error: String?,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val brandBlue = Color(0xFF2196F3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 20.dp), // Add extra padding for navigation bar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Change Password",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = brandBlue,
                cursorColor = brandBlue
            )
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = brandBlue,
                cursorColor = brandBlue
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSave(password, confirmPassword) },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Update Password", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- This is your UI, now as a separate composable ---
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
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onImageClick: () -> Unit,
    imageCacheKey: String?
) {
    var isStatusMenuExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Active", "Inactive")

    // --- Animation State ---
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val brandBlue = Color(0xFF2196F3)

    // --- TextField Style ---
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color(0xFFF5F5F5),
        focusedIndicatorColor = brandBlue,
        unfocusedIndicatorColor = Color(0xFFE0E0E0),
        disabledIndicatorColor = Color.Transparent,
        focusedLabelColor = brandBlue,
        unfocusedLabelColor = Color.DarkGray,
        disabledLabelColor = Color.DarkGray,
        cursorColor = brandBlue
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit User", color = Color.Black, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },

        containerColor = Color(0xFFF5F7FA) // Light grey background
    ) { paddingValues ->
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.slideInVertically { it / 2 } + androidx.compose.animation.fadeIn(),
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Header & Profile Image Section ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile Image
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 20.dp)
                            .clickable { onImageClick() }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
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
                                .background(Color.White)
                                .border(4.dp, Color.White, CircleShape) // Thick white border for separation
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(brandBlue)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- User Info Summary ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$firstName $lastName".trim().ifBlank { "New User" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Form Content ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Section: Personal Details
                    FormSection(title = "Personal Details") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = onFirstNameChange,
                                label = { Text("First Name") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = onLastNameChange,
                                label = { Text("Last Name") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = phone,
                            onValueChange = onPhoneChange,
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )
                    }

                    // Section: Work
                    FormSection(title = "Work Information") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Read-only fields styling
                            OutlinedTextField(
                                value = department,
                                onValueChange = {},
                                label = { Text("Department") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                enabled = false,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = designation,
                                onValueChange = {},
                                label = { Text("Designation") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                enabled = false,
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = role,
                            onValueChange = {},
                            label = { Text("Role") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            enabled = false,
                            singleLine = true
                        )
                    }

                    // Section: Account
                    FormSection(title = "Account Settings") {
                        ExposedDropdownMenuBox(
                            expanded = isStatusMenuExpanded,
                            onExpandedChange = { isStatusMenuExpanded = !isStatusMenuExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                label = { Text("Account Status") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusMenuExpanded)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = isStatusMenuExpanded,
                                onDismissRequest = { isStatusMenuExpanded = false }
                            ) {
                                statusOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            onStatusChange(option)
                                            isStatusMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Change Password Tile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                .clickable { onChangePasswordClick() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(brandBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Key,
                                        contentDescription = null,
                                        tint = brandBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Change Password",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }

                    // Bottom Padding for scroll
                    // Bottom Padding before button
                    Spacer(modifier = Modifier.height(30.dp))

                    // Floating Button (Now Scrollable)
                    Button(
                        onClick = {
                            if (!isSaving) {
                                onUpdateClick()
                            }
                        },
                        // Keep enabled to preserve style, handle click above
                        enabled = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = brandBlue.copy(alpha = 0.5f) // Colored shadow
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brandBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black // Brand Blue
        )
        content()
    }
}