package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * This is the payload for the POST /dealer-api/Dealers/{dealerId}/Hours endpoint
 * It matches your log: { "hour_type": "...", "days": [...] }
 */
data class UpdateHoursRequest(
    val hour_type: String,
    val days: List<HourDetails>
)