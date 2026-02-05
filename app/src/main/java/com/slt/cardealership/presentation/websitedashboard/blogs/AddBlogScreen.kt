package com.slt.cardealership.presentation.websitedashboard.blogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.domain.model.BlogCta
import com.slt.cardealership.domain.model.ManageUsers
import com.slt.cardealership.domain.model.BlogCategory
import androidx.compose.ui.unit.sp
import com.slt.cardealership.domain.model.ResearchMake
import com.slt.cardealership.domain.model.ResearchModel
import com.slt.cardealership.domain.model.ResearchYear
import com.slt.cardealership.domain.model.ResearchTrim
import java.io.File
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.slt.cardealership.presentation.common.AnimatedDropdown
import com.slt.cardealership.presentation.common.LabeledTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlogScreen(
    navController: NavController,
    domainId: Int,
    blogId: String? = null,
    viewModel: AddBlogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(AddBlogEvent.ImageSelected(it)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text(if (blogId == null) "Add Blog" else "Edit Blog") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F6F8)) // Light gray background
        ) {
            val isWideScreen = maxWidth > 900.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.onEvent(AddBlogEvent.ErrorDismissed) }) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                val context = LocalContext.current
                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess) {
                        android.widget.Toast.makeText(context, "Blog saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh_blogs", true)
                        navController.popBackStack()
                    }
                }

                if (isWideScreen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            MainContentSection(state, viewModel)
                        }
                        Column(modifier = Modifier.weight(0.3f)) {
                            SidebarSection(state, viewModel, imageLauncher)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        MainContentSection(state, viewModel)
                        SidebarSection(state, viewModel, imageLauncher)
                    }
                }
            }
        }
    }
}

@Composable
fun MainContentSection(state: AddBlogUiState, viewModel: AddBlogViewModel) {
    // 1. Main Blog Details
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Blog Type
            AnimatedDropdown(
                label = "Blog Type*",
                options = listOf("general", "research", "compare"),
                selectedOption = state.blogType,
                onOptionSelected = { viewModel.onEvent(AddBlogEvent.BlogTypeChanged(it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Research Details (Conditional)
            if (state.blogType == "research" || state.blogType == "compare") {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.onEvent(AddBlogEvent.ToggleVehicleDialog(true)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.blogType == "research") "Add Research Vehicle" else "Add Compare Vehicle")
                }

                if (state.researchComparisons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Added Vehicles:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    state.researchComparisons.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.year} ${item.makeName} ${item.modelName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.trimName.isNotEmpty()) {
                                        Text(
                                            text = item.trimName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            LabeledTextField(
                label = "Title*",
                value = state.title,
                onValueChange = { viewModel.onEvent(AddBlogEvent.TitleChanged(it)) },
                placeholder = "Title* (Required)"
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Slug
            LabeledTextField(
                label = "Slug*",
                value = state.slug,
                onValueChange = { viewModel.onEvent(AddBlogEvent.SlugChanged(it)) },
                placeholder = "Slug* (Required)"
            )
            Text("Note: Slug is located at the very end of a URL.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))

            // Short Description
            LabeledTextField(
                label = "Short Description*",
                value = state.shortDescription,
                onValueChange = { viewModel.onEvent(AddBlogEvent.ShortDescriptionChanged(it)) },
                placeholder = "Short description* (Required)"
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Blog Description (Rich Text Placeholder)
            LabeledTextField(
                label = "Blog description*",
                value = state.description,
                onValueChange = { viewModel.onEvent(AddBlogEvent.DescriptionChanged(it)) },
                placeholder = "Enter blog content here...",
                modifier = Modifier.height(300.dp),
                minLines = 10,
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )
        }
    }

    // 2. SEO Field Options
    ExpandableCard(title = "SEO Field Options") {
        Column {
            LabeledTextField(
                label = "Meta title*",
                value = state.metaTitle,
                onValueChange = { viewModel.onEvent(AddBlogEvent.MetaTitleChanged(it)) },
                placeholder = "Meta Title"
            )
            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                label = "Meta description*",
                value = state.metaDescription,
                onValueChange = { viewModel.onEvent(AddBlogEvent.MetaDescriptionChanged(it)) },
                placeholder = "Meta Description"
            )
        }
    }

    // 3. Manage CTAs
    ExpandableCard(title = "Manage CTAs") {
        Column {
            state.ctas.forEachIndexed { index, cta ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(
                        label = "Label",
                        value = cta.label,
                        onValueChange = { viewModel.onEvent(AddBlogEvent.UpdateCta(index, cta.copy(label = it))) },
                        modifier = Modifier.weight(1f)
                    )
                    LabeledTextField(
                        label = "URL",
                        value = cta.url,
                        onValueChange = { viewModel.onEvent(AddBlogEvent.UpdateCta(index, cta.copy(url = it))) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.onEvent(AddBlogEvent.RemoveCta(index)) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove CTA")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = { viewModel.onEvent(AddBlogEvent.AddCta()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("Add CTA")
            }
        }
    }



    if (state.isVehicleDialogOpen) {
        ManageVehicleDialog(
            state = state,
            onDismiss = { viewModel.onEvent(AddBlogEvent.ToggleVehicleDialog(false)) },
            onSave = { viewModel.onEvent(AddBlogEvent.AddResearchItem) },
            onMakeSelected = { viewModel.onEvent(AddBlogEvent.MakeSelected(it)) },
            onModelSelected = { viewModel.onEvent(AddBlogEvent.ModelSelected(it)) },
            onYearSelected = { viewModel.onEvent(AddBlogEvent.YearSelected(it)) },
            onTrimSelected = { viewModel.onEvent(AddBlogEvent.TrimSelected(it)) }
        )
    }
}

@Composable
fun SidebarSection(
    state: AddBlogUiState,
    viewModel: AddBlogViewModel,
    imageLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    // 1. Action
    ExpandableCard(title = "Action", defaultExpanded = true) {
        Column {
            Text("Status", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedDropdown(
                label = "",
                options = listOf("published", "draft", "revision"),
                selectedOption = state.status,
                onOptionSelected = { viewModel.onEvent(AddBlogEvent.StatusChanged(it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.onEvent(AddBlogEvent.Submit(null)) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // 2. Manage Author
    ExpandableCard(title = "Manage author", defaultExpanded = true) {
        Column {
            val currentAuthor = state.authors.find { it.id == state.selectedAuthorId }
            // Current Author Display
            LabeledTextField(
                label = "Current author",
                value = currentAuthor?.username ?: "Unknown",
                onValueChange = {},
                readOnly = true,
                enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Change Author Dropdown
            Text("Change author", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            val authorOptions = state.authors.map { if (!it.first_name.isNullOrBlank()) "${it.first_name} ${it.last_name}" else it.username }
            AnimatedDropdown(
                label = "",
                options = authorOptions,
                selectedOption = "Select Author", // Always show placeholder behavior
                onOptionSelected = { name ->
                    val author = state.authors.find { (if (!it.first_name.isNullOrBlank()) "${it.first_name} ${it.last_name}" else it.username) == name }
                    author?.let { viewModel.onEvent(AddBlogEvent.AuthorSelected(it.id)) }
                }
            )
        }
    }

    // 3. Featured Image
    ExpandableCard(title = "Featured image", defaultExpanded = true) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("(Note: Minimum resolution is 960x550.)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF5F6F8), RoundedCornerShape(4.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.selectedImageUri != null) {
                    AsyncImage(
                        model = state.selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (!state.featuredImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = state.featuredImageUrl,
                        contentDescription = "Featured Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to select image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))) {
                Text(
                    "We Recommend to upload all images in .webp format to optimize website performance and SEO rankings.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp),
                    color = Color(0xFFF57F17)
                )
            }
        }
    }

    // 4. Categories
    ExpandableCard(title = "Categories", defaultExpanded = true) {
        Column {
            state.categories.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.selectedCategoryIds.contains(category.id),
                        onCheckedChange = { viewModel.onEvent(AddBlogEvent.ToggleCategory(category.id)) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                    )
                    Text(category.name)
                }
            }
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

// SimpleDropdown removed effectively

@Composable
fun ManageVehicleDialog(
    state: AddBlogUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onMakeSelected: (ResearchMake) -> Unit,
    onModelSelected: (ResearchModel) -> Unit,
    onYearSelected: (ResearchYear) -> Unit,
    onTrimSelected: (ResearchTrim) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Vehicle", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Make Dropdown
                ResearchDropdown(
                    label = "Make",
                    items = state.makes,
                    selectedItem = state.selectedMake,
                    itemLabel = { it.name },
                    onItemSelected = onMakeSelected,
                    isRequired = true
                )

                // Model Dropdown
                ResearchDropdown(
                    label = "Model",
                    items = state.models,
                    selectedItem = state.selectedModel,
                    itemLabel = { it.name },
                    onItemSelected = onModelSelected,
                    enabled = state.selectedMake != null,
                    isRequired = true
                )

                // Year Dropdown
                ResearchDropdown(
                    label = "Year",
                    items = state.years,
                    selectedItem = state.selectedYear,
                    itemLabel = { it.year.toString() },
                    onItemSelected = onYearSelected,
                    enabled = state.selectedModel != null,
                    isRequired = true
                )

                // Trim Dropdown (Only for Compare)
                if (state.blogType == "compare") {
                    ResearchDropdown(
                        label = "Trim",
                        items = state.trims,
                        selectedItem = state.selectedTrim,
                        itemLabel = { it.name },
                        onItemSelected = onTrimSelected,
                        enabled = state.selectedYear != null,
                        isRequired = false
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.selectedYear != null, // Basic validation
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ResearchDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    enabled: Boolean = true,
    isRequired: Boolean = false
) {
    val options = items.map { itemLabel(it) }
    val selectedOptionLabel = selectedItem?.let(itemLabel) ?: ""

    AnimatedDropdown(
        label = if (isRequired) "$label*" else label,
        options = options,
        selectedOption = selectedOptionLabel,
        onOptionSelected = { optionLabel ->
            val selected = items.find { itemLabel(it) == optionLabel }
            selected?.let(onItemSelected)
        },
        enabled = enabled
    )
    Spacer(modifier = Modifier.height(16.dp))
}
