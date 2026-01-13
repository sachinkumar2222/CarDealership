package com.slt.cardealership.presentation.websitedashboard.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.slt.cardealership.domain.model.DomainPageCreateRequest
import com.slt.cardealership.domain.model.DomainSlider
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPageScreen(
    navController: NavController,
    domainId: Int,
    viewModel: WebsitePagesViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Form State
    var pageName by remember { mutableStateOf("") }
    var pageContent by remember { mutableStateOf("") }
    var pageType by remember { mutableStateOf("Standard") } // Default
    var status by remember { mutableStateOf("Active") } // Default
    var selectedTemplate by remember { mutableStateOf("Default") }
    var selectedSliderId by remember { mutableStateOf<String?>(null) }

    // SEO State
    var pageH1 by remember { mutableStateOf("") }
    var metaTitle by remember { mutableStateOf("") }
    var metaDescription by remember { mutableStateOf("") }
    var robotsMetaTag by remember { mutableStateOf("index, follow") }

    // Scripts State
    var headerScript by remember { mutableStateOf("") }
    var footerScript by remember { mutableStateOf("") }

    // Images State
    var featuredImageUri by remember { mutableStateOf<Uri?>(null) }
    var heroBannerImageUri by remember { mutableStateOf<Uri?>(null) }
    var bannerType by remember { mutableStateOf("Image") }

    // Dropdown Expanded States
    var pageTypeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }
    var sliderExpanded by remember { mutableStateOf(false) }
    var bannerTypeExpanded by remember { mutableStateOf(false) }
    var robotsExpanded by remember { mutableStateOf(false) }

    // Data from ViewModel
    val sliders by viewModel.sliders.collectAsState()
    val isAddingPage by viewModel.isAddingPage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is WebsitePagesEvent.ShowSuccess -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_pages", true)
                    navController.popBackStack()
                }
                is WebsitePagesEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(domainId) {
        viewModel.fetchSliders(domainId.toString())
    }

    // Image Pickers
    val featuredImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        featuredImageUri = uri
    }

    val heroBannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        heroBannerImageUri = uri
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Page") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Section
            SectionCard(title = "General Information") {
                OutlinedTextField(
                    value = pageName,
                    onValueChange = { pageName = it },
                    label = { Text("Page Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pageContent,
                    onValueChange = { pageContent = it },
                    label = { Text("Page Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
                Text(
                    text = "Note: Rich Text Editor is not supported in this mobile view. Please use HTML or plain text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action Section
            SectionCard(title = "Action") {
                // Page Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = pageTypeExpanded,
                    onExpandedChange = { pageTypeExpanded = !pageTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = pageType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Page Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pageTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = pageTypeExpanded,
                        onDismissRequest = { pageTypeExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Standard", "Landing Page", "Blog Post").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    pageType = type
                                    pageTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Active", "Inactive", "Draft").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    status = item
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Handle Publish
                        val currentTime = System.currentTimeMillis()
                        val request = DomainPageCreateRequest(
                            sqlDomainId = domainId,
                            pageName = pageName,
                            pageDescription = pageContent, // Mapping content to description as per model
                            pageTitle = pageH1,
                            metaTitle = metaTitle,
                            metaDescription = metaDescription,
                            robotsMetaTags = robotsMetaTag,
                            headerScript = headerScript,
                            footerScript = footerScript,
                            pageTypeId = "67bda2a9d84947fd1e3e10eb", // Default to "General" page type ID
                            status = status,
                            insertToSitemap = true,
                            featuredImage = null,
                            sliderId = selectedSliderId,
                            updatedBy = "Admin", // TODO: Get from Auth
                            updatedOn = currentTime,
                            bannerType = if (bannerType == "Image" && heroBannerImageUri == null) "None" else bannerType,
                            bannerUrl = null,
                            pageSlug = pageName.lowercase().replace(" ", "-"),
                            createdBy = "Admin", // TODO: Get from Auth
                            createdOn = currentTime,
                            featuredFilePath = featuredImageUri?.let { uri -> uriToFile(context, uri)?.absolutePath },
                            bannerFilePath = heroBannerImageUri?.let { uri -> uriToFile(context, uri)?.absolutePath }
                        )
                        viewModel.addPage(request)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isAddingPage,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    if (isAddingPage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publishing...")
                    } else {
                        Text("Publish", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Templates Section
            SectionCard(title = "Templates") {
                ExposedDropdownMenuBox(
                    expanded = templateExpanded,
                    onExpandedChange = { templateExpanded = !templateExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTemplate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Template") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = templateExpanded,
                        onDismissRequest = { templateExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Default", "Full Width", "Sidebar Left", "Sidebar Right").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    selectedTemplate = item
                                    templateExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Featured Image Section
            SectionCard(title = "Featured Image") {
                ImagePicker(
                    imageUri = featuredImageUri,
                    onPickImage = { featuredImageLauncher.launch("image/*") },
                    onRemoveImage = { featuredImageUri = null },
                    label = "Add Featured Image"
                )
            }

            // Hero Banner Section
            SectionCard(title = "Hero Banner") {
                ExposedDropdownMenuBox(
                    expanded = bannerTypeExpanded,
                    onExpandedChange = { bannerTypeExpanded = !bannerTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = bannerType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Banner Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bannerTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = bannerTypeExpanded,
                        onDismissRequest = { bannerTypeExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Image", "Video", "None").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    bannerType = item
                                    bannerTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                if (bannerType == "Image") {
                    Spacer(modifier = Modifier.height(16.dp))
                    ImagePicker(
                        imageUri = heroBannerImageUri,
                        onPickImage = { heroBannerLauncher.launch("image/*") },
                        onRemoveImage = { heroBannerImageUri = null },
                        label = "Add Banner Image"
                    )
                }
            }

            // Slider Section
            SectionCard(title = "Slider") {
                ExposedDropdownMenuBox(
                    expanded = sliderExpanded,
                    onExpandedChange = { sliderExpanded = !sliderExpanded }
                ) {
                    OutlinedTextField(
                        value = sliders.find { it.id.toString() == selectedSliderId }?.name ?: "Select Slider",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Slider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sliderExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = sliderExpanded,
                        onDismissRequest = { sliderExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedSliderId = null
                                sliderExpanded = false
                            }
                        )
                        sliders.forEach { slider ->
                            DropdownMenuItem(
                                text = { Text(slider.name) },
                                onClick = {
                                    selectedSliderId = slider.id.toString()
                                    sliderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // SEO Section
            SectionCard(title = "SEO Field Options") {
                OutlinedTextField(
                    value = pageH1,
                    onValueChange = { pageH1 = it },
                    label = { Text("Page H1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = metaTitle,
                    onValueChange = { metaTitle = it },
                    label = { Text("Meta Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = metaDescription,
                    onValueChange = { metaDescription = it },
                    label = { Text("Meta Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = robotsExpanded,
                    onExpandedChange = { robotsExpanded = !robotsExpanded }
                ) {
                    OutlinedTextField(
                        value = robotsMetaTag,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Robots Meta Tag") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = robotsExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = robotsExpanded,
                        onDismissRequest = { robotsExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("index, follow", "noindex, follow", "index, nofollow", "noindex, nofollow").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    robotsMetaTag = item
                                    robotsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Scripts Section
            SectionCard(title = "Scripts") {
                OutlinedTextField(
                    value = headerScript,
                    onValueChange = { headerScript = it },
                    label = { Text("Header Script") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = footerScript,
                    onValueChange = { footerScript = it },
                    label = { Text("Footer Script") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ImagePicker(
    imageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    label: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = Color.White
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F7FA))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { onPickImage() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Add Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Helper to convert Uri to File
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
