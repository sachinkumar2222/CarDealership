package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

// 1. MAIN UI MODEL - Contains all the fields your repository is trying to create.
data class DealerInfo(
    val id: Long,
    val name: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val websiteUrl: String?,
    val aboutText: String?,
    val isVirtual: Boolean?,
    val isClaimed: Boolean?,
    val headerImageUrl: String?, // <-- FIX: Parameter now exists
    val dealerType: String?,
    val dealerCategory: DealerCategory?,
    val amenities: Amenities?,
    val dealerHours: List<DealerHours>?,
    val homeDelivery: HomeDelivery?, // <-- FIX: Parameter now exists
    val homeTestDrive: HomeTestDrive? // <-- FIX: Parameter now exists
    // Note: `showroomImages` and the old `image_url` are removed for now to match the repository
)

// 2. API RESPONSE MODELS - Contain all the fields your repository is trying to read.
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
    @SerializedName("is_claimed") val isClaimed: Boolean?,
    @SerializedName("is_virtual") val isVirtual: Boolean?,
    @SerializedName("image_url") val imageUrl: String?, // <-- FIX: Field now exists
    val description: String? // <-- FIX: Field now exists
)

data class DealerMetasResponse(
    @SerializedName("category") val category: String?,
    @SerializedName("business_segment") val businessSegment: String?,
    val wifi: Boolean?,
    val parking: Boolean?,
    @SerializedName("kids_play_area") val kidsPlayArea: Boolean?,
    @SerializedName("wheelchair_accessible_entrance") val isEntrance: Boolean?,
    @SerializedName("wheelchair_accessible_seating") val isSeating: Boolean?,
    @SerializedName("wheelchair_accessible_restroom") val isRestroom: Boolean?,
    @SerializedName("home_delivery") val homeDelivery: String?,
    @SerializedName("home_delivery_radius") val homeDeliveryRadius: Int?,
    @SerializedName("home_test_drive") val homeTestDrive: Boolean?,
    @SerializedName("home_test_drive_radius") val homeTestDriveRadius: Int?
)


// 3. HELPER MODELS (These should already be correct)
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

data class Amenities(
    val isEntrance: Boolean?,
    val isRestroom: Boolean?,
    val isSeating: Boolean?,
    val isParking: Boolean?,
    val isKidsPlayArea: Boolean?,
    val isWifi: Boolean?
)

data class DealerCategory(
    val name: String?,
    val businessSegment: String?
)

data class HomeDelivery(
    val isAvailable: Boolean,
    val isNationWide: Boolean,
    val radius: Int
)

data class HomeTestDrive(
    val isAvailable: Boolean,
    val radius: Int
)