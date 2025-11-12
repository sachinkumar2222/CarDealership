package com.slt.cardealership.presentation.users

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
// --- FIX 1: Remove this incorrect import ---
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.slt.cardealership.R
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import com.slt.cardealership.domain.model.Department
import com.slt.cardealership.domain.model.Designation // <-- NEW IMPORT
import com.slt.cardealership.presentation.home.backgroundColor
import com.slt.cardealership.presentation.home.businessDarkBlue
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

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

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = surfaceColor,
        unfocusedContainerColor = surfaceColor,
        // ... (rest of colors)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add User", fontWeight = FontWeight.Bold) },
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
                onClick = {
                    // --- FIX 2: Pass all required parameters ---
                    viewModel.addUser(
                        firstName = firstName,
                        lastName = lastName,
                        username = email,
                        password = password,
                        phone = phone.takeIf { it.isNotBlank() },
                        imageUri = imageUri,
                        departmentId = selectedDepartment?.id,
                        designationId = selectedDesignation?.id // <-- Added
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Add User",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Profile Image Section ---
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri ?: R.drawable.load)
                        .error(R.drawable.load)
                        .build(),
                    contentDescription = "Profile Picture Placeholder",
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

            // --- Show Error Message ---
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- Row 1: First Name / Last Name ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    isError = uiState.error != null
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    isError = uiState.error != null
                )
            }

            // --- Email/Username ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email/Username *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                singleLine = true,
                isError = uiState.error != null
            )

            // --- Business Phone ---
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Business Phone") },
                placeholder = { Text("(000) 000-0000") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                singleLine = true
            )
            // --- Row 2: Role / Department ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Role (Not implemented yet) ---
                OutlinedTextField(
                    value = "", // TODO: Connect to Role state
                    onValueChange = { },
                    label = { Text("Role *") },
                    placeholder = { Text("Select Role") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    },
                    readOnly = true,
                    enabled = false // <-- Disabled until API is ready
                )

                // --- Department ---
                ExposedDropdownMenuBox(
                    expanded = isDepartmentMenuExpanded,
                    onExpandedChange = { isDepartmentMenuExpanded = !isDepartmentMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedDepartment?.name ?: "",
                        onValueChange = {},
                        label = { Text("Department *") },
                        placeholder = { Text("Select Department") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors,
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isDepartmentMenuExpanded
                            )
                        },
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

            // --- FIX 4: Implement Designation Dropdown ---
            ExposedDropdownMenuBox(
                expanded = isDesignationMenuExpanded,
                onExpandedChange = {
                    // Only allow expanding if a department is selected
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
                    placeholder = { Text("Select Designation") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    readOnly = true,
                    // Disable if no department is chosen or if loading
                    enabled = selectedDepartment != null && !uiState.isLoading,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = isDesignationMenuExpanded
                        )
                    },
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

            // --- Row 3: Password / Confirm Password ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = uiState.error != null
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = uiState.error != null
                )
            }
        }
    }
}