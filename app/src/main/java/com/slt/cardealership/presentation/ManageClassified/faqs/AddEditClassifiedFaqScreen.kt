package com.slt.cardealership.presentation.ManageClassified.faqs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slt.cardealership.presentation.common.LabeledTextField
import com.slt.cardealership.presentation.faqs.HtmlConverter
import com.slt.cardealership.presentation.faqs.applyEditToAnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFaqBottomSheet(
    siteId: String,
    faqId: String? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddEditClassifiedFaqViewModel = hiltViewModel()
) {
    // Manually initialize ViewModel with params
    LaunchedEffect(siteId, faqId) {
        viewModel.initializeViewModel(siteId, faqId)
    }

    val state by viewModel.state.collectAsState()

    // Local state for Rich Text Editor
    var answerValue by remember { mutableStateOf(TextFieldValue("")) }

    // Sync from ViewModel to Local State (Initial Load)
    LaunchedEffect(state.answer) {
        val converted = HtmlConverter.fromHtml(state.answer)
        if (answerValue.text != converted.text && !state.isSaving) {
            answerValue = TextFieldValue(converted)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditClassifiedFaqViewModel.UiEvent.NavigateBack -> {
                    onSuccess()
                }
                is AddEditClassifiedFaqViewModel.UiEvent.ShowSnackbar -> {
                    // Handled by parent or ignored in sheet context for now, ideally elevate to parent
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = if (state.isEditMode) "Edit FAQ" else "Add FAQ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (state.error != null) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                }

                // Question
                LabeledTextField(
                    label = "Question *",
                    value = state.question,
                    onValueChange = viewModel::onQuestionChange,
                    placeholder = "Enter question"
                )

                // Answer Label
                Column {
                    Text(
                        text = "Answer *",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Rich Text Editor Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Fixed height for editor
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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

                        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                        BasicTextField(
                            value = answerValue,
                            onValueChange = { newVal ->
                                if (newVal.text == answerValue.text) {
                                    answerValue = newVal.copy(annotatedString = answerValue.annotatedString)
                                } else {
                                    answerValue = applyEditToAnnotatedString(answerValue, newVal)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        val htmlAnswer = HtmlConverter.toHtml(answerValue.annotatedString)
                        viewModel.onAnswerChange(htmlAnswer)
                        viewModel.saveFaq()
                    },
                    enabled = !state.isSaving && !state.isLoading && state.question.isNotBlank() && answerValue.text.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (state.isEditMode) "Update FAQ" else "Save FAQ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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


/**
 * Manually applies the text edit (insertion/deletion) found in [newValue]
 * to the [oldValue]'s AnnotatedString, preserving spans.
 */
fun applyEditToAnnotatedString(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    val oldText = oldValue.text
    val newText = newValue.text
    val oldSpans = oldValue.annotatedString.spanStyles

    // 1. Detect Change
    val diff = newText.length - oldText.length

    // Simple case: No change
    if (diff == 0) return newValue.copy(annotatedString = oldValue.annotatedString)

    val builder = AnnotatedString.Builder(newText)

    // Let's try to map old spans to new.
    for (span in oldSpans) {
        val style = span.item
        var start = span.start
        var end = span.end

        // Case: Insertion
        if (diff > 0) {
            val insertPos = oldValue.selection.min
            if (insertPos <= start) {
                start += diff
                end += diff
            } else if (insertPos > start && insertPos <= end) {
                end += diff
            }
        }
        // Case: Deletion
        else {
            val delStart = newValue.selection.min
            val delEnd = newValue.selection.min - diff // diff is negative
            if (delEnd <= start) {
                start += diff // diff is negative
                end += diff
            } else if (delStart < end && delEnd > start) {
                val overlapStart = maxOf(delStart, start)
                val overlapEnd = minOf(delEnd, end)
                val deletedAmount = overlapEnd - overlapStart
                end -= deletedAmount
            }
        }

        // Clamp and Add
        val finalStart = start.coerceIn(0, newText.length)
        val finalEnd = end.coerceIn(0, newText.length)

        if (finalStart < finalEnd) {
            builder.addStyle(style, finalStart, finalEnd)
        }
    }
    return newValue.copy(annotatedString = builder.toAnnotatedString())
}

