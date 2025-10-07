package com.slt.cardealership.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    // Format for displaying dates (e.g., "MM/dd/yyyy")
    const val DISPLAY_DATE_FORMAT = "MM/dd/yyyy"
    // Format for API requests (e.g., "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") - adjust if your API uses a different format
    const val API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        return try {
            val apiSdf = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
            val date = apiSdf.parse(dateString)
            val displaySdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            date?.let { displaySdf.format(it) } ?: ""
        } catch (e: ParseException) {
            // If API format doesn't match, try to parse it directly if it's already in a recognizable format
            // For example, if it's already "yyyy-MM-dd"
            try {
                val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                val displaySdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
                simpleDate?.let { displaySdf.format(it) } ?: ""
            } catch (e2: ParseException) {
                dateString // Fallback to original string if all else fails
            }
        }
    }

    fun formatDisplayDateToApiDate(displayDate: String): String? {
        if (displayDate.isBlank()) return null
        return try {
            val displaySdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            val date = displaySdf.parse(displayDate)
            val apiSdf = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
            date?.let { apiSdf.format(it) }
        } catch (e: ParseException) {
            null
        }
    }

    fun getCalendarFromDisplayDate(displayDate: String): Calendar? {
        if (displayDate.isBlank()) return null
        return try {
            val displaySdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            val date = displaySdf.parse(displayDate)
            val calendar = Calendar.getInstance()
            date?.let { calendar.time = it }
            calendar
        } catch (e: ParseException) {
            null
        }
    }
}