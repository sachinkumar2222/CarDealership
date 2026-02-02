package com.slt.cardealership.presentation.faqs

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
import androidx.compose.material.icons.filled.Link
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
import com.slt.cardealership.presentation.faqs.HtmlConverter
import com.slt.cardealership.presentation.faqs.toggleSpanStyle
import com.slt.cardealership.presentation.faqs.applyEditToAnnotatedString
import com.slt.cardealership.presentation.faq.FaqViewModel
import com.slt.cardealership.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFaqSheet(
    viewModel: FaqViewModel,
    onClose: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val scrollState = rememberScrollState()

    // Local state for the rich text editor
    var answer by remember { mutableStateOf(TextFieldValue("")) }

    // Sync ViewModel state (HTML String) to the local editor state (AnnotatedString)
    LaunchedEffect(formState.answer) {
        if (answer.text != formState.answer && !formState.isSaving) {
            val newAnnotated = HtmlConverter.fromHtml(formState.answer)
            if (answer.text != newAnnotated.text) {
                answer = TextFieldValue(newAnnotated)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f) // Take up to 90% of screen height
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = if (formState.isEditMode) "Edit FAQ" else "Add New FAQ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Question Field
            Text(
                text = "Question *",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = formState.question,
                onValueChange = { viewModel.onFormStateChange(formState.copy(question = it)) },
                placeholder = { Text("Enter your question") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = formState.formError != null && formState.question.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    focusedLabelColor = BrandBlue,
                    cursorColor = BrandBlue,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Answer Field (Rich Text)
            Text(
                text = "Answer *",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val answerBorderColor = if (formState.formError != null && answer.text.isBlank()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = answerBorderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF1F5F9), // Light gray background for toolbar
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (isBold) BrandBlue else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconToggleButton(checked = isItalic, onCheckedChange = {
                        answer = answer.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (isItalic) BrandBlue else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconToggleButton(checked = isUnderlined, onCheckedChange = {
                        answer = answer.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.FormatUnderlined,
                            contentDescription = "Underline",
                            tint = if (isUnderlined) BrandBlue else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = answerBorderColor)

                // Text Area
                BasicTextField(
                    value = answer,
                    onValueChange = { newVal ->
                        if (newVal.text == answer.text) {
                            answer = newVal.copy(annotatedString = answer.annotatedString)
                        } else {
                            if (Math.abs(newVal.text.length - answer.text.length) <= 1) {
                                answer = applyEditToAnnotatedString(answer, newVal)
                            } else {
                                answer = newVal
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1E293B)
                    ),
                    decorationBox = { innerTextField ->
                        if (answer.text.isEmpty()) {
                            Text("Enter answer here...", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
            }

            if (formState.formError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formState.formError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                val htmlAnswer = HtmlConverter.toHtml(answer.annotatedString)
                viewModel.onFormStateChange(formState.copy(answer = htmlAnswer))
                viewModel.saveFaq()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = formState.question.isNotBlank()
                    && answer.text.isNotBlank()
                    && !formState.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                contentColor = Color.White,
                disabledContainerColor = BrandBlue.copy(alpha = 0.5f)
            )
        ) {
            if (formState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (formState.isEditMode) "Update FAQ" else "Save FAQ", fontWeight = FontWeight.Bold)
            }
        }
    }
}