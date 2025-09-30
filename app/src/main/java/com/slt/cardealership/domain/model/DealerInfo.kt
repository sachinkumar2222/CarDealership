package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

// MAIN UI MODEL: Combines data from all API calls for the UI
data class DealerInfo(
    val id: Long,
    val name: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val websiteUrl: String?,
    val aboutText: String?, // Assuming this might come from metas or another endpoint
    val dealerType: String?,
    val dealerCategory: DealerCategory?,
    val amenities: Amenities?,
    val isClaimed: Boolean?,
    val showroomImages: List<String>?,
    val dealerHours: List<DealerHours>?
)

// MODELS FOR API RESPONSES
data class DealerDetailsResponse(
    val id: Long,
    val name: String?,
    val phone: String?,
    @SerializedName("website_url") val websiteUrl: String?,
    val address: String?,
    @SerializedName("state_name") val stateName: String?,
    @SerializedName("city_name") val cityName: String?,
    @SerializedName("zipcode_name") val zipcodeName: String?,
    @SerializedName("dealer_type_id") val dealerTypeId: Int?,
    @SerializedName("is_claimed") // <-- ADD THIS LINE
    val isClaimed: Boolean?
)

data class DealerMetasResponse(
    @SerializedName("category") val categoryName: String?,
    @SerializedName("business_segment") val businessSegment: String?,
    val wifi: Boolean?,
    val parking: Boolean?,
    @SerializedName("kids_play_area") val kidsPlayArea: Boolean?,
    @SerializedName("wheelchair_accessible_entrance") val isEntrance: Boolean?,
    @SerializedName("wheelchair_accessible_seating") val isSeating: Boolean?,
    @SerializedName("wheelchair_accessible_restroom") val isRestroom: Boolean?
    // aboutText seems to be missing from the new responses
)

// Models for .../Dealers/{dealerId}/Hours
data class DealerHours(
    @SerializedName("hours_type") val hoursType: String?,
    @SerializedName("hourDetails") val hourDetails: List<HourDetails>?
)

data class HourDetails(
    val day: String?,
    @SerializedName("open_time") val openTime: String?,
    @SerializedName("close_time") val closeTime: String?,
    @SerializedName("is_close") val isClose: Boolean?
)

// A clean Amenities object for the UI
data class Amenities(
    val isEntrance: Boolean?,
    val isRestroom: Boolean?,
    val isSeating: Boolean?,
    val isParking: Boolean?,
    val isKidsPlayArea: Boolean?,
    val isWifi: Boolean?
)

// A clean Category object for the UI
data class DealerCategory(
    val name: String?,
    val businessSegment: String?
)