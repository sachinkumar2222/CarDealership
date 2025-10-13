package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class BannerListResponse(
    val list: List<Banner>
)

data class Banner(
    val id: String?,
    val title: String?,
    val url: String?,
    @SerializedName("image_url") val imageUrl: String?, // <-- FIX: Changed from image_path
    @SerializedName("created_on") val createdOn: Long?
)

data class GalleryListResponse(
    val list: List<GalleryResponseObject>
)

// This class for the object inside the list is still correct
data class GalleryResponseObject(
    val id: String?,
    val images: List<String>?
)

data class GalleryImage(
    val id: String?,
    val imageUrl: String?
)