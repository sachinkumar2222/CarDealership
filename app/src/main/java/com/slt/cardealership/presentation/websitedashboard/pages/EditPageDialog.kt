package com.slt.cardealership.presentation.websitedashboard.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.slt.cardealership.domain.model.DomainPageDetails
import com.slt.cardealership.domain.model.DomainPageUpdateRequest
import com.slt.cardealership.domain.model.DomainSlider



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexEditPageDialog(
    page: com.slt.cardealership.domain.model.DomainPageDetails,
    domainId: Int,
    sliders: List<com.slt.cardealership.domain.model.DomainSlider>,
    onDismiss: () -> Unit,
    onSave: (com.slt.cardealership.domain.model.DomainPageUpdateRequest) -> Unit
) {
    var pageName by remember { mutableStateOf(page.pageName) }
    var pageSlug by remember { mutableStateOf(page.pageSlug) }
    var pageContent by remember { mutableStateOf(page.pageDescription ?: "") }

// SEO
    var pageH1 by remember { mutableStateOf(page.pageTitle ?: "") }
    var metaTitle by remember { mutableStateOf(page.metaTitle ?: "") }
    var metaDescription by remember { mutableStateOf(page.metaDescription ?: "") }
    var robotsMetaTag by remember { mutableStateOf(page.robotsMetaTags ?: "index, follow") }

// Scripts
    var headerScript by remember { mutableStateOf(page.headerScript ?: "") }
    var footerScript by remember { mutableStateOf(page.footerScript ?: "") }

// Sidebar
// Note: pageTypeName is not in Details, but we have pageTypeId.
// We might need to fetch types or just show ID for now, or pass name from list if needed.
// For now, let's just show the ID or a placeholder.
    var pageType by remember { mutableStateOf(page.pageTypeId) }
    var status by remember { mutableStateOf(page.status) }
    var selectedTemplate by remember { mutableStateOf("Default Template") }
    var featuredImageUrl by remember { mutableStateOf(page.featuredImage ?: "") }
    var bannerType by remember { mutableStateOf(page.bannerType ?: "Image") }
    var bannerUrl by remember { mutableStateOf(page.bannerUrl ?: "") }

    var sliderId by remember { mutableStateOf(page.sliderId ?: "") }

// Image URIs
    var featuredImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var bannerImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val featuredImageLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        featuredImageUri = uri
    }

    val bannerImageLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        bannerImageUri = uri
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen-ish
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Page") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                val request = DomainPageUpdateRequest(
                                    id = page.id,
                                    pageName = pageName,
                                    pageSlug = pageSlug,
                                    status = status,
                                    sqlDomainId = domainId,
                                    insertToSitemap = page.insertToSitemap,
                                    pageTypeId = page.pageTypeId,
                                    createdBy = page.createdBy,
                                    createdOn = page.createdOn,
                                    updatedBy = page.updatedBy,
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
                                    featuredFilePath = featuredImageUri?.toString(),
                                    bannerFilePath = bannerImageUri?.toString()
                                )
                                onSave(request)
                            },
                            modifier = Modifier.padding(end = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("Update", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFFF5F7FA)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Basic Info Section (Page Name, Slug, Status, Type)
                CardSection(title = "Basic Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Page Name
                        OutlinedTextField(
                            value = pageName,
                            onValueChange = { pageName = it },
                            label = { Text("Page Name*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3),
                                cursorColor = Color(0xFF2196F3)
                            ),
                            singleLine = true
                        )

                        // Slug
                        Column {
                            Text(
                                text = "Page Slug",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "https://bellford.com/",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = pageSlug,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { /* TODO: Edit Slug */ },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        "Edit Slug",
                                        tint = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }

                        // Status & Type Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Page Type
                            OutlinedTextField(
                                value = pageType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Page Type") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    focusedLabelColor = Color(0xFF2196F3),
                                    cursorColor = Color(0xFF2196F3)
                                )
                            )

                            // Status
                            var statusExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = statusExpanded,
                                onExpandedChange = { statusExpanded = !statusExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = status,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Status") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
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
                                    DropdownMenuItem(
                                        text = { Text("Active") },
                                        onClick = { status = "active"; statusExpanded = false })
                                    DropdownMenuItem(
                                        text = { Text("Inactive") },
                                        onClick = { status = "inactive"; statusExpanded = false })
                                }
                            }
                        }
                    }
                }

                // 2. Content Section
                CardSection(title = "Page Content") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        // Toolbar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("B", fontWeight = FontWeight.Bold, modifier = Modifier.clickable {})
                            Text(
                                "I",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.clickable {})
                            Text(
                                "U",
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                modifier = Modifier.clickable {})
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        OutlinedTextField(
                            value = pageContent,
                            onValueChange = { pageContent = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color(0xFF2196F3)
                            ),
                            placeholder = { Text("Enter page content here...") }
                        )
                    }
                }

                // 3. SEO Section
                CollapsibleCard(title = "SEO Settings") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Tags Helper
                        Column {
                            Text(
                                "Available Tags",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("{dealer_name}", "{dealer_city}").forEach { tag ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pageH1,
                            onValueChange = { pageH1 = it },
                            label = { Text("Page H1 Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3),
                                cursorColor = Color(0xFF2196F3)
                            )
                        )

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

                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = {}
                        ) {
                            OutlinedTextField(
                                value = robotsMetaTag,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Robots Meta Tag") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    focusedLabelColor = Color(0xFF2196F3),
                                    cursorColor = Color(0xFF2196F3)
                                )
                            )
                        }
                    }
                }

                // 4. Media & Assets
                CollapsibleCard(title = "Media & Assets") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Featured Image
                        Text("Featured Image", style = MaterialTheme.typography.titleSmall)

                        val featuredImageDisplay = featuredImageUri
                            ?: (if (featuredImageUrl.isNotEmpty()) featuredImageUrl else null)

                        if (featuredImageDisplay != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { featuredImageLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                coil3.compose.AsyncImage(
                                    model = featuredImageDisplay,
                                    contentDescription = "Featured Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                // Overlay to indicate editability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Change Image",
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { featuredImageLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Add,
                                        null,
                                        tint = Color(0xFF2196F3)
                                    )
                                    Text(
                                        "Upload Featured Image",
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }

                        // Slider Dropdown
                        var sliderExpanded by remember { mutableStateOf(false) }
                        val selectedSliderName =
                            sliders.find { it.id == sliderId }?.name ?: "Select Slider"

                        ExposedDropdownMenuBox(
                            expanded = sliderExpanded,
                            onExpandedChange = { sliderExpanded = !sliderExpanded }
                        ) {
                            Row(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if(sliderExpanded) Color(0xFF2196F3) else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedSliderName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = sliderExpanded)
                            }

                            ExposedDropdownMenu(
                                expanded = sliderExpanded,
                                onDismissRequest = { sliderExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        sliderId = ""
                                        sliderExpanded = false
                                    }
                                )
                                sliders.forEach { slider ->
                                    DropdownMenuItem(
                                        text = { Text(slider.name) },
                                        onClick = {
                                            sliderId = slider.id
                                            sliderExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Divider()

                        // Banner
                        Text("Hero Banner", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = bannerType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Banner Type") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3),
                                cursorColor = Color(0xFF2196F3)
                            )
                        )

                        val bannerImageDisplay =
                            bannerImageUri ?: (if (bannerUrl.isNotEmpty()) bannerUrl else null)

                        if (bannerImageDisplay != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { bannerImageLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                coil3.compose.AsyncImage(
                                    model = bannerImageDisplay,
                                    contentDescription = "Banner Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                // Overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Change Banner",
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { bannerImageLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Add,
                                        null,
                                        tint = Color(0xFF2196F3)
                                    )
                                    Text(
                                        "Upload Banner Image",
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Advanced Settings (Scripts, Templates, etc.)
                CollapsibleCard(title = "Advanced Settings") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = selectedTemplate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Page Template") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3),
                                cursorColor = Color(0xFF2196F3)
                            )
                        )

                        OutlinedTextField(
                            value = headerScript,
                            onValueChange = { headerScript = it },
                            label = { Text("Header Script") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3),
                                cursorColor = Color(0xFF2196F3)
                            )
                        )

                        OutlinedTextField(
                            value = footerScript,
                            onValueChange = { footerScript = it },
                            label = { Text("Footer Script") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3),
                                cursorColor = Color(0xFF2196F3)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CardSection(title: String? = null, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            content()
        }
    }
}

@Composable
fun CollapsibleCard(
    title: String,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider(modifier = Modifier.padding(bottom = 16.dp))
                    content()
                }
            }
        }
    }
}
