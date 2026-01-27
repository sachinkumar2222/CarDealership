package com.slt.cardealership.presentation.ManageClassified.faqs

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.presentation.faq.HtmlConverter
import com.slt.cardealership.presentation.faq.applyEditToAnnotatedString // Import Helper



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassifiedFaqScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: AddEditClassifiedFaqViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local state for Rich Text Editor
    var answerValue by remember { mutableStateOf(TextFieldValue("")) }

    // Sync from ViewModel to Local State (Initial Load)
    LaunchedEffect(state.answer) {
        // Only if local is empty or we are loading (to avoid overwriting user edits during rapid recomposition)
        // A simple check: if the plain text content differs significantly or it's just HTML tags?
        // Better: We convert ViewModel's HTML to AnnotatedString.
        val converted = HtmlConverter.fromHtml(state.answer)
        if (answerValue.text != converted.text && !state.isSaving) {
            answerValue = TextFieldValue(converted)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditClassifiedFaqViewModel.UiEvent.NavigateBack -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh", true)
                    onNavigateBack()
                }
                is AddEditClassifiedFaqViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditMode) "Edit FAQ" else "Add FAQ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Close") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        val htmlAnswer = HtmlConverter.toHtml(answerValue.annotatedString)
                        viewModel.onAnswerChange(htmlAnswer) // Sync final value to VM
                        viewModel.saveFaq()
                    },
                    enabled = !state.isSaving && !state.isLoading && state.question.isNotBlank() && answerValue.text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (state.isEditMode) "Update" else "Save")
                    }
                }
            }
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.error != null) {
                        Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedTextField(
                        value = state.question,
                        onValueChange = viewModel::onQuestionChange,
                        label = { Text("Question *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            focusedLabelColor = Color(0xFF2196F3)
                        )
                    )

                    Text("Answer *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

                    // Rich Text Editor Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    ) {
                        // Toolbar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val selection = answerValue.selection
                            val overlappingSpans = answerValue.annotatedString.spanStyles.filter {
                                maxOf(it.start, selection.min) < minOf(it.end, selection.max)
                            }

                            val isBold = overlappingSpans.any { it.item.fontWeight == FontWeight.Bold }
                            val isItalic = overlappingSpans.any { it.item.fontStyle == FontStyle.Italic }
                            val isUnderline = overlappingSpans.any { it.item.textDecoration == TextDecoration.Underline }

                            IconToggleButton(checked = isBold, onCheckedChange = {
                                answerValue = answerValue.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            }) {
                                Icon(Icons.Default.FormatBold, "Bold", tint = if (isBold) Color(0xFF2196F3) else Color.Gray)
                            }

                            IconToggleButton(checked = isItalic, onCheckedChange = {
                                answerValue = answerValue.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            }) {
                                Icon(Icons.Default.FormatItalic, "Italic", tint = if (isItalic) Color(0xFF2196F3) else Color.Gray)
                            }

                            IconToggleButton(checked = isUnderline, onCheckedChange = {
                                answerValue = answerValue.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                            }) {
                                Icon(Icons.Default.FormatUnderlined, "Underline", tint = if (isUnderline) Color(0xFF2196F3) else Color.Gray)
                            }
                        }

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        BasicTextField(
                            value = answerValue,
                            onValueChange = { newVal ->
                                if (newVal.text == answerValue.text) {
                                    // Text didn't change (selection change only), keep old AnnotatedString
                                    answerValue = newVal.copy(annotatedString = answerValue.annotatedString)
                                } else {
                                    // Text changed, attempt to preserve styles
                                    answerValue = applyEditToAnnotatedString(answerValue, newVal)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )
                    }
                }
            }
        }
    }
}

// Extension to toggle span styles
fun TextFieldValue.toggleSpanStyle(style: SpanStyle): TextFieldValue {
    val selection = this.selection
    if (selection.collapsed) return this // No text selected

    val annotatedString = this.annotatedString
    val togglingBold = style.fontWeight == FontWeight.Bold
    val togglingItalic = style.fontStyle == FontStyle.Italic
    val togglingUnderline = style.textDecoration == TextDecoration.Underline

    val isCurrentlyActive = annotatedString.spanStyles
        .filter { maxOf(it.start, selection.min) < minOf(it.end, selection.max) }
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
        addStyle(styleToApply, selection.min, selection.max)
    }.toAnnotatedString()

    return this.copy(annotatedString = newAnnotatedString)
}
