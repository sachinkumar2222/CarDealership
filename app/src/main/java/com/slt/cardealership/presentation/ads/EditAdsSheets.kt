package com.slt.cardealership.presentation.ads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil3.compose.AsyncImage
import com.slt.cardealership.presentation.common.GradientButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import com.slt.cardealership.presentation.common.SectionTitle
import com.slt.cardealership.presentation.ads.BrandBlue
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainManagementSheet(
    availableDomains: List<com.slt.cardealership.domain.model.AdvDomain>,
    selectedDomainIds: List<Int>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit
) {
    // Local state for selections
    val currentSelections = remember { mutableStateOf(setOf<Int>()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LaunchedEffect(selectedDomainIds) {
        if (selectedDomainIds.isNotEmpty()) {
            currentSelections.value = selectedDomainIds.toSet()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {
            SectionTitle("Manage Domains")
            // Removed Debug Text
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth()
                ) {
                    items(availableDomains) { domain ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val currentSet = currentSelections.value
                                    currentSelections.value = if (currentSet.contains(domain.id)) {
                                        currentSet - domain.id
                                    } else {
                                        currentSet + domain.id
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = currentSelections.value.contains(domain.id),
                                onCheckedChange = { isChecked ->
                                    val currentSet = currentSelections.value
                                    currentSelections.value = if (isChecked) {
                                        currentSet + domain.id
                                    } else {
                                        currentSet - domain.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BrandBlue,
                                    uncheckedColor = BrandBlue.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = domain.domainName ?: "Unknown Domain")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(currentSelections.value.toList()) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Domains", color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGallerySheet(
    images: List<com.slt.cardealership.domain.model.AdvertisementImage>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAddImage: (java.io.File) -> Unit,
    onDeleteImage: (String) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Convert Uri to File (Need a utility or helper)
            // For now, simpler to assume we have a helper or just try to get path.
            // But we need a real File for the Repository.
            // I'll use a temporary hack or helper.
            val inputStream = context.contentResolver.openInputStream(it)
            val file = java.io.File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            onAddImage(file)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {
            SectionTitle("Image Gallery")
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                 if (images.isEmpty()) {
                     Box(Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                         Text("No images found.")
                     }
                 } else {
                     LazyVerticalGrid(
                         columns = GridCells.Fixed(3),
                         horizontalArrangement = Arrangement.spacedBy(8.dp),
                         verticalArrangement = Arrangement.spacedBy(8.dp),
                         modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth()
                     ) {
                         items(images) { image ->
                              Box(modifier = Modifier.aspectRatio(1f).fillMaxWidth()) {
                                  AsyncImage(
                                      model = image.imageUrl,
                                      contentDescription = null,
                                      modifier = Modifier
                                          .matchParentSize(),
                                      contentScale = ContentScale.Crop
                                  )
                                  // Delete Button
                                  Box(
                                      modifier = Modifier
                                          .align(androidx.compose.ui.Alignment.TopEnd)
                                          .padding(4.dp)
                                          .size(24.dp)
                                          .background(androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.CircleShape)
                                          .clickable { image.id?.let { onDeleteImage(it) } },
                                      contentAlignment = androidx.compose.ui.Alignment.Center
                                  ) {
                                      Icon(
                                          imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                          contentDescription = "Delete",
                                          tint = androidx.compose.ui.graphics.Color.Red,
                                          modifier = Modifier.size(16.dp)
                                      )
                                  }
                              }
                          }
                     }
                 }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Image", color = BrandBlue)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Gallery", color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
