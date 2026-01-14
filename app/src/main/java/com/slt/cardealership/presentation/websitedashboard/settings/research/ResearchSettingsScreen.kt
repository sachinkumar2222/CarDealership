package com.slt.cardealership.presentation.websitedashboard.settings.research

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.slt.cardealership.presentation.websitedashboard.settings.inventory.MultiSelectDropdown
import com.slt.cardealership.presentation.websitedashboard.settings.inventory.RemovableChip
import com.slt.cardealership.presentation.websitedashboard.settings.DropdownSelector
import com.slt.cardealership.ui.theme.BrandBlue

@Composable
fun ResearchSettingsScreen(
    domainId: Int,
    viewModel: ResearchSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.loadData(domainId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlue)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Research Brands Section
        SettingRow(label = "Research brands:") {
            Column(modifier = Modifier.weight(1f)) {
                MultiSelectDropdown(
                    label = "Select Brand",
                    options = uiState.allMakes.filter { make ->
                        uiState.researchMakes.none { it.makeId == make.id }
                    }.map { it.name to it },
                    onOptionSelected = { option ->
                        option?.let { viewModel.addResearchMake(it.second.id) }
                    }
                )

                Row(modifier = Modifier.padding(top = 8.dp)) {
                    uiState.researchMakes.forEach { makeSetting ->
                        RemovableChip(
                            label = makeSetting.makeName.orEmpty(),
                            onRemove = { viewModel.removeResearchMake(makeSetting.makeId) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Body Types Section
        SettingRow(label = "Body types :") {
            Column(modifier = Modifier.weight(1f)) {
                MultiSelectDropdown(
                    label = "Select Body Type",
                    options = uiState.allBodyTypes.filter { bodyType ->
                        uiState.researchBodyTypes.none { it.bodyTypeId == bodyType.id }
                    }.map { it.name to it },
                    onOptionSelected = { option ->
                        option?.let { viewModel.addResearchBodyType(it.second.id) }
                    }
                )

                Row(modifier = Modifier.padding(top = 8.dp)) {
                    uiState.researchBodyTypes.forEach { bodyTypeSetting ->
                        RemovableChip(
                            label = bodyTypeSetting.bodyTypeName.orEmpty(),
                            onRemove = { viewModel.removeResearchBodyType(bodyTypeSetting.bodyTypeId) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Min Year Dropdown
        val currentYear = java.time.Year.now().value
        val years = listOf("Select Year") + (1999..currentYear).map { it.toString() }.reversed()

        SettingRow(label = "Min Year:") {
            DropdownSelector(
                items = years,
                selectedItem = uiState.researchSettings?.minYear?.toString() ?: "Select Year",
                onItemSelected = { viewModel.updateMinYear(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show Discontinued Toggle
        SettingRow(label = "Show discontinued :") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (uiState.researchSettings?.showDiscontinuedMakes == true) "SHOW" else "HIDE",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.researchSettings?.showDiscontinuedMakes == true,
                    onCheckedChange = { viewModel.updateShowDiscontinued(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrandBlue,
                        checkedTrackColor = Color.LightGray,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Save Button
        Button(
            onClick = { viewModel.saveSettings(domainId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            Text("Save", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color.Black
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
