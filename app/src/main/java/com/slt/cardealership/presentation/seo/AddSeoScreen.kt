package com.slt.cardealership.presentation.seo // Your package name

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.presentation.seo.SeoEvent // Make sure to import
import com.slt.cardealership.presentation.seo.SeoViewModel // Make sure to import

// --- DEFINE THE GRADIENT ---
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3),
        Color(0xFF2196F3)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeoScreen(
    navController: NavController,
    // We get the same instance of the ViewModel that the parent (SeoScreen) is using
    viewModel: SeoViewModel = hiltViewModel(
        remember(navController.previousBackStackEntry) {
            navController.previousBackStackEntry!!
        }
    )
) {
    val addState by viewModel.addTagState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }


    // --- Handle Events (Snackbars & Navigation) ---
    LaunchedEffect(Unit) {
        // Clear any old state from the dialog when this screen opens


        viewModel.events.collect { event ->
            when (event) {
                // This screen only listens for the "close" event
                SeoEvent.CloseAddTagDialog -> {
                    Toast.makeText(context, "Tag created successfully!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                // Show errors on this screen's snackbar
                is SeoEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                else -> { /* Other events are for the main screen */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add SEO Tag", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { viewModel.addNewTag() }, // <-- Connect to ViewModel
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    // --- FIX: Validate both fields ---
                    enabled = addState.tagName.isNotBlank() && addState.tagUrl.isNotBlank() && !addState.isSaving,
                    // ---------------------------------
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = blueGradient,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (addState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // --- Tag Name Field ---
            Text(
                "Tag Name *",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = addState.tagName,
                onValueChange = { newName ->
                    viewModel.onAddTagStateChange(addState.copy(tagName = newName))
                },
                placeholder = { Text("Enter tag name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = addState.error != null && addState.tagName.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- FIX: Changed to Tag URL ---
            Text(
                "Tag URL *",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = addState.tagUrl,
                onValueChange = { newUrl ->
                    viewModel.onAddTagStateChange(addState.copy(tagUrl = newUrl))
                },
                placeholder = { Text("Enter tag URL") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = addState.error != null && addState.tagUrl.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            // --- END FIX ---

            // Show validation error
            if (addState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = addState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddSeoScreenPreview() {
    MaterialTheme {
        AddSeoScreen(
            navController = rememberNavController()
            // ViewModel will be in a default state for preview
        )
    }
}