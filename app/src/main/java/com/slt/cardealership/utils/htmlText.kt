package com.slt.cardealership.utils

import android.text.Html
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp // Added for padding/sizing if needed
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import android.util.Log // Import Log

@Composable
fun HtmlText(htmlString: String, modifier: Modifier = Modifier) {
    Log.d("HtmlTextDebug", "HtmlText received: '$htmlString'")

    val annotatedString = buildAnnotatedString {
        val spanned = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_COMPACT)
        Log.d("HtmlTextDebug", "HtmlCompat.fromHtml result (Spanned.length): ${spanned.length}")
        Log.d("HtmlTextDebug", "HtmlCompat.fromHtml result (Spanned.toString()): '${spanned.toString()}'")

        if (spanned.length == 0 || spanned.toString().isBlank()) {
            Log.w("HtmlTextDebug", "Spanned text is empty or blank, returning.")
            // You could display a placeholder here if needed
            append("Failed to parse description.")
            return@buildAnnotatedString // Exit from buildAnnotatedString block
        }


        // Iterate over the Spanned text to apply Compose styles
        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
        Log.d("HtmlTextDebug", "Number of spans found: ${spans.size}")

        spans.forEachIndexed { index, span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            Log.d("HtmlTextDebug", "Span $index: type=${span.javaClass.simpleName}, start=$start, end=$end")

            when (span) {
                is android.text.style.UnderlineSpan -> {
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                }
                is android.text.style.URLSpan -> {
                    addStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline), start, end)
                    addStringAnnotation("URL", span.url, start, end)
                }
                is android.text.style.StyleSpan -> {
                    if (span.style == android.graphics.Typeface.BOLD) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    }
                }
                is android.text.style.ForegroundColorSpan -> { // Check for font color
                    addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                }
                is android.text.style.BackgroundColorSpan -> { // Check for background color
                    addStyle(SpanStyle(background = Color(span.backgroundColor)), start, end)
                }
                // Add any other specific spans you might expect from HtmlCompat.
                // Note: Direct CSS styles in <span> like font-family, letter-spacing are NOT converted to Spans by HtmlCompat.
            }
        }
        append(spanned.toString())
        Log.d("HtmlTextDebug", "Final AnnotatedString length: ${this.length}")

    }

    // This Text composable is what actually displays the content
    Text(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge, // Ensure a visible text style
        color = MaterialTheme.colorScheme.onSurface // Explicitly set text color to ensure visibility
    )
}