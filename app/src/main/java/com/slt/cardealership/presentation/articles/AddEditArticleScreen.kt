package com.slt.cardealership.presentation.articles

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostCta
import com.slt.cardealership.presentation.common.LabeledTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditArticleScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: AddEditArticleViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditArticleViewModel.UiEvent.NavigateBack -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh", true)
                    onNavigateBack()
                }
                is AddEditArticleViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditMode) "Edit Article" else "Add Article", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    // Save button removed from top bar
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5)),
            contentAlignment = Alignment.Center
        ) {
            // Derive a stable state for animation
            val screenState = when {
                state.isLoading -> "Loading"
                state.error != null -> "Error"
                state.post != null -> "Content"
                else -> "Loading"
            }

            AnimatedContent(
                targetState = screenState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "Screen Transition"
            ) { targetScreen ->
                when (targetScreen) {
                    "Loading" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    "Error" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                state.error ?: "Unknown Error",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    "Content" -> {
                        state.post?.let { post ->
                            AddEditArticleForm(post = post, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditArticleForm(post: Post, viewModel: AddEditArticleViewModel) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it, context) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            FormCard(title = "Article Details") {
                LabeledTextField(
                    label = "Article Title *",
                    value = post.name ?: "",
                    onValueChange = viewModel::onTitleChange,
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Color.Gray) }
                )
                LabeledTextField(
                    label = "Article Slug * (Note: URL)",
                    value = post.slug ?: "",
                    onValueChange = viewModel::onSlugChange,
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color.Gray) }
                )

                // Status Dropdown
                var statusExpanded by remember { mutableStateOf(false) }
                val statusMap = mapOf(
                    "Select Status" to "",
                    "Draft" to "draft",
                    "Publish" to "published"
                )
                val currentStatusValue = post.status ?: ""
                val currentStatusDisplay = statusMap.entries.firstOrNull { it.value == currentStatusValue }?.key ?: "Select Status"

                Box(modifier = Modifier.fillMaxWidth()) {
                    LabeledTextField(
                        label = "Status",
                        value = currentStatusDisplay,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "Select Status",
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Select Status", modifier = Modifier.clickable { statusExpanded = true })
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 28.dp) // Offset for label
                            .clickable { statusExpanded = true }
                    )
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        statusMap.forEach { (display, value) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = display,
                                        color = if (value == currentStatusValue && value.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Black
                                    )
                                },
                                onClick = {
                                    viewModel.onStatusChange(value)
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            FormCard(title = "Meta Information (SEO)") {
                LabeledTextField(
                    label = "Meta Title * (Max: 60)",
                    value = post.metaTitle ?: "",
                    onValueChange = viewModel::onMetaTitleChange,
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.Gray) }
                )
                LabeledTextField(
                    label = "Meta Description * (Max: 160)",
                    value = post.metaDescription ?: "",
                    onValueChange = viewModel::onMetaDescriptionChange,
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray) }
                )
            }
        }

        item {
            FormCard(title = "Featured Image *") {
                FeaturedImageUploader(
                    imageUrl = post.image,
                    onImageAdd = { imagePickerLauncher.launch("image/*") },
                    onImageRemove = viewModel::onImageRemoved
                )
            }
        }

        item {
            FormCard(title = "Article Content") {
                LabeledTextField(
                    label = "Enter article text *",
                    value = post.content ?: "",
                    onValueChange = viewModel::onContentChange,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Color.Gray) },
                    singleLine = false,
                    minLines = 15,
                    maxLines = 30
                )
            }
        }

        item {
            FormCard(title = "Tags") {
                var tagsExpanded by remember { mutableStateOf(false) }
                val availableTags = viewModel.state.availableTags

                Box(modifier = Modifier.fillMaxWidth()) {
                    LabeledTextField(
                        label = "Select Tags",
                        value = post.tags?.joinToString(", ") { it.tagName } ?: "Select Tags",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Select Tags", modifier = Modifier.clickable { tagsExpanded = true })
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 28.dp)
                            .clickable { tagsExpanded = true }
                    )
                    DropdownMenu(
                        expanded = tagsExpanded,
                        onDismissRequest = { tagsExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (availableTags.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tags available") },
                                onClick = { tagsExpanded = false },
                                enabled = false
                            )
                        } else {
                            availableTags.forEach { tag ->
                                val isSelected = post.tags?.any { it.tagName == tag.tagName } == true
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(tag.tagName)
                                        }
                                    },
                                    onClick = {
                                        val currentTags = post.tags?.toMutableList() ?: mutableListOf()
                                        val existing = currentTags.find { it.tagName == tag.tagName }
                                        if (existing != null) {
                                            currentTags.remove(existing)
                                        } else {
                                            currentTags.add(tag)
                                        }
                                        viewModel.onTagsChange(currentTags)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            FormCard(title = "Manage CTA(s)") {
                post.ctas?.forEachIndexed { index, cta ->
                    CtaItem(
                        cta = cta,
                        onLabelChange = { viewModel.onCtaLabelChange(index, it) },
                        onUrlChange = { viewModel.onCtaUrlChange(index, it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        item {
            Button(
                onClick = viewModel::onSave,
                enabled = !viewModel.state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                AnimatedVisibility(visible = viewModel.state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (viewModel.state.isSaving) "Saving..." else "Save Article")
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun FeaturedImageUploader(
    imageUrl: String?,
    onImageAdd: () -> Unit,
    onImageRemove: () -> Unit
) {
    val borderColor = if (imageUrl.isNullOrBlank()) Color(0xFF2196F3) else Color.LightGray
    val stroke = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (imageUrl.isNullOrBlank()) Color(0xFFF5F9FF) else Color.Transparent)
            .then(
                if (imageUrl.isNullOrBlank()) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = borderColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = stroke
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                        )
                    }
                } else {
                    Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                }
            )
            .clickable(onClick = onImageAdd),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        "Add Image",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF2196F3)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Upload Featured Image",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Recommended: 960x550 px",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Featured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onImageRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove Image",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CtaItem(
    cta: PostCta,
    onLabelChange: (String) -> Unit,
    onUrlChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("CTA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

        LabeledTextField(
            label = "Label",
            value = cta.label,
            onValueChange = onLabelChange,
            modifier = Modifier.fillMaxWidth()
        )
        LabeledTextField(
            label = "URL",
            value = cta.url,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}