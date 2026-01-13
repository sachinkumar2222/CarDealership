package com.slt.cardealership.presentation.faq

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue

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

    // We assume the change happened at the cursor (or selection).
    // The newValue.selection is the NEW cursor position.
    // If we inserted 1 char, the cursor moved +1.
    // So insertion point was at newValue.selection.max - 1?

    // Let's try to map old spans to new.
    for (span in oldSpans) {
        val style = span.item
        var start = span.start
        var end = span.end

        // Case: Insertion
        if (diff > 0) {
            val insertPos = oldValue.selection.min

            if (insertPos <= start) {
                // Inserted before this span -> Shift it
                start += diff
                end += diff
            } else if (insertPos > start && insertPos <= end) {
                // Inserted INSIDE this span -> Extend it
                // (Matches "typing inside bold makes new text bold")
                end += diff
            }
            // else: Inserted after -> No change to this span
        }
        // Case: Deletion
        else {
            // Where did we delete?
            val delStart = newValue.selection.min
            val delEnd = newValue.selection.min - diff // diff is negative

            // If we deleted text before this span -> Shift back
            if (delEnd <= start) {
                start += diff // diff is negative, so it reduces
                end += diff
            }
            // If we deleted inside/overlapping this span
            else if (delStart < end && delEnd > start) {
                // Shrink the span
                // Length of overlap
                val overlapStart = maxOf(delStart, start)
                val overlapEnd = minOf(delEnd, end)
                val deletedAmount = overlapEnd - overlapStart
                end -= deletedAmount

            }
            // If we deleted after, no change.
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
