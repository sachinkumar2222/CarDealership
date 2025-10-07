//package com.slt.cardealership.presentation.photos
//
//import android.app.DatePickerDialog
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.ImageDecoder
//import android.net.Uri
//import android.os.Build
//import android.provider.MediaStore
//import android.widget.DatePicker
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AddPhotoAlternate
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.hilt.navigation.compose.hiltViewModel
//import coil3.compose.AsyncImage
//import com.slt.cardealership.utils.DateFormatter
//import com.slt.cardealership.utils.uriToFile
//import java.io.File
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UploadBannerDialog(
//    viewModel: UploadBannerViewModel = hiltViewModel(),
//    onDismiss: () -> Unit,
//    onSuccess: () -> Unit // Callback to refresh gallery after successful upload/update
//) {
//    if (!viewModel.showDialog) return
//
//    val context = LocalContext.current
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // Listen for events from ViewModel
//    LaunchedEffect(Unit) {
//        viewModel.bannerOperationEvent.collect { event ->
//            when (event) {
//                is BannerOperationEvent.Success -> {
//                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
//                    onSuccess()
//                }
//                is BannerOperationEvent.Error -> {
//                    snackbarHostState.showSnackbar(event.message)
//                }
//            }
//        }
//    }
//
//    Dialog(onDismissRequest = onDismiss) {
//        Card(modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp)) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                // Header
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = if (viewModel.isEditing) "Edit Banner" else "Add New Banner",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//                    IconButton(onClick = onDismiss) {
//                        Icon(Icons.Default.Close, contentDescription = "Close")
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Scrollable content for the form
//                Column(
//                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    OutlinedTextField(
//                        value = viewModel.title,
//                        onValueChange = viewModel::onTitleChange,
//                        label = { Text("Title *") },
//                        modifier = Modifier.fillMaxWidth(),
//                        isError = viewModel.title.isBlank() && viewModel.errorMessage != null
//                    )
//                    OutlinedTextField(
//                        value = viewModel.url,
//                        onValueChange = viewModel::onUrlChange,
//                        label = { Text("URL *") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
//                        modifier = Modifier.fillMaxWidth(),
//                        isError = viewModel.url.isBlank() && viewModel.errorMessage != null
//                    )
//                    DatePickerField(
//                        label = "Start date *",
//                        date = viewModel.startDate,
//                        onDateSelected = viewModel::onStartDateChange,
//                        isError = viewModel.startDate.isBlank() && viewModel.errorMessage != null
//                    )
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Checkbox(
//                            checked = viewModel.addEndDate,
//                            onCheckedChange = viewModel::onAddEndDateToggle
//                        )
//                        Text("Add End Date")
//                    }
//                    if (viewModel.addEndDate) {
//                        DatePickerField(
//                            label = "End date",
//                            date = viewModel.endDate,
//                            onDateSelected = viewModel::onEndDateChange,
//                            isError = viewModel.addEndDate && viewModel.endDate.isBlank() && viewModel.errorMessage != null
//                        )
//                    }
//                    ImageSelectionSection(
//                        selectedImageUri = viewModel.selectedImageUri,
//                        existingImageUrl = viewModel.existingImageUrl,
//                        onImageSelected = viewModel::onImageSelected,
//                        onImageRemoved = viewModel::onImageRemoved,
//                        isError = !viewModel.isEditing && viewModel.selectedImageUri == null && viewModel.errorMessage != null
//                    )
//                    Column(modifier = Modifier.padding(start = 4.dp)) {
//                        Text("• (PNG, JPEG, JPG, GIF, WebP supported)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
//                        Text("• Resolution should be 1250 x 500", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
//                        Text("• Maximum file size 500KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
//                    }
//                    viewModel.errorMessage?.let {
//                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Action Buttons
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismiss, enabled = !viewModel.isLoading, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
//                        Text("Cancel")
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(
//                        onClick = {
//                            val file = viewModel.selectedImageUri?.let { uri -> uriToFile(context, uri) }
//                            viewModel.uploadOrUpdateBanner(file)
//                        },
//                        enabled = !viewModel.isLoading
//                    ) {
//                        if (viewModel.isLoading) {
//                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
//                        } else {
//                            Text(if (viewModel.isEditing) "Save" else "Upload")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun DatePickerField(
//    label: String,
//    date: String,
//    onDateSelected: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
//    isError: Boolean = false
//) {
//    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//    val year = DateFormatter.getCalendarFromDisplayDate(date)?.get(Calendar.YEAR) ?: calendar.get(Calendar.YEAR)
//    val month = DateFormatter.getCalendarFromDisplayDate(date)?.get(Calendar.MONTH) ?: calendar.get(Calendar.MONTH)
//    val day = DateFormatter.getCalendarFromDisplayDate(date)?.get(Calendar.DAY_OF_MONTH) ?: calendar.get(Calendar.DAY_OF_MONTH)
//
//    val datePickerDialog = DatePickerDialog(context, { _: DatePicker, y, m, d -> onDateSelected(y, m, d) }, year, month, day)
//
//    OutlinedTextField(
//        value = date,
//        onValueChange = {},
//        label = { Text(label) },
//        readOnly = true,
//        trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, "Select Date") } },
//        modifier = Modifier.fillMaxWidth(),
//        isError = isError
//    )
//}
//
//@Composable
//private fun ImageSelectionSection(
//    selectedImageUri: Uri?,
//    existingImageUrl: String?,
//    onImageSelected: (Uri?) -> Unit,
//    onImageRemoved: () -> Unit,
//    isError: Boolean
//) {
//    val context = LocalContext.current
//    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        onImageSelected(uri)
//    }
//
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Text("Image *", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        val cardModifier = if (isError) Modifier.border(1.dp, MaterialTheme.colorScheme.error, MaterialTheme.shapes.small) else Modifier
//        Card(
//            modifier = cardModifier.fillMaxWidth().height(150.dp).clickable { imagePickerLauncher.launch("image/*") },
//            shape = MaterialTheme.shapes.small,
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//        ) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                // --- THIS IS THE COMPLETED LOGIC ---
//                when {
//                    // State 1: A new image has been selected by the user
//                    selectedImageUri != null -> {
//                        val bitmap = remember { mutableStateOf<Bitmap?>(null) }
//                        LaunchedEffect(selectedImageUri) {
//                            bitmap.value = try {
//                                if (Build.VERSION.SDK_INT < 28) {
//                                    MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
//                                } else {
//                                    val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri)
//                                    ImageDecoder.decodeBitmap(source)
//                                }
//                            } catch (e: Exception) { null }
//                        }
//                        bitmap.value?.let {
//                            Image(
//                                bitmap = it.asImageBitmap(),
//                                contentDescription = "Selected Image",
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                            IconButton(
//                                onClick = onImageRemoved,
//                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp),
//                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
//                            ) {
//                                Icon(Icons.Default.Close, "Remove image")
//                            }
//                        }
//                    }
//                    // State 2: Editing, and an existing image URL is present
//                    !existingImageUrl.isNullOrBlank() -> {
//                        AsyncImage(
//                            model = existingImageUrl,
//                            contentDescription = "Existing Image",
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.Crop
//                        )
//                        IconButton(
//                            onClick = onImageRemoved,
//                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp),
//                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
//                        ) {
//                            Icon(Icons.Default.Close, "Remove image")
//                        }
//                    }
//                    // State 3: No image selected or available, show placeholder
//                    else -> {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Icon(Icons.Default.AddPhotoAlternate, "Add Image", tint = MaterialTheme.colorScheme.primary)
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text("Click to add an image", color = MaterialTheme.colorScheme.primary)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}