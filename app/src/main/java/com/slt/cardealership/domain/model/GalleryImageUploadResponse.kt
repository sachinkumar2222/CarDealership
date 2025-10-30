package com.slt.cardealership.domain.model
import com.google.gson.annotations.SerializedName

// Example for: POST /vehicleInventory/gallery (single upload)
data class GalleryImageUploadResponse(
    @SerializedName("url")
    val imageUrl: String,
    @SerializedName("id")
    val imageId: String
)