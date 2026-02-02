package com.slt.cardealership.presentation.seomenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.presentation.common.AnimatedDropdown
import com.slt.cardealership.presentation.common.LabeledTextField
import com.slt.cardealership.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeoMenuSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    viewModel: SeoMenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.addMenuUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAddScreenData()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add New SEO Menu",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else {
                // Category Dropdown
                AnimatedDropdown(
                    label = "Menu Category",
                    selectedOption = uiState.selectedCategory?.name ?: "",
                    options = uiState.allCategories.map { it.name },
                    onOptionSelected = { selectedName ->
                        val category = uiState.allCategories.find { it.name == selectedName }
                        if (category != null) {
                            viewModel.updateAddFormCategory(category)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Menu Label
                LabeledTextField(
                    label = "Menu Label",
                    value = uiState.menuLabel,
                    onValueChange = { viewModel.updateAddFormLabel(it) },
                    placeholder = "Enter menu label",
                    modifier = Modifier.fillMaxWidth()
                )

                // Menu Url
                LabeledTextField(
                    label = "Menu Url",
                    value = uiState.menuUrl,
                    onValueChange = { viewModel.updateAddFormUrl(it) },
                    placeholder = "Enter menu URL",
                    modifier = Modifier.fillMaxWidth()
                )

                // Target Dropdown
                AnimatedDropdown(
                    label = "Menu Target",
                    selectedOption = uiState.selectedTarget,
                    options = viewModel.menuTargets,
                    onOptionSelected = { viewModel.updateAddFormTarget(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Save Button
                Button(
                    onClick = { viewModel.saveNewMenuItem() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save Menu Item",
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
