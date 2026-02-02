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
import com.slt.cardealership.presentation.common.AnimatedDropdown
import com.slt.cardealership.presentation.common.LabeledTextField

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
            val contentState = when {
                state.isLoading -> "Loading"
                state.error != null -> "Error"
                else -> "Content"
            }

            AnimatedContent(
                targetState = contentState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "Content Transition"
            ) { targetLabel ->
                when (targetLabel) {
                    "Loading" -> {
                        CircularProgressIndicator()
                    }
                    "Error" -> {
                        Text(
                            state.error ?: "Unknown Error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    "Content" -> {
                        state.post?.let { post ->
                            AddEditClassifiedArticleForm(post = post, viewModel = viewModel)
                        }
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
                LabeledTextField(
                    value = post.name ?: "",
                    onValueChange = viewModel::onTitleChange,
                    label = "Article Title *",
                    placeholder = "Enter article title",
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Color.Gray) }
                )
                LabeledTextField(
                    value = post.slug ?: "",
                    onValueChange = viewModel::onSlugChange,
                    label = "Article Slug * (Note: URL)",
                    placeholder = "auto-generated-slug",
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color.Gray) }
                )

                // Status Dropdown
                val statusMap = mapOf(
                    "Select Status" to "",
                    "Draft" to "draft",
                    "Publish" to "published"
                )
                val currentStatusValue = post.status ?: ""
                val currentStatusDisplay = statusMap.entries.firstOrNull { it.value == currentStatusValue }?.key ?: "Select Status"

                AnimatedDropdown(
                    label = "Status",
                    options = statusMap.keys.filter { it != "Select Status" }.toList(),
                    selectedOption = currentStatusDisplay,
                    onOptionSelected = { selected ->
                        val value = statusMap[selected] ?: ""
                        viewModel.onStatusChange(value)
                    }
                )
            }
        }

        item {
            FormCard(title = "Meta Information (SEO)") {
                LabeledTextField(
                    value = post.metaTitle ?: "",
                    onValueChange = viewModel::onMetaTitleChange,
                    label = "Meta Title * (Max: 60)",
                    placeholder = "SEO Title",
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.Gray) }
                )
                LabeledTextField(
                    value = post.metaDescription ?: "",
                    onValueChange = viewModel::onMetaDescriptionChange,
                    label = "Meta Description * (Max: 160)",
                    placeholder = "SEO Description",
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray) },
                    singleLine = false,
                    maxLines = 3
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
                    value = post.content ?: "",
                    onValueChange = viewModel::onContentChange,
                    label = "Enter article text *",
                    placeholder = "Write your content here...",
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.height(200.dp),
                    singleLine = false,
                    maxLines = 20
                )
            }
        }

        item {
            FormCard(title = "Tags") {
                val availableTags = viewModel.state.availableTags
                val currentTags = post.tags ?: emptyList()
                val displayTags = if (currentTags.isEmpty()) "Select Tags" else currentTags.joinToString(", ") { it.tagName }

                // Multi-select dropdown logic is complex for AnimatedDropdown, sticking to custom Box but using LabeledTextField style
                var tagsExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    LabeledTextField(
                        value = displayTags,
                        onValueChange = {},
                        label = "Tags",
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { tagsExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select Tags")
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { tagsExpanded = true }
                    )
                    DropdownMenu(
                        expanded = tagsExpanded,
                        onDismissRequest = { tagsExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        availableTags.forEach { tag ->
                            val isSelected = currentTags.any { it.tagName == tag.tagName }
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
                                    val newTags = currentTags.toMutableList()
                                    val existing = newTags.find { it.tagName == tag.tagName }
                                    if (existing != null) {
                                        newTags.remove(existing)
                                    } else {
                                        newTags.add(tag)
                                    }
                                    viewModel.onTagsChange(newTags)
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
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
                shape = RoundedCornerShape(12.dp),
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
                Text(
                    if (viewModel.state.isSaving) "Saving..." else "Save Article",
                    fontWeight = FontWeight.SemiBold
                )
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("CTA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

        LabeledTextField(
            value = cta.label,
            onValueChange = onLabelChange,
            label = "Label"
        )
        LabeledTextField(
            value = cta.url,
            onValueChange = onUrlChange,
            label = "URL"
        )
    }
}
