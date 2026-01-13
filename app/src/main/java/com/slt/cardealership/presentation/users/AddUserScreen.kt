package com.slt.cardealership.presentation.users

// --- FIX 1: Remove this incorrect import ---
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Department
import com.slt.cardealership.domain.model.Designation
import com.slt.cardealership.presentation.home.surfaceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    onBackClick: () -> Unit,
    onAddUserSuccess: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.addUserUiState.collectAsState()

    // --- States for all fields ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- State for Dropdowns ---
    var isDepartmentMenuExpanded by remember { mutableStateOf(false) }
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }

    var isDesignationMenuExpanded by remember { mutableStateOf(false) }
    var selectedDesignation by remember { mutableStateOf<Designation?>(null) }

    // --- Animations ---
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    // --- Role State (Hardcoded for now as per request) ---
    var isRoleMenuExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("") } // Default to empty
    val roles = listOf("dl-admin")

    // Load departments when screen launches
    LaunchedEffect(Unit) {
        viewModel.loadAddUserFormDependencies()
    }

    // When department changes, clear designation and load new ones
    LaunchedEffect(selectedDepartment) {
        selectedDesignation = null // Reset designation
        if (selectedDepartment != null) {
            viewModel.loadDesignations(selectedDepartment!!.id)
        }
    }

    // Handle navigation on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onAddUserSuccess()
        }
    }

    // Clean up state when screen is left
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAddUserState()
        }
    }

    val brandBlue = Color(0xFF2196F3)

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
                title = { Text("Add User", color = Color.Black, fontWeight = FontWeight.Bold) },
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
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri ?: R.drawable.file_searching_rafiki)
                                .error(R.drawable.file_searching_rafiki)
                                .build(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, Color.White, CircleShape)
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
                                contentDescription = "Edit Profile Image",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // --- Error Message ---
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

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
                                onValueChange = { firstName = it },
                                label = { Text("First Name *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                singleLine = true,
                                isError = uiState.error != null
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Last Name *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                singleLine = true,
                                isError = uiState.error != null
                            )
                        }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email/Username *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            singleLine = true,
                            isError = uiState.error != null
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Business Phone") },
                            placeholder = { Text("(000) 000-0000") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )
                    }

                    // Section: Work Information
                    FormSection(title = "Work Information") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Role (Small space - Adjusted to prevent wrapping)
                            ExposedDropdownMenuBox(
                                expanded = isRoleMenuExpanded,
                                onExpandedChange = { isRoleMenuExpanded = !isRoleMenuExpanded },
                                modifier = Modifier.weight(0.4f)
                            ) {
                                OutlinedTextField(
                                    value = selectedRole,
                                    onValueChange = {},
                                    label = { Text("Role *") },
                                    placeholder = { Text("Select") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = textFieldColors,
                                    readOnly = true,
                                    singleLine = true, // Prevent wrapping
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoleMenuExpanded) },
                                    isError = uiState.error != null
                                )
                                ExposedDropdownMenu(
                                    expanded = isRoleMenuExpanded,
                                    onDismissRequest = { isRoleMenuExpanded = false }
                                ) {
                                    roles.forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role) },
                                            onClick = {
                                                selectedRole = role
                                                isRoleMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Department (Large space)
                            ExposedDropdownMenuBox(
                                expanded = isDepartmentMenuExpanded,
                                onExpandedChange = { isDepartmentMenuExpanded = !isDepartmentMenuExpanded },
                                modifier = Modifier.weight(0.6f)
                            ) {
                                OutlinedTextField(
                                    value = selectedDepartment?.name ?: "",
                                    onValueChange = {},
                                    label = { Text("Department *") },
                                    placeholder = { Text("Select") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = textFieldColors,
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDepartmentMenuExpanded) },
                                    isError = uiState.error != null
                                )
                                ExposedDropdownMenu(
                                    expanded = isDepartmentMenuExpanded,
                                    onDismissRequest = { isDepartmentMenuExpanded = false }
                                ) {
                                    uiState.departments.forEach { department ->
                                        DropdownMenuItem(
                                            text = { Text(department.name) },
                                            onClick = {
                                                selectedDepartment = department
                                                isDepartmentMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Designation (Next Line, Full Width)
                        ExposedDropdownMenuBox(
                            expanded = isDesignationMenuExpanded,
                            onExpandedChange = {
                                if (selectedDepartment != null && !uiState.isLoading) {
                                    isDesignationMenuExpanded = !isDesignationMenuExpanded
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedDesignation?.name ?: "",
                                onValueChange = { },
                                label = { Text("Designation *") },
                                placeholder = { Text("Select") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                readOnly = true,
                                enabled = selectedDepartment != null && !uiState.isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDesignationMenuExpanded) },
                                isError = uiState.error != null
                            )
                            ExposedDropdownMenu(
                                expanded = isDesignationMenuExpanded,
                                onDismissRequest = { isDesignationMenuExpanded = false }
                            ) {
                                uiState.designations.forEach { designation ->
                                    DropdownMenuItem(
                                        text = { Text(designation.name) },
                                        onClick = {
                                            selectedDesignation = designation
                                            isDesignationMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Section: Account Setup
                    FormSection(title = "Account Setup") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                isError = uiState.error != null
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                isError = uiState.error != null
                            )
                        }
                    }

                    // Bottom Spacer, reduced slightly as button is now here
                    Spacer(modifier = Modifier.height(30.dp))

                    // Floating Button (Now Scrollable)
                    Button(
                        onClick = {
                            if (!uiState.isLoading) {
                                viewModel.addUser(
                                    firstName = firstName,
                                    lastName = lastName,
                                    username = email,
                                    password = password,
                                    phone = phone.takeIf { it.isNotBlank() },
                                    imageUri = imageUri,
                                    departmentId = selectedDepartment?.id,
                                    designationId = selectedDesignation?.id
                                )
                            }
                        },
                        // Keep enabled to preserve style
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = brandBlue.copy(alpha = 0.5f)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brandBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "Create User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp)) // Extra bottom padding
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