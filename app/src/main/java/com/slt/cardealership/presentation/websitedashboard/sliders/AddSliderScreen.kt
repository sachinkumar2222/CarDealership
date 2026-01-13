package com.slt.cardealership.presentation.websitedashboard.sliders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.DomainSlideItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSliderScreen(
    navController: NavController,
    domainId: Int,
    sliderId: String? = null, // If editing existing slider
    viewModel: AddSliderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sliderId) {
        if (sliderId != null) {
            viewModel.fetchSliderDetails(sliderId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.sliderId == null) "Add Slider" else "Edit Slider") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Slider Name Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Slider name *", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.sliderName,
                                onValueChange = { viewModel.onNameChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Slider name") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Save Button for Name (Only visible if not yet created or if we want to allow updating name)
                            // The user flow implies saving name first creates the slider.
                            // If sliderId is null, show Save. If not null, maybe show Update?
                            // User image shows "Save" button even after creation.
                            Button(
                                onClick = { 
                                    if (uiState.sliderId == null) {
                                        viewModel.createSlider(domainId) 
                                    } else {
                                        // TODO: Implement update slider name if API supports it
                                        // For now, maybe just go back or show success
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                            ) {
                                Text("Save")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Slides Section (Visible only if sliderId is set)
                    if (uiState.sliderId != null) {
                        if (uiState.slides.isEmpty()) {
                            // Empty State
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "No slides",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No slide added found", color = Color.Gray)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.showAddSlideDialog() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add new slide")
                                    }
                                }
                            }
                        } else {
                            // List of Slides
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Slides", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { viewModel.showAddSlideDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add new slide")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.slides) { slide ->
                                    SlideItemCard(
                                        slide = slide,
                                        onEditClick = { viewModel.onEditSlide(slide) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }

    if (uiState.showAddSlideDialog) {
        AddSlideDialog(
            slide = uiState.editingSlide,
            onDismiss = { viewModel.hideAddSlideDialog() },
            onSave = { title, link, target, startDate, endDate, imageFile ->
                if (uiState.editingSlide != null) {
                    viewModel.updateSlide(domainId, uiState.editingSlide!!.id, title, link, target, startDate, endDate, imageFile)
                } else {
                    if (imageFile != null) {
                        viewModel.addSlide(domainId, title, link, target, startDate, endDate, imageFile)
                    }
                }
            },
            isLoading = uiState.isSlideLoading,
            error = uiState.slideError
        )
    }
}

@Composable
fun SlideItemCard(slide: DomainSlideItem, onEditClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = slide.imageUrl,
                contentDescription = slide.title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(slide.title, fontWeight = FontWeight.Bold)
                Text(slide.link, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSlideDialog(
    slide: DomainSlideItem? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long, Long, File?) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var title by remember { mutableStateOf(slide?.title ?: "") }
    var link by remember { mutableStateOf(slide?.link ?: "") }
    var target by remember { mutableStateOf(slide?.target ?: "_self") }
    // Convert seconds to millis for display/editing if slide exists
    var startDate by remember { mutableStateOf<Long?>(slide?.startDate?.times(1000)) }
    var endDate by remember { mutableStateOf<Long?>(slide?.endDate?.times(1000)) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f) // Make it tall
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (slide == null) "Add Slide" else "Edit Slide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content Scrollable
                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text("Title *", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Target
                    Text("Target", style = MaterialTheme.typography.bodyMedium)
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = target,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                            trailingIcon = { 
                                IconButton(onClick = { expanded = true }) {
                                    // Icon(Icons.Default.ArrowDropDown, ...) 
                                }
                            }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("_blank") }, onClick = { target = "_blank"; expanded = false })
                            DropdownMenuItem(text = { Text("_parent") }, onClick = { target = "_parent"; expanded = false })
                            DropdownMenuItem(text = { Text("_self") }, onClick = { target = "_self"; expanded = false })
                            DropdownMenuItem(text = { Text("_top") }, onClick = { target = "_top"; expanded = false })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // URL
                    Text("URL", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Start Date
                    Text("Start Date", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = startDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                            }
                        }
                    )
                    if (showStartDatePicker) {
                        DatePickerModal(
                            onDateSelected = { 
                                startDate = it
                                showStartDatePicker = false
                            },
                            onDismiss = { showStartDatePicker = false }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // End Date
                    var showEndDatePicker by remember { mutableStateOf(false) }
                    Text("End Date", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = endDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                            }
                        }
                    )
                    if (showEndDatePicker) {
                        DatePickerModal(
                            onDateSelected = { 
                                endDate = it
                                showEndDatePicker = false
                            },
                            onDismiss = { showEndDatePicker = false }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Image
                    Text("Image *", style = MaterialTheme.typography.bodyMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp),) // Dashed border ideally
                            .clickable { 
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else if (slide != null) {
                            // Show existing image if no new image selected
                            AsyncImage(
                                model = slide.imageUrl,
                                contentDescription = "Existing Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Blue)
                                Text("Upload Image", color = Color.Gray)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: Recommended resolution 1250 x 500. Max size 500KB.", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                }
                
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                var file: File? = null
                                if (imageUri != null) {
                                    file = uriToFile(context, imageUri!!)
                                }
                                
                                // Allow save if file is present OR if we are editing (file can be null)
                                if (file != null || slide != null) {
                                    onSave(
                                        title, 
                                        link, 
                                        target, 
                                        startDate ?: System.currentTimeMillis(), 
                                        endDate ?: (System.currentTimeMillis() + 31536000000L), 
                                        file
                                    )
                                }
                            }
                        },
                        enabled = !isLoading && title.isNotBlank() && (imageUri != null || slide != null)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(if (slide == null) "Save" else "Update")
                        }
                    }
                }
            }
        }
    }
}

fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
