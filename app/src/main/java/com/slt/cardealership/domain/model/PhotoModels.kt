package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class Banner(
    val id: String?,
    val title: String?,
    val url: String?,
    @SerializedName("image_path") val imagePath: String?,
    @SerializedName("created_on") val createdOn: Long?
)

// For GET /Gallery
data class GalleryImage(
    val id: String?,
    @SerializedName("image_url") val imageUrl: String?
    // Add other fields if the API provides them
)