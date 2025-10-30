package com.slt.cardealership.domain.model
import com.google.gson.annotations.SerializedName

// Example for: GET /vehicleInventory/{id}/gallery
data class VehicleGalleryResponse(
    @SerializedName("images")
    val images: List<String>, // A list of image URLs
    @SerializedName("vehicle_id")
    val vehicleId: String
)