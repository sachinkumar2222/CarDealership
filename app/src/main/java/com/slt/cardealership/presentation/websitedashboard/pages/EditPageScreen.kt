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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.slt.cardealership.domain.model.DomainPageUpdateRequest
import com.slt.cardealership.presentation.common.AnimatedDropdown
import com.slt.cardealership.presentation.common.LabeledTextField
import com.slt.cardealership.presentation.common.LoadingAnimation
import com.slt.cardealership.utils.uriToFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPageScreen(
    navController: NavController,
    domainId: Int,
    pageId: String,
    viewModel: WebsitePagesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var pageDetails by remember { mutableStateOf<com.slt.cardealership.domain.model.DomainPageDetails?>(null) }
    val sliders by viewModel.sliders.collectAsState()
    val isUpdatingPage by viewModel.isUpdatingPage.collectAsState()

    // Form State
    var pageName by remember { mutableStateOf("") }
    var pageSlug by remember { mutableStateOf("") }
    var pageContent by remember { mutableStateOf("") }
    var pageType by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("Default") }
    var featuredImageUrl by remember { mutableStateOf("") }
    var bannerType by remember { mutableStateOf("Image") }
    var bannerUrl by remember { mutableStateOf("") }
    var sliderId by remember { mutableStateOf<String?>(null) }

    // SEO State
    var pageH1 by remember { mutableStateOf("") }
    var metaTitle by remember { mutableStateOf("") }
    var metaDescription by remember { mutableStateOf("") }
    var robotsMetaTag by remember { mutableStateOf("index, follow") }

    // Scripts State
    var headerScript by remember { mutableStateOf("") }
    var footerScript by remember { mutableStateOf("") }

    // Image URIs
    var featuredImageUri by remember { mutableStateOf<Uri?>(null) }
    var bannerImageUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pageId) {
        viewModel.fetchSliders(domainId.toString())
        val details = viewModel.getPageDetails(pageId)
        if (details != null) {
            pageDetails = details
            // Populate State
            pageName = details.pageName
            pageSlug = details.pageSlug
            pageContent = details.pageDescription ?: ""
            pageType = details.pageTypeId
            status = details.status

            featuredImageUrl = details.featuredImage ?: ""
            bannerType = details.bannerType ?: "Image"
            bannerUrl = details.bannerUrl ?: ""
            sliderId = details.sliderId

            pageH1 = details.pageTitle ?: ""
            metaTitle = details.metaTitle ?: ""
            metaDescription = details.metaDescription ?: ""
            robotsMetaTag = details.robotsMetaTags ?: "index, follow"

            headerScript = details.headerScript ?: ""
            footerScript = details.footerScript ?: ""
        }
        isLoading = false
    }

    // Listen for update success
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

    val featuredImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        featuredImageUri = uri
    }

    val bannerImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        bannerImageUri = uri
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Page") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (pageDetails != null) {
                                val request = DomainPageUpdateRequest(
                                    id = pageId,
                                    pageName = pageName,
                                    pageSlug = pageSlug,
                                    status = status,
                                    sqlDomainId = domainId,
                                    insertToSitemap = pageDetails!!.insertToSitemap,
                                    pageTypeId = pageDetails!!.pageTypeId,
                                    createdBy = pageDetails!!.createdBy,
                                    createdOn = pageDetails!!.createdOn,
                                    updatedBy = pageDetails!!.updatedBy, // Or current user
                                    updatedOn = System.currentTimeMillis() / 1000,
                                    pageDescription = pageContent,
                                    pageTitle = pageH1,
                                    metaTitle = metaTitle,
                                    metaDescription = metaDescription,
                                    robotsMetaTags = robotsMetaTag,
                                    headerScript = headerScript,
                                    footerScript = footerScript,
                                    featuredImage = featuredImageUrl,
                                    sliderId = sliderId,
                                    bannerType = bannerType,
                                    bannerUrl = bannerUrl,
                                    featuredFilePath = featuredImageUri?.let { uriToFile(context, it)?.absolutePath },
                                    bannerFilePath = bannerImageUri?.let { uriToFile(context, it)?.absolutePath }
                                )
                                viewModel.updatePage(request)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading && pageDetails != null && !isUpdatingPage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isUpdatingPage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating...")
                        } else {
                            Text("Update Page", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        if (isLoading) {
            LoadingAnimation()
        } else if (pageDetails == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load page details")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // General Information
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
                        minLines = 12,
                        maxLines = 12,
                        singleLine = false
                    )
                    Text(
                        text = "Note: Rich Text Editor is not supported in this mobile view. Please use HTML or plain text.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Action (Status & Type)
                SectionCard(title = "Action") {
                    AnimatedDropdown(
                        label = "Page Type",
                        options = listOf("Standard", "Landing Page", "Blog Post"), // Should ideally come from API/Model
                        selectedOption = "Standard", // Placeholder: Map ID to name logic needed
                        onOptionSelected = { /* pageType = it */ }, // Read-only for now or map back
                        enabled = false // Assuming type is not easily changeable or mapped yet
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedDropdown(
                        label = "Status",
                        options = listOf("active", "inactive", "draft"),
                        selectedOption = status,
                        onOptionSelected = { status = it }
                    )
                }

                // Templates
                SectionCard(title = "Templates") {
                    AnimatedDropdown(
                        label = "Select Template",
                        options = listOf("Default", "Full Width", "Sidebar Left", "Sidebar Right"),
                        selectedOption = selectedTemplate,
                        onOptionSelected = { selectedTemplate = it }
                    )
                }

                // Featured Image
                SectionCard(title = "Featured Image") {
                    EditImagePicker(
                        imageUri = featuredImageUri,
                        imageUrl = featuredImageUrl,
                        onPickImage = { featuredImageLauncher.launch("image/*") },
                        onRemoveImage = {
                            featuredImageUri = null
                            featuredImageUrl = ""
                        },
                        label = "Add Featured Image"
                    )
                }

                // Hero Banner
                SectionCard(title = "Hero Banner") {
                    AnimatedDropdown(
                        label = "Banner Type",
                        options = listOf("Image", "Video", "None"),
                        selectedOption = bannerType,
                        onOptionSelected = { bannerType = it }
                    )

                    if (bannerType == "Image") {
                        Spacer(modifier = Modifier.height(16.dp))
                        EditImagePicker(
                            imageUri = bannerImageUri,
                            imageUrl = if (bannerUrl.isNotEmpty()) bannerUrl else null,
                            onPickImage = { bannerImageLauncher.launch("image/*") },
                            onRemoveImage = {
                                bannerImageUri = null
                                bannerUrl = ""
                            },
                            label = "Add Banner Image"
                        )
                    }
                }

                // Slider
                SectionCard(title = "Slider") {
                    val currentSliderName = sliders.find { it.id.toString() == sliderId }?.name ?: "None"
                    val sliderOptions = listOf("None") + sliders.map { it.name }

                    AnimatedDropdown(
                        label = "Select Slider",
                        options = sliderOptions,
                        selectedOption = currentSliderName,
                        onOptionSelected = { selectedName ->
                            sliderId = if (selectedName == "None") null
                            else sliders.find { it.name == selectedName }?.id.toString()
                        }
                    )
                }

                // SEO Options
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

                // Scripts
                SectionCard(title = "Scripts") {
                    LabeledTextField(
                        label = "Header Script",
                        value = headerScript,
                        onValueChange = { headerScript = it },
                        placeholder = "Header Script",
                        minLines = 8,
                        maxLines = 8,
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LabeledTextField(
                        label = "Footer Script",
                        value = footerScript,
                        onValueChange = { footerScript = it },
                        placeholder = "Footer Script",
                        minLines = 8,
                        maxLines = 8,
                        singleLine = false
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EditImagePicker(
    imageUri: Uri?,
    imageUrl: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    label: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayModel = imageUri ?: (if (!imageUrl.isNullOrEmpty()) imageUrl else null)

        if (displayModel != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            ) {
                val painter = rememberAsyncImagePainter(model = displayModel)
                ComposableImage(
                    painter = painter,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Remove Button
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
                // Edit Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { onPickImage() }
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // Taller hit area
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F7FA))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onPickImage() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Add Image",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = Color(0xFF2196F3),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
