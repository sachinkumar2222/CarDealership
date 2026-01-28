package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class GmbSettingsResponse(
    @SerializedName("token_response")
    val tokenResponse: GmbTokenResponse?
)

data class GmbTokenResponse(
    val response: String?,
    @SerializedName("is_connected")
    val isConnected: Boolean?,
    @SerializedName("is_progress_tracked")
    val isProgressTracked: Boolean?
)

data class GmbMediaResponse(
    @SerializedName("galleryItems")
    val galleryItems: List<GmbGalleryItem>?
)

data class GmbGalleryItem(
    val category: String?,
    @SerializedName("mediaItems")
    val mediaItems: List<GmbMediaItem>?
)

data class GmbMediaItem(
    val name: String?,
    @SerializedName("googleUrl")
    val googleUrl: String?,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    val createTime: String?
)
