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
import androidx.compose.foundation.Image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
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
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Close") }
                },
                actions = {
                    Button(
                        onClick = viewModel::onSave,
                        enabled = !state.isSaving && !state.isLoading,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        // Show a small spinner inside the button when saving
                        AnimatedVisibility(visible = state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        AnimatedVisibility(visible = !state.isSaving) {
                            Text("Save")
                        }
                    }
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
                label = "Content Transition" // Optional label for inspection
            ) { targetState ->
                when {
                    targetState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    targetState.error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                targetState.error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    targetState.post != null -> {
                        AddEditArticleForm(post = targetState.post, viewModel = viewModel)
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
                StyledTextField(
                    value = post.name ?: "",
                    onValueChange = viewModel::onTitleChange,
                    label = "Article Title *",
                    icon = Icons.Default.Title
                )
                StyledTextField(
                    value = post.slug ?: "",
                    onValueChange = viewModel::onSlugChange,
                    label = "Article Slug",
                    icon = Icons.Default.Link
                )
            }
        }

        item {
            FormCard(title = "Meta Information (SEO)") {
                StyledTextField(
                    value = post.metaTitle ?: "",
                    onValueChange = viewModel::onMetaTitleChange,
                    label = "Meta Title (Max: 60)",
                    icon = Icons.Default.TextFields
                )
                StyledTextField(
                    value = post.metaDescription ?: "",
                    onValueChange = viewModel::onMetaDescriptionChange,
                    label = "Meta Description (Max: 160)",
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
            FormCard(title = "Content") {
                StyledTextField(
                    value = post.content ?: "",
                    onValueChange = viewModel::onContentChange,
                    label = "Article Text",
                    icon = Icons.Default.Notes,
                    modifier = Modifier.height(100.dp)
                )
            }
        }
    }
}

// A reusable styled TextField with an icon
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2196F3),
            focusedLabelColor = Color(0xFF2196F3),
            cursorColor = Color(0xFF2196F3),
            focusedLeadingIconColor = Color(0xFF2196F3)
        )
    )
}


@Composable
fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
    val borderColor = if (imageUrl.isNullOrBlank()) MaterialTheme.colorScheme.primary else Color.LightGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onImageAdd),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            // Placeholder content when no image is selected
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AddPhotoAlternate, "Add Image", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add Featured Image", color = MaterialTheme.colorScheme.primary)
                Text("(Recommended: 960 * 550)", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            // Show the selected image
            AsyncImage(
                model = imageUrl,
                contentDescription = "Featured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Close button to remove the image
            IconButton(
                onClick = onImageRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.White)
            }
        }
    }
}