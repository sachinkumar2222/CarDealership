package com.slt.cardealership.presentation.info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slt.cardealership.domain.model.Make
import com.slt.cardealership.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakesEditSheet(
    allMakes: List<Make>,
    selectedMakeIds: List<Int>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    // Use a temporary set for local editing
    var currentSelectedIds by remember { mutableStateOf(selectedMakeIds.toSet()) }

    val filteredMakes = remember(allMakes, searchQuery) {
        if (searchQuery.isBlank()) {
            allMakes
        } else {
            allMakes.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Edit Makes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search makes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMakes) { make ->
                    val isSelected = currentSelectedIds.contains(make.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentSelectedIds = if (isSelected) {
                                    currentSelectedIds - make.id
                                } else {
                                    currentSelectedIds + make.id
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                currentSelectedIds = if (checked) {
                                    currentSelectedIds + make.id
                                } else {
                                    currentSelectedIds - make.id
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = make.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { onSave(currentSelectedIds.toList()) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
