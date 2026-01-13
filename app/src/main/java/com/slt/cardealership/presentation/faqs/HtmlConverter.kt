package com.slt.cardealership.presentation.faq

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

object HtmlConverter {

    /**
     * Converts a simplified HTML string (supporting <strong>, <b>, <i>, <u>, <p>) to AnnotatedString.
     */
    fun fromHtml(html: String): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var currentIndex = 0
        val tags = mutableListOf<TagState>()

        // Regex to find tags: <b>, <strong>, <i>, <u>, <p> and their closing tags
        val regex = Regex("</?(b|strong|i|u|p)>")
        val matches = regex.findAll(html)

        var lastMatchEnd = 0

        for (match in matches) {
            // Append text before the tag
            val textBefore = html.substring(lastMatchEnd, match.range.first)
            builder.append(textBefore)
            currentIndex += textBefore.length

            val tag = match.value
            val isStart = !tag.startsWith("</")
            val tagName = tag.replace(Regex("[</>]"), "")

            if (isStart) {
                tags.add(TagState(tagName, currentIndex))
            } else {
                // Find corresponding start tag (searching backwards)
                val startIndex = tags.indexOfLast { it.name == tagName }
                if (startIndex != -1) {
                    val startTag = tags[startIndex]
                    tags.removeAt(startIndex)

                    val style = when (tagName) {
                        "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                        "i" -> SpanStyle(fontStyle = FontStyle.Italic)
                        "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                        else -> null
                    }
                    style?.let { builder.addStyle(it, startTag.index, currentIndex) }
                }
            }
            lastMatchEnd = match.range.last + 1
        }
        // Append remaining text
        builder.append(html.substring(lastMatchEnd))
        return builder.toAnnotatedString()
    }

    /**
     * Converts AnnotatedString to HTML string (<strong>, <i>, <u>) wrapped in <p>.
     * This is a simplified converter and might produce nested tags.
     */
    fun toHtml(text: AnnotatedString): String {
        val sb = StringBuilder()
        sb.append("<p>")
        val str = text.text
        val spans = text.spanStyles

        var activeBold = false
        var activeItalic = false
        var activeUnderline = false

        for (i in str.indices) {
            // Find styles active at this index
            val styles = spans.filter { i >= it.start && i < it.end }

            val isBold = styles.any { it.item.fontWeight == FontWeight.Bold }
            val isItalic = styles.any { it.item.fontStyle == FontStyle.Italic }
            val isUnderline = styles.any { it.item.textDecoration == TextDecoration.Underline }

            // Close tags that are no longer active
            if (activeUnderline && !isUnderline) { sb.append("</u>"); activeUnderline = false }
            if (activeItalic && !isItalic) { sb.append("</i>"); activeItalic = false }
            if (activeBold && !isBold) { sb.append("</strong>"); activeBold = false }

            // Open tags that became active
            if (!activeBold && isBold) { sb.append("<strong>"); activeBold = true }
            if (!activeItalic && isItalic) { sb.append("<i>"); activeItalic = true }
            if (!activeUnderline && isUnderline) { sb.append("<u>"); activeUnderline = true }

            sb.append(str[i])
        }

        // Close remaining
        if (activeUnderline) sb.append("</u>")
        if (activeItalic) sb.append("</i>")
        if (activeBold) sb.append("</strong>")

        sb.append("</p>")
        return sb.toString()
    }

    private data class TagState(val name: String, val index: Int)
}
