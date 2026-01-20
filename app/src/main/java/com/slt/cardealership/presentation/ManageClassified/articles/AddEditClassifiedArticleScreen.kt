package com.slt.cardealership.presentation.ManageClassified.articles

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostCta
import com.slt.cardealership.presentation.articles.FeaturedImageUploader
import com.slt.cardealership.presentation.articles.FormCard
import com.slt.cardealership.presentation.articles.StyledTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassifiedArticleScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: AddEditClassifiedArticleViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh", true)
                    onNavigateBack()
                }
                is UiEvent.ShowSnackbar -> {
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
                    // Removed Save button from here
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
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "Content Transition"
            ) { targetState ->
                when {
                    targetState.isLoading -> {
                        CircularProgressIndicator()
                    }
                    targetState.error != null -> {
                        Text(
                            targetState.error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    targetState.post != null -> {
                        AddEditClassifiedArticleForm(post = targetState.post, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditClassifiedArticleForm(post: Post, viewModel: AddEditClassifiedArticleViewModel) {
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
                StyledTextField(
                    value = post.name ?: "",
                    onValueChange = viewModel::onTitleChange,
                    label = "Article Title *",
                    icon = Icons.Default.Title
                )
                StyledTextField(
                    value = post.slug ?: "",
                    onValueChange = viewModel::onSlugChange,
                    label = "Article Slug * (Note: URL)",
                    icon = Icons.Default.Link
                )

                // Status Dropdown
                var statusExpanded by remember { mutableStateOf(false) }
                // Options: Display Text -> Value
                val statusMap = mapOf(
                    "Select Status" to "",
                    "Draft" to "draft",
                    "Publish" to "published"
                )

                // Reverse map for display
                val currentStatusValue = post.status ?: ""
                val currentStatusDisplay = statusMap.entries.firstOrNull { it.value == currentStatusValue }?.key ?: "Select Status"

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentStatusDisplay,
                        onValueChange = {},
                        readOnly = true,
                        // If selected is "Select Status", use it as placeholder behavior or just value
                        // User wanted "Select Status" visible.
                        // Removing label to match "clean box" look if desired, or keeping it but making sure value is clear
                        // Image 1 shows "Select Status" inside the box.
                        placeholder = { Text("Select Status") },
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select Status")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    // Overlay a transparent clickable box
                    Box(
                        modifier = Modifier
                            .matchParentSize()
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
                StyledTextField(
                    value = post.metaTitle ?: "",
                    onValueChange = viewModel::onMetaTitleChange,
                    label = "Meta Title * (Max: 60)",
                    icon = Icons.Default.TextFields
                )
                StyledTextField(
                    value = post.metaDescription ?: "",
                    onValueChange = viewModel::onMetaDescriptionChange,
                    label = "Meta Description * (Max: 160)",
                    icon = Icons.Default.Description
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
                StyledTextField(
                    value = post.content ?: "",
                    onValueChange = viewModel::onContentChange,
                    label = "Enter article text *",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    modifier = Modifier.height(200.dp)
                )
            }
        }

        item {
            FormCard(title = "Tags") {
                var tagsExpanded by remember { mutableStateOf(false) }
                val availableTags = viewModel.state.availableTags

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = post.tags?.joinToString(", ") { it.tagName } ?: "Select Tags",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Tags") },
                        trailingIcon = {
                            IconButton(onClick = { tagsExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select Tags")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { tagsExpanded = true }
                    )
                    DropdownMenu(
                        expanded = tagsExpanded,
                        onDismissRequest = { tagsExpanded = false }
                    ) {
                        availableTags.forEach { tag ->
                            val isSelected = post.tags?.any { it.tagName == tag.tagName } == true
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null // Handled by onClick
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(tag.tagName)
                                    }
                                },
                                onClick = {
                                    val currentTags = post.tags?.toMutableList() ?: mutableListOf()
                                    // Toggle logic based on tagName as ID might be null in post.tags
                                    val existing = currentTags.find { it.tagName == tag.tagName }
                                    if (existing != null) {
                                        currentTags.remove(existing)
                                    } else {
                                        currentTags.add(tag)
                                    }
                                    viewModel.onTagsChange(currentTags)
                                    // Don't dismiss to allow multiple selection
                                }
                            )
                        }
                        if (availableTags.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tags available") },
                                onClick = { tagsExpanded = false },
                                enabled = false
                            )
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
                // Removed Add CTA button as per requirement
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
fun CtaItem(
    cta: PostCta,
    onLabelChange: (String) -> Unit,
    onUrlChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("CTA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

        OutlinedTextField(
            value = cta.label,
            onValueChange = onLabelChange,
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = cta.url,
            onValueChange = onUrlChange,
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
