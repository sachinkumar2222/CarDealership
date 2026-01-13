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
                modifier = Modifier.shadow(8.dp)
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
            Text("Blog Type*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            SimpleDropdown(
                items = listOf("general", "research", "compare"),
                selectedItem = state.blogType,
                onItemSelected = { viewModel.onEvent(AddBlogEvent.BlogTypeChanged(it)) }
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
            Text("Title*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onEvent(AddBlogEvent.TitleChanged(it)) },
                placeholder = { Text("Title* (Required)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Slug
            Text("Slug*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            Text("Note: Slug is located at the very end of a URL.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            OutlinedTextField(
                value = state.slug,
                onValueChange = { viewModel.onEvent(AddBlogEvent.SlugChanged(it)) },
                placeholder = { Text("Slug* (Required)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Short Description
            Text("Short Description*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            OutlinedTextField(
                value = state.shortDescription,
                onValueChange = { viewModel.onEvent(AddBlogEvent.ShortDescriptionChanged(it)) },
                placeholder = { Text("Short description* (Required)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Blog Description (Rich Text Placeholder)
            Text("Blog description*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(AddBlogEvent.DescriptionChanged(it)) },
                modifier = Modifier.fillMaxWidth().height(300.dp),
                placeholder = { Text("Enter blog content here...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))

    // 2. SEO Field Options
    ExpandableCard(title = "SEO Field Options") {
        Column {
            Text("Meta title*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            OutlinedTextField(
                value = state.metaTitle,
                onValueChange = { viewModel.onEvent(AddBlogEvent.MetaTitleChanged(it)) },
                placeholder = { Text("Meta Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Meta description*", style = MaterialTheme.typography.labelMedium, color = Color.Black)
            OutlinedTextField(
                value = state.metaDescription,
                onValueChange = { viewModel.onEvent(AddBlogEvent.MetaDescriptionChanged(it)) },
                placeholder = { Text("Meta Description") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))

    // 3. Manage CTAs
    ExpandableCard(title = "Manage CTAs") {
        Column {
            state.ctas.forEachIndexed { index, cta ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cta.label,
                        onValueChange = { viewModel.onEvent(AddBlogEvent.UpdateCta(index, cta.copy(label = it))) },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    OutlinedTextField(
                        value = cta.url,
                        onValueChange = { viewModel.onEvent(AddBlogEvent.UpdateCta(index, cta.copy(url = it))) },
                        label = { Text("URL") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3),
                            cursorColor = Color(0xFF2196F3)
                        )
                    )
                    IconButton(onClick = { viewModel.onEvent(AddBlogEvent.RemoveCta(index)) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove CTA")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = { viewModel.onEvent(AddBlogEvent.AddCta()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
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
            SimpleDropdown(
                items = listOf("published", "draft", "revision"),
                selectedItem = state.status,
                onItemSelected = { viewModel.onEvent(AddBlogEvent.StatusChanged(it)) }
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
    Spacer(modifier = Modifier.height(24.dp))

    // 2. Manage Author
    ExpandableCard(title = "Manage author", defaultExpanded = true) {
        Column {
            // Current Author Display
            Text("Current author", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            val currentAuthor = state.authors.find { it.id == state.selectedAuthorId }
            OutlinedTextField(
                value = currentAuthor?.username ?: "Unknown",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFECEFF1),
                    unfocusedContainerColor = Color(0xFFECEFF1),
                    disabledContainerColor = Color(0xFFECEFF1),
                    focusedBorderColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Change Author Dropdown
            Text("Change author", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            SimpleDropdown(
                items = state.authors.map { if (!it.first_name.isNullOrBlank()) "${it.first_name} ${it.last_name}" else it.username },
                selectedItem = "Select Author", // Always show placeholder for "Change" action
                onItemSelected = { name ->
                    val author = state.authors.find { (if (!it.first_name.isNullOrBlank()) "${it.first_name} ${it.last_name}" else it.username) == name }
                    author?.let { viewModel.onEvent(AddBlogEvent.AuthorSelected(it.id)) }
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))

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
                    Text("Image Selected: ${state.selectedImageUri!!.lastPathSegment}")
                } else if (!state.featuredImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = state.featuredImageUrl,
                        contentDescription = "Featured Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                        Text("Add New", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { imageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                        Text("(PNG, JPEG, WebP)", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                        Text("(Max. file size 500Kb)", style = MaterialTheme.typography.bodySmall, color = Color.Red)
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
    Spacer(modifier = Modifier.height(24.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF2196F3),
                focusedLabelColor = Color(0xFF2196F3),
                cursorColor = Color(0xFF2196F3)
            ),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

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
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f)
            )
            if (isRequired) {
                Text("*", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedItem?.let(itemLabel) ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select $label", color = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = enabled,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                if (items.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No items available") },
                        onClick = { expanded = false }
                    )
                } else {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemLabel(item)) },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
