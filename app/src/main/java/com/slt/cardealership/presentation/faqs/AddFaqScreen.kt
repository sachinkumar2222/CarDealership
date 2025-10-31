package com.slt.cardealership.presentation.faq // Your package name

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// --- DEFINE THE GRADIENT ---
private val blueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3), // Light Blue
        Color(0xFF1565C0)  // Dark Blue
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFaqScreen(
    navController: NavController,
    viewModel: FaqViewModel = hiltViewModel(
        remember(navController.previousBackStackEntry) {
            navController.previousBackStackEntry!!
        }
    )
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // --- FIX: Local state for the rich text editor ---
    var answer by remember { mutableStateOf(TextFieldValue("")) }

    // --- Sync ViewModel state (which is a String) to the local editor state ---
    LaunchedEffect(formState.answer) {
        if (answer.text != formState.answer) {
            answer = TextFieldValue(formState.answer)
        }
    }

    // --- Handle Events (Snackbars & Navigation) ---
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FaqEvent.ShowSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is FaqEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                FaqEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (formState.isEditMode) "Edit FAQ" else "Add FAQ",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    onClick = {
                        // --- CONNECTED ---
                        // 1. Update the ViewModel's state with the text from the editor
                        viewModel.onFormStateChange(formState.copy(answer = answer.text))
                        // 2. Call save
                        viewModel.saveFaq()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formState.question.isNotBlank()
                            && answer.text.isNotBlank() // <-- Use local answer
                            && !formState.isSaving,
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
                        if (formState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (formState.isEditMode) "Update" else "Save")
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // --- Question Field (CONNECTED) ---
                Text(
                    text = "Question *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = formState.question,
                    onValueChange = { viewModel.onFormStateChange(formState.copy(question = it)) },
                    placeholder = { Text("Enter your question") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    isError = formState.formError != null && formState.question.isBlank()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- Answer Field (Using your BasicTextField) ---
                Text(
                    text = "Answer *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // --- Check for form error to show red border ---
                val answerBorderColor = if (formState.formError != null && answer.text.isBlank()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                }

                BasicTextField(
                    value = answer, // <-- Use local state
                    onValueChange = { answer = it }, // <-- Update local state
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = answerBorderColor, // <-- Use error color
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (answer.text.isEmpty()) {
                            Text("Enter Text", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- Formatting Buttons (Work on local state) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val selection = answer.selection
                    val overlappingSpans = answer.annotatedString.spanStyles.filter {
                        maxOf(it.start, selection.start) < minOf(it.end, selection.end)
                    }

                    val isBold = overlappingSpans.any { it.item.fontWeight == FontWeight.Bold }
                    val isItalic = overlappingSpans.any { it.item.fontStyle == FontStyle.Italic }
                    val isUnderlined = overlappingSpans.any { it.item.textDecoration == TextDecoration.Underline }

                    IconToggleButton(checked = isBold, onCheckedChange = {
                        answer = answer.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    }) {
                        Icon(
                            Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconToggleButton(checked = isItalic, onCheckedChange = {
                        answer = answer.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    }) {
                        Icon(
                            Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconToggleButton(checked = isUnderlined, onCheckedChange = {
                        answer = answer.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    }) {
                        Icon(
                            Icons.Default.FormatUnderlined,
                            contentDescription = "Underline",
                            tint = if (isUnderlined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconToggleButton(checked = false, onCheckedChange = {
                        // TODO: Add logic for linking
                    }) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = "Link",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- ADDED: Form Error Text ---
                if (formState.formError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formState.formError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // --- ADDED: Loading overlay for Edit Mode ---
            if (formState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Helper extension function to toggle a SpanStyle on the selected text of a TextFieldValue.
 */
fun TextFieldValue.toggleSpanStyle(style: SpanStyle): TextFieldValue {
    val selection = this.selection
    if (selection.collapsed) return this // No text selected

    val annotatedString = this.annotatedString

    val togglingBold = style.fontWeight == FontWeight.Bold
    val togglingItalic = style.fontStyle == FontStyle.Italic
    val togglingUnderline = style.textDecoration == TextDecoration.Underline

    val isCurrentlyActive = annotatedString.spanStyles
        .filter { maxOf(it.start, selection.start) < minOf(it.end, selection.end) }
        .any {
            (togglingBold && it.item.fontWeight == FontWeight.Bold) ||
                    (togglingItalic && it.item.fontStyle == FontStyle.Italic) ||
                    (togglingUnderline && it.item.textDecoration == TextDecoration.Underline)
        }

    val newAnnotatedString = AnnotatedString.Builder(annotatedString).apply {
        val styleToApply = if (isCurrentlyActive) {
            when {
                togglingBold -> SpanStyle(fontWeight = FontWeight.Normal)
                togglingItalic -> SpanStyle(fontStyle = FontStyle.Normal)
                togglingUnderline -> SpanStyle(textDecoration = TextDecoration.None)
                else -> SpanStyle()
            }
        } else {
            style
        }
        addStyle(styleToApply, selection.start, selection.end)
    }.toAnnotatedString()

    return this.copy(annotatedString = newAnnotatedString)
}


@Preview(showBackground = true)
@Composable
fun AddFaqScreenPreview() {
    MaterialTheme {
        AddFaqScreen(
            navController = rememberNavController()
        )
    }
}