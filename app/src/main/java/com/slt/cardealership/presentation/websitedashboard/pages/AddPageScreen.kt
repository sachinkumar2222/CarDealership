package com.slt.cardealership.presentation.websitedashboard.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image as ComposableImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.slt.cardealership.domain.model.DomainPageCreateRequest
import com.slt.cardealership.presentation.common.AnimatedDropdown
import com.slt.cardealership.presentation.common.LabeledTextField
import com.slt.cardealership.utils.uriToFile
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
                modifier = Modifier.shadow(8.dp),
                title = { Text("Add Page") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
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
                LabeledTextField(
                    label = "Page Name *",
                    value = pageName,
                    onValueChange = { pageName = it },
                    placeholder = "Enter page name"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledTextField(
                    label = "Page Content",
                    value = pageContent,
                    onValueChange = { pageContent = it },
                    placeholder = "Enter page content (HTML supported)",
                    minLines = 8,
                    singleLine = false
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
                AnimatedDropdown(
                    label = "Page Type *",
                    options = listOf("Standard", "Landing Page", "Blog Post"),
                    selectedOption = pageType,
                    onOptionSelected = { pageType = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedDropdown(
                    label = "Status",
                    options = listOf("Active", "Inactive", "Draft"),
                    selectedOption = status,
                    onOptionSelected = { status = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        val request = DomainPageCreateRequest(
                            sqlDomainId = domainId,
                            pageName = pageName,
                            pageDescription = pageContent,
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
                            updatedBy = "Admin",
                            updatedOn = currentTime,
                            bannerType = if (bannerType == "Image" && heroBannerImageUri == null) "None" else bannerType,
                            bannerUrl = null,
                            pageSlug = pageName.lowercase().replace(" ", "-"),
                            createdBy = "Admin",
                            createdOn = currentTime,
                            featuredFilePath = featuredImageUri?.let { uri -> uriToFile(context, uri)?.absolutePath },
                            bannerFilePath = heroBannerImageUri?.let { uri -> uriToFile(context, uri)?.absolutePath }
                        )
                        viewModel.addPage(request)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isAddingPage,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
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
                AnimatedDropdown(
                    label = "Select Template",
                    options = listOf("Default", "Full Width", "Sidebar Left", "Sidebar Right"),
                    selectedOption = selectedTemplate,
                    onOptionSelected = { selectedTemplate = it }
                )
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
                AnimatedDropdown(
                    label = "Banner Type",
                    options = listOf("Image", "Video", "None"),
                    selectedOption = bannerType,
                    onOptionSelected = { bannerType = it }
                )

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
                val currentSliderName = sliders.find { it.id.toString() == selectedSliderId }?.name ?: "None"
                // Need to handle "None" properly in display vs ID
                val sliderOptions = listOf("None") + sliders.map { it.name }

                AnimatedDropdown(
                    label = "Select Slider",
                    options = sliderOptions,
                    selectedOption = currentSliderName,
                    onOptionSelected = { selectedName ->
                        selectedSliderId = if (selectedName == "None") null
                        else sliders.find { it.name == selectedName }?.id.toString()
                    }
                )
            }

            // SEO Section
            SectionCard(title = "SEO Field Options") {
                LabeledTextField(
                    label = "Page H1",
                    value = pageH1,
                    onValueChange = { pageH1 = it },
                    placeholder = "Page H1 Title"
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledTextField(
                    label = "Meta Title",
                    value = metaTitle,
                    onValueChange = { metaTitle = it },
                    placeholder = "Meta Title"
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledTextField(
                    label = "Meta Description",
                    value = metaDescription,
                    onValueChange = { metaDescription = it },
                    placeholder = "Meta Description",
                    minLines = 3,
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedDropdown(
                    label = "Robots Meta Tag",
                    options = listOf("index, follow", "noindex, follow", "index, nofollow", "noindex, nofollow"),
                    selectedOption = robotsMetaTag,
                    onOptionSelected = { robotsMetaTag = it }
                )
            }

            // Scripts Section
            SectionCard(title = "Scripts") {
                LabeledTextField(
                    label = "Header Script",
                    value = headerScript,
                    onValueChange = { headerScript = it },
                    placeholder = "Header Script",
                    minLines = 3,
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledTextField(
                    label = "Footer Script",
                    value = footerScript,
                    onValueChange = { footerScript = it },
                    placeholder = "Footer Script",
                    minLines = 3,
                    singleLine = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
                ComposableImage(
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
