package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

// This is the main, top-level data class
data class DealerInfo(
    @SerializedName("dealer_name")
    val name: String?,
    @SerializedName("address_1")
    val address: String?,
    @SerializedName("primary_phone_no")
    val phone: String?,
    @SerializedName("city_name")
    val city: String?,
    @SerializedName("state_name")
    val state: String?,
    @SerializedName("zip_code")
    val zipCode: String?,
    @SerializedName("website_url")
    val websiteUrl: String?,
    @SerializedName("dealerEmailId")
    val email: String?,
    @SerializedName("aboutText")
    val aboutText: String?,

    @SerializedName("dealer_type")
    val dealerType: String?,
    @SerializedName("dealerCategory")
    val dealerCategory: DealerCategory?,

    // Nested object for amenities
    @SerializedName("amenitiesAccessibility")
    val amenities: AmenitiesAccessibility?,

    // List of nested objects for banners
    @SerializedName("lstBanner")
    val banners: List<Banner>?,

    // List of strings for the header images
    @SerializedName("showroomImages")
    val showroomImages: List<String>?,

    // List of nested objects for business hours
    @SerializedName("dealerHours")
    val dealerHours: List<DealerHours>?
)

data class DealerCategory(
    @SerializedName("name")
    val name: String?,
    @SerializedName("business_segment")
    val businessSegment: String?
)

// A separate data class for the nested "amenitiesAccessibility" object
data class AmenitiesAccessibility(
    @SerializedName("isEntrance")
    val isEntrance: Boolean?,
    @SerializedName("isRestroom")
    val isRestroom: Boolean?,
    @SerializedName("isSeating")
    val isSeating: Boolean?,
    @SerializedName("isParking")
    val isParking: Boolean?,
    @SerializedName("isKidsPlayArea")
    val isKidsPlayArea: Boolean?,
    @SerializedName("isWifi")
    val isWifi: Boolean?
)

// A data class for each object inside the "lstBanner" list
data class Banner(
    @SerializedName("title")
    val title: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("imagePath")
    val imageUrl: String?
)

// A data class for each object inside the "dealerHours" list
data class DealerHours(
    @SerializedName("hours_type")
    val hoursType: String?,
    @SerializedName("hourDetails")
    val hourDetails: List<HourDetails>?
)

// A data class for each object inside the "hourDetails" list
data class HourDetails(
    @SerializedName("day")
    val day: String?,
    @SerializedName("open_time")
    val openTime: String?,
    @SerializedName("close_time")
    val closeTime: String?,
    @SerializedName("is_close")
    val isClose: Boolean?
)
