package com.slt.cardealership.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    navController: NavController
    // viewModel: InventoryViewModel = hiltViewModel() // Share or create new ViewModel
) {
    // State for all the form fields
    var vin by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var trim by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    // ... add all other fields from the screenshot

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Vehicle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { /* TODO: viewModel.saveVehicle() */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Primary Info", style = MaterialTheme.typography.titleLarge)

            // Using a two-column layout for better space utilization on mobile
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = vin,
                    onValueChange = { vin = it },
                    label = { Text("VIN*") },
                    modifier = Modifier.weight(1f)
                )
                // We'll use simple text fields for now.
                // These can be replaced with ExposedDropdownMenuBox for selections.
                OutlinedTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = { Text("Make*") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model*") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year*") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = trim,
                    onValueChange = { trim = it },
                    label = { Text("Trim*") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status*") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                label = { Text("Condition*") },
                modifier = Modifier.fillMaxWidth()
            )

            // ... Add all the other OutlinedTextFields and Dropdowns for the form

            Spacer(modifier = Modifier.height(16.dp))
            Text("This is a simplified form. All fields from the screenshot should be added here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

        }
    }
}
